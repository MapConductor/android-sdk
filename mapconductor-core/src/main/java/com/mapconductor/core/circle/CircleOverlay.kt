package com.mapconductor.core.circle

import androidx.compose.runtime.compositionLocalOf
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.map.MapOverlay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

val LocalCircleCollector =
    compositionLocalOf<MutableStateFlow<List<CircleState>>> {
        error("Circle must be under the <MapView />")
    }

class CircleOverlay(
    override val flow: StateFlow<List<CircleState>>,
) : MapOverlay<CircleState> {
    override suspend fun render(
        data: List<CircleState>,
        controller: MapViewController,
    ) {
        (controller as? CircleCapable)?.compositionCircles(data)
    }
}
