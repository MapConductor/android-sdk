package com.mapconductor.example.pages.map.tilt

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Card
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mapconductor.core.polygon.Polygon
import com.mapconductor.example.MapViewContainer
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold
import com.mapconductor.example.ui.MessageCard
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.tan
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TiltMapPage(onToggleSidebar: () -> Unit = {}) {

    val coroutineScope = rememberCoroutineScope()
    val debounceMs = 80L
    val viewModel = remember { TiltMapPageViewModel() }

    val tilt = viewModel.tilt

    DemoMapPageScaffold(
        menuItems = DefaultMapViewItems(viewModel.initCameraPosition),
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = viewModel::onMapViewChanged,
    ) { paddingValues ->
        val mapViewState = viewModel.mapViewState.collectAsState().value

        mapViewState?.let {
            MapViewContainer(
                state = mapViewState,
            )
        }

        // Message Card
        Card(
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        bottom = paddingValues.calculateBottomPadding() + 16.dp,
                        start = paddingValues.calculateStartPadding(LayoutDirection.Ltr) + 16.dp,
                        end = paddingValues.calculateEndPadding(LayoutDirection.Ltr) + 16.dp,
                    )
                    .sizeIn(
                        maxWidth = 600.dp,
                    ),
        ) {
            Column(modifier = Modifier.padding(6.dp)) {
                TiltCameraDiagram(
                    tilt = tilt,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row {
                    Text("tilt: ${"%.2f".format(tilt)}")
                    Slider(
                        value = viewModel.tilt.toFloat(),
                        onValueChange = { newValue ->
                            coroutineScope.launch {
                                delay(debounceMs)
                                viewModel.tilt = newValue.toDouble()
                            }
                        },
                        onValueChangeFinished = {
                        },
                        valueRange = -89.0f..89.0f, // スライダー範囲
                        steps = 0,
                    )
                }
            }
        }
    }
}

@Composable
private fun TiltCameraDiagram(
    tilt: Double,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val groundY = height * 0.78f
        val originX = width * 0.5f
        val originY = groundY
        val baseCameraY = height * 0.22f
        val maxTilt = 90.0
        val tiltAbs = abs(tilt).coerceIn(0.0, maxTilt)
        val tiltRad = (tiltAbs * PI / 180.0).toFloat()
        val altitudePx = groundY - baseCameraY
        val targetDistance =
            (altitudePx * tan(tiltRad))
                .coerceAtMost(width * 0.44f)
        val targetX = if (tilt < 0.0) originX - targetDistance else originX
        val targetY = groundY
        val cameraX = if (tilt > 0.0) originX + targetDistance else originX
        val cameraY = baseCameraY
        val sightEndX =
            if (tilt == 0.0) {
                cameraX
            } else {
                targetX
            }
        val sightEndY = targetY

        drawLine(
            color = Color(0xFFE4E0EC),
            start = Offset(width * 0.08f, groundY),
            end = Offset(width * 0.94f, groundY),
            strokeWidth = 3f,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = Color(0xFF8E879A),
            start = Offset(cameraX, cameraY),
            end = Offset(cameraX, groundY),
            strokeWidth = 2f,
        )
        drawCircle(
            color = Color(0xFF5DA7FF),
            radius = 8f,
            center = Offset(cameraX, cameraY),
        )
        drawCircle(
            color = Color(0xFFFF6259),
            radius = 7f,
            center = Offset(targetX, targetY),
        )
        drawLine(
            color = Color(0xFFFFC857),
            start = Offset(cameraX, cameraY),
            end = Offset(sightEndX, sightEndY),
            strokeWidth = 4f,
            cap = StrokeCap.Round,
        )

        val cameraBody =
            Path().apply {
                moveTo(cameraX - 12f, cameraY - 8f)
                lineTo(cameraX + 14f, cameraY - 4f)
                lineTo(cameraX + 10f, cameraY + 10f)
                lineTo(cameraX - 12f, cameraY + 8f)
                close()
            }
        drawPath(cameraBody, Color(0xFF2F2A38))
        drawPath(cameraBody, Color.White.copy(alpha = 0.7f), style = Stroke(width = 1.5f))

        drawCircle(
            color = Color(0xFF8E879A),
            radius = 3.5f,
            center = Offset(originX, originY),
        )
        drawLine(
            color = Color(0xFFB8AFCA),
            start = Offset(minOf(cameraX, targetX), groundY + 12f),
            end = Offset(maxOf(cameraX, targetX), groundY + 12f),
            strokeWidth = 2f,
            cap = StrokeCap.Round,
        )
    }
}
