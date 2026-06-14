package com.mapconductor.example.pages.infobubble

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
import java.io.Serializable

data class LocationInfo(
    val name: String,
    val description: String,
    val rating: Float,
    val imageUrl: String? = null,
) : Serializable

@Composable
fun RichContentBubblePage(onToggleSidebar: () -> Unit = {}) {
    val initCameraPosition =
        MapCameraPosition(
            position = GeoPoint.fromLatLong(37.7749, -122.4194),
            zoom = 10.0,
        )
    var selectedMarker by remember { mutableStateOf<MarkerState?>(null) }
    var mapViewState by remember { mutableStateOf<MapViewStateInterface<Any>?>(null) }
    val isDarkTheme = isSystemInDarkTheme()
    val markerState =
        remember {
            val locationInfo =
                LocationInfo(
                    name = "Golden Gate Park",
                    description = "A large urban park with gardens, museums, and recreational areas.",
                    rating = 4.5f,
                )
            MarkerState(
                position = GeoPoint.fromLatLong(37.7694, -122.4862),
                icon = DefaultMarkerIcon(fillColor = Color.Green, label = "🌳"),
                extra = locationInfo,
                onClick = { markerState -> selectedMarker = markerState },
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
        LaunchedEffect(Unit) {
            selectedMarker = markerState
        }

        mapViewState?.let {
            MapViewContainer(
                modifier = Modifier.fillMaxSize(),
                state = mapViewState,
                onMapLoaded = {
                    selectedMarker = markerState
                },
                onMapClick = { selectedMarker = null },
            ) {
                Marker(markerState)

                selectedMarker?.let { marker ->
                    val info = marker.extra as? LocationInfo
                    info?.let {
                        InfoBubble(
                            marker = marker,
                            bubbleColor = if (isDarkTheme) Color.Black else Color.White,
                            borderColor = if (isDarkTheme) Color.Gray else Color.Black,
                            contentPadding = 16.dp,
                            cornerRadius = 12.dp,
                        ) {
                            Column(
                                modifier = Modifier.width(200.dp),
                            ) {
                                Text(
                                    text = info.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = info.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isDarkTheme) Color.White else Color.Gray,
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    repeat(5) { index ->
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = null,
                                            tint = if (index < info.rating.toInt()) Color.Yellow else Color.Gray,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                                    Text(
                                        text = " ${info.rating}/5",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(start = 4.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
