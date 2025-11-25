package com.mapconductor.simplemapapp

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
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
import com.mapconductor.core.circle.Circle
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.info.InfoBubble
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.marker.DefaultIcon
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.googlemaps.GoogleMapsView
import com.mapconductor.googlemaps.rememberGoogleMapViewState
import com.mapconductor.here.HereMapView
import com.mapconductor.here.rememberHereMapViewState
import com.mapconductor.mapbox.MapboxMapView
import com.mapconductor.mapbox.rememberMapboxMapViewState
import com.mapconductor.maplibre.MapLibreDesignType
import com.mapconductor.maplibre.MapLibreMapView
import com.mapconductor.maplibre.rememberMapLibreMapViewState
import com.mapconductor.simplemapapp.ui.theme.MapConductorSDKTheme
import android.os.Bundle
import kotlinx.coroutines.delay

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
    val locations = listOf(
        GeoPointImpl.fromLatLong(37.7749, -122.4194), // San Francisco
        GeoPointImpl.fromLatLong(40.7128, -74.0060),  // New York
        GeoPointImpl.fromLatLong(51.5074, -0.1278)    // London
    )

    val mapViewState = rememberHereMapViewState(
        cameraPosition = MapCameraPositionImpl(
            position = locations[0],
            zoom = 6.0
        ),
    )

    var currentIndex by remember { mutableStateOf(0) }

    // Animate to next location every 5 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            currentIndex = (currentIndex + 1) % locations.size

            val targetPosition = MapCameraPositionImpl(
                position = locations[currentIndex],
                zoom = 6.0,
                bearing = 0.0,
                tilt = 0.0
            )
            mapViewState.moveCameraTo(targetPosition, 1000)
        }
    }


    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
    HereMapView(
        state = mapViewState,
        modifier = modifier,
    ) {
        // Add markers for each location
        locations.forEachIndexed { index, location ->
            Marker(
                position = location,
                icon = DefaultIcon(
                    fillColor = if (index == currentIndex) Color.Red else Color.Gray,
                    label = when (index) {
                        0 -> "SF"
                        1 -> "NYC"
                        2 -> "LON"
                        else -> "$index"
                    }
                )
            )
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
