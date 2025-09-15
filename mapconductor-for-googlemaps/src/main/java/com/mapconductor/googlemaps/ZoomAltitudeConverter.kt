package com.mapconductor.googlemaps

import com.mapconductor.core.zoom.AbstractZoomAltitudeConverter
import kotlin.math.log2
import kotlin.math.pow

class ZoomAltitudeConverter(
    zoom0Altitude: Double = DEFAULT_ZOOM0_ALTITUDE,
) : AbstractZoomAltitudeConverter(zoom0Altitude) {

    override fun zoomLevelToAltitude(zoomLevel: Double, latitude: Double, tilt: Double): Double {
        // Google Maps uses direct zoom levels without altitude conversion
        // For compatibility with the unified system, we simulate altitude
        val clampedZoom = zoomLevel.coerceIn(MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL)
        val altitude = zoom0Altitude / ZOOM_FACTOR.pow(clampedZoom)
        return altitude.coerceIn(MIN_ALTITUDE, MAX_ALTITUDE)
    }

    override fun altitudeToZoomLevel(altitude: Double, latitude: Double, tilt: Double): Double {
        // Google Maps uses direct zoom levels without altitude conversion
        // For compatibility with the unified system, we simulate zoom from altitude
        val clampedAltitude = altitude.coerceIn(MIN_ALTITUDE, MAX_ALTITUDE)
        val zoomLevel = log2(zoom0Altitude / clampedAltitude)
        return zoomLevel.coerceIn(MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL)
    }
}