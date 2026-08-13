package com.mapconductor.kml

import com.mapconductor.core.features.GeoPoint
import kotlin.math.sqrt

/** 当たった位置と、そこまでの距離の 2 乗。近い方を選ぶために距離を持つ。 */
internal data class GeometryHit(
    val wx: Double,
    val wy: Double,
    val distanceSq: Double,
)

/**
 * クリック位置に最も近いフィーチャーを探す部分。
 *
 * 点・線は許容距離で拾い、面は交差数の偶奇による内外判定（穴の中は当たりにしない）。
 * 走査は**後ろから**行う。あとに描いたものが上に見えるので、上にあるものを先に返す。
 *
 * android-geojson-layer の `GeoJSONHitTester.kt` と同じ判定。
 */
internal class KMLHitTester {
    /**
     * Returns the topmost [KMLFeature] whose geometry contains or is near
     * [longitude]/[latitude], or null if nothing is hit.
     * Call this from a map-level onClick handler on the main thread.
     */
    fun hitTest(
        longitude: Double,
        latitude: Double,
        features: List<RenderFeature>,
        index: KMLSpatialIndex?,
        lineTolSq: Double? = null,
        pointTolSq: Double? = null,
    ): KMLTileRenderer.KMLHitTestResult? {
        val wx = KMLWorld.lonToWorld(longitude)
        val wy = KMLWorld.latToWorld(latitude)
        val lineTolerance = if (lineTolSq != null) sqrt(lineTolSq) else HIT_LINE_TOLERANCE
        val pointTolerance = if (pointTolSq != null) sqrt(pointTolSq) else HIT_POINT_TOLERANCE
        val tolerance = maxOf(lineTolerance, pointTolerance)
        val candidates =
            index
                ?.query(wx - tolerance, wy - tolerance, wx + tolerance, wy + tolerance)
                ?: features.indices.toList()

        for (idx in candidates.asReversed()) {
            val feature = features[idx]
            if (!feature.bounds.intersects(wx - tolerance, wy - tolerance, wx + tolerance, wy + tolerance)) continue
            val hit = hitTestGeometry(wx, wy, feature.worldGeometry, lineTolSq, pointTolSq)
            if (hit != null) {
                return KMLTileRenderer.KMLHitTestResult(
                    feature = feature.source,
                    position =
                        GeoPoint.fromLongLat(
                            longitude = KMLWorld.worldToLon(hit.wx),
                            latitude = KMLWorld.worldToLat(hit.wy),
                        ),
                )
            }
        }
        return null
    }

    private fun hitTestGeometry(
        wx: Double,
        wy: Double,
        geometry: WorldGeometry,
        lineTolSq: Double? = null,
        pointTolSq: Double? = null,
    ): GeometryHit? =
        when (geometry) {
            is WorldGeometry.Point -> {
                val distanceSq = KMLWorld.distanceSq(wx, wy, geometry.wx, geometry.wy)
                if (distanceSq <= (pointTolSq ?: HIT_POINT_TOLERANCE_SQ)) {
                    GeometryHit(geometry.wx, geometry.wy, distanceSq)
                } else {
                    null
                }
            }

            is WorldGeometry.Points ->
                hitTestPoints(wx, wy, geometry.points, pointTolSq)

            is WorldGeometry.Line ->
                hitTestRings(wx, wy, geometry.rings, lineTolSq)

            is WorldGeometry.Polygon -> {
                // 内部（穴を除く）はタップ位置そのものを当たりにする。内部でなければ、
                // lineTolSq が指定されているときに限り輪郭の近傍（すぐ外側のタップ）も拾う。
                val rings = geometry.rings
                val inside =
                    rings.isNotEmpty() &&
                        pointInRing(wx, wy, rings[0].coords) &&
                        rings.drop(1).none { hole -> pointInRing(wx, wy, hole.coords) }
                when {
                    inside -> GeometryHit(wx, wy, 0.0)
                    lineTolSq != null -> hitTestRings(wx, wy, geometry.rings, lineTolSq)
                    else -> null
                }
            }

            is WorldGeometry.Collection -> {
                var best: GeometryHit? = null
                for (part in geometry.parts) {
                    val hit = hitTestGeometry(wx, wy, part, lineTolSq, pointTolSq)
                    if (hit != null && (best == null || hit.distanceSq < best.distanceSq)) best = hit
                }
                best
            }

            WorldGeometry.Empty -> null
        }

