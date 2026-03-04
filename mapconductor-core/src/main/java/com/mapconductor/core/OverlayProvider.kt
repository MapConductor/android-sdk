package com.mapconductor.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.mapconductor.core.circle.CircleFingerPrint
import com.mapconductor.core.circle.CircleOverlay
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.controller.MapViewControllerInterface
import com.mapconductor.core.groundimage.GroundImageFingerPrint
import com.mapconductor.core.groundimage.GroundImageOverlay
import com.mapconductor.core.groundimage.GroundImageState
import com.mapconductor.core.info.InfoBubbleEntry
import com.mapconductor.core.map.MapOverlayInterface
import com.mapconductor.core.map.MapOverlayRegistry
import com.mapconductor.core.marker.MarkerFingerPrint
import com.mapconductor.core.marker.MarkerOverlay
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polygon.PolygonFingerPrint
import com.mapconductor.core.polygon.PolygonOverlay
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.core.polyline.PolylineFingerPrint
import com.mapconductor.core.polyline.PolylineOverlay
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.core.raster.RasterLayerFingerPrint
import com.mapconductor.core.raster.RasterLayerOverlay
import com.mapconductor.core.raster.RasterLayerState
import com.mapconductor.settings.Settings
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow

open class MapViewScope {
    val markerCollector =
        ChildCollectorImpl<MarkerState, MarkerFingerPrint>(
            asFlow = { it.asFlow() },
            updateDebounce = Settings.Default.composeEventDebounce,
        )
    val bubbleFlow = MutableStateFlow<MutableMap<String, InfoBubbleEntry>>(mutableMapOf())
    val polylineCollector =
        ChildCollectorImpl<PolylineState, PolylineFingerPrint>(
            asFlow = { it.asFlow() },
            updateDebounce = Settings.Default.composeEventDebounce,
        )
    val circleCollector =
        ChildCollectorImpl<CircleState, CircleFingerPrint>(
            asFlow = { it.asFlow() },
            updateDebounce = Settings.Default.composeEventDebounce,
        )
    val polygonCollector =
        ChildCollectorImpl<PolygonState, PolygonFingerPrint>(
            asFlow = { it.asFlow() },
            updateDebounce = Settings.Default.composeEventDebounce,
        )
    val groundImageCollector =
        ChildCollectorImpl<GroundImageState, GroundImageFingerPrint>(
            asFlow = { it.asFlow() },
            updateDebounce = Settings.Default.composeEventDebounce,
        )
    val rasterLayerCollector =
        ChildCollectorImpl<RasterLayerState, RasterLayerFingerPrint>(
            asFlow = { it.asFlow() },
            updateDebounce = Settings.Default.composeEventDebounce,
        )

    fun buildRegistry(): MapOverlayRegistry {
        val registry = MapOverlayRegistry()
        registry.register(MarkerOverlay(markerCollector.flow))
        registry.register(CircleOverlay(circleCollector.flow))
        registry.register(PolylineOverlay(polylineCollector.flow))
        registry.register(PolygonOverlay(polygonCollector.flow))
        registry.register(GroundImageOverlay(groundImageCollector.flow))
        registry.register(RasterLayerOverlay(rasterLayerCollector.flow))
        return registry
    }
}

@OptIn(FlowPreview::class)
@Composable
fun CollectAndRenderOverlays(
    registry: MapOverlayRegistry,
    controller: MapViewControllerInterface,
) {
    registry.getAll().forEach { overlay ->
        @Suppress("UNCHECKED_CAST")
        val typedOverlay = overlay as MapOverlayInterface<Any>
        val flowState = typedOverlay.flow.collectAsState()

        LaunchedEffect(flowState.value) {
            typedOverlay.render(flowState.value, controller)
        }
    }
}
