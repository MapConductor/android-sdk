package com.mapconductor.kml

import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.ln
import kotlin.math.sin
import kotlin.math.sinh

internal data class WorldBounds(
    val minX: Double,
    val maxX: Double,
    val minY: Double,
    val maxY: Double,
) {
    fun intersects(
        x1: Double,
        y1: Double,
        x2: Double,
        y2: Double,
    ): Boolean = minX <= x2 && maxX >= x1 && minY <= y2 && maxY >= y1
}

internal sealed class WorldGeometry {
    data class Point(
        val wx: Double,
        val wy: Double,
    ) : WorldGeometry()

    data class Points(
        val points: DoubleArray,
    ) : WorldGeometry()

    data class Line(
        val rings: List<WorldRing>,
    ) : WorldGeometry()

    data class Polygon(
        val rings: List<WorldRing>,
    ) : WorldGeometry()

    data class Collection(
        val parts: List<WorldGeometry>,
    ) : WorldGeometry()

    object Empty : WorldGeometry()
}

internal class WorldRing(
    val coords: DoubleArray,
) {
    private val simplifiedByZoom =
        java.util.concurrent.atomic
            .AtomicReferenceArray<DoubleArray>(KMLWorld.MAX_SIMPLIFY_ZOOM + 1)

    fun coordsForZoom(
        zoom: Int,
        tileSize: Int,
    ): DoubleArray {
        if (coords.size < 6) return coords
        val cacheIndex = zoom.coerceIn(0, KMLWorld.MAX_SIMPLIFY_ZOOM)
        simplifiedByZoom.get(cacheIndex)?.let { return it }
        val tolerance = 0.5 / (tileSize.toDouble() * (1 shl cacheIndex))
        val simplified = KMLWorld.simplifyRadial(coords, tolerance)
        return if (simplifiedByZoom.compareAndSet(cacheIndex, null, simplified)) {
            simplified
        } else {
            simplifiedByZoom.get(cacheIndex)
        }
    }
}

/**
 * 緯度経度と世界座標（0..1 の正規化 Web メルカトル）の相互変換、ジオメトリの
 * 世界座標化、範囲の計算、間引き。すべて副作用のない計算。
 *
 * 緯度経度のままではなく先に世界座標へ移しておくのは、タイルを描くたびに
 * 投影を計算し直さないため。
 *
 * android-geojson-layer の `GeoJSONWorld.kt` と同じ式（KML は ios / react に
 * 相当物が無く、android 単独。todo の「片側欠落」参照）。
 */
internal object KMLWorld {
    const val MAX_SIMPLIFY_ZOOM = 22

    fun lonToWorld(lon: Double): Double = lon / 360.0 + 0.5

    fun worldToLon(wx: Double): Double = (wx - 0.5) * 360.0

    fun latToWorld(lat: Double): Double {
        val siny = sin(lat * PI / 180.0).coerceIn(-0.9999, 0.9999)
        return 0.5 - ln((1.0 + siny) / (1.0 - siny)) / (4.0 * PI)
    }

    fun worldToLat(wy: Double): Double = atan(sinh(PI * (1.0 - 2.0 * wy))) * 180.0 / PI

    fun toPixel(
        world: Double,
        worldSize: Double,
        origin: Double,
    ): Float = ((world * worldSize) - origin).toFloat()

    fun segmentOutside(
        ax: Double,
        ay: Double,
        bx: Double,
        by: Double,
        minX: Double,
        minY: Double,
        maxX: Double,
        maxY: Double,
    ): Boolean =
        (ax < minX && bx < minX) ||
            (ax > maxX && bx > maxX) ||
            (ay < minY && by < minY) ||
            (ay > maxY && by > maxY)

    fun toWorldGeometry(geometry: KMLGeometry): WorldGeometry =
        when (geometry) {
            is KMLGeometry.Point ->
                WorldGeometry.Point(
                    wx = lonToWorld(geometry.longitude),
                    wy = latToWorld(geometry.latitude),
                )

            is KMLGeometry.MultiPoint ->
                WorldGeometry.Points(
                    points = flatPoints(geometry.points),
                )

            is KMLGeometry.LineString ->
                WorldGeometry.Line(
                    rings = listOf(WorldRing(flatCoordinates(geometry.coordinates))),
                )

            is KMLGeometry.MultiLineString ->
                WorldGeometry.Line(
                    rings =
                        geometry.lines.map { line ->
                            WorldRing(flatCoordinates(line))
                        },
                )

            is KMLGeometry.Polygon ->
                WorldGeometry.Polygon(
                    rings =
                        geometry.rings.map { ring ->
                            WorldRing(flatCoordinates(ring))
                        },
                )

            is KMLGeometry.MultiPolygon ->
                WorldGeometry.Collection(
                    parts =
                        geometry.polygons.map { poly ->
                            WorldGeometry.Polygon(
                                rings =
                                    poly.map { ring ->
                                        WorldRing(flatCoordinates(ring))
                                    },
                            )
                        },
                )

            is KMLGeometry.GeometryCollection ->
                WorldGeometry.Collection(
                    parts = geometry.geometries.map { toWorldGeometry(it) },
                )

            KMLGeometry.Empty -> WorldGeometry.Empty
        }

