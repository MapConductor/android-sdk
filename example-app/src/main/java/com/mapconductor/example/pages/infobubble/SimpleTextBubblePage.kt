package com.mapconductor.example.pages.infobubble

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.info.InfoBubble
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.marker.DefaultMarkerIcon
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.example.MapViewContainer
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold

@Composable
fun SimpleTextBubblePage(onToggleSidebar: () -> Unit = {}) {
    val initCameraPosition =
        MapCameraPosition(
            position = GeoPoint.fromLatLong(37.7749, -122.4194),
            zoom = 10.0,
        )
    var selectedMarker by remember { mutableStateOf<MarkerState?>(null) }
    var mapViewState by remember { mutableStateOf<MapViewStateInterface<Any>?>(null) }
    val markerState =
        remember {
            MarkerState(
                position = GeoPoint.fromLatLong(37.7749, -122.4194),
                icon = DefaultMarkerIcon(fillColor = Color.Blue, label = "SF"),
                extra = "San Francisco - The Golden Gate City",
                onClick = { it -> selectedMarker = it },
            )
        }

    DemoMapPageScaffold(
        menuItems = DefaultMapViewItems(initCameraPosition),
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = { state ->
            @Suppress("UNCHECKED_CAST")
            mapViewState = state as MapViewStateInterface<Any>
        },
    ) {
        mapViewState?.let {
            MapViewContainer(
                modifier = Modifier.fillMaxSize(),
                state = mapViewState,
                onMapClick = { selectedMarker = null },
                onMapLoaded = {
                    selectedMarker = markerState
                }
            ) {
                Marker(markerState)

                // Show info bubble for selected marker
                selectedMarker?.let { marker ->
                    InfoBubble(marker = marker) {
                        Text(
                            text = marker.extra as? String ?: "No information",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(4.dp),
                        )
                    }
                }
            }
        }
    }
}
