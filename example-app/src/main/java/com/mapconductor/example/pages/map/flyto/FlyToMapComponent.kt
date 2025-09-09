package com.mapconductor.example.pages.map.flyto

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polyline.Polyline
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.example.MapViewContainer

@Composable
fun FlyToMapComponent(
    mapViewState: MapViewState<*>?,
    polylines: List<PolylineState>,
    markers: List<MarkerState>,
    modifier: Modifier = Modifier,
) {
    mapViewState?.let { state ->
        MapViewContainer(
            modifier = modifier,
            state = state,
        ) {
            // Render polylines connecting all markers
            polylines.forEach { polyline ->
                Polyline(polyline)
            }

            // Render markers for fly to destinations
            markers.forEach { marker ->
                Marker(marker)
            }
        }
    }
}
