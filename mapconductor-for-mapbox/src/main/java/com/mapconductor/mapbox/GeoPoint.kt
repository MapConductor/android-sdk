package com.mapconductor.mapbox

import com.mapbox.geojson.Point
import com.mapconductor.core.features.GeoPointImpl

fun GeoPointImpl.toPoint(): Point = Point.fromLngLat(longitude, latitude, altitude)

fun GeoPointImpl.Companion.from(point: Point) = GeoPointImpl(
    latitude = point.latitude(),
    longitude = point.longitude(),
    altitude = point.altitude(),
)

fun Point.toGeoPoint() = GeoPointImpl.fromLongLat(longitude(), latitude())
