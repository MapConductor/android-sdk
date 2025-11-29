package com.mapconductor.simplemapapp

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import com.mapconductor.core.circle.Circle
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.info.InfoBubbleCustom
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.marker.DefaultIcon
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.googlemaps.GoogleMapView
import com.mapconductor.googlemaps.rememberGoogleMapViewState
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
    val center = GeoPointImpl.fromLatLong(37.7749, -122.4194)

    // 地図の作成
    val camera =
        MapCameraPositionImpl(
            position = center,
            zoom = 2.0,
        )
    val mapViewState =
        rememberGoogleMapViewState(
            cameraPosition = camera,
        )

    var circleState =
        remember {
            CircleState(
                id = "circle",
                center = center,
                radiusMeters = 5_000_000.0,
                strokeColor = Color.Red.copy(alpha = 0.3f),
                fillColor = Color.Red.copy(alpha = 0.5f),
                geodesic = true,
            )
        }

    var markerState =
        remember {
            MarkerState(
                id = "marker",
                position = center,
                draggable = true,
            )
        }

    // 動的な円を持つマップ
    // MapView を GoogleMapView、MapboxMapView などのマップ地図SDKに置き換えてください
    GoogleMapView(
        modifier = modifier,
        state = mapViewState,
        onMarkerDrag = { markerState ->
            circleState.center = markerState.position
            println("position = ${(markerState.position as GeoPointImpl).toUrlValue(6)}")
        },
    ) {
        Circle(circleState)

        // 中心マーカー
        Marker(markerState)
    }
}

@Composable
fun LeftInfoBubbleMapExample(modifier: Modifier = Modifier) {
    val center = GeoPointImpl.fromLatLong(37.7749, -122.4194)
    val mapViewState =
        rememberGoogleMapViewState(
            cameraPosition =
                MapCameraPositionImpl(
                    position = center,
                    zoom = 13.0,
                ),
        )
    var selectedMarker by remember { mutableStateOf<MarkerState?>(null) }

    val markerState1 by remember {
        mutableStateOf(
            MarkerState(
                id = "marker1",
                position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                icon =
                    DefaultIcon(
                        fillColor = Color.Blue,
                        label = "1",
                    ),
                draggable = true,
            ),
        )
    }
    val markerState2 by remember {
        mutableStateOf(
            MarkerState(
                id = "marker2",
                position = GeoPointImpl.fromLatLong(37.7849, -122.4094),
                icon =
                    DefaultIcon(
                        fillColor = Color.Red,
                        label = "2",
                    ),
            ),
        )
    }

    GoogleMapView(
        state = mapViewState,
        onMapLoaded = {
            println("Map loaded and ready")
        },
        onMapClick = { geoPoint ->
            selectedMarker = null // 地図クリックで選択解除
        },
        onMarkerClick = { markerState ->
            selectedMarker = markerState
        },
        onMarkerDragStart = { markerState ->
            println("Started dragging marker: ${markerState.id}")
        },
        onMarkerDrag = { markerState ->
            println("Dragging marker to: ${markerState.position}")
        },
        onMarkerDragEnd = { markerState ->
            println("Finished dragging marker: ${markerState.id}")
        },
    ) {
        // インタラクティブなマーカーを持つ地図コンテンツ
        Marker(markerState1)
        Marker(markerState2)

        // 選択されたマーカーの情報を表示
        selectedMarker?.let { marker ->
            val text = GeoPointImpl.from(marker.position).toUrlValue(6)
            // Example: custom bubble with right-side tail
            InfoBubbleCustom(
                marker = marker,
                tailOffset = Offset(1f, 0.5f), // attach at center-right of the bubble
            ) {
                RightTailInfoBubble(
                    bubbleColor = Color.White,
                    borderColor = Color.Black,
                ) {
                    Text(
                        text = text,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
fun MapView(modifier: Modifier = Modifier) {
    val state =
        rememberMapLibreMapViewState(
            cameraPosition =
                MapCameraPositionImpl(
                    position = GeoPointImpl.fromLatLong(21.324513, -157.925074),
                    zoom = 5.0,
                ),
        )

    val markerState =
        remember {
            MarkerState(
                position = GeoPointImpl.fromLatLong(21.324513, -157.925074),
                draggable = true,
            )
        }

    val circleState =
        remember {
            CircleState(
                id = "demo-circle",
                center = markerState.position,
                radiusMeters = 5000.0,
                strokeColor = Color.Magenta,
                strokeWidth = 2.dp,
                fillColor = Color.Cyan.copy(alpha = 0.3f),
                geodesic = true,
            )
        }

    MapLibreMapView(
        modifier = modifier,
        state = state,
        onMarkerDrag = { draggedState ->
            circleState.center = draggedState.position
        },
    ) {
        Marker(markerState)
        Circle(circleState)
    }
}

@Composable
private fun RightTailInfoBubble(
    bubbleColor: Color,
    borderColor: Color,
    contentPadding: androidx.compose.ui.unit.Dp = 8.dp,
    cornerRadius: androidx.compose.ui.unit.Dp = 4.dp,
    tailSize: androidx.compose.ui.unit.Dp = 8.dp,
    content: @Composable () -> Unit,
) {
    Box(modifier = Modifier.wrapContentSize()) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height
            val tail = tailSize.toPx()
            val corner = cornerRadius.toPx()

            val path =
                Path().apply {
                    moveTo(2 * corner, 0f)
                    lineTo(width - tail - 2 * corner, 0f)
                    // top-right corner (before tail)
                    arcTo(
                        rect =
                            Rect(
                                topLeft = Offset(width - tail - 2 * corner, 0f),
                                bottomRight = Offset(width - tail, 2 * corner),
                            ),
                        startAngleDegrees = -90f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false,
                    )
                    // Right edge to tail top
                    lineTo(width - tail, height / 2 - tail / 2)
                    // Tail
                    lineTo(width, height / 2)
                    lineTo(width - tail, height / 2 + tail / 2)
                    // Down to bottom-right corner before tail
                    lineTo(width - tail, height - 2 * corner)
                    // bottom-right corner
                    arcTo(
                        rect =
                            Rect(
                                topLeft = Offset(width - tail - 2 * corner, height - 2 * corner),
                                bottomRight = Offset(width - tail, height),
                            ),
                        startAngleDegrees = 0f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false,
                    )
                    // bottom edge
                    lineTo(2 * corner, height)
                    // bottom-left corner
                    arcTo(
                        rect =
                            Rect(
                                topLeft = Offset(0f, height - 2 * corner),
                                bottomRight = Offset(2 * corner, height),
                            ),
                        startAngleDegrees = 90f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false,
                    )
                    // left edge
                    lineTo(0f, 2 * corner)
                    // top-left corner
                    arcTo(
                        rect =
                            Rect(
                                topLeft = Offset(0f, 0f),
                                bottomRight = Offset(2 * corner, 2 * corner),
                            ),
                        startAngleDegrees = 180f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false,
                    )
                    close()
                }

            drawPath(path, color = bubbleColor, style = Fill)
            drawPath(path, color = borderColor, style = Stroke(width = 2f))
        }

        Box(
            modifier =
                Modifier
                    .padding(
                        start = contentPadding,
                        top = contentPadding,
                        bottom = contentPadding,
                        end = contentPadding + tailSize,
                    ).wrapContentSize()
                    .clip(RoundedCornerShape(cornerRadius)),
        ) {
            content()
        }
    }
}
