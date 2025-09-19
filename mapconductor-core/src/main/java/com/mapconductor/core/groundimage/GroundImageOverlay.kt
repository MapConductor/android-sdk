package com.mapconductor.core.groundimage

import androidx.compose.runtime.compositionLocalOf
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.map.MapOverlay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

val LocalGroundImageCollector =
    compositionLocalOf<MutableStateFlow<MutableMap<String, GroundImageState>>> {
        error("GroundImage must be under the <MapView />")
    }

class GroundImageOverlay(
    override val flow: StateFlow<MutableMap<String, GroundImageState>>,
) : MapOverlay<GroundImageState> {
    override suspend fun render(
        data: MutableMap<String, GroundImageState>,
        controller: MapViewController,
    ) {
        (controller as? GroundImageCapable)?.compositionGroundImages(data.values.toList())
    }
}
