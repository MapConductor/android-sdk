package com.mapconductor.heatmap

import com.mapconductor.core.marker.MarkerOverlayRendererInterface

interface HeatmapOverlayRendererInterface : MarkerOverlayRendererInterface<Unit> {
    fun updateHeatmap(points: List<HeatmapPoint>)
}
