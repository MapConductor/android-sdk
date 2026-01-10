package com.mapconductor.core.raster

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import com.mapconductor.core.MapViewScope

@Composable
fun MapViewScope.RasterLayer(state: RasterLayerState) {
    val collector = LocalRasterLayerCollector.current
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
fun MapViewScope.RasterLayer(
    source: RasterLayerSource,
    opacity: Float = 1.0f,
    visible: Boolean = true,
    userAgent: String? = null,
    id: String? = null,
    extraHeaders: Map<String, String>? = null,
) {
    val state =
        RasterLayerState(
            source = source,
            opacity = opacity,
            visible = visible,
            userAgent = userAgent,
            id = id,
            extraHeaders = extraHeaders,
        )
    RasterLayer(state)
}
