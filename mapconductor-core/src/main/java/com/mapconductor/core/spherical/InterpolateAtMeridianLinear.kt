package com.mapconductor.core.spherical

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface

/**
 * Performs linear interpolation to find the meridian crossing point.
 */
fun interpolateAtMeridianLinear(
    from: GeoPointInterface,
    to: GeoPointInterface,
): GeoPoint {
    val fromLng = from.longitude
    val toLng = to.longitude

    // Determine which meridian to interpolate to (180 or -180)
    val targetMeridian = if (fromLng >= 0) 180.0 else -180.0

    // Calculate the fraction where meridian crossing occurs
    val totalLngDiff = toLng - fromLng
    val meridianDiff = targetMeridian - fromLng
    val fraction = meridianDiff / totalLngDiff

    // Interpolate latitude and altitude at the meridian
    val interpolatedLatitude = from.latitude + fraction * (to.latitude - from.latitude)
    val interpolatedAltitude =
        when {
            from.altitude != null && to.altitude != null ->
                from.altitude!! + fraction * (to.altitude!! - from.altitude!!)
            from.altitude != null -> from.altitude
            to.altitude != null -> to.altitude
            else -> 0.0
        }

    return GeoPoint(
        latitude = interpolatedLatitude,
        longitude = targetMeridian,
        altitude = interpolatedAltitude!!,
    )
}
