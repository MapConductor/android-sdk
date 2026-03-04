package com.mapconductor.core.spherical

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import kotlin.math.abs

/**
 * Splits a list of points by the 180°/-180° meridian line and adds interpolated points
 * at the meridian crossings to eliminate gaps.
 *
 * @param points List of IGeoPoint to split
 * @param geodesic If true, uses geodesic (great circle) interpolation; if false, uses linear interpolation
 * @return List of point groups, each representing a continuous segment without meridian crossings
 */
fun splitByMeridian(
    points: List<GeoPointInterface>,
    geodesic: Boolean,
): List<List<GeoPointInterface>> {
    if (points.isEmpty()) return emptyList()

    val results = mutableListOf<List<GeoPointInterface>>()
    var fragment = mutableListOf<GeoPointInterface>()

    for (i in points.indices) {
        val currentPoint = points[i]

        if (fragment.isEmpty()) {
            fragment.add(currentPoint)
            continue
        }

        val previousPoint = fragment.last()
        val prevLng = previousPoint.longitude
        val currLng = currentPoint.longitude

        // Check if meridian crossing occurs
//        val crossesMeridian = (prevLng >= 0 && currLng < 0) || (prevLng < 0 && currLng >= 0)
        // 180°線交差のみを検出（0°線は除外）
        val lngDiff = currLng - prevLng
        val crossesMeridian = abs(lngDiff) > 180.0

        if (!crossesMeridian) {
            // No meridian crossing, add point to current fragment
            fragment.add(currentPoint)
        } else {
            // Meridian crossing detected, add interpolated point at meridian
            val meridianPoint = interpolateAtMeridian(previousPoint, currentPoint, geodesic)
            fragment.add(meridianPoint)

            // Close current fragment and start new one
            results.add(fragment.toList())
            fragment = mutableListOf<GeoPointInterface>()

            // Add the opposite meridian point to start the new fragment
            val oppositeMeridianPoint = createOppositeMeridianPoint(meridianPoint)
            fragment.add(oppositeMeridianPoint)
            fragment.add(currentPoint)
        }
    }

    if (fragment.isNotEmpty()) {
        results.add(fragment.toList())
    }

    return results
}

/**
 * Interpolates a point at the 180°/-180° meridian line between two points.
 *
 * @param from Starting point
 * @param to Ending point
 * @param geodesic If true, uses geodesic (great circle) interpolation; if false, uses linear interpolation
 * @return Point at the meridian crossing
 */
private fun interpolateAtMeridian(
    from: GeoPointInterface,
    to: GeoPointInterface,
    geodesic: Boolean,
): GeoPoint {
    if (geodesic) {
        // Use geodesic interpolation (great circle path)
        return interpolateAtMeridianGeodesic(from, to)
    } else {
        // Use linear interpolation
        return interpolateAtMeridianLinear(from, to)
    }
}
