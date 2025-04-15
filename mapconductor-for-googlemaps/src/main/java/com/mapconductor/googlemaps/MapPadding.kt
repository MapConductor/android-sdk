package com.mapconductor.googlemaps

data class MapPadding(
    val top: Double = 0.0,
    val left: Double = 0.0,
    val bottom: Double = 0.0,
    val right: Double = 0.0,
) {
    companion object {
        val Zeros = MapPadding(0.0, 0.0, 0.0, 0.0)
    }
}