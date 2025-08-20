package com.mapconductor.core.groundimage

import com.mapconductor.core.controller.MapViewControllerAlias
import com.mapconductor.core.map.MapOverlay
import kotlinx.coroutines.flow.StateFlow

class GroundImageOverlay(
    override val flow: StateFlow<List<GroundImageState>>,
) : MapOverlay<GroundImageState> {
    override suspend fun render(
        data: List<GroundImageState>,
        controller: MapViewControllerAlias,
    ) {
        (controller as? GroundImageCapable<*>)?.compositionGroundImages(data)
    }
}
