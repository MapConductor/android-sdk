package com.mapconductor.here

import com.here.sdk.core.GeoBox
import com.mapconductor.core.features.GeoRectBounds

fun GeoRectBounds.toGeoBox(): GeoBox? {
    val sw = southWest ?: return null
    val ne = northEast ?: return null

    return GeoBox(
        sw.toGeoCoordinates(),
        ne.toGeoCoordinates(),
    )
}

fun GeoBox.toGeoRectBounds(): GeoRectBounds {
    val sw = southWestCorner.toGeoPoint()
    val ne = northEastCorner.toGeoPoint()
    return GeoRectBounds(
        southWest = sw,
        northEast = ne,
    )
}
