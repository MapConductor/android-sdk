package com.mapconductor.heatmap

import com.mapconductor.core.features.GeoPointInterface
import java.io.Serializable

data class HeatmapPoint(
    val position: GeoPointInterface,
    val weight: Double,
) : Serializable
