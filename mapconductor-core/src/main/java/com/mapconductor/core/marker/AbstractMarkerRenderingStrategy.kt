package com.mapconductor.core.marker

import com.mapconductor.core.features.GeoRectBounds
import kotlinx.coroutines.sync.Semaphore

abstract class AbstractMarkerRenderingStrategy<ActualMarker>(
    protected val semaphore: Semaphore,
) : MarkerRenderingStrategy<ActualMarker> {
    override suspend fun onAdd(
        data: List<MarkerState>,
        viewport: GeoRectBounds,
        markerManager: MarkerManager<ActualMarker>,
        renderer: MarkerOverlayRenderer<ActualMarker>,
    ): Boolean {
        // Do nothing here
        return false
    }

    override suspend fun onUpdate(
        state: MarkerState,
        viewport: GeoRectBounds,
        markerManager: MarkerManager<ActualMarker>,
        renderer: MarkerOverlayRenderer<ActualMarker>,
    ): Boolean {
        // Do nothing here
        return false
    }
}
