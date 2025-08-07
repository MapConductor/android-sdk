package com.mapconductor.example.pages.polyline

import androidx.compose.runtime.Composable
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
    onMapClick: OnMapEventHandler = {},
    onMarkerClick: OnMarkerEventHandler = {},
    onPolylineClick: OnPolylineEventHandler = {},
    onMarkerDrag: OnMarkerEventHandler = {},
) {
    mapViewState?.let { it ->
        MapViewContainer(
            modifier = modifier,
            state = it,
            onMapClick = onMapClick,
            onMarkerClick = onMarkerClick,
            onPolylineClick = onPolylineClick,
            onMarkerDrag = onMarkerDrag,
        ) {
            // Polyline
            Polyline(viewModel.polylineState)

            // Waypoint markers
            viewModel.wayPointMarkers.forEach { marker ->
                Marker(marker)
            }
        }
    }
}
