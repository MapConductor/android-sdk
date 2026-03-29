package com.mapconductor.simplemapapp

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mapconductor.arcgis.map.ArcGISMapView
import com.mapconductor.arcgis.map.rememberArcGISMapViewState
import com.mapconductor.core.circle.Circle
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.groundimage.GroundImage
import com.mapconductor.core.groundimage.GroundImageState
import com.mapconductor.core.info.InfoBubble
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.marker.DefaultMarkerIcon
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.MarkerAnimation
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.Markers
import com.mapconductor.core.polygon.Polygon
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.core.polyline.Polyline
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.googlemaps.GoogleMapDesign
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val image = ContextCompat.getDrawable(this, R.drawable.overlayimg)!!

        setContent {
            MapConductorSDKTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TokyoExample(
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
val HND_AIR_PORT = GeoPoint.fromLatLong(35.548852, 139.784086)
val SFO_AIR_PORT = GeoPoint.fromLatLong(37.615223, -122.389979)
val HNL_AIR_PORT = GeoPoint.fromLatLong( 21.324513, -157.925074)
val airpots = listOf(
    HND_AIR_PORT,
    HNL_AIR_PORT,
    SFO_AIR_PORT
)


val goryokaku = listOf(
    GeoPoint.fromLatLong(41.79883, 140.75675),
    GeoPoint.fromLatLong(41.799240000000005, 140.75875000000002),
    GeoPoint.fromLatLong(41.797650000000004, 140.75905),
    GeoPoint.fromLatLong(41.79637, 140.76018000000002),
    GeoPoint.fromLatLong(41.79567, 140.75845),
    GeoPoint.fromLatLong(41.794470000000004, 140.75714000000002),
    GeoPoint.fromLatLong(41.795010000000005, 140.75611),
    GeoPoint.fromLatLong(41.79477000000001, 140.75484),
    GeoPoint.fromLatLong(41.79576, 140.75475),
    GeoPoint.fromLatLong(41.796150000000004, 140.75364000000002),
    GeoPoint.fromLatLong(41.79744, 140.75454000000002),
    GeoPoint.fromLatLong(41.79909000000001, 140.75465),
    GeoPoint.fromLatLong(41.79883, 140.75673),
)

@Composable
fun TokyoExample(modifier: Modifier = Modifier) {
    var selectedMarker by remember { mutableStateOf<MarkerState?>(null) }

    val center = GeoPoint(
        latitude = 35.6762,
        longitude = 139.6503,
    )

    val mapViewState =
        rememberHereMapViewState(
            cameraPosition =
                MapCameraPosition(
                    position = center,
                    zoom = 2.0,
                ),
        )

    val markerState = remember { MarkerState(
        position = center,
        icon = DefaultMarkerIcon().copy(
            label = "Tokyo",
        ),
        onClick = {
            selectedMarker = it
        }
    ) }

    HereMapView(
        state = mapViewState,
        modifier = modifier,
    ) {
        Marker(markerState)

        selectedMarker?.let {
            InfoBubble(
                marker = it,
            ) {
                Text("Hello, world!")
            }
        }
    }
}

@Composable
fun MapviewExample(modifier: Modifier = Modifier) {
    val initCameraPosition = MapCameraPosition(
        position = GeoPoint(37.422198, -122.085377),
        zoom = 18.0,
        tilt = 60.0,
        bearing = 30.0,
    )
    val mapViewState = rememberGoogleMapViewState(
        cameraPosition = initCameraPosition,
    )

    GoogleMapView(
        state = mapViewState,
        modifier = modifier,
    ) {
    }
}

@Composable
fun MarkerExample(modifier: Modifier = Modifier) {
    var selectedMarker by remember { mutableStateOf<MarkerState?>(null) }

    val initCameraPosition = MapCameraPosition(
        position = GeoPoint(37.422198, -122.085377),
        zoom = 18.0,
    )
    val mapViewState = rememberGoogleMapViewState(
        cameraPosition = initCameraPosition,
    )
    val markerState = remember { MarkerState(
        position = GeoPoint(37.422198, -122.085377),
        icon = DefaultMarkerIcon().copy(
            label = "GoogleMaps"
        ),
        onClick = {
            selectedMarker = it
        },
        ) }

    GoogleMapView(
        state = mapViewState,
        modifier = modifier,
    ) {
//        selectedMarker?.let {
//            InfoBubble(
//                marker = it,
//            ) {
//                Text("Hello, world!")
//            }
//        }

        Marker(markerState)
    }
}

@Composable
fun PolylineExample(modifier: Modifier = Modifier) {
    val initCameraPosition = MapCameraPosition(
        position = GeoPoint(41.79,140.75),
        zoom = 3.0,
    )
    val mapViewState = rememberGoogleMapViewState(
        cameraPosition = initCameraPosition,
    )

    val polylineState = remember { PolylineState(
        points = airpots,
        strokeColor = Color.Blue.copy(alpha = 0.5f),
        strokeWidth = 4.dp,
        geodesic = true,
    ) }

    GoogleMapView(mapViewState) {
        Polyline(polylineState)
    }
}
@Composable
fun CirleExample(modifier: Modifier = Modifier) {
    val initCameraPosition = MapCameraPosition(
        position = GeoPoint(37.422198, -122.085377),
        zoom = 17.0,
    )
    val mapViewState = rememberGoogleMapViewState(
        cameraPosition = initCameraPosition,
    )
    val circleState = remember { CircleState(
        center = GeoPoint(37.422198, -122.085377),
        radiusMeters = 50.0,
        fillColor = Color.Blue.copy(alpha = 0.5f),
    )
    }

    GoogleMapView(
        state = mapViewState,
        modifier = modifier,
    ) {
        Circle(circleState)
    }
}

@Composable
fun PolygonExample(modifier: Modifier = Modifier) {
    val initCameraPosition = MapCameraPosition(
        position = GeoPoint(41.79,140.75),
        zoom = 14.0,
//        tilt = 60.0,
//        bearing = 30.0,
    )
    val mapViewState = rememberHereMapViewState(
        cameraPosition = initCameraPosition,
    )

    val polygonState = remember { PolygonState(
        points = goryokaku,
        strokeColor = Color.Blue.copy(alpha = 0.5f),
        fillColor =  Color.Red.copy(alpha = 0.7f),
    ) }

    HereMapView(mapViewState) {
        Polygon(polygonState)
    }
}

@Composable
fun GroundImageExample(modifier: Modifier = Modifier, image: Drawable) {
    val initCameraPosition = MapCameraPosition(
        position = GeoPoint(51.511649,-0.100761),
        zoom = 11.0,
    )
    val mapViewState = rememberArcGISMapViewState(
        cameraPosition = initCameraPosition,
    )

    val groundImageState = remember { GroundImageState(
        bounds = GeoRectBounds(
            southWest = GeoPoint.fromLatLong(51.476747, -0.167729),
            northEast = GeoPoint.fromLatLong(51.546550,-0.033792),
        ),
        image = image,
        opacity = 0.5f,
    )
    }

    ArcGISMapView(
        state = mapViewState,
        modifier = modifier,
    ) {
        GroundImage(groundImageState)
    }
}

@Composable
fun HolePolygonExample(modifier: Modifier = Modifier) {
    val mapViewState =
        rememberMapLibreMapViewState(
            cameraPosition =
                MapCameraPosition(
                    position = GeoPoint(43.06050568387817, 141.35374551567804),
                    zoom = 11.0,
                ),
        )

    val polygonState =
        remember {
            PolygonState(
                points =
                    listOf(
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
                holes =
                    listOf(
                        listOf( // 1
                            GeoPoint(43.10086924222251, 141.35290903949243),
                            GeoPoint(43.04444342582366, 141.4118953480885),
                            GeoPoint(43.05060149394299, 141.30656265416695),
                        ),
                        listOf( // 2
                            GeoPoint(43.06035050410283, 141.31990479539704),
                            GeoPoint(43.038284739487004, 141.33324693662706),
                            GeoPoint(43.049062034871525, 141.28690055130158),
                        ),
                    ),
                fillColor = Color(0xCC787880),
                strokeColor = Color.Red,
                strokeWidth = 2.dp,
            )
        }

//    val polygonState2 = remember { tokyoPolygonState }

    MapLibreMapView(
        state = mapViewState,
        modifier = modifier,
    ) {
        Polygon(polygonState)
//        Polygon(polygonState2)
    }
}

// @Composable
// fun BasicGeoJSONExample(
//    modifier: Modifier = Modifier,
// ) {
//    val context = LocalContext.current
//    val mapViewState =
//        rememberMapLibreMapViewState(
//            cameraPosition =
//                MapCameraPosition(
//                    position = GeoPoint(35.691153, 139.756878),
//                    zoom = 10.0,
//                )
//        )
// //    val geoJson =
// //        remember {
// //            // A small sample with MultiPoint, MultiLineString and MultiPolygon (lon/lat order).
// //            """
// //            {
// //              "type": "FeatureCollection",
// //              "features": [
// //                {
// //                  "type": "Feature",
// //                  "id": "multipoint",
// //                  "properties": { "name": "MultiPoint sample" },
// //                  "geometry": {
// //                    "type": "MultiPoint",
// //                    "coordinates": [
// //                      [139.76669, 35.68049],
// //                      [139.75688, 35.69115],
// //                      [139.78100, 35.67350]
// //                    ]
// //                  }
// //                },
// //                {
// //                  "type": "Feature",
// //                  "id": "multiline",
// //                  "properties": { "name": "MultiLineString sample" },
// //                  "geometry": {
// //                    "type": "MultiLineString",
// //                    "coordinates": [
// //                      [[139.74, 35.69], [139.77, 35.69], [139.80, 35.69]],
// //                      [[139.76, 35.66], [139.76, 35.68], [139.76, 35.70]]
// //                    ]
// //                  }
// //                },
// //                {
// //                  "type": "Feature",
// //                  "id": "multipolygon",
// //                  "properties": { "name": "MultiPolygon sample" },
// //                  "geometry": {
// //                    "type": "MultiPolygon",
// //                    "coordinates": [
// //                      [
// //                        [
// //                          [139.7500, 35.6750],
// //                          [139.7800, 35.6750],
// //                          [139.7800, 35.6950],
// //                          [139.7500, 35.6950],
// //                          [139.7500, 35.6750]
// //                        ]
// //                      ]
// //                    ]
// //                  }
// //                }
// //              ]
// //            }
// //            """.trimIndent()
// //        }
//    val geoJsonInputStream = remember {
//        context.assets.open("prefectures.geojson")
//    }
//
//    MapLibreMapView(
//        modifier = modifier,
//        state = mapViewState,
//        markerTiling = MarkerTilingOptions.Default.copy(
//            minMarkerCount = 0,
//            debugTileOverlay = true,
//        ),
//    ) {
//        GeoJsonLayer(
//            geoJsonInputStream = geoJsonInputStream,
//            geoJsonKey = geoJsonInputStream.hashCode(),
//            style =
//                GeoJsonLayerStyle(
//                    polylineStrokeColor = Color(0xFF1565C0),
//                    polylineStrokeWidth = 3.dp,
//                    polygonStrokeColor = Color(0xFF2E7D32),
//                    polygonStrokeWidth = 2.dp,
//                    polygonFillColor = Color(0x332E7D32),
//                ),
//        )
//    }
// }

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

    val camera =
        remember {
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

//    HereMapView(
//        state = mapViewState,
//        modifier = modifier,
//    ) {
//        HeatmapOverlay(
//            trackPointUpdates = false,
//        ) {
//            HeatmapPoints(points)
//        }
//    }
}
