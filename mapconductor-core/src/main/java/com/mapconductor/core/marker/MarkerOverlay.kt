package com.mapconductor.core.marker

import androidx.compose.runtime.compositionLocalOf
import com.mapconductor.core.ChildCollector
import com.mapconductor.core.controller.MapViewControllerInterface
import com.mapconductor.core.map.MapOverlayInterface
import kotlinx.coroutines.flow.StateFlow

val LocalMarkerCollector =
    compositionLocalOf<ChildCollector<MarkerState>> {
        error("Marker must be under the <MapView />")
    }

class MarkerOverlay(
    override val flow: StateFlow<MutableMap<String, MarkerState>>,
) : MapOverlayInterface<MarkerState> {
    override suspend fun render(
        data: MutableMap<String, MarkerState>,
        controller: MapViewControllerInterface,
    ) {
        (controller as? MarkerCapableInterface)?.let { markerController ->
            markerController.compositionMarkers(data.values.toList())
        }
    }
}
