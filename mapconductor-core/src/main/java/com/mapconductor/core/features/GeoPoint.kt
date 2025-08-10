package com.mapconductor.core.features

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mapconductor.core.toFixed
import kotlin.math.abs

interface IGeoPoint {
    val latitude: Double
    val longitude: Double
    val altitude: Double?
}

class GeoPoint(
    latitude: Double,
    longitude: Double,
    altitude: Double = 0.0,
) : IGeoPoint {
    override var latitude by mutableStateOf(latitude)
    override var longitude by mutableStateOf(longitude)
    override var altitude by mutableStateOf(altitude)

    fun toUrlValue(precision: Int = 6): String = "${latitude.toFixed(precision)},${longitude.toFixed(precision)}"

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

    fun copy(
        latitude: Double? = null,
        longitude: Double? = null,
        altitude: Double? = null,
    ): GeoPoint =
        GeoPoint(
            latitude = latitude ?: this.latitude,
            longitude = longitude ?: this.longitude,
            altitude = altitude ?: this.altitude,
        )

    companion object {
        fun fromLatLong(
            latitude: Double,
            longitude: Double,
        ) = GeoPoint(latitude, longitude)

        fun fromLongLat(
            longitude: Double,
            latitude: Double,
        ) = GeoPoint(latitude, longitude)

        fun from(position: IGeoPoint) =
            when (position) {
                is GeoPoint -> position
                else ->
                    GeoPoint(
                        latitude = position.latitude,
                        longitude = position.longitude,
                        altitude = position.altitude ?: 0.0,
                    )
            }
    }
}
