package com.mapconductor.core.raster

import com.mapconductor.core.map.MapCameraPosition
import kotlinx.coroutines.CoroutineScope

interface RasterLayerOverlayRendererInterface<ActualLayer> {
    abstract val coroutine: CoroutineScope

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
