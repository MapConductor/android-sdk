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
import kotlin.time.Duration.Companion.milliseconds
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

open class MapViewScope {
    val overflowScope = CoroutineScope(Dispatchers.IO)
    val markerAddSharedFlow = MutableSharedFlow<MarkerState>(1000)
    val markerFlow = MutableStateFlow<MutableMap<String, MarkerState>>(mutableMapOf())
    val bubbleFlow = MutableStateFlow<MutableMap<String, InfoBubbleEntry>>(mutableMapOf())
    val polylineFlow = MutableStateFlow<MutableMap<String, PolylineState>>(mutableMapOf())
    val circleFlow = MutableStateFlow<MutableMap<String, CircleState>>(mutableMapOf())
    val polygonFlow = MutableStateFlow<MutableMap<String, PolygonState>>(mutableMapOf())
    val groundImageFlow = MutableStateFlow<MutableMap<String, GroundImageState>>(mutableMapOf())

    init {
        CoroutineScope(Dispatchers.IO).launch {
            markerAddSharedFlow.debounceBatch(1.milliseconds, 500).collect { states ->
                val newMap = markerFlow.value.toMutableMap()
                states.forEach { state ->
                    newMap.set(state.id, state)
                }
                markerFlow.value = newMap
            }
        }
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
