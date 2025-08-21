package com.mapconductor.core.polyline

import com.mapconductor.core.controller.MapViewControllerAlias
import com.mapconductor.core.map.MapOverlay
import kotlinx.coroutines.flow.StateFlow

class PolylineOverlay(
    override val flow: StateFlow<List<PolylineState>>,
) : MapOverlay<PolylineState> {
    override suspend fun render(
        data: List<PolylineState>,
        controller: MapViewControllerAlias,
    ) {
        (controller as? PolylineCapable)?.compositionPolylines(data)
    }
}
