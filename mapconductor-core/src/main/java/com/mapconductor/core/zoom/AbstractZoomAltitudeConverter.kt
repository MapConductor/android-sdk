package com.mapconductor.core.zoom

abstract class AbstractZoomAltitudeConverter(
    protected val zoom0Altitude: Double,
) {
    companion object {
        const val DEFAULT_ZOOM0_ALTITUDE = 171_319_879.0 // Calibrated to match Google Maps visible regions
        const val ZOOM_FACTOR = 2.0
        const val MIN_ZOOM_LEVEL = 0.0
        const val MAX_ZOOM_LEVEL = 22.0
        const val MIN_ALTITUDE = 100.0
        const val MAX_ALTITUDE = 50_000_000.0
        const val MIN_COS_LAT = 0.01
        const val MIN_COS_TILT = 0.05
        const val WEB_MERCATOR_INITIAL_MPP_256 = 156_543.033_928
    }

    abstract fun zoomLevelToAltitude(
        zoomLevel: Double,
        latitude: Double,
        tilt: Double,
    ): Double

    abstract fun altitudeToZoomLevel(
        altitude: Double,
        latitude: Double,
        tilt: Double,
    ): Double
}
