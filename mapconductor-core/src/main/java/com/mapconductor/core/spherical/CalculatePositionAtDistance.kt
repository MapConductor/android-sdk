package com.mapconductor.core.spherical

import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.projection.Earth
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

// Calculate a position at a specific distance and bearing from a center point
fun calculatePositionAtDistance(
    center: GeoPointImpl,
    distanceMeters: Double,
    bearingDegrees: Double,
): GeoPointImpl {
    val earthRadiusKm = Earth.RADIUS_METERS / 1000
    val distanceKm = distanceMeters / 1000.0
    val bearingRad = Math.toRadians(bearingDegrees)

    val lat1Rad = Math.toRadians(center.latitude)
    val lng1Rad = Math.toRadians(center.longitude)

    val lat2Rad =
        Math.asin(
            sin(lat1Rad) * cos(distanceKm / earthRadiusKm) +
                cos(lat1Rad) * sin(distanceKm / earthRadiusKm) * cos(bearingRad),
        )

    val lng2Rad =
        lng1Rad +
            atan2(
                sin(bearingRad) * sin(distanceKm / earthRadiusKm) * cos(lat1Rad),
                cos(distanceKm / earthRadiusKm) - sin(lat1Rad) * sin(lat2Rad),
            )

    return GeoPointImpl.fromLatLong(
        latitude = Math.toDegrees(lat2Rad),
        longitude = Math.toDegrees(lng2Rad),
    )
}
