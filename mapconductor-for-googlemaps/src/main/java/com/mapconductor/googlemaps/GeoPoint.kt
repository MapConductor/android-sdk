package com.mapconductor.googlemaps

import com.google.android.gms.maps.model.LatLng
import com.mapconductor.core.features.GeoPoint

fun GeoPoint.toLatLng(): LatLng = LatLng(latitude, longitude)

fun GeoPoint.Companion.from(latLng: LatLng) = GeoPoint(latLng.latitude, latLng.longitude)

fun LatLng.toGeoPoint() = GeoPoint.fromLatLong(latitude, longitude)
