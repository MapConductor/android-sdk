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
import com.mapconductor.googlemaps.GoogleMapsView
import com.mapconductor.googlemaps.rememberGoogleMapViewState
import com.mapconductor.maplibre.MapLibreMapView
import com.mapconductor.maplibre.rememberMapLibreMapViewState
import com.mapconductor.simple_map_app.ui.theme.MapConductorSDKTheme
import android.os.Bundle

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
            position = GeoPointImpl.fromLatLong(52.35673, 4.91638),
            zoom = 5.0,
        )
    )
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
    }
}
