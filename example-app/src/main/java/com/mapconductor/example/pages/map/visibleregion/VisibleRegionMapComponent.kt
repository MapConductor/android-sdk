package com.mapconductor.example.pages.map.visibleregion

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPositionInterface
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.map.OnMapLoadedHandler
import com.mapconductor.core.marker.ColorDefaultIcon
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.spherical.WGS84Geodesic.computeDistanceBetween
import com.mapconductor.example.MapViewContainer
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

@Composable
fun VisibleRegionMapComponent(
    modifier: Modifier = Modifier,
    mapViewState: MapViewStateInterface<*>,
    onMapLoaded: OnMapLoadedHandler? = null,
    onCameraChanged: ((MapCameraPositionInterface) -> Unit)? = null,
) {
    var currentCameraPosition by remember { mutableStateOf<MapCameraPositionInterface?>(null) }
    var visibleRegionInfo by remember { mutableStateOf<VisibleRegionInfo?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        MapViewContainer(
            modifier = Modifier.fillMaxSize(),
            state = mapViewState,
            onMapLoaded = onMapLoaded,
            onCameraMoveEnd = { cameraPosition ->
                currentCameraPosition = cameraPosition
                onCameraChanged?.invoke(cameraPosition)

                cameraPosition.visibleRegion?.let { visibleRegion ->
                    visibleRegionInfo = createVisibleRegionInfo(visibleRegion)
                }
            },
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
                            position = GeoPoint.fromLatLong(centerLat, centerLng),
                            icon = ColorDefaultIcon(fillColor = Color.Red, label = "Center"),
                        ),
                    )

                    // Corner point markers
                    visibleRegion.nearLeft?.let { point ->
                        Marker(
                            MarkerState(
                                id = "near_left",
                                position = point,
                                icon = ColorDefaultIcon(fillColor = Color.Blue, label = "NL"),
                            ),
                        )
                    }

                    visibleRegion.nearRight?.let { point ->
                        Marker(
                            MarkerState(
                                id = "near_right",
                                position = point,
                                icon = ColorDefaultIcon(fillColor = Color.Green, label = "NR"),
                            ),
                        )
                    }

                    visibleRegion.farLeft?.let { point ->
                        Marker(
                            MarkerState(
                                id = "far_left",
                                position = point,
                                icon = ColorDefaultIcon(fillColor = Color.Yellow, label = "FL"),
                            ),
                        )
                    }

                    visibleRegion.farRight?.let { point ->
                        Marker(
                            MarkerState(
                                id = "far_right",
                                position = point,
                                icon = ColorDefaultIcon(fillColor = Color.Magenta, label = "FR"),
                            ),
                        )
                    }

                    // SW/NE corner markers
                    bounds.southWest?.let { point ->
                        Marker(
                            MarkerState(
                                id = "southwest_corner",
                                position = point,
                                icon = ColorDefaultIcon(fillColor = Color.Black, label = "SW"),
                            ),
                        )
                    }

                    bounds.northEast?.let { point ->
                        Marker(
                            MarkerState(
                                id = "northeast_corner",
                                position = point,
                                icon = ColorDefaultIcon(fillColor = Color.Black, label = "NE"),
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
}

@SuppressLint("DefaultLocale")
@Composable
fun VisibleRegionInfoPanel(
    modifier: Modifier = Modifier,
    cameraPosition: MapCameraPositionInterface?,
    visibleRegionInfo: VisibleRegionInfo?,
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboard.current
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
                                appendLine(
                                    value = String.format("  Size: %.2f × %.2f km", info.widthKm, info.heightKm),
                                )
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
            VisibleRegionRow(
                context = context,
                copyData = copyData,
            ) {
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

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                CameraDataRow(
                    cameraPosition = cameraPosition,
                    visibleRegionInfo = visibleRegionInfo,
                )
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun CameraDataRow(
    cameraPosition: MapCameraPositionInterface?,
    visibleRegionInfo: VisibleRegionInfo?,
) {
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
                        InfoRow(
                            label = "Size",
                            value = String.format("%.2f x %.2f km", info.widthKm, info.heightKm),
                        )
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
                            InfoRow(
                                label = "Near Left",
                                value = formatLatLng(point),
                                color = Color.Blue,
                            )
                        }
                        visibleRegion.nearRight?.let { point ->
                            InfoRow(
                                label = "Near Right",
                                value = formatLatLng(point),
                                color = Color.Green,
                            )
                        }
                        visibleRegion.farLeft?.let { point ->
                            InfoRow(
                                label = "Far Left",
                                value = formatLatLng(point),
                                color = Color(0xFFFFD700),
                            )
                        }
                        visibleRegion.farRight?.let { point ->
                            InfoRow(
                                label = "Far Right",
                                value = formatLatLng(point),
                                color = Color.Magenta,
                            )
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

@Composable
fun VisibleRegionRow(
    context: Context,
    copyData: String,
    content: (@Composable () -> Unit)? = null,
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
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("VisibleRegion", copyData)
                    clipboard.setPrimaryClip(clip)
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Copy visible region data",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            content?.invoke()
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
private fun formatLatLng(position: GeoPointInterface): String =
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
        computeDistanceBetween(
            bounds.southWest!!,
            GeoPoint(bounds.southWest!!.latitude, bounds.northEast!!.longitude),
        )
    val heightKm =
        computeDistanceBetween(
            bounds.southWest!!,
            GeoPoint(bounds.northEast!!.latitude, bounds.southWest!!.longitude),
        )

    return VisibleRegionInfo(
        bounds = "",
        corners = emptyList(),
        centerPoint = "",
        widthKm = widthKm,
        heightKm = heightKm,
    )
}
