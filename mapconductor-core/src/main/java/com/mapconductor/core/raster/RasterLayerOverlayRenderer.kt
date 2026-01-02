package com.mapconductor.core.raster

import com.mapconductor.core.map.MapCameraPositionImpl

interface RasterLayerOverlayRenderer<ActualLayer> {
    interface AddParams {
        val state: RasterLayerState
    }

    interface ChangeParams<ActualLayer> {
        val current: RasterLayerEntity<ActualLayer>
        val prev: RasterLayerEntity<ActualLayer>
    }

    suspend fun onAdd(data: List<AddParams>): List<ActualLayer?>

    suspend fun onChange(data: List<ChangeParams<ActualLayer>>): List<ActualLayer?>

    suspend fun onRemove(data: List<RasterLayerEntity<ActualLayer>>)

    suspend fun onCameraChanged(mapCameraPosition: MapCameraPositionImpl) {}

    suspend fun onPostProcess()
}
