package com.mapconductor.here

import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.GeoCoordinatesUpdate
import com.here.sdk.core.GeoOrientation
import com.here.sdk.core.GeoOrientationUpdate
import com.mapconductor.core.features.GeoPoint

fun GeoPoint.toGeoCoordinates(): GeoCoordinates = GeoCoordinates(latitude, longitude)

fun GeoPoint.Companion.from(geoCoordinates: GeoCoordinates) =
    GeoPoint(geoCoordinates.latitude, geoCoordinates.longitude)

fun GeoCoordinates.toGeoPoint() = GeoPoint.fromLatLong(latitude, longitude)

fun GeoCoordinates.toUpdate() = GeoCoordinatesUpdate(this)

fun GeoOrientation.toUpdate() = GeoOrientationUpdate(this)
