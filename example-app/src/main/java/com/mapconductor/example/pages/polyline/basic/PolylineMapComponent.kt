package com.mapconductor.example.pages.polyline

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapconductor.compose.marker.Markers
import com.mapconductor.compose.polyline.Polyline
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.example.MapViewContainer

@Composable
fun PolylineMapComponent(
    mapViewState: MapViewStateInterface<*>?,
    polylineState: PolylineState,
    wayPointMarkers: List<MarkerState>,
    modifier: Modifier = Modifier,
) {
    mapViewState?.let { it ->
        MapViewContainer(
            modifier = modifier,
            state = it,
        ) {
            // Polyline
            Polyline(polylineState)

            // Waypoint markers
            Markers(wayPointMarkers)
        }
    }
}
