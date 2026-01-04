package com.mapconductor.core.raster

import com.mapconductor.core.map.MapCameraPosition

interface RasterLayerOverlayRendererInterface<ActualLayer> {
    interface AddParamsInterface {
        val state: RasterLayerState
    }

    interface ChangeParamsInterface<ActualLayer> {
        val current: RasterLayerEntityInterface<ActualLayer>
        val prev: RasterLayerEntityInterface<ActualLayer>
    }

    suspend fun onAdd(data: List<AddParamsInterface>): List<ActualLayer?>

    suspend fun onChange(data: List<ChangeParamsInterface<ActualLayer>>): List<ActualLayer?>

    suspend fun onRemove(data: List<RasterLayerEntityInterface<ActualLayer>>)

    suspend fun onCameraChanged(mapCameraPosition: MapCameraPosition) {}

    suspend fun onPostProcess()
}
