package com.mapconductor.core.polygon

import androidx.compose.runtime.compositionLocalOf
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.map.MapOverlay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

val LocalPolygonCollector =
    compositionLocalOf<MutableStateFlow<MutableMap<String, PolygonState>>> {
        error("Polygon must be under the <MapView />")
    }

class PolygonOverlay(
    override val flow: StateFlow<MutableMap<String, PolygonState>>,
) : MapOverlay<PolygonState> {
    override suspend fun render(
        data: MutableMap<String, PolygonState>,
        controller: MapViewController,
    ) {
        (controller as? PolygonCapable)?.compositionPolygons(data.values.toList())
    }
}
