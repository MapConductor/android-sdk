package com.mapconductor.example.pages.map.flyto

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.polyline.Polyline
import com.mapconductor.example.MapViewContainer

@Composable
fun FlyToMapComponent(
    mapViewState: MapViewState<*>?,
    viewModel: FlyToPageViewModel,
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
            viewModel.polylines.forEach { polyline ->
                key(polyline.id) {
                    Polyline(polyline)
                }
            }
            
            // Render markers for fly to destinations
            viewModel.markers.forEach { marker ->
                key(marker.id) {
                    Marker(marker)
                }
            }
        }
    }
}
