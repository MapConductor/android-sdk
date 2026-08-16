package com.mapconductor.example.pages.map.camerarestriction

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mapconductor.compose.polygon.Polygon
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.map.CameraRestriction
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.example.MapViewContainer
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold

/**
 * `CameraRestriction` を実際の地図で試すページ — ios の CameraRestrictionPage /
 * react の camera-restriction と同一仕様。
 *
 * 許可矩形（東京駅周辺）を赤いポリゴンで可視化し、ボタンで「許可されない場所」への
 * 移動を要求する。ネイティブに制限 API を持つプロバイダは移動自体を拒否し、
 * クランプ方式のプロバイダは動いた後で引き戻される。どちらでも最終的に
 * 読み出しが制限内に収まっていれば正しい。
 */

private const val SOUTH = 35.63
private const val WEST = 139.70
private const val NORTH = 35.75
private const val EAST = 139.85
private const val MIN_ZOOM = 12.0
private const val MAX_ZOOM = 16.0

private val startPosition = GeoPoint(35.681236, 139.767125)

private val restriction =
    CameraRestriction(
        bounds =
            GeoRectBounds(
                southWest = GeoPoint(SOUTH, WEST),
                northEast = GeoPoint(NORTH, EAST),
            ),
        minZoom = MIN_ZOOM,
        maxZoom = MAX_ZOOM,
    )

private val boundsPolygon =
    PolygonState(
        id = "camera-restriction-bounds",
        points =
            listOf(
                GeoPoint(SOUTH, WEST),
                GeoPoint(SOUTH, EAST),
                GeoPoint(NORTH, EAST),
                GeoPoint(NORTH, WEST),
            ),
        strokeColor = Color.Red,
        strokeWidth = 3.dp,
        fillColor = Color.Red.copy(alpha = 0.10f),
        geodesic = false,
    )

@Composable
fun CameraRestrictionMapPage(onToggleSidebar: () -> Unit = {}) {
    val initCameraPosition =
        remember {
            MapCameraPosition(
                position = startPosition,
                zoom = 14.0,
                bearing = 0.0,
                tilt = 0.0,
                paddings = null,
            )
        }

    var enabled by remember { mutableStateOf(true) }
    var cameraText by remember { mutableStateOf("?") }
    var mapViewState by remember { mutableStateOf<MapViewStateInterface<*>?>(null) }

    fun moveCamera(
        position: GeoPoint,
        zoom: Double,
    ) {
        mapViewState?.moveCameraTo(
            MapCameraPosition(
                position = position,
                zoom = zoom,
                bearing = 0.0,
                tilt = 0.0,
                paddings = null,
            ),
        )
    }

    DemoMapPageScaffold(
        menuItems = DefaultMapViewItems(initCameraPosition),
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = { state ->
            mapViewState?.cameraPosition?.let { state.moveCameraTo(it) }
            mapViewState = state
        },
    ) { paddingValues ->
        mapViewState?.let { state ->
            MapViewContainer(
                state = state,
                cameraRestriction = if (enabled) restriction else null,
                onCameraMove = { c ->
                    cameraText = "%.5f,%.5f,%.2f".format(c.position.latitude, c.position.longitude, c.zoom)
                },
                onCameraMoveEnd = { c ->
                    cameraText = "%.5f,%.5f,%.2f".format(c.position.latitude, c.position.longitude, c.zoom)
                },
            ) {
                Polygon(boundsPolygon)
            }
        }

        // ios / react と同じ左下配置
        Card(
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        bottom = paddingValues.calculateBottomPadding() + 16.dp,
                        start = 16.dp,
                    ).sizeIn(maxWidth = 420.dp),
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("restriction", modifier = Modifier.padding(end = 8.dp))
                    Switch(checked = enabled, onCheckedChange = { enabled = it })
                }
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    Button(
                        onClick = {
                            // 矩形の北東よりさらに外側へ。
                            moveCamera(
                                GeoPoint(36.20, 140.40),
                                mapViewState?.cameraPosition?.zoom ?: 14.0,
                            )
                        },
                        modifier = Modifier.padding(end = 8.dp),
                    ) { Text("Move outside") }
                    Button(
                        onClick = { moveCamera(startPosition, 20.0) },
                        modifier = Modifier.padding(end = 8.dp),
                    ) { Text("Zoom > max") }
                    Button(
                        onClick = { moveCamera(startPosition, 8.0) },
                        modifier = Modifier.padding(end = 8.dp),
                    ) { Text("Zoom < min") }
                    Button(
                        onClick = { moveCamera(startPosition, 14.0) },
                    ) { Text("Reset") }
                }
                Text(cameraText, style = MaterialTheme.typography.bodySmall)
                Text(
                    "limits lat %.2f..%.2f lng %.2f..%.2f zoom %.0f..%.0f"
                        .format(SOUTH, NORTH, WEST, EAST, MIN_ZOOM, MAX_ZOOM),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
