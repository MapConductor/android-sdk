package com.mapconductor.simplemapapp

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mapconductor.core.circle.Circle
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.info.InfoBubble
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.marker.DefaultIcon
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.mapbox.MapboxMapView
import com.mapconductor.mapbox.rememberMapboxMapViewState
import com.mapconductor.maplibre.MapLibreMapView
import com.mapconductor.maplibre.rememberMapLibreMapViewState
import com.mapconductor.simplemapapp.ui.theme.MapConductorSDKTheme
import android.os.Bundle

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MapConductorSDKTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BasicMapExample(
                        modifier =
                            Modifier
                                .padding(innerPadding)
                                .fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
fun BasicMapExample(modifier: Modifier = Modifier) {
    val sanFrancisco = GeoPointImpl.fromLatLong(37.7749, -122.4194)
    val camera =
        MapCameraPositionImpl(
            position = sanFrancisco,
            zoom = 13.0,
        )

    val mapViewState =
        rememberMapboxMapViewState(
            cameraPosition = camera,
        )
    var selectedMarker by remember { mutableStateOf<MarkerState?>(null) }

    MapboxMapView(
        state = mapViewState,
        modifier = modifier,
        onMapClick = { geoPoint ->
            selectedMarker = null
        },
        onMarkerClick = { markerState -> selectedMarker = markerState },
    ) {
        val markerState =
            MarkerState(
                position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                icon = DefaultIcon(fillColor = Color.Blue, label = "SF"),
                extra = "San Francisco - The Golden Gate City",
            )

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

@Composable
fun MapView(modifier: Modifier = Modifier) {
    val state =
        rememberMapLibreMapViewState(
            cameraPosition =
                MapCameraPositionImpl(
                    position = GeoPointImpl.fromLatLong(21.324513, -157.925074),
                    zoom = 5.0,
                ),
        )

    val markerState =
        remember {
            MarkerState(
                position = GeoPointImpl.fromLatLong(21.324513, -157.925074),
                draggable = true,
            )
        }

    val circleState =
        remember {
            CircleState(
                id = "demo-circle",
                center = markerState.position,
                radiusMeters = 5000.0,
                strokeColor = Color.Magenta,
                strokeWidth = 2.dp,
                fillColor = Color.Cyan.copy(alpha = 0.3f),
                geodesic = true,
            )
        }

    MapLibreMapView(
        modifier = modifier,
        state = state,
        onMarkerDrag = { draggedState ->
            circleState.center = draggedState.position
        },
    ) {
        Marker(markerState)
        Circle(circleState)
    }
}
