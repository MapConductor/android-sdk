package com.mapconductor.example.pages.circle

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapconductor.core.circle.Circle
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.circle.OnCircleEventHandler
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.example.MapViewContainer

@Composable
fun CircleMapComponent(
    mapViewState: MapViewState<*>?,
    circleState: CircleState,
    centerMarker: MarkerState,
    edgeMarker: MarkerState,
    modifier: Modifier = Modifier,
    onCircleClick: OnCircleEventHandler = {},
    onMarkerMove: OnMarkerEventHandler = {},
) {
    mapViewState?.let { it ->
        MapViewContainer(
            modifier = modifier,
            state = it,
            onMarkerDragStart = onMarkerMove,
            onMarkerDrag = onMarkerMove,
            onMarkerDragEnd = onMarkerMove,
            onCircleClick = onCircleClick,
        ) {
            // Circle
            Circle(circleState)

            // Center marker (not draggable)
            Marker(centerMarker)

            // Edge marker (draggable)
            Marker(edgeMarker)
        }
    }
}
