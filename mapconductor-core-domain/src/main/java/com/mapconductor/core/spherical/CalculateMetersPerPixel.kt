package com.mapconductor.core.spherical

import com.mapconductor.core.projection.Earth
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow

fun calculateMetersPerPixel(
    latitude: Double,
    zoom: Double,
    tileSize: Double = 256.0,
): Double {
    // Web Mercator projection formula for meters per pixel
    // Based on the standard: 1 pixel = 78271.484 meters at zoom 0 at the equator

    // At zoom level 0, the entire world (earthCircumferenceMeters meters) fits in tileSize pixels
    val metersPerPixelAtEquator = Earth.CIRCUMFERENCE_METERS / tileSize

    // Adjust for zoom level (each zoom level halves the meters per pixel)
    val metersPerPixelAtZoom = metersPerPixelAtEquator / 2.0.pow(zoom)

    // Adjust for latitude (Mercator projection stretches at higher latitudes)
    val latitudeRadians = Math.toRadians(abs(latitude))
    val latitudeAdjustment = cos(latitudeRadians)

    return (metersPerPixelAtZoom * latitudeAdjustment)
}

fun meterToPixel(
    meter: Double,
    latitude: Double,
    zoom: Double,
    tileSize: Double = 256.0, // Google Mapsはデフォルト256pxだが、Mapbox v10+はデフォルト512px
): Double {
    val earthCircumference = 2 * Math.PI * Earth.RADIUS_METERS
    val metersPerPixel = cos(Math.toRadians(latitude)) * earthCircumference / (tileSize * 2.0.pow(zoom))
    return meter / metersPerPixel
}
