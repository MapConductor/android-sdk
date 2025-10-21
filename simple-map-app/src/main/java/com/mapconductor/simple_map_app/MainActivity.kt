package com.mapconductor.simple_map_app

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.marker.DefaultIcon
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.simple_map_app.ui.theme.MapConductorSDKTheme
import android.os.Bundle
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mapconductor.core.polyline.Polyline
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.maplibre.MapLibreMapView
import com.mapconductor.maplibre.rememberMapLibreMapViewState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MapConductorSDKTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MapView(
                        modifier = Modifier.padding(innerPadding)
                            .fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun MapView(modifier: Modifier = Modifier) {
    val state = rememberMapLibreMapViewState(
        cameraPosition = MapCameraPositionImpl(
            position = GeoPointImpl.fromLatLong(21.382314, -157.933097),
            zoom = 5.0,
        )
    )
    val polylinePoints = mutableListOf(
        GeoPointImpl.fromLatLong(35.548852, 139.784086), // HND_AIR_PORT
        GeoPointImpl.fromLatLong(37.615223, -122.389979), // SFO_AIR_PORT
        GeoPointImpl.fromLatLong(21.324513, -157.925074), // HNL_AIR_PORT
    )

    val polylineState = remember {

        PolylineState(
            id = "example_polyline",
            points = polylinePoints,
            strokeColor = Color.Red,
            strokeWidth = 4.dp,
            geodesic = true,
        )
    }
    val markerState = remember {
        MarkerState(
            position = GeoPointImpl.fromLatLong(52.35673, 4.91638),
            icon = DefaultIcon(scale = 2.0f),
            draggable = true,
        )
    }
    MapLibreMapView(
        modifier = modifier,
        state = state,
    ) {
        Marker(markerState)

        Polyline(polylineState)
    }
}
