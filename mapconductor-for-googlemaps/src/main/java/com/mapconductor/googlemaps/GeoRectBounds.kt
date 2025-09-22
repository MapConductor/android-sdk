package com.mapconductor.googlemaps

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.mapconductor.core.features.GeoPointImpl
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
        southWest = GeoPointImpl(southwest.latitude, southwest.longitude),
        northEast = GeoPointImpl(northeast.latitude, northeast.longitude),
    )
