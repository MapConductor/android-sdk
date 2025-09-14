package com.mapconductor.core.polyline

import androidx.compose.runtime.compositionLocalOf
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.map.MapOverlay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

val LocalPolylineCollector =
    compositionLocalOf<MutableStateFlow<MutableMap<String, PolylineState>>> {
        error("Polyline must be under the <MapView />")
    }

class PolylineOverlay(
    override val flow: StateFlow<MutableMap<String, PolylineState>>,
) : MapOverlay<PolylineState> {
    override suspend fun render(
        data: MutableMap<String, PolylineState>,
        controller: MapViewController,
    ) {
        (controller as? PolylineCapable)?.compositionPolylines(data.values.toList())
    }
}
