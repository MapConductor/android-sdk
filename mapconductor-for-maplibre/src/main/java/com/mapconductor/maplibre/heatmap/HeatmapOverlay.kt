package com.mapconductor.maplibre.heatmap

import androidx.compose.runtime.Composable
import com.mapconductor.core.heatmap.HeatmapPointState
import com.mapconductor.heatmap.HeatmapDefaults
import com.mapconductor.heatmap.HeatmapGradient
import com.mapconductor.heatmap.HeatmapOverlay as SharedHeatmapOverlay
import com.mapconductor.heatmap.HeatmapStrategy
import com.mapconductor.maplibre.MapLibreMapViewScope

@Suppress("UNUSED_PARAMETER")
@Composable
fun MapLibreMapViewScope.HeatmapOverlay(
    radiusPx: Int = HeatmapDefaults.DEFAULT_RADIUS_PX,
    opacity: Double = HeatmapDefaults.DEFAULT_OPACITY,
    gradient: HeatmapGradient = HeatmapGradient.DEFAULT,
    maxIntensity: Double? = null,
    expandMargin: Double = HeatmapStrategy.DEFAULT_EXPAND_MARGIN,
    weightProvider: (HeatmapPointState) -> Double = { state -> state.weight },
    content: @Composable () -> Unit,
) {
    SharedHeatmapOverlay(
        radiusPx = radiusPx,
        opacity = opacity,
        gradient = gradient,
        maxIntensity = maxIntensity,
        weightProvider = weightProvider,
        content = content,
    )
}
