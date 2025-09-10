package com.mapconductor.example.pages.marker.postoffice

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.mapconductor.core.info.InfoBubble
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.example.MapViewContainer
import com.mapconductor.example.pages.map.basic.StoreInfo
import com.mapconductor.example.pages.map.basic.StoreInfoView

@Composable
fun PostOfficeMapComponent(
    modifier: Modifier = Modifier,
    mapViewState: MapViewState<*>,
    selectedMarker: MarkerState?,
    markers: List<MarkerState> = emptyList<MarkerState>(),
    onMapClick: OnMapEventHandler = {},
    onMarkerClick: OnMarkerEventHandler = {},
) {

    val darkTheme: Boolean = isSystemInDarkTheme()
    val bubbleColor by remember {
        mutableStateOf(if (darkTheme) Color.Black else Color.White)
    }

    MapViewContainer(
        modifier = modifier,
        state = mapViewState,
        onMapClick = onMapClick,
        onMarkerClick = onMarkerClick,
    ) {
        markers.forEach { markerState -> Marker(markerState) }

        selectedMarker?.let {
            InfoBubble(
                bubbleColor = bubbleColor,
                marker = it,
            ) {
                PostOfficeInfoView(
                    info = it.extra as PostOffice,
                )
            }
        }
    }
}
