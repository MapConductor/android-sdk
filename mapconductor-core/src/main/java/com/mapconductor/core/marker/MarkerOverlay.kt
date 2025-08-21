package com.mapconductor.core.marker

import com.mapconductor.core.controller.MapViewControllerAlias
import com.mapconductor.core.map.MapOverlay
import kotlinx.coroutines.flow.StateFlow

class MarkerOverlay(
    override val flow: StateFlow<List<MarkerState>>,
) : MapOverlay<MarkerState> {
    override suspend fun render(
        data: List<MarkerState>,
        controller: MapViewControllerAlias,
    ) {
        (controller as? MarkerCapable<*>)?.compositionMarkers(data)
    }
}
