package com.mapconductor.example.pages.infobubble

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import com.mapconductor.compose.info.InfoBubbleCustom
import com.mapconductor.compose.marker.Marker
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.marker.DefaultMarkerIcon
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.example.MapViewContainer
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold

@Composable
fun StyledInfoBubblePage(onToggleSidebar: () -> Unit = {}) {
    val initCameraPosition =
        MapCameraPosition(
            position = GeoPoint.fromLatLong(37.7849, -122.4094),
            zoom = 15.0,
        )
    var mapViewState by remember { mutableStateOf<MapViewStateInterface<Any>?>(null) }
    var selectedMarker by remember { mutableStateOf<MarkerState?>(null) }
    val onMarkerClick: OnMarkerEventHandler = { markerState ->
        selectedMarker = markerState
    }
    val onMarkerDragStart: OnMarkerEventHandler = { markerState ->
        println("マーカーのドラッグを開始: ${markerState.id}")
    }
    val onMarkerDrag: OnMarkerEventHandler = { markerState ->
        println("マーカーをドラッグ中: ${markerState.position}")
    }
    val onMarkerDragEnd: OnMarkerEventHandler = { markerState ->
        println("マーカーのドラッグが終了: ${markerState.id}")
    }

    val markerState1 by remember {
        mutableStateOf(
            MarkerState(
                id = "marker1",
                position = GeoPoint.fromLatLong(37.7749, -122.4194),
                icon =
                    DefaultMarkerIcon(
                        fillColor = Color.Blue,
                        infoAnchor = Offset(0.5f, 0.25f),
                        label = "1",
                    ),
                draggable = true,
                onClick = onMarkerClick,
                onDragStart = onMarkerDragStart,
                onDrag = onMarkerDrag,
                onDragEnd = onMarkerDragEnd,
            ),
        )
    }
    val markerState2 by remember {
        mutableStateOf(
            MarkerState(
                id = "marker2",
                position = GeoPoint.fromLatLong(37.7849, -122.4094),
                icon =
                    DefaultMarkerIcon(
                        fillColor = Color.Red,
                        infoAnchor = Offset(0.5f, 0.25f),
                        label = "2",
                    ),
                onClick = onMarkerClick,
            ),
        )
    }

    DemoMapPageScaffold(
        menuItems = DefaultMapViewItems(initCameraPosition),
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = { state ->
            @Suppress("UNCHECKED_CAST")
            mapViewState = state as MapViewStateInterface<Any>
        },
    ) {
        mapViewState?.let {
            MapViewContainer(
                modifier = Modifier.fillMaxSize(),
                state = mapViewState,
                onMapLoaded = {
                    selectedMarker = markerState1
                },
                onMapClick = { geoPoint ->
                    selectedMarker = null // 地図クリックで選択解除
                },
            ) {
                // インタラクティブなマーカーを持つ地図コンテンツ
                Marker(markerState1)
                Marker(markerState2)

                // 選択されたマーカーの情報を表示
                selectedMarker?.let { marker ->
                    val text = GeoPoint.from(marker.position).toUrlValue(6)
                    // 右側に尾を持つカスタム吹き出しの例
                    InfoBubbleCustom(
                        marker = marker,
                        tailOffset = Offset(0f, 0.5f), // attach at center-left of the bubble
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
                    // start at top-left (after tail area)
                    moveTo(tail + 2 * corner, 0f)
                    lineTo(width - 2 * corner, 0f)
                    // top-right corner
                    arcTo(
                        rect =
                            Rect(
                                topLeft = Offset(width - 2 * corner, 0f),
                                bottomRight = Offset(width, 2 * corner),
                            ),
                        startAngleDegrees = -90f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false,
                    )
                    // right edge
                    lineTo(width, height - 2 * corner)
                    // bottom-right corner
                    arcTo(
                        rect =
                            Rect(
                                topLeft = Offset(width - 2 * corner, height - 2 * corner),
                                bottomRight = Offset(width, height),
                            ),
                        startAngleDegrees = 0f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false,
                    )
                    // bottom edge towards left (before tail)
                    lineTo(tail + 2 * corner, height)
                    // bottom-left corner (before tail)
                    arcTo(
                        rect =
                            Rect(
                                topLeft = Offset(tail, height - 2 * corner),
                                bottomRight = Offset(tail + 2 * corner, height),
                            ),
                        startAngleDegrees = 90f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false,
                    )
                    // left edge up to tail bottom
                    lineTo(tail, height / 2 + tail / 2)
                    // Tail on left side
                    lineTo(0f, height / 2)
                    lineTo(tail, height / 2 - tail / 2)
                    // left edge up to top-left corner
                    lineTo(tail, 2 * corner)
                    // top-left corner
                    arcTo(
                        rect =
                            Rect(
                                topLeft = Offset(tail, 0f),
                                bottomRight = Offset(tail + 2 * corner, 2 * corner),
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
                        start = contentPadding + tailSize,
                        top = contentPadding,
                        bottom = contentPadding,
                        end = contentPadding,
                    ).wrapContentSize()
                    .clip(RoundedCornerShape(cornerRadius)),
        ) {
            content()
        }
    }
}
