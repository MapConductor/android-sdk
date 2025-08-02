package com.mapconductor.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.groundimage.GroundImageState
import com.mapconductor.core.info.InfoBubbleEntry
import com.mapconductor.core.map.MapOverlay
import com.mapconductor.core.map.MapOverlayRegistry
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polyline.PolylineState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// data class OverlayProvider<T>(
//    val compositionLocal: ProvidableCompositionLocal<MutableStateFlow<List<T>>>,
//    val stateFlow: MutableStateFlow<List<T>>,
// )

// @Composable
// fun ProvideOverlayLocals(
//    providers: List<OverlayProvider<*>>,
//    content: @Composable () -> Unit,
// ) {
//    val wrapped =
//        providers.foldRight(content) { provider, acc ->
//            @Suppress("UNCHECKED_CAST")
//            {
//                val local = provider.compositionLocal as ProvidableCompositionLocal<MutableStateFlow<List<Any?>>>
//                val flow = provider.stateFlow as MutableStateFlow<List<Any?>>
//                CompositionLocalProvider(local provides flow) {
//                    acc()
//                }
//            }
//        }
//
//    wrapped()
// }

// @Composable
// fun <T> CollectAndRenderOverlays(
//    map: T?,
//    registry: MapOverlayRegistry,
//    controller: MapViewController,
// ) {
//    registry.getAll().forEach { overlay ->
//        @Suppress("UNCHECKED_CAST")
//        val typedOverlay = overlay as MapOverlay<Any>
//
//        val flowState = typedOverlay.flow.collectAsState()
//
//        LaunchedEffect(map, flowState.value) {
//            if (map == null) return@LaunchedEffect
//            typedOverlay.render(flowState.value, controller)
//        }
//    }
// }

open class MapViewScope {
    val markerFlow = MutableStateFlow<List<MarkerState>>(emptyList())
    val bubbleFlow = MutableStateFlow<List<InfoBubbleEntry>>(emptyList())
    val polylineFlow = MutableStateFlow<List<PolylineState>>(emptyList())
    val circleFlow = MutableStateFlow<List<CircleState>>(emptyList())
    val groundImageFlow = MutableStateFlow<List<GroundImageState>>(emptyList())

    fun buildRegistry(): MapOverlayRegistry {
        val registry = MapOverlayRegistry()
        registry.register(MarkerOverlay(markerFlow))
        registry.register(CircleOverlay(circleFlow))
        registry.register(PolylineOverlay(polylineFlow))
        registry.register(GroundImageOverlay(groundImageFlow))
        return registry
    }
}

class MarkerOverlay(
    override val flow: StateFlow<List<MarkerState>>,
) : MapOverlay<MarkerState> {
    override suspend fun render(
        data: List<MarkerState>,
        controller: MapViewController<*, *, *, *>,
    ) {
        controller.addMarkers(data)
    }
}

class CircleOverlay(
    override val flow: StateFlow<List<CircleState>>,
) : MapOverlay<CircleState> {
    override suspend fun render(
        data: List<CircleState>,
        controller: MapViewController<*, *, *, *>,
    ) {
        controller.addCircles(data)
    }
}

val LocalMarkerCollector =
    compositionLocalOf<MutableStateFlow<List<MarkerState>>> {
        error("Marker must be under the <MapView />")
    }

class PolylineOverlay(
    override val flow: StateFlow<List<PolylineState>>,
) : MapOverlay<PolylineState> {
    override suspend fun render(
        data: List<PolylineState>,
        controller: MapViewController<*, *, *, *>,
    ) {
        controller.addPolylines(data)
    }
}

class GroundImageOverlay(
    override val flow: StateFlow<List<GroundImageState>>,
) : MapOverlay<GroundImageState> {
    override suspend fun render(
        data: List<GroundImageState>,
        controller: MapViewController<*, *, *, *>,
    ) {
        controller.addGroundImages(data)
    }
}

val LocalPolylineCollector =
    compositionLocalOf<MutableStateFlow<List<PolylineState>>> {
        error("Polyline must be under the <MapView />")
    }

val LocalCircleCollector =
    compositionLocalOf<MutableStateFlow<List<CircleState>>> {
        error("Circle must be under the <MapView />")
    }

val LocalGroundImageCollector =
    compositionLocalOf<MutableStateFlow<List<GroundImageState>>> {
        error("GroundImage must be under the <MapView />")
    }

@Composable
fun CollectAndRenderOverlays(
    registry: MapOverlayRegistry,
    controller: MapViewController<*, *, *, *>,
) {
    registry.getAll().forEach { overlay ->
        @Suppress("UNCHECKED_CAST")
        val typedOverlay = overlay as MapOverlay<Any>

        LaunchedEffect(Unit) {
            typedOverlay.flow.collect {
                typedOverlay.render(it, controller)
            }
        }

//        val flowState = typedOverlay.flow.collectAsState()
//
//        LaunchedEffect(flowState.value) {
//            typedOverlay.render(flowState.value.toSet().toList(), controller)
//        }
    }
}