    fun flatPoints(points: List<KMLGeometry.Point>): DoubleArray {
        val coords = DoubleArray(points.size * 2)
        var i = 0
        for (point in points) {
            coords[i++] = lonToWorld(point.longitude)
            coords[i++] = latToWorld(point.latitude)
        }
        return coords
    }

    fun flatCoordinates(points: List<LonLat>): DoubleArray {
        val coords = DoubleArray(points.size * 2)
        var i = 0
        for (point in points) {
            coords[i++] = lonToWorld(point.longitude)
            coords[i++] = latToWorld(point.latitude)
        }
        return coords
    }

    fun computeBounds(geometry: WorldGeometry): WorldBounds =
        when (geometry) {
            is WorldGeometry.Point -> WorldBounds(geometry.wx, geometry.wx, geometry.wy, geometry.wy)
            is WorldGeometry.Points -> boundsOfCoords(geometry.points)
            is WorldGeometry.Line -> boundsOfRings(geometry.rings)
            is WorldGeometry.Polygon -> boundsOfRings(geometry.rings)
            is WorldGeometry.Collection -> {
                if (geometry.parts.isEmpty()) {
                    WorldBounds(0.0, 1.0, 0.0, 1.0)
                } else {
                    val childBounds = geometry.parts.map { computeBounds(it) }
                    WorldBounds(
                        minX = childBounds.minOf { it.minX },
                        maxX = childBounds.maxOf { it.maxX },
                        minY = childBounds.minOf { it.minY },
                        maxY = childBounds.maxOf { it.maxY },
                    )
                }
            }
            WorldGeometry.Empty -> WorldBounds(0.0, 1.0, 0.0, 1.0)
        }

    fun boundsOfCoords(coords: DoubleArray): WorldBounds {
        if (coords.isEmpty()) return WorldBounds(0.0, 1.0, 0.0, 1.0)
        var minX = coords[0]
        var maxX = coords[0]
        var minY = coords[1]
        var maxY = coords[1]
        var i = 2
        while (i < coords.size) {
            val x = coords[i]
            val y = coords[i + 1]
            if (x < minX) minX = x
            if (x > maxX) maxX = x
            if (y < minY) minY = y
            if (y > maxY) maxY = y
            i += 2
        }
        return WorldBounds(minX, maxX, minY, maxY)
    }

    fun boundsOfRings(rings: List<WorldRing>): WorldBounds {
        var minX = Double.MAX_VALUE
        var maxX = -Double.MAX_VALUE
        var minY = Double.MAX_VALUE
        var maxY = -Double.MAX_VALUE
        for (ring in rings) {
            val coords = ring.coords
            var i = 0
            while (i < coords.size) {
                val x = coords[i]
                val y = coords[i + 1]
                if (x < minX) minX = x
                if (x > maxX) maxX = x
                if (y < minY) minY = y
                if (y > maxY) maxY = y
                i += 2
            }
        }
        return if (minX <= maxX) {
            WorldBounds(minX, maxX, minY, maxY)
        } else {
            WorldBounds(0.0, 1.0, 0.0, 1.0)
        }
    }

    fun distanceSq(
        ax: Double,
        ay: Double,
        bx: Double,
        by: Double,
    ): Double {
        val dx = ax - bx
        val dy = ay - by
        return dx * dx + dy * dy
    }

    fun simplifyRadial(
        coords: DoubleArray,
        tolerance: Double,
    ): DoubleArray {
        if (coords.size <= 4 || tolerance <= 0.0) return coords
        val toleranceSq = tolerance * tolerance
        val output = DoubleArray(coords.size)
        var out = 0
        var lastX = coords[0]
        var lastY = coords[1]
        output[out++] = lastX
        output[out++] = lastY

        var i = 2
        while (i < coords.size - 2) {
            val x = coords[i]
            val y = coords[i + 1]
            if (distanceSq(lastX, lastY, x, y) > toleranceSq) {
                output[out++] = x
                output[out++] = y
                lastX = x
                lastY = y
            }
            i += 2
        }

        val endX = coords[coords.size - 2]
        val endY = coords[coords.size - 1]
        if (out < 2 || output[out - 2] != endX || output[out - 1] != endY) {
            output[out++] = endX
            output[out++] = endY
        }
        return if (out == coords.size) coords else output.copyOf(out)
    }
}
