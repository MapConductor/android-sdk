package com.mapconductor.googlemaps

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoRectBounds

fun GeoRectBounds.toLatLngBounds(): LatLngBounds? {
    val sw = southWest ?: return null
    val ne = northEast ?: return null

    return LatLngBounds(
        LatLng(sw.latitude, sw.longitude),
        LatLng(ne.latitude, ne.longitude),
    )
}

fun LatLngBounds.toGeoRectBounds(): GeoRectBounds =
    GeoRectBounds(
        southWest = GeoPoint(southwest.latitude, southwest.longitude),
        northEast = GeoPoint(northeast.latitude, northeast.longitude),
    )
