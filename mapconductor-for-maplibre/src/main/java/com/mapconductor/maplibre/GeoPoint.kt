package com.mapconductor.maplibre

import com.mapconductor.core.features.GeoPointImpl
import org.maplibre.android.geometry.LatLng

fun GeoPointImpl.toLatLng(): LatLng = LatLng(this.latitude, this.longitude, this.altitude)

fun GeoPointImpl.Companion.from(latLng: LatLng) = GeoPointImpl(latLng.latitude, latLng.longitude, latLng.altitude)

fun LatLng.toGeoPoint() = GeoPointImpl(latitude, longitude, altitude)

