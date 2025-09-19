package com.mapconductor.here

import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.GeoCoordinatesUpdate
import com.here.sdk.core.GeoOrientation
import com.here.sdk.core.GeoOrientationUpdate
import com.mapconductor.core.features.GeoPointImpl

fun GeoPointImpl.toGeoCoordinates(): GeoCoordinates = GeoCoordinates(latitude, longitude)

fun GeoPointImpl.Companion.from(geoCoordinates: GeoCoordinates) =
    GeoPointImpl(geoCoordinates.latitude, geoCoordinates.longitude)

fun GeoCoordinates.toGeoPoint() = GeoPointImpl.fromLatLong(latitude, longitude)

fun GeoCoordinates.toUpdate() = GeoCoordinatesUpdate(this)

fun GeoOrientation.toUpdate() = GeoOrientationUpdate(this)
