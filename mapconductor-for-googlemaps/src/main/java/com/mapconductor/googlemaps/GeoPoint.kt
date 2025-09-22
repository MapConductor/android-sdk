package com.mapconductor.googlemaps

import com.google.android.gms.maps.model.LatLng
import com.mapconductor.core.features.GeoPointImpl

fun GeoPointImpl.toLatLng(): LatLng = LatLng(latitude, longitude)

fun GeoPointImpl.Companion.from(latLng: LatLng) = GeoPointImpl(latLng.latitude, latLng.longitude)

fun LatLng.toGeoPoint() = GeoPointImpl.fromLatLong(latitude, longitude)
