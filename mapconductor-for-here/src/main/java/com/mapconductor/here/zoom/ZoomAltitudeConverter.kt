package com.mapconductor.here.zoom

import com.mapconductor.core.zoom.AbstractZoomAltitudeConverter
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.log2
import kotlin.math.max
import kotlin.math.pow

class ZoomAltitudeConverter(
    zoom0Altitude: Double = AbstractZoomAltitudeConverter.DEFAULT_ZOOM0_ALTITUDE,
) : AbstractZoomAltitudeConverter(zoom0Altitude) {
    companion object {
        // HERE-specific optimized zoom0 altitude to match Google Maps visible regions
        const val HERE_OPTIMIZED_ZOOM0_ALTITUDE = 162159201.449375

        /**
         * HERE's raster tile callbacks (LOD) are offset from the camera zoom reported by the map.
         * We normalize this here so call sites don't need to embed a magic number.
         */
        const val TILE_ZOOM_LEVEL_OFFSET: Double = 0.51
    }

    fun cameraZoomToTileZoom(cameraZoom: Double): Double =
        (cameraZoom - TILE_ZOOM_LEVEL_OFFSET).coerceAtLeast(0.0)

    fun tileZoomToCameraZoom(tileZoom: Double): Double =
        (tileZoom + TILE_ZOOM_LEVEL_OFFSET).coerceAtLeast(0.0)

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
        // HERE uses direct zoom levels, but MapConductor treats `altitude` as a unified physical scale proxy.
        // distance = zoom0Altitude * cos(latitude) / (2^zoom)
        // altitude = distance * cos(tilt)
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
        // zoom = log2(zoom0Altitude * cos(latitude) / (altitude / cos(tilt)))
        val clampedAltitude = altitude.coerceIn(MIN_ALTITUDE, MAX_ALTITUDE)
        val cosLat = cosLatitudeFactor(latitude)
        val cosTilt = cosTiltFactor(tilt)
        val distance = clampedAltitude / cosTilt
        val zoomLevel = log2((zoom0Altitude * cosLat) / distance)
        return zoomLevel.coerceIn(MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL)
    }
}
