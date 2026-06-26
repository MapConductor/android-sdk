package com.mapconductor.example.pages.circle

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.mapconductor.core.circle.Circle
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polygon.Polygon
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.core.spherical.Spherical
import com.mapconductor.example.MapViewContainer
import kotlin.math.max
import kotlin.math.roundToInt
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun CircleMapComponent(
    mapViewState: MapViewStateInterface<*>?,
    circleState: CircleState,
    centerMarker: MarkerState,
    edgeMarker: MarkerState,
    modifier: Modifier = Modifier,
) {
    var labelSize by remember { mutableStateOf(IntSize.Zero) }
    var labelPosition by remember { mutableStateOf<Offset?>(null) }
    val coroutine = rememberCoroutineScope()

    mapViewState?.let { state ->
        MapViewContainer(
            modifier = modifier,
            state = state,
            onCameraMove = {
                coroutine.launch {
                    mapViewState.getMapViewHolder()?.toScreenOffset(edgeMarker.position)?.let { screenOffset ->
                        labelPosition = screenOffset
                    }
                }
            }
        ) {
            // Circle
            Circle(circleState)

            // Radius line polygon from center (C) to edge (E)
            // Stable id prevents multiple polygons accumulating during rapid drag
            Polygon(
                PolygonState(
                    id = "circle-radius-line",
                    points = listOf(centerMarker.position, edgeMarker.position),
                    strokeColor = Color.Gray,
                    fillColor = Color.Transparent,
                ),
            )

            // Center marker (not draggable)
            Marker(centerMarker)

            // Edge marker (draggable)
            Marker(edgeMarker)

            LaunchedEffect(edgeMarker.position, mapViewState.cameraPosition) {
                mapViewState.getMapViewHolder()?.toScreenOffset(edgeMarker.position)?.let { screenOffset ->
                    labelPosition = screenOffset
                }
            }
            labelPosition?.let { pos ->
                Box(
                    modifier = Modifier
                        .onGloballyPositioned { labelSize = it.size }
                        .offset {
                            IntOffset(
                                x = pos.x.roundToInt(),
                                y = pos.y.roundToInt(),
                            )
                        },
                ) {
                    val labelText = "${circleState.radiusMeters.toInt()} m"
                    // White outline
                    BasicText(
                        text = labelText,
                        style = TextStyle(
                            color = Color.White,
                            drawStyle = Stroke(width = 6f),
                        ),
                    )
                    // Black fill
                    BasicText(
                        text = labelText,
                        style = TextStyle(color = Color.Black),
                    )
                }
            }
        }
    }
}
