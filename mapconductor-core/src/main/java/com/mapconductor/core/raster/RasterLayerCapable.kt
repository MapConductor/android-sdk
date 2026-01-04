package com.mapconductor.core.raster

interface RasterLayerCapableInterface {
    suspend fun compositionRasterLayers(data: List<RasterLayerState>)

    suspend fun updateRasterLayer(state: RasterLayerState)

    fun hasRasterLayer(state: RasterLayerState): Boolean
}
