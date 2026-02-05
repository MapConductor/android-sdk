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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.geojson.GeoJsonLayer
import com.mapconductor.core.geojson.GeoJsonLayerStyle
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.marker.MarkerTilingOptions
import com.mapconductor.core.polygon.Polygon
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.heatmap.HeatmapOverlay
import com.mapconductor.heatmap.HeatmapPoints
import com.mapconductor.here.HereMapView
import com.mapconductor.here.rememberHereMapViewState
import com.mapconductor.maplibre.MapLibreMapView
import com.mapconductor.maplibre.rememberMapLibreMapViewState
import com.mapconductor.simplemapapp.postoffice.HeatmapLayerPageViewModel
import com.mapconductor.simplemapapp.postoffice.HeatmapLayerViewModelInterface
import com.mapconductor.simplemapapp.postoffice.PostOfficeDataLoader
import com.mapconductor.simplemapapp.ui.theme.MapConductorSDKTheme
import android.os.Bundle

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MapConductorSDKTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HolePolygonExample(
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
fun HolePolygonExample(
    modifier: Modifier = Modifier,
) {
    val mapViewState =
        rememberHereMapViewState(
            cameraPosition =
                MapCameraPosition(
                    position = GeoPoint(43.06050568387817, 141.35374551567804),
                    zoom = 11.0,
                )
        )

    val polygonState = remember {
        PolygonState(
            points = listOf(
                GeoPoint(85.0, 90.0),
                GeoPoint(85.0, 0.1),
                GeoPoint(85.0, -90.0),
                GeoPoint(85.0, -179.9),
                GeoPoint(0.0, -179.9),
                GeoPoint(-85.0, -179.9),
                GeoPoint(-85.0, -90.0),
                GeoPoint(-85.0, 0.1),
                GeoPoint(-85.0, 90.0),
                GeoPoint(-85.0, 179.9),
                GeoPoint(0.0, 179.9),
                GeoPoint(85.0, 179.9),
            ),
            holes = listOf(
                listOf( // 1
                    GeoPoint(43.10086924222251, 141.35290903949243),
                    GeoPoint(43.04444342582366, 141.4118953480885),
                    GeoPoint(43.05060149394299, 141.30656265416695),
                ),
                listOf( // 2
                    GeoPoint(43.06035050410283, 141.31990479539704),
                    GeoPoint(43.038284739487004, 141.33324693662706),
                    GeoPoint(43.049062034871525, 141.28690055130158),
                )
            ),
            fillColor = Color(0xCC787880),
            strokeColor = Color.Red,
            strokeWidth = 2.dp,
        )
    }

    val polygonState2 = remember { tokyoPolygonState }

    HereMapView(
        state = mapViewState,
        modifier = modifier,
    ) {
//        Polygon(polygonState)
        Polygon(polygonState2)
    }
}

@Composable
fun BasicGeoJSONExample(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val mapViewState =
        rememberMapLibreMapViewState(
            cameraPosition =
                MapCameraPosition(
                    position = GeoPoint(35.691153, 139.756878),
                    zoom = 10.0,
                )
        )
//    val geoJson =
//        remember {
//            // A small sample with MultiPoint, MultiLineString and MultiPolygon (lon/lat order).
//            """
//            {
//              "type": "FeatureCollection",
//              "features": [
//                {
//                  "type": "Feature",
//                  "id": "multipoint",
//                  "properties": { "name": "MultiPoint sample" },
//                  "geometry": {
//                    "type": "MultiPoint",
//                    "coordinates": [
//                      [139.76669, 35.68049],
//                      [139.75688, 35.69115],
//                      [139.78100, 35.67350]
//                    ]
//                  }
//                },
//                {
//                  "type": "Feature",
//                  "id": "multiline",
//                  "properties": { "name": "MultiLineString sample" },
//                  "geometry": {
//                    "type": "MultiLineString",
//                    "coordinates": [
//                      [[139.74, 35.69], [139.77, 35.69], [139.80, 35.69]],
//                      [[139.76, 35.66], [139.76, 35.68], [139.76, 35.70]]
//                    ]
//                  }
//                },
//                {
//                  "type": "Feature",
//                  "id": "multipolygon",
//                  "properties": { "name": "MultiPolygon sample" },
//                  "geometry": {
//                    "type": "MultiPolygon",
//                    "coordinates": [
//                      [
//                        [
//                          [139.7500, 35.6750],
//                          [139.7800, 35.6750],
//                          [139.7800, 35.6950],
//                          [139.7500, 35.6950],
//                          [139.7500, 35.6750]
//                        ]
//                      ]
//                    ]
//                  }
//                }
//              ]
//            }
//            """.trimIndent()
//        }
    val geoJsonInputStream = remember {
        context.assets.open("prefectures.geojson")
    }

    MapLibreMapView(
        modifier = modifier,
        state = mapViewState,
        markerTiling = MarkerTilingOptions.Default.copy(
            minMarkerCount = 0,
            debugTileOverlay = true,
        ),
    ) {
        GeoJsonLayer(
            geoJsonInputStream = geoJsonInputStream,
            geoJsonKey = geoJsonInputStream.hashCode(),
            style =
                GeoJsonLayerStyle(
                    polylineStrokeColor = Color(0xFF1565C0),
                    polylineStrokeWidth = 3.dp,
                    polygonStrokeColor = Color(0xFF2E7D32),
                    polygonStrokeWidth = 2.dp,
                    polygonFillColor = Color(0x332E7D32),
                ),
        )
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
