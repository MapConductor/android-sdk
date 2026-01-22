package com.mapconductor.maplibre.zoom

import com.mapconductor.core.zoom.AbstractZoomAltitudeConverter
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.log2
import kotlin.math.max
import kotlin.math.pow

class ZoomAltitudeConverter(
    zoom0Altitude: Double = DEFAULT_ZOOM0_ALTITUDE,
) : AbstractZoomAltitudeConverter(zoom0Altitude) {
    companion object {
        /**
         * Empirical offset:
         * GoogleZoom ≈ MapLibreSDK.zoom + 1.0
         */
        const val MAPLIBRE_TO_GOOGLE_ZOOM_OFFSET = 1.0

        fun maplibreZoomToGoogleZoom(maplibreZoom: Double): Double =
            (maplibreZoom + MAPLIBRE_TO_GOOGLE_ZOOM_OFFSET).coerceIn(MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL)

        fun googleZoomToMaplibreZoom(googleZoom: Double): Double =
            (googleZoom - MAPLIBRE_TO_GOOGLE_ZOOM_OFFSET).coerceIn(MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL)
    }

    private fun cosLatitudeFactor(latitudeDeg: Double): Double {
        val clampedLat = latitudeDeg.coerceIn(-85.0, 85.0)
        val latRad = Math.toRadians(clampedLat)
        return max(MIN_COS_LAT, abs(cos(latRad)))
    }

    private fun cosTiltFactor(tiltDeg: Double): Double {
        val clampedTilt = tiltDeg.coerceIn(0.0, 90.0)
        val tiltRad = Math.toRadians(clampedTilt)
        return max(MIN_COS_TILT, cos(tiltRad))
    }

    override fun zoomLevelToAltitude(
        zoomLevel: Double,
        latitude: Double,
        tilt: Double,
    ): Double {
        val googleZoom = maplibreZoomToGoogleZoom(zoomLevel)
        val cosLat = cosLatitudeFactor(latitude)
        val cosTilt = cosTiltFactor(tilt)
        val distance = (zoom0Altitude * cosLat) / ZOOM_FACTOR.pow(googleZoom)
        val altitude = distance * cosTilt
        return altitude.coerceIn(MIN_ALTITUDE, MAX_ALTITUDE)
    }

    override fun altitudeToZoomLevel(
        altitude: Double,
        latitude: Double,
        tilt: Double,
    ): Double {
        val clampedAltitude = altitude.coerceIn(MIN_ALTITUDE, MAX_ALTITUDE)
        val cosLat = cosLatitudeFactor(latitude)
        val cosTilt = cosTiltFactor(tilt)
        val distance = clampedAltitude / cosTilt
        val googleZoom = log2((zoom0Altitude * cosLat) / distance)
        return googleZoomToMaplibreZoom(googleZoom)
    }
}
