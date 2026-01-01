package com.mapconductor.core.raster

interface RasterLayerCapable {
    suspend fun compositionRasterLayers(data: List<RasterLayerState>)

    suspend fun updateRasterLayer(state: RasterLayerState)

    fun hasRasterLayer(state: RasterLayerState): Boolean
}
