package com.mapconductor.core.polyline

import androidx.compose.runtime.compositionLocalOf
import com.mapconductor.core.ChildCollector
import com.mapconductor.core.controller.MapViewControllerInterface
import com.mapconductor.core.map.MapOverlayInterface
import kotlinx.coroutines.flow.StateFlow

val LocalPolylineCollector =
    compositionLocalOf<ChildCollector<PolylineState>> {
        error("Polyline must be under the <MapView />")
    }

class PolylineOverlay(
    override val flow: StateFlow<MutableMap<String, PolylineState>>,
) : MapOverlayInterface<PolylineState> {
    override suspend fun render(
        data: MutableMap<String, PolylineState>,
        controller: MapViewControllerInterface,
    ) {
        (controller as? PolylineCapableInterface)?.compositionPolylines(data.values.toList())
    }
}
