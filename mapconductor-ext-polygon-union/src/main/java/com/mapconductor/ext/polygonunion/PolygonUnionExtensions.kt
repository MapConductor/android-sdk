package com.mapconductor.ext.polygonunion

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.core.polygon.union as coreUnion
import com.mapconductor.core.polygon.unionHoles as coreUnionHoles
import com.mapconductor.core.polygon.unionHolesInPlace as coreUnionHolesInPlace

/**
 * Approximates hole union by replacing all holes with a single convex hull ring of their vertices.
 *
 * This guarantees there is no overlap between holes (so Mapbox XOR/even-odd artifacts disappear),
 * but it can remove more area than the true union (concavities are lost, and disjoint holes become one).
 */
fun PolygonState.unionHolesConvexHull(): PolygonState {
    if (holes.isEmpty()) return this

    data class Pt(
        val x: Double,
        val y: Double,
    )

    val pts =
        holes
            .asSequence()
            .flatten()
            .map { Pt(x = it.longitude, y = it.latitude) }
            .distinct()
            .sortedWith(compareBy<Pt> { it.x }.thenBy { it.y })
            .toList()

    if (pts.size < 3) return this

    fun cross(
        o: Pt,
        a: Pt,
        b: Pt,
    ): Double = (a.x - o.x) * (b.y - o.y) - (a.y - o.y) * (b.x - o.x)

    val lower = ArrayList<Pt>()
    for (p in pts) {
        while (lower.size >= 2 && cross(lower[lower.size - 2], lower[lower.size - 1], p) <= 0.0) {
            lower.removeAt(lower.size - 1)
        }
        lower.add(p)
    }

    val upper = ArrayList<Pt>()
    for (p in pts.asReversed()) {
        while (upper.size >= 2 && cross(upper[upper.size - 2], upper[upper.size - 1], p) <= 0.0) {
            upper.removeAt(upper.size - 1)
        }
        upper.add(p)
    }

    // Exclude last point of each list (it's the starting point of the other list).
    val hull = (lower.dropLast(1) + upper.dropLast(1))
    if (hull.size < 3) return this

    val ring = hull.map { GeoPoint.fromLatLong(latitude = it.y, longitude = it.x) }
    return PolygonState(
        points = points,
        holes = listOf(ring),
        id = id,
        strokeColor = strokeColor,
        strokeWidth = strokeWidth,
        fillColor = fillColor,
        geodesic = geodesic,
        zIndex = zIndex,
        extra = extra,
        onClick = onClick,
    )
}

fun PolygonState.unionHolesConvexHullInPlace(): PolygonState {
    val merged = unionHolesConvexHull()
    if (merged === this) return this
    holes = merged.holes
    return this
}
