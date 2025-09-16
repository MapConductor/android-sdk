package com.mapconductor.example.pages.map.visibleregion

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.map.IMapCameraPosition
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.OnMapLoadedHandler
import com.mapconductor.core.marker.ColorDefaultIcon
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.example.MapViewContainer
import android.annotation.SuppressLint

@Composable
fun VisibleRegionMapComponent(
    modifier: Modifier = Modifier,
    mapViewState: MapViewState<*>,
    onMapLoaded: OnMapLoadedHandler? = null,
    onCameraChanged: ((IMapCameraPosition) -> Unit)? = null,
) {
    var currentCameraPosition by remember { mutableStateOf<IMapCameraPosition?>(null) }
    var visibleRegionInfo by remember { mutableStateOf<VisibleRegionInfo?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        MapViewContainer(
            modifier = Modifier.fillMaxSize(),
            state = mapViewState,
            onMapLoaded = onMapLoaded,
        ) {
            currentCameraPosition?.visibleRegion?.let { visibleRegion ->
                val bounds = visibleRegion.bounds
                if (!bounds.isEmpty && bounds.southWest != null && bounds.northEast != null) {
                    val centerLat = bounds.center!!.latitude
                    val centerLng = bounds.center!!.longitude

                    // Center marker
                    Marker(
                        MarkerState(
                            id = "center_marker",
                            position = GeoPoint(centerLat, centerLng),
                            icon = ColorDefaultIcon(fillColor = Color.Red),
                        ),
                    )

                    // Corner point markers
                    visibleRegion.nearLeft?.let { point ->
                        Marker(
                            MarkerState(
                                id = "near_left",
                                position = GeoPoint.from(point),
                                icon = ColorDefaultIcon(fillColor = Color.Blue),
                            ),
                        )
                    }

                    visibleRegion.nearRight?.let { point ->
                        Marker(
                            MarkerState(
                                id = "near_right",
                                position = GeoPoint.from(point),
                                icon = ColorDefaultIcon(fillColor = Color.Green),
                            ),
                        )
                    }

                    visibleRegion.farLeft?.let { point ->
                        Marker(
                            MarkerState(
                                id = "far_left",
                                position = GeoPoint.from(point),
                                icon = ColorDefaultIcon(fillColor = Color.Yellow),
                            ),
                        )
                    }

                    visibleRegion.farRight?.let { point ->
                        Marker(
                            MarkerState(
                                id = "far_right",
                                position = GeoPoint.from(point),
                                icon = ColorDefaultIcon(fillColor = Color.Magenta),
                            ),
                        )
                    }

                    // SW/NE corner markers
                    bounds.southWest?.let { point ->
                        Marker(
                            MarkerState(
                                id = "southwest_corner",
                                position = GeoPoint.from(point),
                                icon = ColorDefaultIcon(fillColor = Color.Black),
                            ),
                        )
                    }

                    bounds.northEast?.let { point ->
                        Marker(
                            MarkerState(
                                id = "northeast_corner",
                                position = GeoPoint.from(point),
                                icon = ColorDefaultIcon(fillColor = Color.Black),
                            ),
                        )
                    }
                }
            }
        }

        VisibleRegionInfoPanel(
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .widthIn(max = 350.dp)
                    .alpha(1.0f),
            cameraPosition = currentCameraPosition,
            visibleRegionInfo = visibleRegionInfo,
        )
    }

    LaunchedEffect(mapViewState.cameraPosition) {
        mapViewState.cameraPosition.collect { position ->
            position?.let {
                currentCameraPosition = it
                onCameraChanged?.invoke(it)

                it.visibleRegion?.let { visibleRegion ->
                    visibleRegionInfo = createVisibleRegionInfo(visibleRegion)
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun VisibleRegionInfoPanel(
    modifier: Modifier = Modifier,
    cameraPosition: IMapCameraPosition?,
    visibleRegionInfo: VisibleRegionInfo?,
) {
    val clipboardManager = LocalClipboardManager.current
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val copyData =
        remember(cameraPosition, visibleRegionInfo) {
            buildString {
                appendLine("=== Visible Region Info ===")

                cameraPosition?.let { camera ->
                    appendLine("Camera:")
                    appendLine("  Zoom: ${String.format("%.2f", camera.zoom)}")
                    appendLine("  Altitude: ${String.format("%.2f", camera.position.altitude ?: 0.0)}")
                    appendLine("  Bearing: ${String.format("%.2f°", camera.bearing)}")
                    appendLine("  Tilt: ${String.format("%.2f°", camera.tilt)}")
                    appendLine("  Position: ${formatLatLng(camera.position)}")

                    camera.visibleRegion?.let { visibleRegion ->
                        val bounds = visibleRegion.bounds
                        if (!bounds.isEmpty && bounds.southWest != null && bounds.northEast != null) {
                            appendLine()
                            appendLine("Visible Region:")

                            visibleRegionInfo?.let { info ->
                                appendLine("  Size: ${String.format("%.2f", info.widthKm)} × ${String.format("%.2f", info.heightKm)} km")
                            }

                            appendLine("  SW Corner: ${formatLatLng(bounds.southWest!!)}")
                            appendLine("  NE Corner: ${formatLatLng(bounds.northEast!!)}")

                            if (visibleRegion.nearLeft != null ||
                                visibleRegion.nearRight != null ||
                                visibleRegion.farLeft != null ||
                                visibleRegion.farRight != null
                            ) {
                                appendLine()
                                appendLine("Corner Points:")

                                visibleRegion.nearLeft?.let { point ->
                                    appendLine("  Near Left: ${formatLatLng(point)}")
                                }
                                visibleRegion.nearRight?.let { point ->
                                    appendLine("  Near Right: ${formatLatLng(point)}")
                                }
                                visibleRegion.farLeft?.let { point ->
                                    appendLine("  Far Left: ${formatLatLng(point)}")
                                }
                                visibleRegion.farRight?.let { point ->
                                    appendLine("  Far Right: ${formatLatLng(point)}")
                                }
                            }
                        } else {
                            appendLine("No visible region data")
                        }
                    } ?: appendLine("No visible region data")
                } ?: appendLine("No camera position data")
            }
        }

    Card(
        modifier = modifier.animateContentSize(animationSpec = tween(300)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(8.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Visible Region",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )

                Row {
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(copyData))
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Copy visible region data",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        modifier =
                            Modifier
                                .clickable { isExpanded = !isExpanded }
                                .padding(12.dp),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier =
                        Modifier
                            .heightIn(max = 300.dp)
                            .verticalScroll(rememberScrollState()),
                ) {
                    cameraPosition?.let { camera ->
                        InfoRow("Zoom", String.format("%.2f", camera.zoom))
                        InfoRow("Altitude", String.format("%.2f", camera.position.altitude ?: 0.0))
                        InfoRow("Bearing", String.format("%.2f°", camera.bearing))
                        InfoRow("Tilt", String.format("%.2f°", camera.tilt))
                        InfoRow("Position", formatLatLng(camera.position))

                        camera.visibleRegion?.let { visibleRegion ->
                            val bounds = visibleRegion.bounds
                            if (!bounds.isEmpty && bounds.southWest != null && bounds.northEast != null) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                                Text(
                                    text = "Bounds & Size",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                visibleRegionInfo?.let { info ->
                                    InfoRow("Size", "${String.format("%.2f", info.widthKm)} × ${String.format("%.2f", info.heightKm)} km")
                                }

                                InfoRow("SW Corner", formatLatLng(bounds.southWest!!))
                                InfoRow("NE Corner", formatLatLng(bounds.northEast!!))

                                if (visibleRegion.nearLeft != null ||
                                    visibleRegion.nearRight != null ||
                                    visibleRegion.farLeft != null ||
                                    visibleRegion.farRight != null
                                ) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Corner Points",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )

                                    visibleRegion.nearLeft?.let { point ->
                                        InfoRow("Near Left", formatLatLng(point), color = Color.Blue)
                                    }
                                    visibleRegion.nearRight?.let { point ->
                                        InfoRow("Near Right", formatLatLng(point), color = Color.Green)
                                    }
                                    visibleRegion.farLeft?.let { point ->
                                        InfoRow("Far Left", formatLatLng(point), color = Color(0xFFFFD700))
                                    }
                                    visibleRegion.farRight?.let { point ->
                                        InfoRow("Far Right", formatLatLng(point), color = Color.Magenta)
                                    }
                                }
                            }
                        } ?: run {
                            Text(
                                text = "No visible region data",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    } ?: run {
                        Text(
                            text = "No camera position data",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = color,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = color,
            modifier = Modifier.weight(1.5f),
        )
    }
}

@SuppressLint("DefaultLocale")
private fun formatLatLng(position: IGeoPoint): String =
    "${String.format("%.6f", position.latitude)}, ${String.format("%.6f", position.longitude)}"

private fun createVisibleRegionInfo(visibleRegion: com.mapconductor.core.map.VisibleRegion): VisibleRegionInfo {
    val bounds = visibleRegion.bounds
    if (bounds.isEmpty || bounds.southWest == null || bounds.northEast == null) {
        return VisibleRegionInfo(
            bounds = "Empty bounds",
            corners = emptyList(),
            centerPoint = "N/A",
            widthKm = 0.0,
            heightKm = 0.0,
        )
    }

    val widthKm =
        calculateDistance(
            bounds.southWest!!.latitude, bounds.southWest!!.longitude,
            bounds.southWest!!.latitude, bounds.northEast!!.longitude,
        )
    val heightKm =
        calculateDistance(
            bounds.southWest!!.latitude, bounds.southWest!!.longitude,
            bounds.northEast!!.latitude, bounds.southWest!!.longitude,
        )

    return VisibleRegionInfo(
        bounds = "",
        corners = emptyList(),
        centerPoint = "",
        widthKm = widthKm,
        heightKm = heightKm,
    )
}

private fun calculateDistance(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double,
): Double {
    val earthRadius = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a =
        kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
            kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
            kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
    val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
    return earthRadius * c
}
