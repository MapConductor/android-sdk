package com.mapconductor.example.pages.marker.labelposition

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import com.mapconductor.compose.info.InfoBubble
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

// 信州大学（松本キャンパス）周辺に 3 × 3 で並べ、それぞれ違う labelPosition を付ける。
// react-sdk の marker-label-position、ios-sdk の MarkerLabelPositionMapComponent と同じ配置。
private val CENTER = GeoPoint.fromLatLong(36.2526, 137.9828)
private const val STEP_LATITUDE = 0.004
private const val STEP_LONGITUDE = 0.007

private val POSITIONS =
    listOf(
        listOf("left top", "center top", "right top"),
        listOf("left center", "center center", "right center"),
        listOf("left bottom", "center bottom", "right bottom"),
    )

@Composable
fun MarkerLabelPositionPage(
    onToggleSidebar: () -> Unit = {},
) {
    val initCameraPosition =
        MapCameraPosition(
            position = CENTER,
            zoom = 14.0,
        )
    var selectedId by remember { mutableStateOf<String?>(null) }
    var mapViewState by remember { mutableStateOf<MapViewStateInterface<Any>?>(null) }
    val onMarkerClick: OnMarkerEventHandler = { markerState -> selectedId = markerState.id }
    val markerStates =
        remember {
            POSITIONS.flatMapIndexed { rowIndex, row ->
                row.mapIndexed { columnIndex, labelPosition ->
                    MarkerState(
                        id = labelPosition,
                        position =
                            GeoPoint.fromLatLong(
                                CENTER.latitude + (1 - rowIndex) * STEP_LATITUDE,
                                CENTER.longitude + (columnIndex - 1) * STEP_LONGITUDE,
                            ),
                        icon =
                            DefaultMarkerIcon(
                                label = if (labelPosition == "center center") "信" else "信州大学",
                                labelPosition = labelPosition,
                                debug = true,
                            ),
                        extra = labelPosition,
                        onClick = onMarkerClick,
                    )
                }
            }
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
                state = mapViewState,
                onMapClick = { selectedId = null },
            ) {
                markerStates.forEach { markerState ->
                    Marker(markerState)
                    if (markerState.id == selectedId) {
                        InfoBubble(
                            marker = markerState,
                            bubbleColor = Color.White,
                            borderColor = Color.Black,
                        ) {
                            Text(
                                text = "labelPosition: \"${markerState.extra}\"",
                                fontFamily = FontFamily.Monospace,
                            )
                        }
                    }
                }
            }
        }
    }
}
