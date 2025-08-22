package com.mapconductor.example.pages.polygon

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.polygon.OnPolygonEventHandler
import com.mapconductor.core.polygon.Polygon
import com.mapconductor.example.MapViewContainer

@Composable
fun PolygonMapComponent(
    mapViewState: MapViewState<*>?,
    viewModel: PolygonMapPageViewModel,
    modifier: Modifier = Modifier,
    onPolygonClick: OnPolygonEventHandler = {},
    onMarkerDrag: OnMarkerEventHandler = {},
) {
    mapViewState?.let { mapViewState ->
        MapViewContainer(
            modifier = modifier,
            state = mapViewState,
            onPolygonClick = onPolygonClick,
            onMarkerDrag = onMarkerDrag,
        ) {
            // Polygon
            Polygon(viewModel.polygonState)

            // Vertex markers (draggable)
            viewModel.polygonVertexMarkers.forEach { marker ->
                Marker(marker)
            }
        }
    }
}
