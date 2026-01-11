package com.mapconductor.simplemapapp

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Scaffold
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.arcgismaps.mapping.ArcGISMap
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
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.core.spherical.Spherical
import com.mapconductor.example.pages.marker.postoffice.TokyoPostOffices
import com.mapconductor.googlemaps.GoogleMapView
import com.mapconductor.googlemaps.rememberGoogleMapViewState
import com.mapconductor.heatmap.HeatmapOverlay
import com.mapconductor.heatmap.HeatmapPoint
import com.mapconductor.here.HereMapView
import com.mapconductor.here.rememberHereMapViewState
import com.mapconductor.maplibre.MapLibreDesign
import com.mapconductor.maplibre.MapLibreMapView
import com.mapconductor.maplibre.rememberMapLibreMapViewState
import com.mapconductor.simplemapapp.ui.theme.MapConductorSDKTheme
import android.os.SystemClock
import android.os.Bundle
import android.util.Log
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val heatmapLogs = remember { HeatmapDebugLogBuffer() }
            MapConductorSDKTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = {
                        Column {
                            FloatingActionButton(
                                onClick = { heatmapLogs.dumpToLogcat() },
                            ) {
                                Text("Dump logs")
                            }
                            Spacer(Modifier.height(12.dp))
                            FloatingActionButton(
                                onClick = { heatmapLogs.clear() },
                            ) {
                                Text("Clear")
                            }
                        }
                    },
                ) { innerPadding ->
                    GoogleMapHeatmapExample(
                        modifier =
                            Modifier
                                .padding(innerPadding)
                                .fillMaxSize(),
                        heatmapLogs = heatmapLogs,
                    )
                }
            }
        }
    }
}

@Composable
fun GoogleMapHeatmapExample(
    modifier: Modifier = Modifier,
    heatmapLogs: HeatmapDebugLogBuffer,
) {
    val center = GeoPoint.fromLatLong(35.681236, 139.767125)
    val mapViewState =
        rememberArcGISMapViewState(
            cameraPosition =
                MapCameraPosition(
                    position = center,
                    zoom = 11.0,
                ),
        )

    ArcGISMapView(
        state = mapViewState,
        modifier = modifier,
    ) {
        key(Unit) {
            HeatmapOverlay(
                tileSize = 512,
                debugLogger = heatmapLogs::add,
            ) {
                TokyoPostOffices.mapIndexed { index, postOffice ->
                    HeatmapPoint(
                        position = postOffice.position,
                        weight = 1.0,
                    )
                }
            }
        }
    }
}

class HeatmapDebugLogBuffer(
    private val maxLines: Int = 4000,
) {
    private val lock = Any()
    private val lines: MutableList<String> = mutableListOf()

    fun add(message: String) {
        val line = "${SystemClock.elapsedRealtime()} $message"
        synchronized(lock) {
            if (lines.size >= maxLines) {
                // Drop oldest to bound memory.
                lines.removeAt(0)
            }
            lines.add(line)
        }
    }

    fun clear() {
        synchronized(lock) {
            lines.clear()
        }
    }

    fun dumpToLogcat(tag: String = "HeatmapDebug") {
        val snapshot =
            synchronized(lock) {
                lines.toList()
            }
        Log.i(tag, "---- dump start (${snapshot.size} lines) ----")
        snapshot.forEach { Log.i(tag, it) }
        Log.i(tag, "---- dump end ----")
    }
}

@Composable
fun MapLibre(modifier: Modifier = Modifier) {
    val center = GeoPoint.fromLatLong(52.5163, 13.3777)

    val camera = MapCameraPosition(position = center, zoom = 13.0)
    val mapViewState = rememberHereMapViewState(cameraPosition = camera)

    var selectedMarker by remember { mutableStateOf<MarkerState?>(null) }

    HereMapView(
        state = mapViewState,
        modifier = modifier,
    ) {
        val markerState =
            MarkerState(
                position = center,
                id = "my-marker",
                onClick = { markerState -> selectedMarker = markerState },
            )
        Marker(
            markerState,
        )
    }
}

