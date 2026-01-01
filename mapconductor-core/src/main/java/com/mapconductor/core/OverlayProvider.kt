package com.mapconductor.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.mapconductor.core.circle.CircleOverlay
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.groundimage.GroundImageOverlay
import com.mapconductor.core.groundimage.GroundImageState
import com.mapconductor.core.info.InfoBubbleEntry
import com.mapconductor.core.map.MapOverlay
import com.mapconductor.core.map.MapOverlayRegistry
import com.mapconductor.core.marker.MarkerOverlay
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
import kotlinx.coroutines.launch

open class MapViewScope {
    val markerCollector =
        com.mapconductor.core.marker
            .MarkerCollector()
    val bubbleFlow = MutableStateFlow<MutableMap<String, InfoBubbleEntry>>(mutableMapOf())
    val polylineFlow = MutableStateFlow<MutableMap<String, PolylineState>>(mutableMapOf())
    val polylineRemoveSharedFlow = MutableSharedFlow<String>(1000)
    val circleFlow = MutableStateFlow<MutableMap<String, CircleState>>(mutableMapOf())
    val circleRemoveSharedFlow = MutableSharedFlow<String>(1000)
    val polygonFlow = MutableStateFlow<MutableMap<String, PolygonState>>(mutableMapOf())
    val polygonRemoveSharedFlow = MutableSharedFlow<String>(1000)
    val groundImageFlow = MutableStateFlow<MutableMap<String, GroundImageState>>(mutableMapOf())
    val groundImageRemoveSharedFlow = MutableSharedFlow<String>(1000)

    init {
        CoroutineScope(Dispatchers.IO).launch {
            polylineRemoveSharedFlow.debounceBatch(5.milliseconds, 300).collect { ids ->
                val newMap = polylineFlow.value.toMutableMap()
                ids.forEach { id ->
                    newMap.remove(id)
                }
                polylineFlow.value = newMap
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            circleRemoveSharedFlow.debounceBatch(5.milliseconds, 300).collect { ids ->
                val newMap = circleFlow.value.toMutableMap()
                ids.forEach { id ->
                    newMap.remove(id)
                }
                circleFlow.value = newMap
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            polygonRemoveSharedFlow.debounceBatch(5.milliseconds, 300).collect { ids ->
                val newMap = polygonFlow.value.toMutableMap()
                ids.forEach { id ->
                    newMap.remove(id)
                }
                polygonFlow.value = newMap
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            groundImageRemoveSharedFlow.debounceBatch(5.milliseconds, 300).collect { ids ->
                val newMap = groundImageFlow.value.toMutableMap()
                ids.forEach { id ->
                    newMap.remove(id)
                }
                groundImageFlow.value = newMap
            }
        }
    }

    fun buildRegistry(): MapOverlayRegistry {
        val registry = MapOverlayRegistry()
        registry.register(MarkerOverlay(markerCollector.flow))
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
        val flowState = typedOverlay.flow.collectAsState()

        LaunchedEffect(flowState.value) {
            typedOverlay.render(flowState.value, controller)
        }
    }
}
