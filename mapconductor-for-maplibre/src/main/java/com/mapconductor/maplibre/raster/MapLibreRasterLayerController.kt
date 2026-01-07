package com.mapconductor.maplibre.raster

import com.mapconductor.core.raster.RasterLayerController
import com.mapconductor.core.raster.RasterLayerEntity
import com.mapconductor.core.raster.RasterLayerManager
import com.mapconductor.core.raster.RasterLayerManagerInterface
import com.mapconductor.core.raster.RasterLayerOverlayRendererInterface
import com.mapconductor.core.raster.RasterLayerState

class MapLibreRasterLayerController(
    rasterLayerManager: RasterLayerManagerInterface<MapLibreRasterLayerHandle> = RasterLayerManager(),
    renderer: MapLibreRasterLayerOverlayRenderer,
) : RasterLayerController<MapLibreRasterLayerHandle>(rasterLayerManager, renderer) {
    suspend fun reapplyStyle() {
        val states = rasterLayerManager.allEntities().map { it.state }
        if (states.isEmpty()) return
        val addParams =
            states.map { state ->
                object : RasterLayerOverlayRendererInterface.AddParamsInterface {
                    override val state: RasterLayerState = state
                }
            }
        val layers = renderer.onAdd(addParams)
        layers.forEachIndexed { index, layer ->
            layer?.let {
                rasterLayerManager.registerEntity(
                    RasterLayerEntity(
                        layer = it,
                        state = states[index],
                    ),
                )
            }
        }
        renderer.onPostProcess()
    }
}