@Composable
fun BasicMapExample(modifier: Modifier = Modifier) {
    val mapViewState = rememberHereMapViewState()
    var polygons by remember { mutableStateOf<List<PolygonState>>(emptyList()) }

    HereMapView(
        state = mapViewState,
        modifier = modifier,
        onMapClick = { clicked ->
            mapViewState.getMapViewHolder()?.let { holder ->
                val screenXY = holder.toScreenOffset(clicked)!!
                val leftTop =
                    holder.fromScreenOffsetSync(
                        Offset(
                            screenXY.x - 10.0f,
                            screenXY.y - 10.0f,
                        ),
                    )!!
                val rightTop =
                    holder.fromScreenOffsetSync(
                        Offset(
                            screenXY.x + 20.0f,
                            screenXY.y - 10.0f,
                        ),
                    )!!
                val rightBottom =
                    holder.fromScreenOffsetSync(
                        Offset(
                            screenXY.x + 20.0f,
                            screenXY.y + 20.0f,
                        ),
                    )!!
                val leftBottom =
                    holder.fromScreenOffsetSync(
                        Offset(
                            screenXY.x - 10.0f,
                            screenXY.y + 20.0f,
                        ),
                    )!!
                polygons = polygons +
                    PolygonState(
                        id = "polygon-${clicked.hashCode()}",
                        points =
                            listOf(
                                leftTop,
                                rightTop,
                                rightBottom,
                                leftBottom,
                            ),
                    )

                val viewarea =
                    Rectangle2D(
                        Point2D((screenXY.x - 10.0).toDouble(), (screenXY.y - 10.0).toDouble()),
                        Size2D(10.0, 10.0),
                    )

                holder.mapView.pick(null, viewarea) { pickResult ->
                    pickResult?.let { result ->
                        result.mapContent?.pickedPlaces?.forEach {
                            println("categoryId: ${it.placeCategoryId}, name: ${it.name}")
                        }
                    }
                }
            }
        },
    ) {
    }
}

@Composable
fun MarkerAnimationExample(modifier: Modifier = Modifier) {
    val startPosition = GeoPoint.fromLatLong(37.775111, -122.419206)
    val endPosition = GeoPoint.fromLatLong(37.780522, -122.412522)

    var markerState by remember {
        mutableStateOf(
            MarkerState(
                position = startPosition,
                icon = DefaultMarkerIcon(fillColor = Color.Green, label = "移動中"),
                extra = "アニメーションするマーカー",
            ),
        )
    }

    LaunchedEffect(Unit) {
        val path =
            (0..10)
                .map { it * 0.1 }
                .map {
                    Spherical.sphericalInterpolate(
                        from = startPosition,
                        to = endPosition,
                        fraction = it,
                    )
                }

        var direction = 1
        var idx = 0
        while (true) {
            delay(1000)
            for (i in 0..path.size - 2) {
                idx += direction
                markerState.position = path[idx]
                println("$idx : ${GeoPoint.from(path[idx]).toUrlValue()}")
                delay(50)
            }
            direction = direction * -1
        }
    }
    val mapViewState =
        rememberMapLibreMapViewState(
            cameraPosition =
                MapCameraPosition(
                    position = GeoPoint.fromLatLong(37.7791, -122.4144),
                    zoom = 15.0,
                ),
            mapDesign = MapLibreDesign.OsmBrightEn,
        )

    MapLibreMapView(
        modifier = modifier,
        state = mapViewState,
    ) {
        Marker(markerState)
    }
}

@Composable
fun GoogleMapStrategyMarkerExample(
    modifier: Modifier = Modifier,
    postOfficeIcon: ImageIcon,
) {
    val context = LocalContext.current
    val center = GeoPoint.fromLatLong(35.681236, 139.767125)
    val mapViewState =
        rememberGoogleMapViewState(
            cameraPosition =
                MapCameraPosition(
                    position = center,
                    zoom = 14.0,
                ),
        )

//    val markers =
//        remember {
//            TokyoPostOffices.map { it ->
//                MarkerState(
//                    position = it.position,
//                    id = it.hashCode().toString(),
//                    icon = postOfficeIcon,
//                    extra = it,
//                )
//            }
//        }

    GoogleMapView(
        state = mapViewState,
        modifier = modifier,
    ) {
    }
}
