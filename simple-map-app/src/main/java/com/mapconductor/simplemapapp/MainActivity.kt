package com.mapconductor.simplemapapp

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mapconductor.core.circle.Circle
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.info.InfoBubble
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.marker.DefaultIcon
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polyline.Polyline
import com.mapconductor.googlemaps.GoogleMapsView
import com.mapconductor.googlemaps.rememberGoogleMapViewState
import com.mapconductor.maplibre.MapLibreMapView
import com.mapconductor.maplibre.rememberMapLibreMapViewState
import com.mapconductor.simplemapapp.ui.theme.MapConductorSDKTheme
import android.os.Bundle
import android.widget.Toast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    val center = GeoPointImpl.fromLatLong(37.7749, -122.4194)
    val mapViewState = rememberGoogleMapViewState(
        cameraPosition = MapCameraPositionImpl(
            position = center,
            zoom = 13.0,
        ),
    )
    var selectedMarker by remember { mutableStateOf<MarkerState?>(null) }

    val markerState1 by remember { mutableStateOf(MarkerState(
        id = "marker1",
        position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        icon = DefaultIcon(
            fillColor = Color.Blue,
            label = "1"
        ),
        draggable = true,
    )) }
    val markerState2 by remember { mutableStateOf( MarkerState(
        id = "marker2",
        position = GeoPointImpl.fromLatLong(37.7849, -122.4094),
        icon = DefaultIcon(
            fillColor = Color.Red,
            label = "2"
        ),
    )) }

    GoogleMapsView(
        state = mapViewState,
        onMapLoaded = {
            println("Map loaded and ready")
        },
        onMapClick = { geoPoint ->
            selectedMarker = null // 地図クリックで選択解除
        },
        onMarkerClick = { markerState ->
            selectedMarker = markerState
        },
        onMarkerDragStart = { markerState ->
            println("Started dragging marker: ${markerState.id}")
        },
        onMarkerDrag = { markerState ->
            println("Dragging marker to: ${markerState.position}")
        },
        onMarkerDragEnd = { markerState ->
            println("Finished dragging marker: ${markerState.id}")
        }
    ) {
        // インタラクティブなマーカーを持つ地図コンテンツ
        Marker(markerState1)
        Marker(markerState2)

        // 選択されたマーカーの情報を表示
        selectedMarker?.let { marker ->
            val text = GeoPointImpl.from(marker.position).toUrlValue(6)
            InfoBubble(
                marker = marker,
            ) {
                Text(text)
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
