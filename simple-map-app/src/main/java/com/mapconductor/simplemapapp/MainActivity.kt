package com.mapconductor.simplemapapp

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.MapStyleOptions
import com.here.sdk.mapview.MapScheme
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.viewport
import com.mapconductor.arcgis.ArcGISMapViewHolder
import com.mapconductor.arcgis.map.ArcGISMapViewStateImpl
import com.mapconductor.arcgis.map.rememberArcGISMapViewState
import com.mapconductor.core.circle.Circle
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.info.InfoBubbleCustom
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.map.VisibleRegion
import com.mapconductor.core.marker.DefaultIcon
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polygon.Polygon
import com.mapconductor.googlemaps.GoogleMapView
import com.mapconductor.googlemaps.rememberGoogleMapViewState
import com.mapconductor.here.HereMapView
import com.mapconductor.here.rememberHereMapViewState
import com.mapconductor.mapbox.MapboxMapView
import com.mapconductor.mapbox.MapboxMapViewHolder
import com.mapconductor.mapbox.rememberMapboxMapViewState
import com.mapconductor.maplibre.MapLibreDesignType
import com.mapconductor.maplibre.MapLibreMapView
import com.mapconductor.maplibre.rememberMapLibreMapViewState
import com.mapconductor.simplemapapp.ui.theme.MapConductorSDKTheme
import java.lang.ref.WeakReference
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.delay

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
    // 地図のカメラ位置
    val mapViewState = rememberHereMapViewState(
        cameraPosition = MapCameraPositionImpl(
            position = GeoPointImpl.fromLatLong(35.6812, 139.7671),
            zoom = 12.0
        ),
    )

    // mutableStateOfにすることで、mapSchemeが変化したら 再描画
    var mapScheme by remember { mutableStateOf(MapScheme.NORMAL_DAY) }

    // ViewHolderの取得
    val hereMapViewHolder = mapViewState.getMapViewHolder()
    LaunchedEffect(hereMapViewHolder, mapScheme) {
        if (hereMapViewHolder == null) return@LaunchedEffect
        hereMapViewHolder.map.loadScene(mapScheme) { errorCode ->
            if (errorCode != null) {
                Log.e("HERE", "Failed to load map scene: ${errorCode.name}")
            }
        }
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Spacer(modifier = Modifier.size(20.dp))

            // 通常の地図表示
            Button(onClick = {
                mapScheme = MapScheme.NORMAL_DAY
            }) {
                Text(
                    text = "Normal"
                )
            }
            Spacer(modifier = Modifier.size(20.dp))

            // 衛星画像表示
            Button(onClick = {
                mapScheme = MapScheme.SATELLITE
            }) {
                Text(
                    text = "Satellite"
                )
            }
        }

        HereMapView(
            state = mapViewState,
            modifier = Modifier.fillMaxSize(),
        ) {}
    }
}
