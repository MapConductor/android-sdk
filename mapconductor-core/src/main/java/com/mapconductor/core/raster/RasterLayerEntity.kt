package com.mapconductor.core.raster

interface RasterLayerEntity<ActualLayer> {
    val layer: ActualLayer
    val state: RasterLayerState
    val fingerPrint: RasterLayerFingerPrint
}

data class RasterLayerEntityImpl<ActualLayer>(
    override val layer: ActualLayer,
    override val state: RasterLayerState,
) : RasterLayerEntity<ActualLayer> {
    override val fingerPrint: RasterLayerFingerPrint = state.fingerPrint()
}
