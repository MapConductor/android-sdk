package com.mapconductor.here.marker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import com.mapconductor.core.map.LocalMapViewController
import com.mapconductor.core.marker.LocalMarkerCollector
import com.mapconductor.core.marker.MarkerCollector
import com.mapconductor.core.marker.MarkerRenderingStrategyInterface
import com.mapconductor.core.marker.StrategyMarkerController
import com.mapconductor.here.HereActualMarker
import com.mapconductor.here.HereMapViewController
import com.mapconductor.here.HereViewHolder
import com.mapconductor.settings.Settings
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce

@OptIn(FlowPreview::class)
@Composable
fun MarkerRenderingGroup(
    strategy: MarkerRenderingStrategyInterface<HereActualMarker>,
    content: @Composable () -> Unit,
) {
    val mapController = LocalMapViewController.current
    val hereController = mapController as? HereMapViewController ?: return
    val holder = hereController.holder as? HereViewHolder ?: return
    val markerCollector = remember { MarkerCollector() }
    val renderer = remember(holder) { HereMarkerRenderer(holder = holder) }
    val markerController =
        remember(strategy, renderer) {
            StrategyMarkerController(
                strategy = strategy,
                renderer = renderer,
            )
        }

    LaunchedEffect(hereController, markerController) {
        hereController.registerOverlayController(markerController)
        hereController.registerMarkerEventController(
            StrategyHereMarkerEventController(markerController),
        )
    }

    val markers = markerCollector.flow.collectAsState()
    LaunchedEffect(markers.value) {
        markerController.add(markers.value.values.toList())
    }

    markers.value.values.forEach { markerState ->
        LaunchedEffect(markerState.id) {
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
