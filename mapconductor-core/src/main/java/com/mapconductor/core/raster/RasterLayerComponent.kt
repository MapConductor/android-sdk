package com.mapconductor.core.raster

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import com.mapconductor.core.MapViewScope
import java.io.Serializable

@Composable
fun MapViewScope.RasterLayer(state: RasterLayerState) {
    LaunchedEffect(state.fingerPrint()) {
        val newMap = rasterLayerFlow.value.toMutableMap()
        newMap[state.id] = state
        rasterLayerFlow.value = newMap
    }

    DisposableEffect(state.id) {
        onDispose {
            rasterLayerRemoveSharedFlow.tryEmit(state.id)
        }
    }
}

@Composable
fun MapViewScope.RasterLayer(
    source: RasterSource,
    opacity: Float = 1.0f,
    visible: Boolean = true,
    id: String? = null,
    extra: Serializable? = null,
) {
    val state =
        RasterLayerState(
            source = source,
            opacity = opacity,
            visible = visible,
            id = id,
            extra = extra,
        )
    RasterLayer(state)
}
