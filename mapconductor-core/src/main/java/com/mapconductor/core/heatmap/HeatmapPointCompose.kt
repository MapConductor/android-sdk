package com.mapconductor.core.heatmap

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.features.GeoPoint

@Composable
fun MapViewScope.HeatmapPoint(state: HeatmapPointState) {
    val collector = LocalHeatmapPointCollector.current
    LaunchedEffect(state) {
        collector.add(state)
    }

    DisposableEffect(state.id) {
        onDispose {
            collector.remove(state.id)
        }
    }
}

@Composable
fun MapViewScope.HeatmapPoint(
    position: GeoPoint,
    weight: Double = 1.0,
    id: String? = null,
) {
    val state =
        HeatmapPointState(
            id = id,
            position = position,
            weight = weight,
        )
    HeatmapPoint(state)
}
