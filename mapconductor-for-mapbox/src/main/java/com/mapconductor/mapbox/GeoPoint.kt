package com.mapconductor.mapbox

import androidx.annotation.Keep
import com.mapbox.geojson.Point
import com.mapconductor.core.GeoPointBase
import com.mapconductor.core.GeoPointImpl

interface GeoPointMapboxImpl: GeoPointImpl {
    fun toPoint(): Point
}
@ConsistentCopyVisibility
data class GeoPoint private constructor(
    override val latitude: Double,
    override val longitude: Double,
): GeoPointBase(latitude, longitude), GeoPointMapboxImpl {
    override fun toPoint() = Point.fromLngLat(longitude, latitude)
    companion object {
        @Keep
        @JvmStatic
        fun fromLatLong(latitude: Double, longitude: Double) = GeoPoint(latitude, longitude)

        @Keep
        @JvmStatic
        fun fromLongLat(longitude: Double, latitude: Double) = GeoPoint(latitude, longitude)

        @Keep
        @JvmStatic
        fun fromImpl(geoPointImpl: GeoPointImpl) = when(geoPointImpl) {
            is GeoPoint -> geoPointImpl
            else -> GeoPoint(
                geoPointImpl.latitude,
                geoPointImpl.longitude,
            )
        }
    }
}

fun Point.toGeoPoint() = GeoPoint.fromLongLat(longitude(), latitude())
