package com.mapconductor.googlemaps.marker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mapconductor.core.map.LocalMapViewController
import com.mapconductor.core.marker.LocalMarkerCollector
import com.mapconductor.core.marker.MarkerCollector
import com.mapconductor.core.marker.MarkerRenderingStrategy
import com.mapconductor.core.marker.StrategyMarkerController
import com.mapconductor.googlemaps.GoogleMapActualMarker
import com.mapconductor.googlemaps.GoogleMapViewControllerImpl
import com.mapconductor.googlemaps.GoogleMapViewHolder
import com.mapconductor.settings.Settings
import android.util.Log
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce

@OptIn(FlowPreview::class)
@Composable
fun MarkerRenderingGroup(
    strategy: MarkerRenderingStrategy<GoogleMapActualMarker>,
    content: @Composable () -> Unit,
) {
    val mapController = LocalMapViewController.current
    val googleMapController = mapController as? GoogleMapViewControllerImpl ?: return
    val holder = googleMapController.holder as? GoogleMapViewHolder ?: return
    val markerCollector = remember {
        MarkerCollector()
    }
    val renderer = remember(holder) {
        GoogleMapMarkerRenderer(holder = holder)
    }
    val markerController =
        remember(strategy, renderer) {
            StrategyMarkerController(
                strategy = strategy,
                renderer = renderer,
            )
        }
    val mapLoaded by googleMapController.mapLoadedState.collectAsState()
    var isRegistered by remember { mutableStateOf(false) }
    var requestedInitialCameraUpdate by remember { mutableStateOf(false) }

    LaunchedEffect(googleMapController, markerController) {
        googleMapController.registerOverlayController(markerController)
        googleMapController.registerMarkerEventController(
            StrategyGoogleMapMarkerEventController(markerController),
        )
        isRegistered = true
    }

    LaunchedEffect(mapLoaded, isRegistered) {
        if (!mapLoaded || !isRegistered || requestedInitialCameraUpdate) return@LaunchedEffect
        requestedInitialCameraUpdate = true
        googleMapController.sendInitialCameraUpdate()
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
