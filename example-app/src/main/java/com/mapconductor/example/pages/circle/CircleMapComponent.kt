package com.mapconductor.example.pages.circle

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.mapconductor.compose.circle.Circle
import com.mapconductor.compose.marker.Marker
import com.mapconductor.compose.polyline.Polyline
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.map.OnCameraMoveHandler
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.example.MapViewContainer

@Composable
fun CircleMapComponent(
    mapViewState: MapViewStateInterface<*>?,
    circleState: CircleState,
    centerMarker: MarkerState,
    edgeMarker: MarkerState,
    labelPosition: IntOffset?,
    modifier: Modifier = Modifier,
    onMapCameraMove: OnCameraMoveHandler = { },
) {
    var labelSize by remember { mutableStateOf(IntSize.Zero) }

    mapViewState?.let { state ->
        MapViewContainer(
            modifier = modifier,
            state = state,
            onCameraMove = onMapCameraMove,
        ) {
            // Circle
            Circle(circleState)

            // Radius line polygon from center (C) to edge (E)
            // Stable id prevents multiple polygons accumulating during rapid drag
            Polyline(
                PolylineState(
                    points = listOf(centerMarker.position, edgeMarker.position),
                    id = "circle-radius-line",
                    strokeColor = Color.White.toArgb(),
                    strokeWidth = 3f,
                ),
            )

            // Center marker (not draggable)
            Marker(centerMarker)

            // Edge marker (draggable)
            Marker(edgeMarker)

            Box(modifier = Modifier.fillMaxSize()) {
                labelPosition?.let { pos ->
                    Box(
                        modifier =
                            Modifier
                                .onGloballyPositioned { labelSize = it.size }
                                .offset {
                                    IntOffset(
                                        pos.x - labelSize.width / 2,
                                        pos.y - labelSize.height / 2,
                                    )
                                },
                    ) {
                        val labelText = "${circleState.radiusMeters.toInt()} m"
                        // White outline
                        BasicText(
                            text = labelText,
                            style =
                                TextStyle(
                                    color = Color.White,
                                    drawStyle = Stroke(width = 6f),
                                ),
                        )
                        // Black fill
                        BasicText(
                            text = labelText,
                            style = TextStyle(color = Color.Red),
                        )
                    }
                }
            }
        }
    }
}
