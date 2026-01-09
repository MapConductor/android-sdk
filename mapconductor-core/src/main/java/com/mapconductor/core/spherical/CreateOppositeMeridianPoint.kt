package com.mapconductor.core.spherical

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface

/**
 * Creates a point at the opposite meridian (180° ↔ -180°) with the same latitude and altitude.
 *
 * @param point Point at one meridian
 * @return Point at the opposite meridian
 */
fun createOppositeMeridianPoint(point: GeoPointInterface): GeoPoint {
    val oppositeLongitude = if (point.longitude >= 0) -180.0 else 180.0

    return GeoPoint(
        latitude = point.latitude,
        longitude = oppositeLongitude,
        altitude = point.altitude ?: 0.0,
    )
}
