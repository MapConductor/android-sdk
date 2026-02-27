package com.mapconductor.arcgis.zoom

import com.mapconductor.core.zoom.AbstractZoomAltitudeConverter
import kotlin.math.cos
import kotlin.math.log2
import kotlin.math.max
import kotlin.math.pow

class ZoomAltitudeConverter(
    zoom0Altitude: Double = ARCGIS_OPTIMIZED_ZOOM0_ALTITUDE,
) : AbstractZoomAltitudeConverter(zoom0Altitude) {
    companion object {
        // ArcGIS-specific calibrated zoom0 altitude.
        const val ARCGIS_OPTIMIZED_ZOOM0_ALTITUDE = 124000000.0
        // Reference viewport height (in dp) used for calibration.
        // Google Maps displays geographic range proportional to screen pixel count,
        // so we scale altitude based on viewport height to match that behavior.
        private const val REFERENCE_HEIGHT_DP = 577.0
    }

    private fun cosLatitudeFactor(latitudeDeg: Double): Double {
        val latRad = Math.toRadians(latitudeDeg)
        return max(MIN_COS_LAT, cos(latRad))
    }

    private fun cosTiltFactor(tiltDeg: Double): Double {
        val tiltRad = Math.toRadians(tiltDeg)
        return max(MIN_COS_TILT, cos(tiltRad))
    }

    private fun resolveZoom0Altitude(
        viewportWidthPx: Int?,
        viewportHeightPx: Int?,
    ): Double {
        val height = viewportHeightPx ?: return zoom0Altitude
        if (height <= 0) return zoom0Altitude

        // Google Maps displays geographic range proportional to screen size.
        // At the same zoom level, a smaller screen shows a smaller geographic area.
        // In ArcGIS SceneView with fixed vertical FOV, altitude determines visible range.
        // Scale altitude based on viewport height to match Google Maps behavior:
        // - Smaller screen (less height) → lower altitude → smaller visible area
        // - Larger screen (more height) → higher altitude → larger visible area
        //
        // Use a fractional exponent (0.42) to dampen the scaling effect, as ArcGIS's 3D
        // perspective projection doesn't scale linearly with viewport size like Google Maps' 2D tiles.
        // Empirically calibrated across multiple device sizes.
        val heightScale = (height.toDouble() / REFERENCE_HEIGHT_DP).pow(0.42)
        return zoom0Altitude * heightScale
    }

    private fun zoomLevelToAltitudeInternal(
        zoomLevel: Double,
        latitude: Double,
        tilt: Double,
        viewportWidthPx: Int?,
        viewportHeightPx: Int?,
    ): Double {
        val effectiveZoom0Altitude = resolveZoom0Altitude(viewportWidthPx, viewportHeightPx)
        val clampedZoom = zoomLevel.coerceIn(MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL)
        val cosLat = cosLatitudeFactor(latitude)
        val cosTilt = cosTiltFactor(tilt)
        val distance = (effectiveZoom0Altitude * cosLat) / ZOOM_FACTOR.pow(clampedZoom)
        val altitude = distance * cosTilt
        return altitude.coerceIn(MIN_ALTITUDE, MAX_ALTITUDE)
    }

    private fun altitudeToZoomLevelInternal(
        altitude: Double,
        latitude: Double,
        tilt: Double,
        viewportWidthPx: Int?,
        viewportHeightPx: Int?,
    ): Double {
        val effectiveZoom0Altitude = resolveZoom0Altitude(viewportWidthPx, viewportHeightPx)
        val clampedAltitude = altitude.coerceIn(MIN_ALTITUDE, MAX_ALTITUDE)
        val cosLat = cosLatitudeFactor(latitude)
        val cosTilt = cosTiltFactor(tilt)
        val distance = clampedAltitude / cosTilt
        val zoomLevel = log2((effectiveZoom0Altitude * cosLat) / distance)
        return zoomLevel.coerceIn(MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL)
    }

    override fun zoomLevelToAltitude(
        zoomLevel: Double,
        latitude: Double,
        tilt: Double,
    ): Double = zoomLevelToAltitudeInternal(zoomLevel, latitude, tilt, null, null)

    override fun altitudeToZoomLevel(
        altitude: Double,
        latitude: Double,
        tilt: Double,
    ): Double = altitudeToZoomLevelInternal(altitude, latitude, tilt, null, null)

    fun zoomLevelToAltitude(
        zoomLevel: Double,
        latitude: Double,
        tilt: Double,
        viewportWidthPx: Int,
        viewportHeightPx: Int,
    ): Double = zoomLevelToAltitudeInternal(zoomLevel, latitude, tilt, viewportWidthPx, viewportHeightPx)

    fun altitudeToZoomLevel(
        altitude: Double,
        latitude: Double,
        tilt: Double,
        viewportWidthPx: Int,
        viewportHeightPx: Int,
    ): Double = altitudeToZoomLevelInternal(altitude, latitude, tilt, viewportWidthPx, viewportHeightPx)

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

    fun zoomLevelToDistance(
        zoomLevel: Double,
        latitude: Double,
        viewportWidthPx: Int,
        viewportHeightPx: Int,
    ): Double {
        val effectiveZoom0Altitude = resolveZoom0Altitude(viewportWidthPx, viewportHeightPx)
        val clampedZoom = zoomLevel.coerceIn(MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL)
        val cosLat = cosLatitudeFactor(latitude)
        val distance = (effectiveZoom0Altitude * cosLat) / ZOOM_FACTOR.pow(clampedZoom)
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
