package com.mapconductor.mapbox

import com.mapbox.geojson.Point
import com.mapconductor.core.features.GeoPoint

fun GeoPoint.toPoint(): Point = Point.fromLngLat(longitude, latitude, altitude)

fun GeoPoint.Companion.from(point: Point) =
    GeoPoint(
        latitude = point.latitude(),
        longitude = point.longitude(),
        altitude = point.altitude(),
    )

fun Point.toGeoPoint() = GeoPoint.fromLongLat(longitude(), latitude())
