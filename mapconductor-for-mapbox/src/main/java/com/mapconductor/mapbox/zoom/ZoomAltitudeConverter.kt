package com.mapconductor.mapbox.zoom

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
         * GoogleZoom ≈ MapboxSDK.zoom + 1.0
         */
        const val MAPBOX_TO_GOOGLE_ZOOM_OFFSET = 1.0

        fun mapboxZoomToGoogleZoom(mapboxZoom: Double): Double =
            (mapboxZoom + MAPBOX_TO_GOOGLE_ZOOM_OFFSET).coerceIn(MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL)

        fun googleZoomToMapboxZoom(googleZoom: Double): Double =
            (googleZoom - MAPBOX_TO_GOOGLE_ZOOM_OFFSET).coerceIn(MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL)
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
        // MapConductor(Mapbox).zoom is offset from Google-like zoom by ~+1.0.
        // Convert to Google-like zoom first, then apply WebMercator scale math.
        // distance = zoom0Altitude * cos(latitude) / (2^zoom)
        // altitude = distance * cos(tilt)
        val googleZoom = mapboxZoomToGoogleZoom(zoomLevel)
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
        // googleZoom = log2(zoom0Altitude * cos(latitude) / (altitude / cos(tilt)))
        // mapboxZoom = googleZoom + 1.0 (approx)
        val clampedAltitude = altitude.coerceIn(MIN_ALTITUDE, MAX_ALTITUDE)
        val cosLat = cosLatitudeFactor(latitude)
        val cosTilt = cosTiltFactor(tilt)
        val distance = clampedAltitude / cosTilt
        val googleZoom = log2((zoom0Altitude * cosLat) / distance)
        return googleZoomToMapboxZoom(googleZoom)
    }
}
