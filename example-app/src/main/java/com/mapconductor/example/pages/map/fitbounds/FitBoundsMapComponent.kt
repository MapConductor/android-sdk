package com.mapconductor.example.pages.map.fitbounds

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapconductor.compose.marker.Marker
import com.mapconductor.compose.polygon.Polygon
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.example.MapViewContainer

@Composable
fun FitBoundsMapComponent(
    mapViewState: MapViewStateInterface<*>?,
    marker: MarkerState,
    boundsPolygon: PolygonState?,
    onMapLongClick: OnMapEventHandler? = null,
    modifier: Modifier = Modifier,
) {
    mapViewState?.let { state ->
        MapViewContainer(
            modifier = modifier,
            state = state,
            onMapLongClick = onMapLongClick,
        ) {
            Marker(marker)
            boundsPolygon?.let { Polygon(it) }
        }
    }
}
