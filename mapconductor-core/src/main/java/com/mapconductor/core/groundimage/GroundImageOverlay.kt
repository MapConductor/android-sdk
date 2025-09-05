package com.mapconductor.core.groundimage

import androidx.compose.runtime.compositionLocalOf
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.map.MapOverlay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

val LocalGroundImageCollector =
    compositionLocalOf<MutableStateFlow<List<GroundImageState>>> {
        error("GroundImage must be under the <MapView />")
    }

class GroundImageOverlay(
    override val flow: StateFlow<List<GroundImageState>>,
) : MapOverlay<GroundImageState> {
    override suspend fun render(
        data: List<GroundImageState>,
        controller: MapViewController,
    ) {
        (controller as? GroundImageCapable)?.compositionGroundImages(data)
    }
}
