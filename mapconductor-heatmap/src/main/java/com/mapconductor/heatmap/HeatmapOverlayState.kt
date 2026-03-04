package com.mapconductor.heatmap

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * State holder for HeatmapOverlay.
 * Allows dynamic modification of heatmap properties with automatic re-rendering.
 *
 * Example:
 * ```kotlin
 * val heatmapState = remember { HeatmapOverlayState() }
 *
 * // Modify properties dynamically
 * heatmapState.radiusPx = 30
 * heatmapState.opacity = 0.5
 *
 * HeatmapOverlay(state = heatmapState) {
 *     points.forEach { pointState ->
 *         HeatmapPoint(pointState)
 *     }
 * }
 * ```
 */
class HeatmapOverlayState(
    radiusPx: Int = HeatmapDefaults.DEFAULT_RADIUS_PX,
    opacity: Double = HeatmapDefaults.DEFAULT_OPACITY,
    gradient: HeatmapGradient = HeatmapGradient.DEFAULT,
    maxIntensity: Double? = null,
    weightProvider: (HeatmapPointState) -> Double = { state -> state.weight },
) {
    /**
     * The radius of each point in pixels.
     * Changing this value will trigger re-rendering of the heatmap.
     */
    var radiusPx by mutableStateOf(radiusPx)

    /**
     * The opacity of the heatmap layer (0.0 to 1.0).
     * Changing this value will trigger re-rendering of the heatmap.
     */
    var opacity by mutableStateOf(opacity)

    /**
     * The color gradient used for the heatmap.
     * Changing this value will trigger re-rendering of the heatmap.
     */
    var gradient by mutableStateOf(gradient)

    /**
     * Optional maximum intensity value for normalization.
     * If null, the maximum intensity is calculated from the data.
     * Changing this value will trigger re-rendering of the heatmap.
     */
    var maxIntensity by mutableStateOf(maxIntensity)

    /**
     * Function to extract weight from HeatmapPointState.
     * Changing this value will trigger re-rendering of the heatmap.
     */
    var weightProvider by mutableStateOf(weightProvider)

    /**
     * Creates a copy of this state with optionally modified properties.
     */
    fun copy(
        radiusPx: Int = this.radiusPx,
        opacity: Double = this.opacity,
        gradient: HeatmapGradient = this.gradient,
        maxIntensity: Double? = this.maxIntensity,
        weightProvider: (HeatmapPointState) -> Double = this.weightProvider,
    ): HeatmapOverlayState =
        HeatmapOverlayState(
            radiusPx = radiusPx,
            opacity = opacity,
            gradient = gradient,
            maxIntensity = maxIntensity,
            weightProvider = weightProvider,
        )
}
