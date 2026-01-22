package com.mapconductor.core.marker

import com.mapconductor.core.raster.RasterLayerState

/**
 * Callback interface for managing RasterLayer from MarkerController.
 * This is used to decouple the MarkerController from the RasterLayerController.
 */
fun interface MarkerTileRasterLayerCallback {
    /**
     * Called when the marker tile RasterLayer needs to be added, updated, or removed.
     * @param state The RasterLayerState to add/update, or null to remove
     */
    suspend fun onRasterLayerUpdate(state: RasterLayerState?)
}
