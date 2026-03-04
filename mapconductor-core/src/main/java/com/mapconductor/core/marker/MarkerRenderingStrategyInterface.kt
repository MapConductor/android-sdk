package com.mapconductor.core.marker

import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.map.MapCameraPosition

/**
 * Strategy interface for handling marker rendering during camera changes.
 * Different map providers may have different optimal strategies for marker management.
 */
interface MarkerRenderingStrategyInterface<ActualMarker> {
    val markerManager: MarkerManager<ActualMarker>

    fun clear()

    suspend fun onAdd(
        data: List<MarkerState>,
        viewport: GeoRectBounds,
        renderer: MarkerOverlayRendererInterface<ActualMarker>,
    ): Boolean

    suspend fun onUpdate(
        state: MarkerState,
        viewport: GeoRectBounds,
        renderer: MarkerOverlayRendererInterface<ActualMarker>,
    ): Boolean

    /**
     * Handle camera position changes and update marker rendering accordingly.
     *
     * @param cameraPosition The new camera position
     * @param renderer The marker overlay renderer
     */
    suspend fun onCameraChanged(
        cameraPosition: MapCameraPosition,
        renderer: MarkerOverlayRendererInterface<ActualMarker>,
    )
}
