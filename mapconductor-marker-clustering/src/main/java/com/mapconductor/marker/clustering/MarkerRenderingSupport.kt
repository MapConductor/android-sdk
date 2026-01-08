package com.mapconductor.marker.clustering

import com.mapconductor.core.marker.MarkerEventControllerInterface
import com.mapconductor.core.marker.MarkerOverlayRendererInterface
import com.mapconductor.core.marker.MarkerRenderingStrategyInterface
import com.mapconductor.core.marker.StrategyMarkerController
import kotlinx.coroutines.flow.StateFlow

interface MarkerRenderingSupport<ActualMarker> {
    fun createMarkerRenderer(
        strategy: MarkerRenderingStrategyInterface<ActualMarker>,
    ): MarkerOverlayRendererInterface<ActualMarker>

    fun createMarkerEventController(
        controller: StrategyMarkerController<ActualMarker>,
        renderer: MarkerOverlayRendererInterface<ActualMarker>,
    ): MarkerEventControllerInterface<ActualMarker>

    fun registerMarkerEventController(controller: MarkerEventControllerInterface<ActualMarker>)

    val mapLoadedState: StateFlow<Boolean>?
        get() = null

    fun onMarkerRenderingReady() {}
}
