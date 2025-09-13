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
    private val markerMapFlow = MutableStateFlow<Map<String, MarkerState>>(emptyMap())
    val markerFlow = MutableStateFlow<List<MarkerState>>(emptyList())
    val bubbleFlow = MutableStateFlow<List<InfoBubbleEntry>>(emptyList())
    val polylineFlow = MutableStateFlow<List<PolylineState>>(emptyList())
    val circleFlow = MutableStateFlow<List<CircleState>>(emptyList())
    val polygonFlow = MutableStateFlow<List<PolygonState>>(emptyList())
    val groundImageFlow = MutableStateFlow<List<GroundImageState>>(emptyList())

    // Internal method to update marker map and sync to list
    internal fun updateMarker(state: MarkerState) {
        val currentMap = markerMapFlow.value
        val newMap = currentMap + (state.id to state)
        markerMapFlow.value = newMap
        markerFlow.value = newMap.values.toList()
    }

    // Internal method to remove marker from map and sync to list
    internal fun removeMarker(id: String) {
        val currentMap = markerMapFlow.value
        val newMap = currentMap - id
        markerMapFlow.value = newMap
        markerFlow.value = newMap.values.toList()
    }

    // Internal method to bulk update markers for better performance
    internal fun updateMarkers(states: List<MarkerState>) {
        val currentMap = markerMapFlow.value
        val newMap = currentMap + states.associateBy { it.id }
        markerMapFlow.value = newMap
        markerFlow.value = newMap.values.toList()
    }

    // Internal method to clear all markers
    internal fun clearMarkers() {
        markerMapFlow.value = emptyMap()
        markerFlow.value = emptyList()
    }

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
