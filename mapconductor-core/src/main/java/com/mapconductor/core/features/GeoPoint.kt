package com.mapconductor.core.features

import com.mapconductor.core.toFixed
import kotlin.math.abs

interface GeoPointInterface {
    fun wrap(): GeoPointInterface

    val latitude: Double
    val longitude: Double
    val altitude: Double?
}

data class GeoPoint(
    override val latitude: Double,
    override val longitude: Double,
    override val altitude: Double = 0.0,
) : GeoPointInterface {
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

    override fun wrap(): GeoPointInterface {
        var wrappedLatitude = latitude
        var wrappedLongitude = longitude

        // Handle latitude overflow/underflow
        if (wrappedLatitude > 90.0) {
            // If latitude exceeds 90, wrap around from -90
            val excess = wrappedLatitude - 90.0
            wrappedLatitude = -90.0 + excess
            // Also flip longitude by 180 degrees when crossing pole
            wrappedLongitude += 180.0
        } else if (wrappedLatitude < -90.0) {
            // If latitude is below -90, wrap around from 90
            val deficit = -90.0 - wrappedLatitude
            wrappedLatitude = 90.0 - deficit
            // Also flip longitude by 180 degrees when crossing pole
            wrappedLongitude += 180.0
        }

        // Normalize longitude to [-180, 180] range
        wrappedLongitude = (((wrappedLongitude + 180) % 360 + 360) % 360) - 180

        return GeoPoint(wrappedLatitude, wrappedLongitude, altitude)
    }

    companion object {
        fun fromLatLong(
            latitude: Double,
            longitude: Double,
        ) = GeoPoint(latitude, longitude)

        fun fromLongLat(
            longitude: Double,
            latitude: Double,
        ) = GeoPoint(latitude, longitude)

        fun from(position: GeoPointInterface) =
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

/**
 * Extension function to create a normalized GeoPointInterface with clamped/normalized coordinates
 */
fun GeoPointInterface.normalize(): GeoPoint =
    GeoPoint(
        latitude = this.latitude.coerceIn(-90.0, 90.0),
        longitude = (((this.longitude + 180) % 360 + 360) % 360) - 180,
        altitude = this.altitude ?: 0.0,
    )

/**
 * Extension function to check if a GeoPointInterface is valid
 */
fun GeoPointInterface.isValid(): Boolean = latitude in -90.0..90.0 && longitude in -180.0..180.0
