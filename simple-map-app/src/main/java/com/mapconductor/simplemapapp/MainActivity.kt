package com.mapconductor.simplemapapp

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mapconductor.arcgis.ArcGISMapView
import com.mapconductor.arcgis.rememberArcGISMapViewState
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.marker.DefaultIcon
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.MarkerAnimation
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polygon.Polygon
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.core.polyline.Polyline
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.googlemaps.GoogleMapsView
import com.mapconductor.googlemaps.rememberGoogleMapViewState
import com.mapconductor.here.HereMapView
import com.mapconductor.here.rememberHereMapViewState
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
                    MapView(
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
fun MapView(modifier: Modifier = Modifier) {
    val state =
        rememberHereMapViewState(
            cameraPosition =
                MapCameraPositionImpl(
                    position = GeoPointImpl(24.886, -70.268),
                    zoom = 3.0,
                ),
        )

    var clickedPosition by remember { mutableStateOf<MarkerState?>(null) }

    val points = listOf(
        GeoPointImpl.fromLongLat(23.66, 56.42),
        GeoPointImpl.fromLongLat(13.39, 2.95),
        GeoPointImpl.fromLongLat(-87.82, 38.58),
        GeoPointImpl.fromLongLat(23.66, 56.42),
    )

    val polylineState = remember {
        PolygonState(
            points = points,
            strokeColor = Color.Yellow.copy(alpha = 0.3f),
            strokeWidth = 3.dp,
            fillColor = Color.Green.copy(alpha = 0.5f),
            geodesic = false,
            zIndex = 0,
        )
    }

    val geodesicPolylineState = remember {
        PolygonState(
            points = points,
            strokeColor = Color.Red.copy(alpha = 0.3f),
            strokeWidth = 3.dp,
            fillColor = Color.Blue.copy(alpha = 0.5f),
            geodesic = true,
            zIndex = 1,
        )
    }

    HereMapView(
        modifier = modifier,
        state = state,
        onPolygonClick = { event ->
            clickedPosition =
                MarkerState(
                    id = "clicked_position",
                    position = event.clicked,
                    icon = DefaultIcon(
                        fillColor = event.state.fillColor.copy(alpha = 1.0f),
                    ),
                )
        },
    ) {
        clickedPosition?.let {
            Marker(it)
        }

        Polygon(polylineState)
        Polygon(geodesicPolylineState)
    }
}

@Composable
fun MapView2(modifier: Modifier = Modifier) {
    val state =
        rememberMapLibreMapViewState(
            cameraPosition =
                MapCameraPositionImpl(
                    position = GeoPointImpl.fromLatLong(21.382314, -157.933097),
                    zoom = 5.0,
                ),
        )
    val polylinePoints =
        mutableListOf(
            GeoPointImpl.fromLatLong(35.548852, 139.784086), // HND_AIR_PORT
            GeoPointImpl.fromLatLong(37.615223, -122.389979), // SFO_AIR_PORT
            GeoPointImpl.fromLatLong(21.324513, -157.925074), // HNL_AIR_PORT
        )

    val polylineState =
        remember {
            PolylineState(
                id = "example_polyline",
                points = polylinePoints,
                strokeColor = Color.Red,
                strokeWidth = 4.dp,
                geodesic = true,
            )
        }
    var clickedPosition by remember { mutableStateOf<GeoPoint?>(null) }

    MapLibreMapView(
        modifier = modifier,
        state = state,
        onPolylineClick = { event ->
            clickedPosition = event.clicked
        },
    ) {
        clickedPosition?.let {
            Marker(
                position = it,
                icon = DefaultIcon(scale = 2.0f),
                draggable = true,
            )
        }

        Polyline(polylineState)
    }
}
