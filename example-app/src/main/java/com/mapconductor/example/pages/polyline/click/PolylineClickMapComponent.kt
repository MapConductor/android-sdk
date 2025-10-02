package com.mapconductor.example.pages.polyline

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polyline.OnPolylineEventHandler
import com.mapconductor.core.polyline.Polyline
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.example.MapViewContainer

@Composable
fun PolylineClickMapComponent(
    mapViewState: MapViewState<*>?,
    polylineState: PolylineState,
    markers: List<MarkerState>,
    modifier: Modifier = Modifier,
    onPolylineClick: OnPolylineEventHandler = {},
) {
    mapViewState?.let { it ->
        MapViewContainer(
            modifier = modifier,
            state = it,
            onPolylineClick = onPolylineClick,
        ) {
            // Polyline
            Polyline(polylineState)

            // Waypoint markers
            markers.forEach { marker ->
                Marker(marker)
            }
        }
    }
}
