package com.mapconductor.example.pages.infobubble

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.text.font.FontWeight
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.info.InfoBubble
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.DefaultIcon
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.example.MapViewContainer
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold

@Composable
fun MultipleBubblesPage(onToggleSidebar: () -> Unit = {}) {
    val initCameraPosition =
        MapCameraPositionImpl(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            zoom = 15.0,
        )
    var selectedMarkers by remember { mutableStateOf(setOf<String>()) }
    var mapViewState by remember { mutableStateOf<MapViewState<Any>?>(null) }
    val markerData =
        remember {
            listOf(
                Triple(GeoPointImpl.fromLatLong(37.7749, -122.4194), "Restaurant A", Color.Red),
                Triple(GeoPointImpl.fromLatLong(37.7849, -122.4094), "Hotel B", Color.Blue),
                Triple(GeoPointImpl.fromLatLong(37.7649, -122.4294), "Shop C", Color.Green),
            )
        }
    val markerStates =
        remember {
            markerData.mapIndexed { index, (position, name, color) ->
                MarkerState(
                    id = "marker_$index",
                    position = position,
                    icon = DefaultIcon(fillColor = color, label = "${index + 1}"),
                    extra = name,
                )
            }
        }

    // Initially open the info bubble
    LaunchedEffect(Unit) {
        selectedMarkers = markerStates.map { it.id }.toSet()
    }

    DemoMapPageScaffold(
        menuItems = DefaultMapViewItems(initCameraPosition),
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = { state ->
            mapViewState = state as MapViewState<Any>
        },
    ) { paddingValues ->

        mapViewState?.let {
            MapViewContainer(
                modifier = Modifier.fillMaxSize(),
                state = mapViewState,
                onMapClick = {
                    selectedMarkers = emptySet() // Clear all selections
                },
                onMarkerClick = { markerState ->
                    selectedMarkers =
                        if (selectedMarkers.contains(markerState.id)) {
                            selectedMarkers - markerState.id // Deselect
                        } else {
                            selectedMarkers + markerState.id // Select
                        }
                },
            ) {
                markerStates.forEach { markerState ->
                    Marker(markerState)

                    // Show bubble if marker is selected
                    if (selectedMarkers.contains(markerState.id)) {
                        InfoBubble(
                            marker = markerState,
                            bubbleColor = Color.White,
                            borderColor = Color.Black,
                        ) {
                            Column {
                                Text(
                                    text = markerState.extra as String,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = "Tap to close",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
