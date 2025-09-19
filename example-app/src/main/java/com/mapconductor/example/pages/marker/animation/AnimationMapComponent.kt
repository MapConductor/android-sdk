package com.mapconductor.example.pages.marker.animation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapconductor.core.circle.OnCircleEventHandler
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.example.MapViewContainer

@Composable
fun AnimationMapComponent(
    mapViewState: MapViewState<*>?,
    viewModel: AnimationPageViewModel,
    modifier: Modifier = Modifier,
    onMapClick: OnMapEventHandler = {},
    onMarkerClick: OnMarkerEventHandler = {},
    onCircleClick: OnCircleEventHandler = {},
    onMarkerDrag: OnMarkerEventHandler = {},
) {
    mapViewState?.let {
        MapViewContainer(
            modifier = modifier,
            state = it,
            onMapClick = onMapClick,
            onMarkerClick = onMarkerClick,
            onMarkerDrag = onMarkerDrag,
            onCircleClick = onCircleClick,
        ) {
            viewModel.allMarkers.forEach { marker ->
                Marker(marker)
            }
        }
    }
}
