package com.mapconductor.simplemapapp

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.here.sdk.core.Point2D
import com.here.sdk.core.Rectangle2D
import com.here.sdk.core.Size2D
import com.mapconductor.arcgis.map.ArcGISMapView
import com.mapconductor.arcgis.map.rememberArcGISMapViewState
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.marker.DefaultMarkerIcon
import com.mapconductor.core.marker.ImageIcon
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.MarkerAnimation
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.MarkerTilingOptions
import com.mapconductor.core.marker.Markers
import com.mapconductor.core.polygon.Polygon
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.core.polyline.Polyline
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.core.spherical.Spherical
import com.mapconductor.example.pages.marker.postoffice.TokyoPostOffices
import com.mapconductor.googlemaps.GoogleMapView
import com.mapconductor.googlemaps.rememberGoogleMapViewState
import com.mapconductor.heatmap.HeatmapOverlay
import com.mapconductor.heatmap.HeatmapPoints
import com.mapconductor.here.HereMapView
import com.mapconductor.here.rememberHereMapViewState
import com.mapconductor.mapbox.MapboxMapView
import com.mapconductor.mapbox.rememberMapboxMapViewState
import com.mapconductor.maplibre.MapLibreDesign
import com.mapconductor.maplibre.MapLibreMapView
import com.mapconductor.maplibre.rememberMapLibreMapViewState
import com.mapconductor.simplemapapp.postoffice.HeatmapLayerPageViewModel
import com.mapconductor.simplemapapp.postoffice.HeatmapLayerViewModelInterface
import com.mapconductor.simplemapapp.postoffice.PostOfficeDataLoader
import com.mapconductor.simplemapapp.ui.theme.MapConductorSDKTheme
import android.graphics.drawable.Drawable
import android.os.Bundle
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MapConductorSDKTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HeatmapExample(
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
fun BasicGroundImageExample(
    drawable: Drawable,
    modifier: Modifier = Modifier,
) {
    val mapViewState =
        rememberMapLibreMapViewState(
            cameraPosition =
                MapCameraPosition(
                    position = GeoPoint(35.691153, 139.756878),
                    zoom = 10.0,
                )
        )
    val markers = remember {
        TokyoPostOffices.map { MarkerState(
            position = it.position,
            icon = ImageIcon(
                image = drawable,
                scale = 0.3f,
            )
        ) }
    }

    MapLibreMapView(
        modifier = modifier,
        state = mapViewState,
        markerTiling = MarkerTilingOptions.Default.copy(
            minMarkerCount = 0,
            debugTileOverlay = true,
        ),
    ) {
        Markers(markers)
    }
}

@Composable
fun HeatmapExample(modifier: Modifier = Modifier) {

    val context = LocalContext.current
    val dataLoader = remember { PostOfficeDataLoader(context) }

    val viewModel: HeatmapLayerViewModelInterface =
        viewModel<HeatmapLayerPageViewModel>(
            factory =
                object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        if (modelClass.isAssignableFrom(HeatmapLayerPageViewModel::class.java)) {
                            @Suppress("UNCHECKED_CAST")
                            return HeatmapLayerPageViewModel(
                                dataLoader = dataLoader,
                            ) as T
                        }
                        throw IllegalArgumentException("Unknown ViewModel class")
                    }
                },
        )
    LaunchedEffect(Unit) {
        viewModel.loadPostOfficeData()
    }
    val points = viewModel.heatmapPoints.collectAsState().value

    val camera = remember {
        MapCameraPosition(
            position =
                GeoPoint.fromLatLong(
                    latitude = 35.68049,
                    longitude = 139.76669,
                ),
            zoom = 10.0,
            bearing = 0.0,
            tilt = 0.0,
            paddings = null,
        )
    }
    val mapViewState = rememberHereMapViewState(cameraPosition = camera)

    HereMapView(
        state = mapViewState,
        modifier = modifier,
    ) {
        HeatmapOverlay(
            trackPointUpdates = false,
        ) {
            HeatmapPoints(points)
        }
    }
}
