package com.mapconductor.marker.clustering

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.mapconductor.core.map.LocalMapViewController
import com.mapconductor.core.marker.LocalMarkerCollector
import com.mapconductor.core.marker.MarkerCollector
import com.mapconductor.core.marker.MarkerRenderingStrategyInterface
import com.mapconductor.core.marker.StrategyMarkerController
import com.mapconductor.settings.Settings
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce

@OptIn(FlowPreview::class)
@Composable
internal fun <ActualMarker> MarkerRenderingGroup(
    strategy: MarkerRenderingStrategyInterface<ActualMarker>,
    content: @Composable () -> Unit,
) {
    val mapController = LocalMapViewController.current

    @Suppress("UNCHECKED_CAST")
    val renderingSupport = mapController as? MarkerRenderingSupport<ActualMarker> ?: return
    val markerCollector = remember { MarkerCollector() }
    val renderer =
        remember(mapController, strategy) {
            renderingSupport.createMarkerRenderer(strategy)
        }
    val markerController =
        remember(strategy, renderer) {
            StrategyMarkerController(
                strategy = strategy,
                renderer = renderer,
            )
        }
    val eventController =
        remember(markerController, renderer) {
            renderingSupport.createMarkerEventController(markerController, renderer)
        }

    var isRegistered by remember { mutableStateOf(false) }

    LaunchedEffect(mapController, markerController, eventController) {
        mapController.registerOverlayController(markerController)
        renderingSupport.registerMarkerEventController(eventController)
        isRegistered = true
    }

    val mapLoaded = renderingSupport.mapLoadedState?.collectAsState()?.value ?: true
    var requestedInitialCameraUpdate by remember { mutableStateOf(false) }

    LaunchedEffect(mapLoaded, isRegistered) {
        if (!mapLoaded || !isRegistered || requestedInitialCameraUpdate) return@LaunchedEffect
        requestedInitialCameraUpdate = true
        renderingSupport.onMarkerRenderingReady()
    }

    val markers = markerCollector.flow.collectAsState()
    LaunchedEffect(mapLoaded, markers.value) {
        if (!mapLoaded) return@LaunchedEffect
        markerController.add(markers.value.values.toList())
    }

    markers.value.values.forEach { markerState ->
        LaunchedEffect(markerState.id, mapLoaded) {
            if (!mapLoaded) return@LaunchedEffect
            markerState.asFlow().debounce(Settings.Default.composeEventDebounce).collectLatest {
                if (markerController.getEntity(markerState.id) != null) {
                    markerController.update(markerState)
                }
            }
        }
    }

    CompositionLocalProvider(LocalMarkerCollector provides markerCollector) {
        content()
    }
}
