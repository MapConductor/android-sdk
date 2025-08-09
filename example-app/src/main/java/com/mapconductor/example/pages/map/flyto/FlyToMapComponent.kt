package com.mapconductor.example.pages.map.flyto

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.polyline.Polyline
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.example.MapViewContainer

@Composable
fun FlyToMapComponent(
    mapViewState: MapViewState<*>?,
    polylines: List<PolylineState>,
    markers: List<MarkerState>,
    modifier: Modifier = Modifier,
    onMapClick: OnMapEventHandler = {},
    onMarkerClick: OnMarkerEventHandler = {},
) {
    mapViewState?.let { state ->
        MapViewContainer(
            modifier = modifier,
            state = state,
            onMapClick = onMapClick,
            onMarkerClick = onMarkerClick,
        ) {
            // Render polylines connecting all markers
            polylines.forEach { polyline ->
                key(polyline.id) {
                    Polyline(polyline)
                }
            }

            // Render markers for fly to destinations
            markers.forEach { marker ->
                key(marker.id) {
                    Marker(marker)
                }
            }
        }
    }
}
