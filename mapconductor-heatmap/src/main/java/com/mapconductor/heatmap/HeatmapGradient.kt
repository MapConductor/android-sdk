package com.mapconductor.heatmap

import kotlin.math.roundToInt
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

    fun startPoints(): FloatArray = stops.map { it.position.toFloat() }.toFloatArray()

    fun colorAt(position: Double): Int {
        val clamped = position.coerceIn(0.0, 1.0)
        if (stops.size == 1) return stops.first().color
        val lower = stops.lastOrNull { it.position <= clamped } ?: stops.first()
        val upper = stops.firstOrNull { it.position >= clamped } ?: stops.last()
        if (lower.position == upper.position) return lower.color
        val ratio = (clamped - lower.position) / (upper.position - lower.position)
        return lerpColor(lower.color, upper.color, ratio)
    }

    private fun lerpColor(
        start: Int,
        end: Int,
        ratio: Double,
    ): Int {
        val clamped = ratio.coerceIn(0.0, 1.0)
        val a = (Color.alpha(start) + (Color.alpha(end) - Color.alpha(start)) * clamped).roundToInt()
        val r = (Color.red(start) + (Color.red(end) - Color.red(start)) * clamped).roundToInt()
        val g = (Color.green(start) + (Color.green(end) - Color.green(start)) * clamped).roundToInt()
        val b = (Color.blue(start) + (Color.blue(end) - Color.blue(start)) * clamped).roundToInt()
        return Color.argb(a, r, g, b)
    }

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