    private fun hitTestPoints(
        wx: Double,
        wy: Double,
        coords: DoubleArray,
        pointTolSq: Double? = null,
    ): GeometryHit? {
        val tolSq = pointTolSq ?: HIT_POINT_TOLERANCE_SQ
        var best: GeometryHit? = null
        var i = 0
        while (i < coords.size) {
            val distanceSq = KMLWorld.distanceSq(wx, wy, coords[i], coords[i + 1])
            if (distanceSq <= tolSq && (best == null || distanceSq < best.distanceSq)) {
                best = GeometryHit(coords[i], coords[i + 1], distanceSq)
            }
            i += 2
        }
        return best
    }

    private fun hitTestRings(
        wx: Double,
        wy: Double,
        rings: List<WorldRing>,
        lineTolSq: Double? = null,
    ): GeometryHit? {
        var best: GeometryHit? = null
        for (ring in rings) {
            val hit = hitTestLine(wx, wy, ring.coords, lineTolSq)
            if (hit != null && (best == null || hit.distanceSq < best.distanceSq)) best = hit
        }
        return best
    }

    private fun hitTestLine(
        wx: Double,
        wy: Double,
        coords: DoubleArray,
        lineTolSq: Double? = null,
    ): GeometryHit? {
        val tolSq = lineTolSq ?: HIT_LINE_TOLERANCE_SQ
        var best: GeometryHit? = null
        var i = 2
        while (i < coords.size) {
            val hit = closestPointOnSegment(wx, wy, coords[i - 2], coords[i - 1], coords[i], coords[i + 1])
            if (hit.distanceSq <= tolSq && (best == null || hit.distanceSq < best.distanceSq)) {
                best = hit
            }
            i += 2
        }
        return best
    }

    private fun pointInRing(
        wx: Double,
        wy: Double,
        ring: DoubleArray,
    ): Boolean {
        var inside = false
        var j = ring.size - 2
        var i = 0
        while (i < ring.size) {
            val xi = ring[i]
            val yi = ring[i + 1]
            val xj = ring[j]
            val yj = ring[j + 1]
            if (((yi > wy) != (yj > wy)) && (wx < (xj - xi) * (wy - yi) / (yj - yi) + xi)) {
                inside = !inside
            }
            j = i
            i += 2
        }
        return inside
    }

    private fun closestPointOnSegment(
        px: Double,
        py: Double,
        ax: Double,
        ay: Double,
        bx: Double,
        by: Double,
    ): GeometryHit {
        val dx = bx - ax
        val dy = by - ay
        if (dx == 0.0 && dy == 0.0) return GeometryHit(ax, ay, KMLWorld.distanceSq(px, py, ax, ay))
        val t = ((px - ax) * dx + (py - ay) * dy) / (dx * dx + dy * dy)
        val tc = t.coerceIn(0.0, 1.0)
        val wx = ax + tc * dx
        val wy = ay + tc * dy
        return GeometryHit(wx, wy, KMLWorld.distanceSq(px, py, wx, wy))
    }

    companion object {
        const val HIT_LINE_TOLERANCE = 0.0002
        const val HIT_LINE_TOLERANCE_SQ = HIT_LINE_TOLERANCE * HIT_LINE_TOLERANCE
        const val HIT_POINT_TOLERANCE = 0.0004
        const val HIT_POINT_TOLERANCE_SQ = HIT_POINT_TOLERANCE * HIT_POINT_TOLERANCE
    }
}
