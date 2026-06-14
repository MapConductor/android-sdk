package com.mapconductor.example.pages.infobubble

import androidx.compose.foundation.clickable
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
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.info.InfoBubble
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.marker.DefaultMarkerIcon
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.example.MapViewContainer
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold

@Composable
fun MultipleBubblesPage(onToggleSidebar: () -> Unit = {}) {
    val initCameraPosition =
        MapCameraPosition(
            position = GeoPoint.fromLatLong(37.7749, -122.4194),
            zoom = 15.0,
            tilt = 45.0,
        )
    var selectedMarkers by remember { mutableStateOf(setOf<String>()) }
    var mapViewState by remember { mutableStateOf<MapViewStateInterface<Any>?>(null) }
    val markerData =
        remember {
            listOf(
                Triple(GeoPoint.fromLatLong(37.7749, -122.4194), "Restaurant A", Color.Red),
                Triple(GeoPoint.fromLatLong(37.7849, -122.4094), "Hotel B", Color.Blue),
                Triple(GeoPoint.fromLatLong(37.7649, -122.4294), "Shop C", Color.Green),
            )
        }
    val onMarkerClick: OnMarkerEventHandler = { markerState ->
        selectedMarkers =
            if (selectedMarkers.contains(markerState.id)) {
                selectedMarkers - markerState.id // Deselect
            } else {
                selectedMarkers + markerState.id // Select
            }
    }
    val markerStates =
        remember {
            markerData.mapIndexed { index, (position, name, color) ->
                MarkerState(
                    id = "marker_$index",
                    position = position,
                    icon = DefaultMarkerIcon(fillColor = color, label = "${index + 1}"),
                    extra = name,
                    onClick = onMarkerClick,
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
            @Suppress("UNCHECKED_CAST")
            mapViewState = state as MapViewStateInterface<Any>
        },
    ) {
        mapViewState?.let {
            MapViewContainer(
                modifier = Modifier.fillMaxSize(),
                state = mapViewState,
                onMapLoaded = {
                    selectedMarkers = markerStates.map { it.id }.toSet()
                },
                onMapClick = {
                    selectedMarkers = emptySet() // Clear all selections
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
                            Column(
                                modifier =
                                    Modifier
                                        .clickable(
                                            true,
                                            onClick = {
                                                val filtered =
                                                    selectedMarkers
                                                        .filter { it != markerState.id }
                                                        .toSet()
                                                selectedMarkers = filtered
                                            },
                                        ),
                            ) {
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
