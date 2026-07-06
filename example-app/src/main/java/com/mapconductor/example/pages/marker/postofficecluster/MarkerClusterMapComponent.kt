package com.mapconductor.example.pages.marker.postofficecluster

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mapconductor.compose.info.InfoBubble
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.OnMapEventHandler
import com.mapconductor.core.OnMapLoadedHandler
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.example.MapViewContainer
import com.mapconductor.marker.clustering.MarkerClusterGroup
import com.mapconductor.marker.clustering.MarkerClusterGroupState
import com.mapconductor.postoffice.PostOffice
import com.mapconductor.postoffice.PostOfficeInfoView

@Composable
fun MarkerClusterMapComponent(
    modifier: Modifier = Modifier,
    mapViewState: MapViewStateInterface<*>,
    selectedMarker: MarkerState?,
    markers: List<MarkerState> = emptyList<MarkerState>(),
    onMapLoaded: OnMapLoadedHandler? = null,
    onMapClick: OnMapEventHandler? = null,
    onInfoWndClick: ((PostOffice) -> Unit)? = null,
    clusterGroupState: MarkerClusterGroupState? = null,
) {
    val darkTheme: Boolean = isSystemInDarkTheme()
    val bubbleColor by remember {
        mutableStateOf(if (darkTheme) Color.Black else Color.White)
    }
    val resolvedClusterGroupState =
        clusterGroupState ?: remember {
            MarkerClusterGroupState(
                enableZoomAnimation = true,
                enablePanAnimation = true,
            )
        }

    MapViewContainer(
        modifier = modifier,
        state = mapViewState,
        onMapLoaded = onMapLoaded,
        onMapClick = onMapClick,
    ) {
        MarkerClusterGroup(
            state = resolvedClusterGroupState,
            markers = markers,
        )

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
