package com.mapconductor.arcgis

import com.arcgismaps.geometry.GeometryEngine
import com.arcgismaps.geometry.Point
import com.arcgismaps.geometry.SpatialReference
import com.mapconductor.core.features.GeoPoint

fun GeoPoint.toPoint(spatialReference: SpatialReference = SpatialReference.webMercator()) : Point =
    Point(x = longitude, y = latitude, z = altitude, spatialReference = spatialReference)

fun GeoPoint.Companion.fromLatLongAltitude(latitude: Double, longitude: Double, altitude: Double) =
    GeoPoint(latitude = latitude, longitude = longitude, altitude = altitude)

fun GeoPoint.Companion.fromLongLat(longitude: Double, latitude: Double, altitude: Double) =
    GeoPoint(latitude = latitude, longitude = longitude, altitude = altitude)

fun GeoPoint.Companion.from(point: Point) {
    val wehMercatorPoint = if (point.spatialReference != SpatialReference.webMercator()) {
        GeometryEngine.projectOrNull(point, SpatialReference.webMercator()) as Point
    } else {
        point
    }

    GeoPoint(
        longitude = wehMercatorPoint.x,
        latitude = wehMercatorPoint.y,
        altitude = wehMercatorPoint.z ?: 0.0
    )
}

fun Point.toGeoPoint(): GeoPoint {
    val wehMercatorPoint = if (this.spatialReference != SpatialReference.webMercator()) {
        GeometryEngine.projectOrNull(this, SpatialReference.webMercator()) as Point
    } else {
        this
    }
    return GeoPoint(
        longitude = wehMercatorPoint.x,
        latitude = wehMercatorPoint.y,
        altitude = wehMercatorPoint.z ?: 0.0,
    )
}