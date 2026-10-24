package com.mapconductor.example.pages.marker.streettree

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mapconductor.compose.info.InfoBubble
import com.mapconductor.compose.marker.Markers
import com.mapconductor.core.OnMapEventHandler
import com.mapconductor.core.OnMapLoadedHandler
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.MarkerTilingOptions
import com.mapconductor.example.MapViewContainer
import com.mapconductor.streettree.StreetTree
import com.mapconductor.streettree.StreetTreeInfoView

@Composable
fun StreetTreeMapComponent(
    mapViewState: MapViewStateInterface<*>,
    selectedMarker: MarkerState?,
    modifier: Modifier = Modifier,
    markers: List<MarkerState> = emptyList(),
    markerTiling: MarkerTilingOptions? = null,
    onMapLoaded: OnMapLoadedHandler? = null,
    onMapClick: OnMapEventHandler? = null,
) {
    val bubbleColor = if (isSystemInDarkTheme()) Color.Black else Color.White

    MapViewContainer(
        modifier = modifier,
        state = mapViewState,
        markerTiling = markerTiling,
        onMapLoaded = onMapLoaded,
        onMapClick = onMapClick,
    ) {
        Markers(markers)

        selectedMarker?.let { marker ->
            (marker.extra as? StreetTree)?.let { tree ->
                InfoBubble(bubbleColor = bubbleColor, marker = marker) {
                    StreetTreeInfoView(tree = tree)
                }
            }
        }
    }
}
