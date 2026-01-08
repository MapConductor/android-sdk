package com.mapconductor.example.pages.marker.postofficecluster

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mapconductor.core.info.InfoBubble
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.core.map.OnMapLoadedHandler
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.example.MapViewContainer
import com.mapconductor.marker.clustering.MarkerClusterGroup
import com.mapconductor.marker.clustering.MarkerClusterGroupState

@Composable
fun <ActualMarker> PostOfficeMapComponent(
    modifier: Modifier = Modifier,
    mapViewState: MapViewStateInterface<*>,
    selectedMarker: MarkerState?,
    markers: List<MarkerState> = emptyList<MarkerState>(),
    onMapLoaded: OnMapLoadedHandler? = null,
    onMapClick: OnMapEventHandler? = null,
    onInfoWndClick: ((PostOffice) -> Unit)? = null,
    clusterGroupState: MarkerClusterGroupState<ActualMarker>? = null,
) {
    val darkTheme: Boolean = isSystemInDarkTheme()
    val bubbleColor by remember {
        mutableStateOf(if (darkTheme) Color.Black else Color.White)
    }
    val resolvedClusterGroupState =
        clusterGroupState ?: remember { MarkerClusterGroupState<ActualMarker>() }

    MapViewContainer(
        modifier = modifier,
        state = mapViewState,
        onMapLoaded = onMapLoaded,
        onMapClick = onMapClick,
    ) {
        MarkerClusterGroup(state = resolvedClusterGroupState) {
            markers.forEach { markerState -> Marker(markerState) }
        }

        val selectedPostOffice = selectedMarker?.extra as? PostOffice
        if (selectedMarker != null && selectedPostOffice != null) {
            InfoBubble(
                bubbleColor = bubbleColor,
                marker = selectedMarker,
            ) {
                PostOfficeInfoView(
                    info = selectedPostOffice,
                    onClick = onInfoWndClick,
                )
            }
        }
    }
}
