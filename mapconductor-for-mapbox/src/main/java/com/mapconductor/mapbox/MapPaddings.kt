package com.mapconductor.mapbox

import com.mapbox.maps.EdgeInsets

data class MapPaddings(
    val top: Double,
    val left: Double,
    val bottom: Double,
    val right: Double,
) {
    internal fun toEdgeInsects() = EdgeInsets(
        top,
        left,
        bottom,
        right,
    )

    companion object {
        val Zeros = MapPaddings(0.0, 0.0, 0.0, 0.0)
    }
}

fun EdgeInsets.toPaddings() = MapPaddings(top, left, bottom, right)
