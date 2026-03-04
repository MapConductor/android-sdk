package com.mapconductor.core.raster

import androidx.compose.runtime.compositionLocalOf
import com.mapconductor.core.ChildCollector
import com.mapconductor.core.controller.MapViewControllerInterface
import com.mapconductor.core.map.MapOverlayInterface
import kotlinx.coroutines.flow.StateFlow

val LocalRasterLayerCollector =
    compositionLocalOf<ChildCollector<RasterLayerState>> {
        error("RasterLayer must be under the <MapView />")
    }

class RasterLayerOverlay(
    override val flow: StateFlow<MutableMap<String, RasterLayerState>>,
) : MapOverlayInterface<RasterLayerState> {
    override suspend fun render(
        data: MutableMap<String, RasterLayerState>,
        controller: MapViewControllerInterface,
    ) {
        (controller as? RasterLayerCapableInterface)?.compositionRasterLayers(data.values.toList())
    }
}
