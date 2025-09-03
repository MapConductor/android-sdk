package com.mapconductor.core.polyline

import androidx.compose.runtime.compositionLocalOf
import com.mapconductor.core.controller.MapViewControllerAlias
import com.mapconductor.core.map.MapOverlay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

val LocalPolylineCollector =
    compositionLocalOf<MutableStateFlow<List<PolylineState>>> {
        error("Polyline must be under the <MapView />")
    }

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
