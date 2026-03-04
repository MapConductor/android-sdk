package com.mapconductor.core.groundimage

import androidx.compose.runtime.compositionLocalOf
import com.mapconductor.core.ChildCollector
import com.mapconductor.core.controller.MapViewControllerInterface
import com.mapconductor.core.map.MapOverlayInterface
import kotlinx.coroutines.flow.StateFlow

val LocalGroundImageCollector =
    compositionLocalOf<ChildCollector<GroundImageState>> {
        error("GroundImage must be under the <MapView />")
    }

class GroundImageOverlay(
    override val flow: StateFlow<MutableMap<String, GroundImageState>>,
) : MapOverlayInterface<GroundImageState> {
    override suspend fun render(
        data: MutableMap<String, GroundImageState>,
        controller: MapViewControllerInterface,
    ) {
        (controller as? GroundImageCapableInterface)?.compositionGroundImages(data.values.toList())
    }
}
