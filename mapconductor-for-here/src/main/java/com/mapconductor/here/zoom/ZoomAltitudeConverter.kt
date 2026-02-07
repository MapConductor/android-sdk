package com.mapconductor.here.zoom

import com.mapconductor.core.zoom.AbstractZoomAltitudeConverter
import kotlin.math.cos
import kotlin.math.log2
import kotlin.math.max
import kotlin.math.pow

class ZoomAltitudeConverter(
    zoom0Altitude: Double = DEFAULT_ZOOM0_ALTITUDE,
) : AbstractZoomAltitudeConverter(zoom0Altitude) {
    companion object {
        /**
         * Empirical offset between HERE zoom and "Google-like" zoom near the equator.
         *
         * GoogleZoom ≈ HereZoom + [HERE_ZOOM_TO_GOOGLE_ZOOM_AT_EQUATOR]  (latitude ~= 0)
         */
        const val HERE_ZOOM_TO_GOOGLE_ZOOM_AT_EQUATOR = 0.00

        private fun cosLatitudeFactor(latitudeDeg: Double): Double {
            val latRad = Math.toRadians(latitudeDeg)
            return max(MIN_COS_LAT, cos(latRad))
        }

        /**
         * Latitude-dependent correction to better match WebMercator scale behavior.
         *
         * This term is 0 at the equator and decreases (negative) as latitude increases.
         */
        private fun latitudeZoomCorrection(latitudeDeg: Double): Double = log2(cosLatitudeFactor(latitudeDeg))

        fun hereZoomToGoogleZoom(
            hereZoom: Double,
            latitude: Double,
        ): Double {
            val googleZoom = hereZoom + HERE_ZOOM_TO_GOOGLE_ZOOM_AT_EQUATOR + latitudeZoomCorrection(latitude)
            return googleZoom.coerceIn(MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL)
        }

        fun googleZoomToHereZoom(
            googleZoom: Double,
            latitude: Double,
        ): Double {
            val hereZoom = googleZoom - HERE_ZOOM_TO_GOOGLE_ZOOM_AT_EQUATOR - latitudeZoomCorrection(latitude)
            return hereZoom.coerceIn(MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL)
        }
    }

    override fun zoomLevelToAltitude(
        zoomLevel: Double,
        latitude: Double,
        tilt: Double,
    ): Double {
        val clampedZoom = zoomLevel.coerceIn(MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL)
        val cosLat = cosLatitudeFactor(latitude)
        val tiltRad = Math.toRadians(tilt)
        val cosTilt = max(MIN_COS_TILT, cos(tiltRad))
        val distance = (zoom0Altitude * cosLat) / ZOOM_FACTOR.pow(clampedZoom)
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
        val tiltRad = Math.toRadians(tilt)
        val cosTilt = max(MIN_COS_TILT, cos(tiltRad))
        val distance = clampedAltitude / cosTilt
        val zoomLevel = log2((zoom0Altitude * cosLat) / distance)
        return zoomLevel.coerceIn(MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL)
    }
}
