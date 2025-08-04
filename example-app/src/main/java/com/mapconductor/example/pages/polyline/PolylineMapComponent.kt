package com.mapconductor.example.pages.polyline

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.polyline.OnPolylineEventHandler
import com.mapconductor.core.polyline.Polyline
import com.mapconductor.example.MapViewContainer

@Composable
fun PolylineMapComponent(
    mapViewState: MapViewState<*>?,
    viewModel: PolylinePageViewModel,
    modifier: Modifier = Modifier,
    onMapClickHandler: OnMapEventHandler = {},
    onMarkerClickHandler: OnMarkerEventHandler = {},
    onPolylineClickHandler: OnPolylineEventHandler = {},
    onMarkerDrag: OnMarkerEventHandler = {},
) {
    mapViewState?.let { mapViewState ->
        MapViewContainer(
            modifier = modifier,
            state = mapViewState,
            onMapClick = onMapClickHandler,
            onMarkerClick = onMarkerClickHandler,
            onPolylineClick = onPolylineClickHandler,
            onMarkerDrag = onMarkerDrag,
        ) {
            // Polyline
            key(viewModel.polylineState.id) {
                Polyline(viewModel.polylineState)
            }

            // Waypoint markers
            viewModel.wayPointMarkers.forEach { marker ->
                key(marker.id) {
                    Marker(marker)
                }
            }
        }
    }
}