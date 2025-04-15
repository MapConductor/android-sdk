package com.mapconductor.googlemaps

import androidx.annotation.Keep
import com.google.android.gms.maps.model.LatLng
import com.mapconductor.core.GeoPointBase
import com.mapconductor.core.GeoPointImpl

interface GeoPointGMapsImpl: GeoPointImpl {
    fun toLatLng(): LatLng
}
data class GeoPoint private constructor(
    override val latitude: Double,
    override val longitude: Double,
): GeoPointBase(), GeoPointGMapsImpl {
    override fun toLatLng() = LatLng(latitude, longitude)
    companion object {
        @Keep
        @JvmStatic
        fun fromLatLong(latitude: Double, longitude: Double) = GeoPoint(latitude, longitude)

        @Keep
        @JvmStatic
        fun fromLongLat(longitude: Double, latitude: Double) = GeoPoint(latitude, longitude)
    }
}

fun LatLng.toGeoPoint() = GeoPoint.fromLatLong(latitude, longitude)
