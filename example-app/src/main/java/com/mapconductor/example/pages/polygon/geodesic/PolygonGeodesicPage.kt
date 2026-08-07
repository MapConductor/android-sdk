package com.mapconductor.example.pages.polygon.geodesic

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mapconductor.compose.info.InfoBubble
import com.mapconductor.compose.marker.Marker
import com.mapconductor.compose.polygon.Polygon
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.example.MapViewContainer
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold
import com.mapconductor.example.ui.MessageCard

@Composable
fun PolygonGeodesicPage(onToggleSidebar: () -> Unit = {}) {
    val viewModel = remember { PolygonGeodesicPageViewModel() }
    val isDarkTheme = isSystemInDarkTheme()

    val points =
        listOf(
            GeoPoint.fromLongLat(23.66, 56.42),
            GeoPoint.fromLongLat(13.39, 2.95),
            GeoPoint.fromLongLat(-87.82, 38.58),
            GeoPoint.fromLongLat(23.66, 56.42),
        )

    val polylineState =
        remember {
            PolygonState(
                points = points,
                strokeColor = Color.Yellow.copy(alpha = 0.3f),
                strokeWidth = 3.dp,
                fillColor = Color.Green.copy(alpha = 0.5f),
                geodesic = false,
                zIndex = 0,
                onClick = viewModel::onPolygonClicked,
            )
        }

    val geodesicPolylineState =
        remember {
            PolygonState(
                points = points,
                strokeColor = Color.Red.copy(alpha = 0.3f),
                strokeWidth = 3.dp,
                fillColor = Color.Blue.copy(alpha = 0.5f),
                geodesic = true,
                zIndex = 1,
                onClick = viewModel::onPolygonClicked,
            )
        }

    DemoMapPageScaffold(
        menuItems = DefaultMapViewItems(viewModel.initCameraPosition),
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = viewModel::onMapViewChanged,
    ) { paddingValues ->
        val mapViewState = viewModel.mapViewState.collectAsState()
        val marker = viewModel.markerState.collectAsState()
        val clickedLabel = viewModel.clickedLabel.collectAsState()

        mapViewState.value?.let {
            MapViewContainer(
                state = it,
            ) {
                Polygon(polylineState)
                Polygon(geodesicPolylineState)

                marker.value?.let { markerState ->
                    Marker(markerState)
                    // クリックしたポリゴン（Linear/Geodesic）を InfoBubble で表示する（React と同じロジック）。
                    clickedLabel.value?.let { label ->
                        InfoBubble(
                            marker = markerState,
                            bubbleColor = if (isDarkTheme) Color.Black else Color.White,
                            borderColor = if (isDarkTheme) Color.Gray else Color.Black,
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isDarkTheme) Color.White else Color.Gray,
                            )
                        }
                    }
                }
            }
        }

        MessageCard(
            title = "Polygon Geodesic Example",
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        bottom = paddingValues.calculateBottomPadding() + 16.dp,
                        start = paddingValues.calculateStartPadding(LayoutDirection.Ltr) + 16.dp,
                        end = paddingValues.calculateEndPadding(LayoutDirection.Ltr) + 16.dp,
                    ),
        ) {
            Text(
                """
                Tap on the polygons!
                A marker drops at the tapped point (colored to match the polygon),
                and an info bubble shows which polygon was clicked
                (Linear Triangle or Geodesic Triangle).
                """.trimIndent(),
            )
        }
    }
}
