package com.mapconductor.core.spherical

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface

/**
 * Performs geodesic interpolation to find the meridian crossing point.
 * Uses iterative method to find where the great circle path crosses the meridian.
 */
fun interpolateAtMeridianGeodesic(
    from: GeoPointInterface,
    to: GeoPointInterface,
): GeoPoint {
    val fromLng = from.longitude

    // Determine target meridian
    val targetMeridian = if (fromLng >= 0) 180.0 else -180.0

    // Use binary search to find the crossing point on the great circle
    var low = 0.0
    var high = 1.0
    val tolerance = 1e-10
    val maxIterations = 50

    var iteration = 0
    while (iteration < maxIterations && (high - low) > tolerance) {
        val mid = (low + high) / 2.0
        val interpolatedPoint = Spherical.sphericalInterpolate(from, to, mid)
        val interpolatedLng = interpolatedPoint.longitude

        // Normalize longitude to handle crossing
        val normalizedLng =
            when {
                interpolatedLng > 180 -> interpolatedLng - 360
                interpolatedLng <= -180 -> interpolatedLng + 360
                else -> interpolatedLng
            }

        // Check which side of the target meridian we're on
        val onTargetSide =
            if (targetMeridian > 0) {
                // Looking for 180°
                normalizedLng >= 0
            } else {
                // Looking for -180°
                normalizedLng < 0
            }

        val fromOnTargetSide =
            if (targetMeridian > 0) {
                fromLng >= 0
            } else {
                fromLng < 0
            }

        if (onTargetSide == fromOnTargetSide) {
            low = mid
        } else {
            high = mid
        }

        iteration++
    }

    // Final interpolation at the crossing point
    val finalFraction = (low + high) / 2.0
    val crossingPoint = Spherical.sphericalInterpolate(from, to, finalFraction)

    // Ensure the longitude is exactly at the target meridian
    return GeoPoint(
        latitude = crossingPoint.latitude,
        longitude = targetMeridian,
        altitude = crossingPoint.altitude,
    )
}
