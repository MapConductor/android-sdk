package com.mapconductor.core.polygon

import com.mapconductor.core.controller.MapViewControllerAlias
import com.mapconductor.core.map.MapOverlay
import kotlinx.coroutines.flow.StateFlow

class PolygonOverlay(
    override val flow: StateFlow<List<PolygonState>>,
) : MapOverlay<PolygonState> {
    override suspend fun render(
        data: List<PolygonState>,
        controller: MapViewControllerAlias,
    ) {
        (controller as? PolygonCapable)?.compositionPolygons(data)
    }
}
