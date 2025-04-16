package com.mapconductor.here

import androidx.annotation.Keep
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.GeoCoordinatesUpdate
import com.here.sdk.core.GeoOrientation
import com.here.sdk.core.GeoOrientationUpdate
import com.mapconductor.core.GeoPointBase
import com.mapconductor.core.GeoPointImpl

interface GeoPointHereImpl: GeoPointImpl {
    fun toGeoCoordinates(): GeoCoordinates

}

@ConsistentCopyVisibility
data class GeoPoint private constructor(
    override val latitude: Double,
    override val longitude: Double,
): GeoPointBase(latitude, longitude), GeoPointHereImpl {
    override fun toGeoCoordinates() = GeoCoordinates(latitude, longitude)

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

internal fun GeoCoordinates.toGeoPoint() = GeoPoint.fromLatLong(latitude, longitude)
internal fun GeoCoordinates.toUpdate() = GeoCoordinatesUpdate(this)
internal fun GeoOrientation.toUpdate() = GeoOrientationUpdate(this)