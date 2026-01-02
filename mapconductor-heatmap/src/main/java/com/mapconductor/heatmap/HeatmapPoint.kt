package com.mapconductor.heatmap

import com.mapconductor.core.features.GeoPoint
import java.io.Serializable

data class HeatmapPoint(
    val position: GeoPoint,
    val weight: Double,
) : Serializable
