package com.mapconductor.core.raster

interface RasterLayerEntityInterface<ActualLayer> {
    val layer: ActualLayer
    val state: RasterLayerState
    val fingerPrint: RasterLayerFingerPrint
}

data class RasterLayerEntity<ActualLayer>(
    override val layer: ActualLayer,
    override val state: RasterLayerState,
) : RasterLayerEntityInterface<ActualLayer> {
    override val fingerPrint: RasterLayerFingerPrint = state.fingerPrint()
}
