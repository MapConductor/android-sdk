package com.mapconductor.core.raster

import androidx.compose.runtime.compositionLocalOf
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.map.MapOverlay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

val LocalRasterLayerCollector =
    compositionLocalOf<MutableStateFlow<MutableMap<String, RasterLayerState>>> {
        error("RasterLayer must be under the <MapView />")
    }

class RasterLayerOverlay(
    override val flow: StateFlow<MutableMap<String, RasterLayerState>>,
) : MapOverlay<RasterLayerState> {
    override suspend fun render(
        data: MutableMap<String, RasterLayerState>,
        controller: MapViewController,
    ) {
        (controller as? RasterLayerCapable)?.compositionRasterLayers(data.values.toList())
    }
}
