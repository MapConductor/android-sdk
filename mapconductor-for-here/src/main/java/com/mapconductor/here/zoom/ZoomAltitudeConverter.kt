package com.mapconductor.here.zoom

import com.mapconductor.core.zoom.AbstractZoomAltitudeConverter
import kotlin.math.log2
import kotlin.math.pow

class ZoomAltitudeConverter(
    zoom0Altitude: Double = AbstractZoomAltitudeConverter.DEFAULT_ZOOM0_ALTITUDE,
) : AbstractZoomAltitudeConverter(zoom0Altitude) {
    companion object {
        // HERE-specific optimized zoom0 altitude to match Google Maps visible regions
        const val HERE_OPTIMIZED_ZOOM0_ALTITUDE = 162159201.449375
    }

    override fun zoomLevelToAltitude(
        zoomLevel: Double,
        latitude: Double,
        tilt: Double,
    ): Double {
        // HERE Maps uses direct zoom levels similar to Google Maps
        // For compatibility with the unified system, we simulate altitude
        val clampedZoom = zoomLevel.coerceIn(MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL)
        val altitude = zoom0Altitude / ZOOM_FACTOR.pow(clampedZoom)
        return altitude.coerceIn(MIN_ALTITUDE, MAX_ALTITUDE)
    }

    override fun altitudeToZoomLevel(
        altitude: Double,
        latitude: Double,
        tilt: Double,
    ): Double {
        // HERE Maps uses direct zoom levels similar to Google Maps
        // For compatibility with the unified system, we simulate zoom from altitude
        val clampedAltitude = altitude.coerceIn(MIN_ALTITUDE, MAX_ALTITUDE)
        val zoomLevel = log2(zoom0Altitude / clampedAltitude)
        return zoomLevel.coerceIn(MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL)
    }
}
