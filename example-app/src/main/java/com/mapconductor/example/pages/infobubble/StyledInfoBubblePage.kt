package com.mapconductor.example.pages.infobubble

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.info.InfoBubble
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.DefaultIcon
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.example.MapViewContainer
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold

@Composable
fun StyledInfoBubblePage(onToggleSidebar: () -> Unit = {}) {
    val initCameraPosition =
        MapCameraPositionImpl(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            zoom = 10.0,
        )
    var selectedMarker by remember { mutableStateOf<MarkerState?>(null) }
    var mapViewState by remember { mutableStateOf<MapViewState<Any>?>(null) }
    val isDarkTheme = isSystemInDarkTheme()

    val onMarkerClick: OnMarkerEventHandler = { markerState -> selectedMarker = markerState }

    val markerState = remember {
        MarkerState(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = DefaultIcon(fillColor = Color.Green, label = "POI"),
            extra = "Point of Interest",
            onClick = onMarkerClick,
        )
    }

    DemoMapPageScaffold(
        menuItems = DefaultMapViewItems(initCameraPosition),
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = { state ->
            mapViewState = state as MapViewState<Any>
        },
    ) {

        LaunchedEffect(Unit) {
            selectedMarker = markerState
        }

        mapViewState?.let {
            MapViewContainer(
                modifier = Modifier.fillMaxSize(),
                state = mapViewState,
                onMapClick = { selectedMarker = null },
            ) {
                Marker(markerState)

                // Show info bubble for selected marker
                selectedMarker?.let { marker ->
                    InfoBubble(
                        marker = marker,
                        bubbleColor = if (isDarkTheme) Color.Black else Color.White,
                        borderColor = if (isDarkTheme) Color.Gray else Color.Black,
                        contentPadding = 12.dp,
                        cornerRadius = 8.dp,
                        tailSize = 10.dp,
                    ) {
                        Text(
                            text = marker.extra as? String ?: "",
                            color = if (isDarkTheme) Color.White else Color.Black,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        }
    }
}
