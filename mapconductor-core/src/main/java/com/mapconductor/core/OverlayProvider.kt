package com.mapconductor.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.mapconductor.core.circle.CircleOverlay
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.groundimage.GroundImageOverlay
import com.mapconductor.core.groundimage.GroundImageState
import com.mapconductor.core.info.InfoBubbleEntry
import com.mapconductor.core.map.MapOverlay
import com.mapconductor.core.map.MapOverlayRegistry
import com.mapconductor.core.marker.MarkerOverlay
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polygon.PolygonOverlay
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.core.polyline.PolylineOverlay
import com.mapconductor.core.polyline.PolylineState
import kotlinx.coroutines.flow.MutableStateFlow

open class MapViewScope {
    val markerFlow = MutableStateFlow<List<MarkerState>>(emptyList())
    val bubbleFlow = MutableStateFlow<List<InfoBubbleEntry>>(emptyList())
    val polylineFlow = MutableStateFlow<List<PolylineState>>(emptyList())
    val circleFlow = MutableStateFlow<List<CircleState>>(emptyList())
    val polygonFlow = MutableStateFlow<List<PolygonState>>(emptyList())
    val groundImageFlow = MutableStateFlow<List<GroundImageState>>(emptyList())

    fun buildRegistry(): MapOverlayRegistry {
        val registry = MapOverlayRegistry()
        registry.register(MarkerOverlay(markerFlow))
        registry.register(CircleOverlay(circleFlow))
        registry.register(PolylineOverlay(polylineFlow))
        registry.register(PolygonOverlay(polygonFlow))
        registry.register(GroundImageOverlay(groundImageFlow))
        return registry
    }
}

@Composable
fun CollectAndRenderOverlays(
    registry: MapOverlayRegistry,
    controller: MapViewController,
) {
    registry.getAll().forEach { overlay ->
        @Suppress("UNCHECKED_CAST")
        val typedOverlay = overlay as MapOverlay<Any>

        LaunchedEffect(Unit) {
            typedOverlay.flow.collect {
                if (it.isNotEmpty()) {
                    typedOverlay.render(it, controller)
                }
            }
        }

//        val flowState = typedOverlay.flow.collectAsState()
//
//        LaunchedEffect(flowState.value) {
//            typedOverlay.render(flowState.value.toSet().toList(), controller)
//        }
    }
}
