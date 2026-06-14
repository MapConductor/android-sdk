package com.mapconductor.simplemapapp

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.info.InfoBubble
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.marker.Marker
import com.mapconductor.geojson.GeoJSONFeature
import com.mapconductor.geojson.GeoJSONLayer
import com.mapconductor.geojson.GeoJSONLayerState
import com.mapconductor.geojson.GeoJSONParser
import com.mapconductor.maplibre.MapLibreMapView
import com.mapconductor.maplibre.rememberMapLibreMapViewState
import com.mapconductor.simplemapapp.ui.theme.MapConductorSDKTheme
import java.util.zip.ZipInputStream
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // val image = ContextCompat.getDrawable(this, R.drawable.overlayimg)!!

        setContent {
            MapConductorSDKTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SimpleMap(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

val HND_AIR_PORT = GeoPoint.fromLatLong(35.548852, 139.784086)
val SFO_AIR_PORT = GeoPoint.fromLatLong(37.615223, -122.389979)
val HNL_AIR_PORT = GeoPoint.fromLatLong(21.324513, -157.925074)
val airpots =
    listOf(
        HND_AIR_PORT,
        HNL_AIR_PORT,
        SFO_AIR_PORT,
    )

val goryokaku =
    listOf(
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
fun SimpleMap(modifier: Modifier) {
    val mapViewState =
        rememberMapLibreMapViewState(
            cameraPosition =
                MapCameraPosition(
                    position = GeoPoint(35.68, 139.77),
                    zoom = 13.0,
                ),
        )

    MapLibreMapView(
        state = mapViewState,
        modifier = modifier,
        onMapLoaded = {
            Log.d("SimpleMap", "---->loaded")
        },
    ) {
        Marker(
            position = GeoPoint(35.68, 139.77),
            onClick = {
                Log.d("SimpleMap", "--->clicked")
            },
        )
    }
}

@Composable
fun GeoJsonLayerExample(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val mapViewState =
        rememberMapLibreMapViewState(
            cameraPosition =
                MapCameraPosition(
                    position = GeoPoint(35.68, 139.77),
                    zoom = 13.0,
                ),
        )

    var selectedFeature by remember { mutableStateOf<GeoJSONFeature?>(null) }
    var tappedPosition by remember { mutableStateOf<GeoPoint?>(null) }

    val layerState =
        remember {
            GeoJSONLayerState(
                strokeColor = android.graphics.Color.argb(220, 30, 136, 229),
                fillColor = android.graphics.Color.argb(60, 30, 136, 229),
                strokeWidth = 1.5f,
                onClick = { feature, position ->
                    selectedFeature = feature
                    tappedPosition = GeoPoint.from(position)
                },
            )
        }

    var features by remember { mutableStateOf<List<GeoJSONFeature>>(emptyList()) }

    LaunchedEffect(Unit) {
        features =
            withContext(Dispatchers.IO) {
                ZipInputStream(context.assets.open("tokyo-6-areas.zip")).use { zis ->
                    zis.nextEntry ?: return@withContext emptyList()
                    GeoJSONParser.parseStream(zis)
                }
            }
    }

    MapLibreMapView(
        state = mapViewState,
        modifier = modifier,
        onMapClick = {
            if (!layerState.processClick(it)) {
                tappedPosition = null
                selectedFeature = null
            }
        },
    ) {
        GeoJSONLayer(state = layerState, features = features)

        tappedPosition?.let { pt ->
            InfoBubble(position = pt) {
                selectedFeature?.properties?.let {
                    TableView(it, 0.3f, 0.7f)
                }
            }
        }
    }
}
