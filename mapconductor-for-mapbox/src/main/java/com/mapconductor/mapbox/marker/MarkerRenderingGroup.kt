package com.mapconductor.mapbox.marker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import com.mapconductor.core.map.LocalMapViewController
import com.mapconductor.core.marker.LocalMarkerCollector
import com.mapconductor.core.marker.MarkerCollector
import com.mapconductor.core.marker.MarkerRenderingStrategy
import com.mapconductor.core.marker.StrategyMarkerController
import com.mapconductor.mapbox.MapboxActualMarker
import com.mapconductor.mapbox.MapboxMapViewControllerImpl
import com.mapconductor.mapbox.MapboxMapViewHolder
import com.mapconductor.settings.Settings
import java.util.UUID
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce

@OptIn(FlowPreview::class)
@Composable
fun MarkerRenderingGroup(
    strategy: MarkerRenderingStrategy<MapboxActualMarker>,
    content: @Composable () -> Unit,
) {
    val mapController = LocalMapViewController.current
    val mapboxController = mapController as? MapboxMapViewControllerImpl ?: return
    val holder = mapboxController.holder as? MapboxMapViewHolder ?: return
    val markerCollector = remember { MarkerCollector() }
    val groupId = remember { UUID.randomUUID().toString() }
    val markerLayer =
        remember(groupId) {
            MarkerLayer(
                sourceId = "markers-source-$groupId",
                layerId = "markers-layer-$groupId",
            )
        }
    val dragLayer =
        remember(groupId) {
            MarkerDragLayer(
                sourceId = "marker-drag-source-$groupId",
                layerId = "marker-drag-layer-$groupId",
            )
        }
    val renderer =
        remember(holder, strategy, markerLayer, dragLayer) {
            MapboxMarkerOverlayRenderer(
                holder = holder,
                markerManager = strategy.markerManager,
                markerLayer = markerLayer,
                dragLayer = dragLayer,
            )
        }
    val markerController =
        remember(strategy, renderer) {
            StrategyMarkerController(
                strategy = strategy,
                renderer = renderer,
            )
        }

    LaunchedEffect(mapboxController, markerController, renderer) {
        mapboxController.registerOverlayController(markerController)
        mapboxController.registerMarkerEventController(
            StrategyMapboxMarkerEventController(
                controller = markerController,
                renderer = renderer,
            ),
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
