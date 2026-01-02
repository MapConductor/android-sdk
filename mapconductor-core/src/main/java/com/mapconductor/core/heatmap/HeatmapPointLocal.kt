package com.mapconductor.core.heatmap

import androidx.compose.runtime.compositionLocalOf

val LocalHeatmapPointCollector =
    compositionLocalOf<HeatmapPointCollector> {
        error("HeatmapPoint must be under the <HeatmapOverlay />")
    }
