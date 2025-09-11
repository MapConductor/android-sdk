package com.mapconductor.mapbox

import com.mapbox.maps.CoordinateBounds
import com.mapconductor.core.features.GeoRectBounds

fun GeoRectBounds.toGeoBox(): CoordinateBounds? {
    val sw = southWest ?: return null
    val ne = northEast ?: return null

    return CoordinateBounds(
        sw.toPoint(),
        ne.toPoint(),
    )
}

fun CoordinateBounds.toGeoRectBounds(): GeoRectBounds {
    val sw = southwest.toGeoPoint()
    val ne = northeast.toGeoPoint()
    return GeoRectBounds(
        southWest = sw,
        northEast = ne,
    )
}
