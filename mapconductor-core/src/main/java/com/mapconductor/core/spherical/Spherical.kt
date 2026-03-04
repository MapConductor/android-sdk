package com.mapconductor.core.spherical

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.normalizeLng
import com.mapconductor.core.projection.Earth
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Spherical geometry utility functions for calculating distances, headings,
 * and positions on Earth's surface using the spherical Earth model.
 *
 * This is a Kotlin port of the Cordova Google Maps plugin spherical.js utilities.
 * Uses GeoPointInterface instead of LatLng for coordinate representation.
 */
object Spherical {
    // Mathematical constants
    private const val PI = Math.PI
    private const val RAD_TO_DEG = 180.0 / PI
    private const val DEG_TO_RAD = PI / 180.0

    /**
     * Returns the distance, in meters, between two GeoPointInterface locations.
     * Uses the haversine formula.
     *
     * @param from Starting point
     * @param to Ending point
     * @return Distance in meters
     */
    fun computeDistanceBetween(
        from: GeoPointInterface,
        to: GeoPointInterface,
    ): Double {
        val lat1Rad = from.latitude * DEG_TO_RAD
        val lat2Rad = to.latitude * DEG_TO_RAD
        val deltaLat = (to.latitude - from.latitude) * DEG_TO_RAD
        val deltaLng = (to.longitude - from.longitude) * DEG_TO_RAD

        val haversineA =
            sin(deltaLat / 2) * sin(deltaLat / 2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(deltaLng / 2) * sin(deltaLng / 2)

        val centralAngle = 2 * atan2(sqrt(haversineA), sqrt(1 - haversineA))

        return Earth.RADIUS_METERS * centralAngle
    }

    /**
     * Returns the heading from one GeoPointInterface to another GeoPointInterface.
     * Headings are expressed in degrees clockwise from North within the range (-180, 180).
     *
     * @param from Starting point
     * @param to Ending point
     * @return Heading in degrees
     */
    fun computeHeading(
        from: GeoPointInterface,
        to: GeoPointInterface,
    ): Double {
        val lat1Rad = from.latitude * DEG_TO_RAD
        val lat2Rad = to.latitude * DEG_TO_RAD
        val deltaLng = (to.longitude - from.longitude) * DEG_TO_RAD

        val deltaY = sin(deltaLng) * cos(lat2Rad)
        val deltaX = cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(deltaLng)

        var heading = atan2(deltaY, deltaX) * RAD_TO_DEG

        // Normalize to (-180, 180]
        while (heading > 180) heading -= 360
        while (heading <= -180) heading += 360

        return heading
    }

    /**
     * Returns the GeoPointInterface resulting from moving a distance from an origin
     * in the specified heading (expressed in degrees clockwise from north).
     *
     * @param origin Starting point
     * @param distance Distance to travel in meters
     * @param heading Direction to travel in degrees (0 = North, 90 = East)
     * @return New GeoPointInterface position
     */
    fun computeOffset(
        origin: GeoPointInterface,
        distance: Double,
        heading: Double,
    ): GeoPoint {
        val distanceRad = distance / Earth.RADIUS_METERS
        val headingRad = heading * DEG_TO_RAD
        val lat1Rad = origin.latitude * DEG_TO_RAD
        val lng1Rad = origin.longitude * DEG_TO_RAD

        val lat2Rad =
            asin(
                sin(lat1Rad) * cos(distanceRad) +
                    cos(lat1Rad) * sin(distanceRad) * cos(headingRad),
            )

        val lng2Rad =
            lng1Rad +
                atan2(
                    sin(headingRad) * sin(distanceRad) * cos(lat1Rad),
                    cos(distanceRad) - sin(lat1Rad) * sin(lat2Rad),
                )

        return GeoPoint(
            latitude = lat2Rad * RAD_TO_DEG,
            longitude = lng2Rad * RAD_TO_DEG,
            altitude = origin.altitude ?: 0.0,
        )
    }

    /**
     * Returns the location of origin when provided with a GeoPointInterface destination,
     * meters travelled and original heading.
     * Headings are expressed in degrees clockwise from North.
     *
     * @param to Destination point
     * @param distance Distance travelled in meters
     * @param heading Original heading in degrees
     * @return Origin GeoPointInterface position, or null if no solution is available
     */
    fun computeOffsetOrigin(
        to: GeoPointInterface,
        distance: Double,
        heading: Double,
    ): GeoPoint? {
        // Calculate the reverse heading
        val reverseHeading = (heading + 180) % 360

        return try {
            computeOffset(to, distance, reverseHeading)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Returns the length of the given path in meters.
     *
     * @param path List of GeoPointInterface locations defining the path
     * @return Length in meters
     */
    fun computeLength(path: List<GeoPointInterface>): Double {
        if (path.size < 2) return 0.0

        var length = 0.0
        for (i in 1 until path.size) {
            length += computeDistanceBetween(path[i - 1], path[i])
        }

        return length
    }

    /**
     * Returns the area of a closed path in square meters.
     * The path should form a closed polygon.
     *
     * @param path List of GeoPointInterface locations defining the closed path
     * @return Area in square meters
     */
    fun computeArea(path: List<GeoPointInterface>): Double = abs(computeSignedArea(path))

    /**
     * Returns the signed area of a closed path in square meters.
     * The signed area may be used to determine the orientation of the path.
     * Positive values indicate counter-clockwise orientation,
     * negative values indicate clockwise orientation.
     *
     * @param path List of GeoPointInterface locations defining the closed path
     * @return Signed area in square meters
     */
    fun computeSignedArea(path: List<GeoPointInterface>): Double {
        if (path.size < 3) return 0.0

        var area = 0.0
        val pointCount = path.size

        for (i in path.indices) {
            val j = (i + 1) % pointCount
            val lat1 = path[i].latitude * DEG_TO_RAD
            val lat2 = path[j].latitude * DEG_TO_RAD
            val deltaLng = (path[j].longitude - path[i].longitude) * DEG_TO_RAD

            area += deltaLng * (2 + sin(lat1) + sin(lat2))
        }

        return area * Earth.RADIUS_METERS * Earth.RADIUS_METERS / 2.0
    }

    /**
     * Interpolates between two GeoPointInterface locations along the great circle path using spherical linear interpolation (Slerp).
     * This method considers Earth's curvature and provides high accuracy for any distance.
     *
     * @param from Starting point
     * @param to Ending point
     * @param fraction Interpolation fraction (0.0 = from, 1.0 = to)
     * @return Interpolated GeoPointInterface position
     */
    fun sphericalInterpolate(
        from: GeoPointInterface,
        to: GeoPointInterface,
        fraction: Double,
    ): GeoPoint {
        // ラジアンに変換
        val lat1 = from.latitude * DEG_TO_RAD
        val lng1 = from.longitude * DEG_TO_RAD
        val lat2 = to.latitude * DEG_TO_RAD
        val lng2 = to.longitude * DEG_TO_RAD

        // 3D単位ベクトルに変換
        val x1 = cos(lat1) * cos(lng1)
        val y1 = cos(lat1) * sin(lng1)
        val z1 = sin(lat1)

        val x2 = cos(lat2) * cos(lng2)
        val y2 = cos(lat2) * sin(lng2)
        val z2 = sin(lat2)

        // 内積から角度を求める
        val dot = x1 * x2 + y1 * y2 + z1 * z2
        val angle = acos(dot.coerceIn(-1.0, 1.0))

        // 非常に近い点は線形補間
        if (angle < 1e-6) {
            val interpolatedAltitude =
                when {
                    from.altitude != null && to.altitude != null ->
                        from.altitude!! + fraction * (to.altitude!! - from.altitude!!)
                    from.altitude != null -> from.altitude
                    to.altitude != null -> to.altitude
                    else -> 0.0
                }

            return GeoPoint(
                latitude = from.latitude + fraction * (to.latitude - from.latitude),
                longitude = from.longitude + fraction * (to.longitude - from.longitude),
                altitude = interpolatedAltitude!!,
            )
        }

        // 球面線形補間（Slerp）
        val sinAngle = sin(angle)
        val weightFrom = sin((1 - fraction) * angle) / sinAngle
        val weightTo = sin(fraction * angle) / sinAngle

        val vectorX = weightFrom * x1 + weightTo * x2
        val vectorY = weightFrom * y1 + weightTo * y2
        val vectorZ = weightFrom * z1 + weightTo * z2

        // 3Dベクトルから緯度経度に変換
        val lat = asin(vectorZ) * RAD_TO_DEG
        val lng = atan2(vectorY, vectorX) * RAD_TO_DEG

        val interpolatedAltitude =
            when {
                from.altitude != null && to.altitude != null ->
                    from.altitude!! + fraction * (to.altitude!! - from.altitude!!)
                from.altitude != null -> from.altitude
                to.altitude != null -> to.altitude
                else -> 0.0
            }

        return GeoPoint(
            latitude = lat,
            longitude = lng,
            altitude = interpolatedAltitude!!,
        )
    }

    /**
     * Performs linear interpolation between two GeoPointInterface locations without considering Earth's curvature.
     * This method treats coordinates as if they were on a flat plane, which may result in
     * inaccurate results for large distances but is computationally faster.
     *
     * For longitude interpolation, this method automatically chooses the shorter path,
     * which may cross the 180°/-180° meridian line if that results in a shorter distance.
     *
     * Use this method only when:
     * - Working with small distances where Earth's curvature is negligible
     * - Performance is critical and accuracy can be sacrificed
     * - Working with projected coordinate systems
     *
     * @param from Starting point
     * @param to Ending point
     * @param fraction Interpolation fraction (0.0 = from, 1.0 = to)
     * @return Linearly interpolated GeoPointInterface position
     */
    fun linearInterpolate(
        from: GeoPointInterface,
        to: GeoPointInterface,
        fraction: Double,
    ): GeoPoint {
        val interpolatedAltitude =
            when {
                from.altitude != null && to.altitude != null ->
                    from.altitude!! + fraction * (to.altitude!! - from.altitude!!)
                from.altitude != null -> from.altitude
                to.altitude != null -> to.altitude
                else -> 0.0
            }

        // Latitude interpolation is straightforward
        val interpolatedLatitude = from.latitude + fraction * (to.latitude - from.latitude)

        // Longitude interpolation: choose the shorter path
        val fromLng = from.longitude
        val toLng = to.longitude

        // Calculate both possible longitude differences
        val directDiff = toLng - fromLng
        val crossMeridianDiff =
            when {
                directDiff > 180 -> directDiff - 360 // Cross westward
                directDiff < -180 -> directDiff + 360 // Cross eastward
                else -> directDiff // Direct path is shorter
            }

        // Use the shorter difference for interpolation
        val interpolatedLongitude = fromLng + fraction * crossMeridianDiff

        // Normalize longitude to [-180, 180] range
        val normalizedLongitude = com.mapconductor.core.normalizeLng(interpolatedLongitude)

        return GeoPoint(
            latitude = interpolatedLatitude,
            longitude = normalizedLongitude,
            altitude = interpolatedAltitude!!,
        )
    }

    /**
     * Clamps latitude to the range [-90, 90].
     */
    private fun clampLat(lat: Double): Double = lat.coerceIn(-90.0, 90.0)
}
