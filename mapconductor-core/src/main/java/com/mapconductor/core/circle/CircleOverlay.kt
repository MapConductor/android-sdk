package com.mapconductor.core.circle

import androidx.compose.runtime.compositionLocalOf
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.map.MapOverlay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

val LocalCircleCollector =
    compositionLocalOf<MutableStateFlow<MutableMap<String, CircleState>>> {
        error("Circle must be under the <MapView />")
    }

class CircleOverlay(
    override val flow: StateFlow<MutableMap<String, CircleState>>,
) : MapOverlay<CircleState> {
    override suspend fun render(
        data: MutableMap<String, CircleState>,
        controller: MapViewController,
    ) {
        (controller as? CircleCapable)?.compositionCircles(data.values.toList())
    }
}
