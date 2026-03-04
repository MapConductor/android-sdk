package com.mapconductor.heatmap

import android.graphics.Color

data class HeatmapGradientStop(
    val position: Double,
    val color: Int,
)

class HeatmapGradient(
    stops: List<HeatmapGradientStop>,
) {
    val stops: List<HeatmapGradientStop> =
        stops
            .sortedBy { it.position }
            .also { sorted ->
                require(sorted.isNotEmpty()) { "HeatmapGradient requires at least one stop." }
                sorted.forEach { stop ->
                    require(stop.position in 0.0..1.0) { "HeatmapGradient stop position must be in [0, 1]." }
                }
            }

    fun colors(): IntArray = stops.map { it.color }.toIntArray()

    companion object {
        val DEFAULT =
            HeatmapGradient(
                listOf(
                    HeatmapGradientStop(
                        position = 0.2,
                        color = Color.rgb(102, 225, 0),
                    ),
                    HeatmapGradientStop(
                        position = 1.0,
                        color = Color.rgb(255, 0, 0),
                    ),
                ),
            )
    }
}

object HeatmapDefaults {
    const val DEFAULT_RADIUS_PX: Int = 20
    const val DEFAULT_OPACITY: Double = 0.7
    const val DEFAULT_MAX_ZOOM: Int = 22
}
