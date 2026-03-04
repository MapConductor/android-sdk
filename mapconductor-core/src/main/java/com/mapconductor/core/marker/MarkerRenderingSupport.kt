package com.mapconductor.core.marker

import com.mapconductor.core.map.MapServiceKey
import kotlinx.coroutines.flow.StateFlow

/**
 * Map-scoped capability used by marker-clustering (and other marker-rendering plugins)
 * to create per-group renderers/controllers without requiring the map controller itself
 * to implement plugin interfaces.
 */
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

/**
 * Registry key used to look up [MarkerRenderingSupport] from [com.mapconductor.core.map.LocalMapServiceRegistry].
 */
object MarkerRenderingSupportKey : MapServiceKey<MarkerRenderingSupport<*>>
