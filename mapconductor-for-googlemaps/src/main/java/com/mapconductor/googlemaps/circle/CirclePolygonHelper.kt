package com.mapconductor.googlemaps.circle

import com.google.android.gms.maps.model.LatLng
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Helper object to generate polygon points for circles.
 * Supports both geodesic (great circle on sphere) and non-geodesic (flat plane) circles.
 */
object CirclePolygonHelper {
    private const val NUM_SEGMENTS = 64 // Number of polygon segments for circle approximation
    private const val EARTH_RADIUS_METERS = 6371000.0 // Earth's radius in meters

    /**
     * Generates polygon points for a circle.
     *
     * @param center Center point of the circle
     * @param radiusMeters Radius in meters
     * @param geodesic If true, creates a geodesic circle (great circle on sphere).
     *                 If false, creates a non-geodesic circle (flat plane projection).
     * @return List of LatLng points forming a polygon approximation of the circle
     */
    fun generateCirclePoints(
        center: LatLng,
        radiusMeters: Double,
        geodesic: Boolean,
    ): List<LatLng> =
        if (geodesic) {
            generateGeodesicCirclePoints(center, radiusMeters)
        } else {
            generateNonGeodesicCirclePoints(center, radiusMeters)
        }

    /**
     * Generates geodesic circle points using the Haversine formula.
     * This creates a true circle on the Earth's surface (great circle).
     */
    private fun generateGeodesicCirclePoints(
        center: LatLng,
        radiusMeters: Double,
    ): List<LatLng> {
        val points = mutableListOf<LatLng>()
        val centerLatRad = Math.toRadians(center.latitude)
        val centerLngRad = Math.toRadians(center.longitude)
        val angularDistance = radiusMeters / EARTH_RADIUS_METERS

        for (i in 0..NUM_SEGMENTS) {
            val bearing = 2.0 * PI * i / NUM_SEGMENTS

            // Calculate destination point using great circle formula
            val lat =
                atan2(
                    sin(centerLatRad) * cos(angularDistance) +
                        cos(centerLatRad) * sin(angularDistance) * cos(bearing),
                    sqrt(
                        (
                            cos(centerLatRad) * cos(angularDistance) -
                                sin(centerLatRad) * sin(angularDistance) * cos(bearing)
                        ) *
                            (
                                cos(centerLatRad) * cos(angularDistance) -
                                    sin(centerLatRad) * sin(angularDistance) * cos(bearing)
                            ) +
                            (sin(angularDistance) * sin(bearing)) *
                            (sin(angularDistance) * sin(bearing)),
                    ),
                )

            val lng =
                centerLngRad +
                    atan2(
                        sin(bearing) * sin(angularDistance) * cos(centerLatRad),
                        cos(angularDistance) - sin(centerLatRad) * sin(lat),
                    )

            points.add(
                LatLng(
                    Math.toDegrees(lat),
                    Math.toDegrees(lng),
                ),
            )
        }

        return points
    }

    /**
     * Generates non-geodesic circle points using simple planar projection.
     * This creates a circle on a flat plane (good for small radii and low latitudes).
     */
    private fun generateNonGeodesicCirclePoints(
        center: LatLng,
        radiusMeters: Double,
    ): List<LatLng> {
        val points = mutableListOf<LatLng>()
        val centerLatRad = Math.toRadians(center.latitude)

        // Calculate degrees per meter for latitude and longitude at the center point
        val latDegreesPerMeter = 1.0 / (EARTH_RADIUS_METERS * PI / 180.0)
        val lngDegreesPerMeter = 1.0 / (EARTH_RADIUS_METERS * PI / 180.0 * cos(centerLatRad))

        for (i in 0..NUM_SEGMENTS) {
            val angle = 2.0 * PI * i / NUM_SEGMENTS
            val dx = radiusMeters * cos(angle)
            val dy = radiusMeters * sin(angle)

            val lat = center.latitude + dy * latDegreesPerMeter
            val lng = center.longitude + dx * lngDegreesPerMeter

            points.add(LatLng(lat, lng))
        }

        return points
    }
}
