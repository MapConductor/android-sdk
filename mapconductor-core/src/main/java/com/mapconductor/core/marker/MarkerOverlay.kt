package com.mapconductor.core.marker

import androidx.compose.runtime.compositionLocalOf
import com.mapconductor.core.controller.MapViewControllerAlias
import com.mapconductor.core.map.MapOverlay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

val LocalMarkerCollector =
    compositionLocalOf<MutableStateFlow<List<MarkerState>>> {
        error("Marker must be under the <MapView />")
    }

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
