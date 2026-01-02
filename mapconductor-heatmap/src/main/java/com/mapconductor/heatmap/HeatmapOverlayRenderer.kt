package com.mapconductor.heatmap

import com.mapconductor.core.marker.MarkerOverlayRenderer

interface HeatmapOverlayRenderer : MarkerOverlayRenderer<Unit> {
    fun updateHeatmap(points: List<HeatmapPoint>)
}
