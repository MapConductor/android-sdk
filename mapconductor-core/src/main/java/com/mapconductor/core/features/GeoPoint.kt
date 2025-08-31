package com.mapconductor.core.features

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.mapconductor.core.spherical.Spherical
import com.mapconductor.core.toFixed
import kotlin.math.abs

interface IGeoPoint {
    val latitude: Double
    val longitude: Double
    val altitude: Double?
}

data class GeoPoint(
    override val latitude: Double,
    override val longitude: Double,
    override val altitude: Double = 0.0,
) : IGeoPoint {
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
// class GeoPoint(
//    latitude: Double,
//    longitude: Double,
//    altitude: Double = 0.0,
// ) : IGeoPoint {
//    override var latitude by mutableStateOf(latitude)
//    override var longitude by mutableStateOf(longitude)
//    override var altitude by mutableStateOf(altitude)
//
//    fun toUrlValue(precision: Int = 6): String = "${latitude.toFixed(precision)},${longitude.toFixed(precision)}"
//
//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (other !is GeoPoint) return false
//
//        val tolerance = 1e-7
//        return abs(latitude - other.latitude) < tolerance &&
//            abs(longitude - other.longitude) < tolerance &&
//            abs(altitude - other.altitude) < tolerance
//    }
//
//    override fun hashCode(): Int {
//        // 誤差許容しているため、丸めた値を使って hash を安定させる
//        val latHash = (latitude * 1e7).toLong()
//        val lngHash = (longitude * 1e7).toLong()
//        val altHash = (altitude * 1e7).toLong()
//
//        var result = latHash.hashCode()
//        result = 31 * result + lngHash.hashCode()
//        result = 31 * result + altHash.hashCode()
//        return result
//    }
//
//    fun copy(
//        latitude: Double? = null,
//        longitude: Double? = null,
//        altitude: Double? = null,
//    ): GeoPoint =
//        GeoPoint(
//            latitude = latitude ?: this.latitude,
//            longitude = longitude ?: this.longitude,
//            altitude = altitude ?: this.altitude,
//        )
//
//    companion object {
//        fun fromLatLong(
//            latitude: Double,
//            longitude: Double,
//        ) = GeoPoint(latitude, longitude)
//
//        fun fromLongLat(
//            longitude: Double,
//            latitude: Double,
//        ) = GeoPoint(latitude, longitude)
//
//        fun from(position: IGeoPoint) = GeoPoint(
//            latitude = position.latitude,
//            longitude = position.longitude,
//            altitude = position.altitude ?: 0.0,
//        )
//    }
// }

/**
 * Extension function to create a normalized GeoPoint with clamped/normalized coordinates
 */
fun IGeoPoint.normalize(): GeoPoint =
    GeoPoint(
        latitude = this.latitude.coerceIn(-90.0, 90.0),
        longitude = (((this.longitude + 180) % 360 + 360) % 360) - 180,
        altitude = this.altitude ?: 0.0,
    )

/**
 * Extension function to check if a GeoPoint is valid
 */
fun IGeoPoint.isValid(): Boolean = latitude in -90.0..90.0 && longitude in -180.0..180.0

/**
 * Extension function to calculate distance to another point
 */
fun IGeoPoint.distanceTo(other: IGeoPoint): Double = Spherical.computeDistanceBetween(this, other)

/**
 * Extension function to calculate heading to another point
 */
fun IGeoPoint.headingTo(other: IGeoPoint): Double = Spherical.computeHeading(this, other)

/**
 * Extension function to move to a new position
 */
fun IGeoPoint.offset(
    distance: Double,
    heading: Double,
): GeoPoint = Spherical.computeOffset(this, distance, heading)

/**
 * Extension function for spherical interpolation (considers Earth's curvature)
 */
fun IGeoPoint.interpolateTo(
    other: IGeoPoint,
    fraction: Double,
): GeoPoint = Spherical.interpolate(this, other, fraction)

/**
 * Extension function for linear interpolation (ignores Earth's curvature)
 */
fun IGeoPoint.linearInterpolateTo(
    other: IGeoPoint,
    fraction: Double,
): GeoPoint = Spherical.linearInterpolate(this, other, fraction)
