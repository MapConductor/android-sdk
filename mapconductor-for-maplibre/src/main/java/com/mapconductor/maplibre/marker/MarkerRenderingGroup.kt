package com.mapconductor.maplibre.marker

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
import com.mapconductor.maplibre.MapLibreActualMarker
import com.mapconductor.maplibre.MapLibreMapViewHolder
import com.mapconductor.maplibre.MapLibreViewControllerImpl
import com.mapconductor.settings.Settings
import java.util.UUID
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce

@OptIn(FlowPreview::class)
@Composable
fun MarkerRenderingGroup(
    strategy: MarkerRenderingStrategy<MapLibreActualMarker>,
    content: @Composable () -> Unit,
) {
    val mapController = LocalMapViewController.current
    val mapLibreController = mapController as? MapLibreViewControllerImpl ?: return
    val holder = mapLibreController.holder as? MapLibreMapViewHolder ?: return
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
            MapLibreMarkerOverlayRenderer(
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

    LaunchedEffect(mapLibreController, markerController, renderer) {
        mapLibreController.registerOverlayController(markerController)
        mapLibreController.registerMarkerEventController(
            StrategyMapLibreMarkerEventController(
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
