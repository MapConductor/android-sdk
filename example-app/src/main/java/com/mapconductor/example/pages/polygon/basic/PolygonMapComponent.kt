package com.mapconductor.example.pages.polygon.basic

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.polygon.Polygon
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.example.MapViewContainer

@Composable
fun PolygonMapComponent(
    mapViewState: MapViewState<*>?,
    polygonVertexMarkers: List<MarkerState>,
    polygonState: PolygonState,
    modifier: Modifier = Modifier,
    onMarkerDrag: OnMarkerEventHandler,
) {
    mapViewState?.let { it ->
        MapViewContainer(
            modifier = modifier,
            state = it,
            onMarkerDrag = onMarkerDrag,
        ) {
            // Polygon
            Polygon(polygonState)

            // Vertex markers (draggable)
            polygonVertexMarkers.forEach { marker ->
                Marker(marker)
            }
        }
    }
}
