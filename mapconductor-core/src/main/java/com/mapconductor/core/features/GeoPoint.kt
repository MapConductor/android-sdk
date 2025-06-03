package com.mapconductor.core.features

import com.mapconductor.core.toFixed
import kotlin.math.abs

interface IGeoPoint {
    val latitude: Double
    val longitude: Double
    val altitude: Double?
}

class GeoPoint(
    override val latitude: Double,
    override val longitude: Double,
    override val altitude: Double = 0.0,
) : IGeoPoint {

    fun toUrlValue(precision: Int = 6): String {
        return "${latitude.toFixed(precision)},${longitude.toFixed(precision)}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GeoPoint) return false

        val tolerance = 1e-7
        return abs(latitude - other.latitude) < tolerance &&
                abs(longitude - other.longitude) < tolerance &&
                abs(altitude - other.altitude) < tolerance
    }

    override fun hashCode(): Int {
        // 誤差許容しているため、丸めた値を使って hash を安定させる
        val latHash = (latitude * 1e7).toLong()
        val lngHash = (longitude * 1e7).toLong()
        val altHash = (altitude * 1e7).toLong()

        var result = latHash.hashCode()
        result = 31 * result + lngHash.hashCode()
        result = 31 * result + altHash.hashCode()
        return result
    }

    companion object {
        fun fromLatLong(latitude: Double, longitude: Double) = GeoPoint(latitude, longitude)
        fun fromLongLat(longitude: Double, latitude: Double) = GeoPoint(latitude, longitude)
        fun from(position: IGeoPoint) = when(position) {
            is GeoPoint -> position
            else -> GeoPoint(
                latitude = position.latitude,
                longitude = position.longitude,
                altitude = position.altitude ?: 0.0,
            )
        }
    }
}


