package com.mapconductor.mapbox

import com.mapbox.geojson.Point
import com.mapconductor.core.features.GeoPoint

fun GeoPoint.toPoint() : Point = Point.fromLngLat(longitude, latitude)
fun GeoPoint.Companion.from(point: Point) = GeoPoint(point.latitude(), point.longitude())
fun Point.toGeoPoint() = GeoPoint.fromLongLat(longitude(), latitude())
