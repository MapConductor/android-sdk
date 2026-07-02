package com.mapconductor.example.pages.polyline

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mapconductor.compose.marker.Marker
import com.mapconductor.compose.polyline.Polyline
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.example.MapViewContainer

@Composable
fun PolylineClickMapComponent(
    mapViewState: MapViewStateInterface<*>?,
    polylineState: PolylineState,
    markers: List<MarkerState>,
    modifier: Modifier = Modifier,
) {
    mapViewState?.let { it ->
        MapViewContainer(
            modifier = modifier,
            state = it,
        ) {
            // Polyline
            Polyline(polylineState)
            Polyline(
                polylineState.copy(
                    id = "${polylineState.id}-straight",
                    geodesic = false,
                    strokeColor = Color.Blue,
                ),
            )

            // Waypoint markers
            markers.forEach { marker ->
                Marker(marker)
            }
        }
    }
}
