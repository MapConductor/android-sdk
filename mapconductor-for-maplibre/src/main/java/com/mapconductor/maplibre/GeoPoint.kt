package com.mapconductor.maplibre

import com.mapconductor.core.features.GeoPoint
import org.maplibre.android.geometry.LatLng
import org.maplibre.geojson.Point

fun GeoPoint.toLatLng(): LatLng = LatLng(this.latitude, this.longitude, this.altitude)

fun GeoPoint.Companion.from(latLng: LatLng) = GeoPoint(latLng.latitude, latLng.longitude, latLng.altitude)

fun LatLng.toGeoPoint() = GeoPoint(latitude, longitude, altitude)

fun GeoPoint.toPoint(): Point = Point.fromLngLat(longitude, latitude)

fun GeoPoint.Companion.from(point: Point) =
    GeoPoint(
        latitude = point.latitude(),
        longitude = point.longitude(),
        altitude = point.altitude(),
    )
