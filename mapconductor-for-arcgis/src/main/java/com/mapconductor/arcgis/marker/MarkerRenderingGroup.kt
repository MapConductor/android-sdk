package com.mapconductor.arcgis.marker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import com.arcgismaps.mapping.view.GraphicsOverlay
import com.mapconductor.arcgis.ArcGISActualMarker
import com.mapconductor.arcgis.ArcGISMapViewHolder
import com.mapconductor.arcgis.map.ArcGISMapViewControllerImpl
import com.mapconductor.core.map.LocalMapViewController
import com.mapconductor.core.marker.LocalMarkerCollector
import com.mapconductor.core.marker.MarkerCollector
import com.mapconductor.core.marker.MarkerRenderingStrategy
import com.mapconductor.core.marker.StrategyMarkerController
import com.mapconductor.settings.Settings
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce

@OptIn(FlowPreview::class)
@Composable
fun MarkerRenderingGroup(
    strategy: MarkerRenderingStrategy<ArcGISActualMarker>,
    content: @Composable () -> Unit,
) {
    val mapController = LocalMapViewController.current
    val arcgisController = mapController as? ArcGISMapViewControllerImpl ?: return
    val holder = arcgisController.holder as? ArcGISMapViewHolder ?: return
    val markerCollector = remember { MarkerCollector() }
    val markerLayer = remember { GraphicsOverlay() }
    val renderer =
        remember(holder, markerLayer) {
            ArcGISMarkerRenderer(
                markerLayer = markerLayer,
                holder = holder,
            )
        }
    val markerController =
        remember(strategy, renderer) {
            StrategyMarkerController(
                strategy = strategy,
                renderer = renderer,
            )
        }

    LaunchedEffect(arcgisController, markerController, markerLayer) {
        arcgisController.registerMarkerOverlayLayer(markerLayer)
        arcgisController.registerOverlayController(markerController)
        arcgisController.registerMarkerEventController(
            StrategyArcGISMarkerEventController(markerController),
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
