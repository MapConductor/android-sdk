package com.mapconductor.arcgis

import com.arcgismaps.geometry.GeometryEngine
import com.arcgismaps.geometry.Point
import com.arcgismaps.geometry.SpatialReference
import com.mapconductor.core.features.GeoPoint

fun GeoPoint.toPoint(spatialReference: SpatialReference = SpatialReference.wgs84()) : Point =
    Point(x = longitude, y = latitude, z = altitude, spatialReference = spatialReference)

fun GeoPoint.Companion.fromLatLongAltitude(latitude: Double, longitude: Double, altitude: Double) =
    GeoPoint(latitude = latitude, longitude = longitude, altitude = altitude)

fun GeoPoint.Companion.fromLongLat(longitude: Double, latitude: Double, altitude: Double) =
    GeoPoint(latitude = latitude, longitude = longitude, altitude = altitude)

fun GeoPoint.Companion.from(point: Point) {
    val wgs84Point = if (point.spatialReference != SpatialReference.wgs84()) {
        GeometryEngine.projectOrNull(point, SpatialReference.wgs84()) as Point
    } else {
        point
    }

    GeoPoint(
        longitude = wgs84Point.x,
        latitude = wgs84Point.y,
        altitude = wgs84Point.z ?: 0.0
    )
}

fun Point.toGeoPoint(): GeoPoint {
    val wgs84Point = if (this.spatialReference != SpatialReference.wgs84()) {
        GeometryEngine.projectOrNull(this, SpatialReference.wgs84()) as Point
    } else {
        this
    }
    return GeoPoint(
        longitude = wgs84Point.x,
        latitude = wgs84Point.y,
        altitude = wgs84Point.z ?: 0.0,
    )
}