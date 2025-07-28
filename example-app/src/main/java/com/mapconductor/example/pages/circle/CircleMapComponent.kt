package com.mapconductor.example.pages.circle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import com.mapconductor.core.circle.Circle
import com.mapconductor.core.circle.OnCircleEventHandler
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.example.MapViewContainer

@Composable
fun CircleMapComponent(
    mapViewState: MapViewState<*>?,
    viewModel: CirclePageViewModel,
    modifier: Modifier = Modifier,
    onMapClickHandler: OnMapEventHandler = {},
    onMarkerClickHandler: OnMarkerEventHandler = {},
    onCircleClickHandler: OnCircleEventHandler = {},
    onMarkerDrag: OnMarkerEventHandler = {},
) {
    mapViewState?.let { mapViewState ->
        MapViewContainer(
            modifier = modifier,
            state = mapViewState,
            onMapClick = onMapClickHandler,
            onMarkerClick = onMarkerClickHandler,
            onCircleClick = onCircleClickHandler,
            onMarkerDrag = onMarkerDrag,
        ) {
            // Circle
            key(viewModel.circleState.id) {
                Circle(viewModel.circleState)
            }

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
