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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

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
            markerAddSharedFlow.debounceBatch(5.milliseconds, 100).collect { states ->
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

@OptIn(FlowPreview::class)
@Composable
fun CollectAndRenderOverlays(
    registry: MapOverlayRegistry,
    controller: MapViewController,
) {
    registry.getAll().forEach { overlay ->
        @Suppress("UNCHECKED_CAST")
        val typedOverlay = overlay as MapOverlay<Any>

        LaunchedEffect(Unit) {
            typedOverlay.flow
                .debounce(100) // Debounce updates for 100ms to prevent excessive rendering
                .collect { items ->
                    if (items.isNotEmpty()) {
                        // Process items in chunks to prevent main thread blocking
                        val chunks = items.values.chunked(50) // Process 50 items at a time

                        chunks.forEach { chunk ->
                            val chunkMap =
                                chunk
                                    .associateBy {
                                        when (it) {
                                            is MarkerState -> it.id
                                            is CircleState -> it.id
                                            is PolylineState -> it.id
                                            is PolygonState -> it.id
                                            is GroundImageState -> it.id
                                            else -> it.toString()
                                        }
                                    }.toMutableMap()

                            typedOverlay.render(chunkMap, controller)

                            // Yield to allow other coroutines and UI updates
                            yield()
                        }
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
