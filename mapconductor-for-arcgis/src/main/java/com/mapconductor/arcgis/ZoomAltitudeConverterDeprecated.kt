package com.mapconductor.arcgis

import com.mapconductor.core.zoom.AbstractZoomAltitudeConverter
import kotlin.math.cos
import kotlin.math.log2
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.tan

class ZoomAltitudeConverterDeprecated(
    zoom0Altitude: Double = DEFAULT_ZOOM0_ALTITUDE,
) : AbstractZoomAltitudeConverter(zoom0Altitude) {
    companion object {
        const val WEB_MERCATOR_INITIAL_MPP_256 = 156_543.033_928

        fun verticalFovFromHorizontal(
            horizontalFovDeg: Double,
            aspectRatio: Double,
        ): Double {
            val hRad = Math.toRadians(horizontalFovDeg)
            val vRad = 2.0 * kotlin.math.atan(kotlin.math.tan(hRad / 2.0) / aspectRatio)
            return Math.toDegrees(vRad)
        }

        fun computeZoom0DistanceForView(
            viewportHeightPx: Int,
            verticalFovDeg: Double,
            initialMetersPerPixel: Double = WEB_MERCATOR_INITIAL_MPP_256,
        ): Double {
            val vh = viewportHeightPx.coerceAtLeast(1)
            val vRad = Math.toRadians(verticalFovDeg)
            return (initialMetersPerPixel * vh) / (2.0 * tan(vRad / 2.0))
        }
    }

    private fun cosLatitudeFactor(latitudeDeg: Double): Double {
        val latRad = Math.toRadians(latitudeDeg)
        return max(MIN_COS_LAT, cos(latRad))
    }

    private fun cosTiltFactor(tiltDeg: Double): Double {
        val tiltRad = Math.toRadians(tiltDeg)
        return max(MIN_COS_TILT, cos(tiltRad))
    }

    override fun zoomLevelToAltitude(
        zoomLevel: Double,
        latitude: Double,
        tilt: Double,
    ): Double {
        val clampedZoom = zoomLevel.coerceIn(MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL)
        val cosLat = cosLatitudeFactor(latitude)
        val cosTilt = cosTiltFactor(tilt)
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
        val cosTilt = cosTiltFactor(tilt)
        val distance = clampedAltitude / cosTilt
        val zoomLevel = log2((zoom0Altitude * cosLat) / distance)
        return zoomLevel.coerceIn(MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL)
    }

    // Legacy methods for backward compatibility
    fun zoomLevelToAltitude(zoomLevel: Double): Double {
        val clampedZoom = zoomLevel.coerceIn(MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL)
        val altitude = zoom0Altitude / ZOOM_FACTOR.pow(clampedZoom)
        return altitude.coerceIn(MIN_ALTITUDE, MAX_ALTITUDE)
    }

    fun zoomLevelToAltitude(
        zoomLevel: Double,
        latitude: Double,
    ): Double {
        val clampedZoom = zoomLevel.coerceIn(MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL)
        val cosLat = cosLatitudeFactor(latitude)
        val altitude = (zoom0Altitude * cosLat) / ZOOM_FACTOR.pow(clampedZoom)
        return altitude.coerceIn(MIN_ALTITUDE, MAX_ALTITUDE)
    }

    fun zoomLevelToDistance(
        zoomLevel: Double,
        latitude: Double,
    ): Double {
        val clampedZoom = zoomLevel.coerceIn(MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL)
        val cosLat = cosLatitudeFactor(latitude)
        val distance = (zoom0Altitude * cosLat) / ZOOM_FACTOR.pow(clampedZoom)
        return distance.coerceIn(MIN_ALTITUDE, MAX_ALTITUDE)
    }

    fun altitudeToZoomLevel(altitude: Double): Double {
        val clampedAltitude = altitude.coerceIn(MIN_ALTITUDE, MAX_ALTITUDE)
        val zoomLevel = log2(zoom0Altitude / clampedAltitude)
        return zoomLevel.coerceIn(MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL)
    }

    fun altitudeToZoomLevel(
        altitude: Double,
        latitude: Double,
    ): Double {
        val clampedAltitude = altitude.coerceIn(MIN_ALTITUDE, MAX_ALTITUDE)
        val cosLat = cosLatitudeFactor(latitude)
        val zoomLevel = log2((zoom0Altitude * cosLat) / clampedAltitude)
        return zoomLevel.coerceIn(MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL)
    }
}
