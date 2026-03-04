package com.mapconductor.core.marker

import com.mapconductor.core.features.GeoRectBounds
import kotlinx.coroutines.sync.Semaphore

abstract class AbstractMarkerRenderingStrategy<ActualMarker>(
    protected val semaphore: Semaphore,
) : MarkerRenderingStrategyInterface<ActualMarker> {
    protected val defaultMarkerIcon = DefaultMarkerIcon().toBitmapIcon()

    override fun clear() {
        markerManager.clear()
    }

    /**
     * MarkerManager instance provided by dependency injection.
     * Each strategy can provide its own optimized MarkerManager implementation.
     */
    abstract override val markerManager: MarkerManager<ActualMarker>

    override suspend fun onAdd(
        data: List<MarkerState>,
        viewport: GeoRectBounds,
        renderer: MarkerOverlayRendererInterface<ActualMarker>,
    ): Boolean {
        // Do nothing here
        return false
    }

    override suspend fun onUpdate(
        state: MarkerState,
        viewport: GeoRectBounds,
        renderer: MarkerOverlayRendererInterface<ActualMarker>,
    ): Boolean {
        // Do nothing here
        return false
    }
}
