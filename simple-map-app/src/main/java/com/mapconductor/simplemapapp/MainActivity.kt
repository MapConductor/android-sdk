package com.mapconductor.simplemapapp

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.arcgismaps.toolkit.authentication.AuthenticatorState
import com.here.sdk.core.Point2D
import com.here.sdk.core.Rectangle2D
import com.here.sdk.core.Size2D
import com.here.sdk.mapview.MapScene
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polyline.Polyline
import com.mapconductor.core.polygon.Polygon
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.googlemaps.GoogleMapView
import com.mapconductor.googlemaps.GoogleMapViewState
import com.mapconductor.googlemaps.rememberGoogleMapViewState
import com.mapconductor.here.HereMapView
import com.mapconductor.here.rememberHereMapViewState
import com.mapconductor.maplibre.MapLibreMapView
import com.mapconductor.maplibre.rememberMapLibreMapViewState
import com.mapconductor.simplemapapp.ui.theme.MapConductorSDKTheme
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

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
fun MapLibre() {
    val center = GeoPointImpl.fromLatLong(52.5163, 13.3777)

    val camera = MapCameraPositionImpl(position = center, zoom = 13.0)
val mapViewState = rememberHereMapViewState(cameraPosition = camera)

var selectedMarker by remember { mutableStateOf<MarkerState?>(null) }

HereMapView(
    state = mapViewState,
    onMarkerClick = { markerState -> selectedMarker = markerState }
) {
    val markerState = MarkerState(
        position = center,
        id = "my-marker"
    )
    Marker(
        markerState
    )
}
}

@Composable
fun BasicMapExample(modifier: Modifier = Modifier) {
    val mapViewState = rememberHereMapViewState()
    var polygons by remember { mutableStateOf<List<PolygonState>>(emptyList()) }

    HereMapView(
        state = mapViewState,
        onMapClick = { clicked ->
            mapViewState.getMapViewHolder()?.let { holder ->
                val screenXY = holder.toScreenOffset(clicked)!!
                val leftTop = holder.fromScreenOffsetSync(
                    Offset(
                        screenXY.x - 10.0f,
                        screenXY.y - 10.0f,
                    )
                )!!
                val rightTop = holder.fromScreenOffsetSync(
                    Offset(
                        screenXY.x + 20.0f,
                        screenXY.y - 10.0f,
                    )
                )!!
                val rightBottom = holder.fromScreenOffsetSync(
                    Offset(
                        screenXY.x + 20.0f,
                        screenXY.y + 20.0f,
                    )
                )!!
                val leftBottom = holder.fromScreenOffsetSync(
                    Offset(
                        screenXY.x - 10.0f,
                        screenXY.y + 20.0f,
                    )
                )!!
                polygons = polygons + PolygonState(
                    id = "polygon-${clicked.hashCode()}",
                    points = listOf(
                        leftTop,
                        rightTop,
                        rightBottom,
                        leftBottom,
                    )
                )

                val viewarea = Rectangle2D(
                        Point2D((screenXY.x - 10.0).toDouble(),(screenXY.y - 10.0).toDouble()),
                        Size2D(10.0, 10.0)
                    )

                holder.mapView.pick(null, viewarea) { pickResult ->
                    pickResult?.let { result ->
                        result.mapContent?.pickedPlaces?.forEach {
                            println("categoryId: ${it.placeCategoryId}, name: ${it.name}")
                        }
                    }
                }
            }
        }
    ) {

    }
}


