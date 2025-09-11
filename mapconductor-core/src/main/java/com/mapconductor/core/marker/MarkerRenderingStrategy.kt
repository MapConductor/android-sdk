package com.mapconductor.core.marker

import com.mapconductor.core.map.MapCameraPosition

/**
 * Strategy interface for handling marker rendering during camera changes.
 * Different map providers may have different optimal strategies for marker management.
 */
interface MarkerRenderingStrategy<ActualMarker> {
    /**
     * Handle camera position changes and update marker rendering accordingly.
     *
     * @param cameraPosition The new camera position
     * @param markerManager The marker manager containing all markers
     * @param renderer The marker overlay renderer
     */
    suspend fun onCameraChanged(
        cameraPosition: MapCameraPosition,
        markerManager: MarkerManager<ActualMarker>,
        renderer: MarkerOverlayRenderer<ActualMarker>,
    )
}
