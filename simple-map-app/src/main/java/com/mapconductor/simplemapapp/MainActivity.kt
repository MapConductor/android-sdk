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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.here.sdk.core.Point2D
import com.here.sdk.core.Rectangle2D
import com.here.sdk.core.Size2D
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.groundimage.GroundImage
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.marker.DefaultMarkerIcon
import com.mapconductor.core.marker.ImageIcon
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.core.spherical.Spherical
import com.mapconductor.googlemaps.GoogleMapView
import com.mapconductor.googlemaps.rememberGoogleMapViewState
import com.mapconductor.here.HereMapView
import com.mapconductor.here.rememberHereMapViewState
import com.mapconductor.mapbox.MapboxMapView
import com.mapconductor.mapbox.rememberMapboxMapViewState
import com.mapconductor.maplibre.MapLibreDesign
import com.mapconductor.maplibre.MapLibreMapView
import com.mapconductor.maplibre.rememberMapLibreMapViewState
import com.mapconductor.simplemapapp.ui.theme.MapConductorSDKTheme
import android.graphics.drawable.Drawable
import android.os.Bundle
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val drawable = ContextCompat.getDrawable(this, R.drawable.overlayimg)!!

        setContent {
            MapConductorSDKTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BasicGroundImageExample(
                        drawable = drawable,
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
        rememberMapboxMapViewState(
            cameraPosition =
                MapCameraPosition(
                    position = GeoPoint(51.511649,-0.100761),
                    zoom = 12.0,
                ),
        )

    val bounds = GeoRectBounds(
        southWest = GeoPoint.fromLatLong(51.476747, -0.167729),
        northEast = GeoPoint.fromLatLong(51.546550, -0.033792),
    )
    MapboxMapView(
        modifier = modifier,
        state = mapViewState,
    ) {
        GroundImage(
            id = "groundimage",
            bounds = bounds,
            image = drawable,
            opacity = 0.6f
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
