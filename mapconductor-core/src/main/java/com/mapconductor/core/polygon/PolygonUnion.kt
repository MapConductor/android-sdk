package com.mapconductor.core.polygon

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.LinearRing
import org.locationtech.jts.geom.Polygon
import org.locationtech.jts.geom.util.GeometryFixer
import org.locationtech.jts.geom.util.PolygonExtracter
import org.locationtech.jts.operation.union.CascadedPolygonUnion

/**
 * Unions overlapping hole rings (2D lon/lat plane) and returns a new [PolygonState] with merged holes.
 *
 * Notes:
 * - This uses planar geometry (not geodesic). For very large polygons or near poles, results may differ from
 *   spherical expectations.
 * - If union fails for any reason, this returns the original [PolygonState] unchanged.
 */
fun PolygonState.unionHoles(): PolygonState {
    if (holes.size <= 1) return this

    return runCatching {
        val geometryFactory = GeometryFactory()

        fun ensureClosed(ring: List<GeoPointInterface>): List<GeoPointInterface> {
            if (ring.isEmpty()) return ring
            val first = ring.first()
            val last = ring.last()
            return if (first.latitude == last.latitude && first.longitude == last.longitude) ring else ring + first
        }

        fun toLinearRing(ring: List<GeoPointInterface>): LinearRing {
            val closed = ensureClosed(ring)
            if (closed.size < 4) return geometryFactory.createLinearRing(emptyArray())
            val coords =
                closed
                    .map { p ->
                        Coordinate(p.longitude, p.latitude)
                    }.toTypedArray()
            return geometryFactory.createLinearRing(coords)
        }

        fun toPolygon(shellRing: List<GeoPointInterface>): Polygon {
            val shell = toLinearRing(shellRing)
            return geometryFactory.createPolygon(shell)
        }

        fun coordsToRing(coords: Array<Coordinate>): List<GeoPointInterface> {
            if (coords.size < 4) return emptyList()
            val raw = coords.map { c -> GeoPoint.fromLatLong(latitude = c.y, longitude = c.x) }
            // JTS returns closed rings; drop the last point if it matches the first.
            val open =
                if (raw.size >= 2 &&
                    raw.first().latitude == raw.last().latitude &&
                    raw.first().longitude == raw.last().longitude
                ) {
                    raw.dropLast(1)
                } else {
                    raw
                }
            if (open.size < 3) return emptyList()
            return open
        }

        val holeGeometries =
            holes
                .mapNotNull { hole ->
                    val poly = toPolygon(hole)
                    if (poly.isEmpty) null else poly
                }.mapNotNull { poly ->
                    val fixed = GeometryFixer.fix(poly)
                    if (fixed.isEmpty) null else fixed
                }

        if (holeGeometries.isEmpty()) return@runCatching this

        val holesUnion: Geometry = CascadedPolygonUnion.union(holeGeometries)
        if (holesUnion.isEmpty) return@runCatching this

        // For visual correctness (remove XOR overlap artifacts), we only need holes to be non-overlapping.
        // Intersecting with an "outer" ring can fail or erase results when the outer is unusual.
        val effectiveUnion: Geometry = holesUnion

        val unionedPolygons =
            PolygonExtracter
                .getPolygons(effectiveUnion)
                .filterIsInstance<Polygon>()
        val nextHoles =
            unionedPolygons
                .map { poly -> coordsToRing(poly.exteriorRing.coordinates) }
                .filter { it.isNotEmpty() }

        if (nextHoles.isEmpty()) return@runCatching this

        fun signedAreaLonLat(ring: List<GeoPointInterface>): Double {
            if (ring.size < 3) return 0.0
            var area = 0.0
            for (i in ring.indices) {
                val a = ring[i]
                val b = ring[(i + 1) % ring.size]
                area += (a.longitude * b.latitude) - (b.longitude * a.latitude)
            }
            return area / 2.0
        }

        // Normalize hole winding. Many renderers expect holes to have opposite winding; make holes clockwise.
        val normalizedHoles =
            nextHoles.map { ring ->
                val area = signedAreaLonLat(ring)
                if (area > 0.0) ring.asReversed() else ring
            }

        PolygonState(
            points = points,
            holes = normalizedHoles,
            id = id,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth,
            fillColor = fillColor,
            geodesic = geodesic,
            zIndex = zIndex,
            extra = extra,
            onClick = onClick,
        )
    }.getOrElse { this }
}

/**
 * Alias for [unionHoles], to keep callsites short.
 */
fun PolygonState.union(): PolygonState = unionHoles()

/**
 * In-place variant: mutates [holes] to the merged result.
 */
fun PolygonState.unionHolesInPlace(): PolygonState {
    val merged = unionHoles()
    if (merged === this) return this
    holes = merged.holes
    return this
}
