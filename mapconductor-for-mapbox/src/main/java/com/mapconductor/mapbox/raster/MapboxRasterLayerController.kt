package com.mapconductor.mapbox.raster

import com.mapconductor.core.raster.RasterLayerController
import com.mapconductor.core.raster.RasterLayerEntityImpl
import com.mapconductor.core.raster.RasterLayerManager
import com.mapconductor.core.raster.RasterLayerManagerImpl
import com.mapconductor.core.raster.RasterLayerOverlayRenderer
import com.mapconductor.core.raster.RasterLayerState

class MapboxRasterLayerController(
    rasterLayerManager: RasterLayerManager<MapboxRasterLayerHandle> = RasterLayerManagerImpl(),
    renderer: MapboxRasterLayerOverlayRenderer,
) : RasterLayerController<MapboxRasterLayerHandle>(rasterLayerManager, renderer) {
    suspend fun reapplyStyle() {
        val states = rasterLayerManager.allEntities().map { it.state }
        if (states.isEmpty()) return
        val addParams =
            states.map { state ->
                object : RasterLayerOverlayRenderer.AddParams {
                    override val state: RasterLayerState = state
                }
            }
        val layers = renderer.onAdd(addParams)
        layers.forEachIndexed { index, layer ->
            layer?.let {
                rasterLayerManager.registerEntity(
                    RasterLayerEntityImpl(
                        layer = it,
                        state = states[index],
                    ),
                )
            }
        }
        renderer.onPostProcess()
    }
}
