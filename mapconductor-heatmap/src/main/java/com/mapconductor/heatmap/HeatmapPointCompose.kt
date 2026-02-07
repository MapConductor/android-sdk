package com.mapconductor.heatmap

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import com.mapconductor.core.ChildCollector
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.features.GeoPointInterface
import java.io.Serializable

val LocalHeatmapPointCollector =
    compositionLocalOf<ChildCollector<HeatmapPointState>> {
        error("HeatmapPoint must be under the <HeatmapOverlay />")
    }

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
    position: GeoPointInterface,
    weight: Double = 1.0,
    id: String? = null,
    extra: Serializable? = null,
) {
    val state =
        HeatmapPointState(
            position = position,
            weight = weight,
            id = id,
            extra = extra,
        )
    HeatmapPoint(state)
}

@Composable
fun MapViewScope.HeatmapPoints(states: List<HeatmapPointState>) {
    val collector = LocalHeatmapPointCollector.current

    LaunchedEffect(states, states.size) {
        // For very large marker sets, avoid per-marker SharedFlow emits which can backpressure and
        // block rendering; instead publish the whole map in one StateFlow update.
        collector.replaceAll(states)
    }

    DisposableEffect(Unit) {
        onDispose {
            // Clear all markers on dispose in one shot.
            collector.replaceAll(emptyList())
        }
    }
}
