package com.mapconductor.example.pages.marker.animation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapconductor.compose.marker.Marker
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.OnMapEventHandler
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.example.MapViewContainer

@Composable
fun AnimationMapComponent(
    mapViewState: MapViewStateInterface<*>?,
    allMarkers: List<MarkerState>,
    modifier: Modifier = Modifier,
    onMapClick: OnMapEventHandler = {},
) {
    mapViewState?.let {
        MapViewContainer(
            modifier = modifier,
            state = it,
            onMapClick = onMapClick,
        ) {
            allMarkers.forEach { marker ->
                Marker(marker)
            }
        }
    }
}
