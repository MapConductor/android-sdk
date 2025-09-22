package com.mapconductor.mapbox

import com.mapbox.geojson.Point
import com.mapconductor.core.features.GeoPointImpl

fun GeoPointImpl.toPoint(): Point = Point.fromLngLat(longitude, latitude)

fun GeoPointImpl.Companion.from(point: Point) = GeoPointImpl(point.latitude(), point.longitude())

fun Point.toGeoPoint() = GeoPointImpl.fromLongLat(longitude(), latitude())
