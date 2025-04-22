package com.mapconductor.arcgis

import androidx.annotation.Keep
import com.arcgismaps.geometry.Point
import com.arcgismaps.geometry.SpatialReference
import com.mapconductor.core.GeoPointBase
import com.mapconductor.core.GeoPointInterface

interface GeoPointArcGisImpl: GeoPointInterface {
    fun toPoint() : Point
}
@ConsistentCopyVisibility
data class GeoPoint private constructor(
    override val latitude: Double,
    override val longitude: Double,
    override val altitude: Double = 0.0,
): GeoPointBase(latitude, longitude, altitude), GeoPointArcGisImpl {

    override fun toPoint(): Point = Point(
        longitude,
        latitude,
        altitude,
        SpatialReference.wgs84(),
    )

    companion object {
        @Keep
        @JvmStatic
        fun fromLatLong(latitude: Double, longitude: Double, altitude: Double? = 0.0) =
            GeoPoint(latitude, longitude, altitude ?: 0.0)

        @Keep
        @JvmStatic
        fun fromLongLat(longitude: Double, latitude: Double, altitude: Double? = 0.0) =
            GeoPoint(latitude, longitude, altitude ?: 0.0)

        @Keep
        @JvmStatic
        fun fromImpl(geoPointImpl: GeoPointInterface) = when(geoPointImpl) {
            is GeoPoint -> geoPointImpl
            else -> GeoPoint(
                geoPointImpl.latitude,
                geoPointImpl.longitude,
                geoPointImpl.altitude ?: 0.0,
            )
        }
    }
}

fun Point.toGeoPoint() = GeoPoint.fromLongLat(
    longitude = this.y,
    latitude = this.x,
    altitude = this.z,
)