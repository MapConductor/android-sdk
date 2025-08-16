package com.mapconductor.example.pages.animation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import com.mapconductor.core.circle.Circle
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
    mapViewState?.let { it ->
        MapViewContainer(
            modifier = modifier,
            state = it,
            onMapClick = onMapClick,
            onMarkerClick = onMarkerClick,
            onCircleClick = onCircleClick,
            onMarkerDrag = onMarkerDrag,
        ) {
            // Center marker (not draggable)
            key(viewModel.centerMarker.id) {
                Marker(viewModel.centerMarker)
            }

            // Edge marker (draggable)
            key(viewModel.edgeMarker.id) {
                Marker(viewModel.edgeMarker)
            }
        }
    }
}
