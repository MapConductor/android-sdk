package com.mapconductor.example.pages.polyline

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.polyline.Polyline
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.example.MapViewContainer

@Composable
fun PolylineMapComponent(
    mapViewState: MapViewState<*>?,
    polylineState: PolylineState,
    wayPointMarkers: List<MarkerState>,
    modifier: Modifier = Modifier,
    onMarkerDrag: OnMarkerEventHandler = {},
) {
    mapViewState?.let { it ->
        MapViewContainer(
            modifier = modifier,
            state = it,
            onMarkerDragStart = onMarkerDrag,
            onMarkerDrag = onMarkerDrag,
            onMarkerDragEnd = onMarkerDrag,
        ) {
            // Polyline
            Polyline(polylineState)

            // Waypoint markers
            wayPointMarkers.forEach { marker ->
                Marker(marker)
            }
        }
    }
}
