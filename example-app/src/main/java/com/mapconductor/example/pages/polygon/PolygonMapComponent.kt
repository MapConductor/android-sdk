package com.mapconductor.example.pages.polygon

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.OnMarkerEventHandler

@Composable
fun PolygonMapComponent(
    mapViewState: MapViewState<*>?,
    modifier: Modifier = Modifier,
    onMarkerDrag: OnMarkerEventHandler = {},
) {
//    mapViewState?.let { it ->
//        MapViewContainer(
//            modifier = modifier,
//            state = it,
//            onMarkerDrag = onMarkerDrag,
//        ) {
//            // Polyline
//            Polyline(viewModel.polylineState)
//
//            // Waypoint markers
//            viewModel.wayPointMarkers.forEach { marker ->
//                Marker(marker)
//            }
//        }
//    }
}
