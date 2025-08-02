package com.mapconductor.arcgis

import kotlin.math.log2
import kotlin.math.pow

/**
 * ArcGISとGoogle Mapsの間でズームレベルと高度を変換するユーティリティクラス
 */
class ZoomAltitudeConverter(
    private val zoom0Altitude: Double = DEFAULT_ZOOM0_ALTITUDE,
) {
    companion object {
        const val DEFAULT_ZOOM0_ALTITUDE = 152_343_750.0 // バイナリテストで確定
        private const val ZOOM_FACTOR = 2.0
        private const val MIN_ZOOM_LEVEL = 0.0
        private const val MAX_ZOOM_LEVEL = 22.0
        private const val MIN_ALTITUDE = 100.0
        private const val MAX_ALTITUDE = 50_000_000.0
    }

    fun zoomLevelToAltitude(zoomLevel: Double): Double {
        val clampedZoom = zoomLevel.coerceIn(MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL)
        val altitude = zoom0Altitude / (ZOOM_FACTOR.pow(clampedZoom))
        return altitude.coerceIn(MIN_ALTITUDE, MAX_ALTITUDE)
    }

    fun altitudeToZoomLevel(altitude: Double): Double {
        val clampedAltitude = altitude.coerceIn(MIN_ALTITUDE, MAX_ALTITUDE)
        val zoomLevel = log2(zoom0Altitude / clampedAltitude)
        return zoomLevel.coerceIn(MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL)
    }
}
