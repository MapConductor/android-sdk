package com.mapconductor.example.pages.polygon.basic

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapconductor.compose.marker.Marker
import com.mapconductor.compose.polygon.Polygon
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.example.MapViewContainer

@Composable
fun PolygonMapComponent(
    mapViewState: MapViewStateInterface<*>?,
    polygonVertexMarkers: List<MarkerState>,
    polygonState: PolygonState,
    modifier: Modifier = Modifier,
) {
    mapViewState?.let { it ->
        MapViewContainer(
            modifier = modifier,
            state = it,
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
