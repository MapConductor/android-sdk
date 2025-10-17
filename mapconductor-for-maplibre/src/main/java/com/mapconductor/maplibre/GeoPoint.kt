package com.mapconductor.maplibre

import com.mapconductor.core.features.GeoPointImpl
import org.maplibre.android.geometry.LatLng
import org.maplibre.geojson.Point

fun GeoPointImpl.toLatLng(): LatLng = LatLng(this.latitude, this.longitude, this.altitude)

fun GeoPointImpl.Companion.from(latLng: LatLng) = GeoPointImpl(latLng.latitude, latLng.longitude, latLng.altitude)

fun LatLng.toGeoPoint() = GeoPointImpl(latitude, longitude, altitude)

fun GeoPointImpl.toPoint(): Point = Point.fromLngLat(longitude, latitude)

fun GeoPointImpl.Companion.from(point: Point) = GeoPointImpl(
    latitude = point.latitude(),
    longitude = point.longitude(),
    altitude = point.altitude(),
)
