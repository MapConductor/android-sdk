package com.mapconductor.example.pages.polygon.geodesic

import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.polygon.Polygon
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.example.MapViewContainer
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold
import com.mapconductor.example.ui.MessageCard

@Composable
fun PolygonGeodesicPage(onToggleSidebar: () -> Unit = {}) {
    val viewModel = remember { PolygonGeodesicPageViewModelImpl() }

    val points =
        listOf(
            GeoPointImpl.fromLongLat(23.66, 56.42),
            GeoPointImpl.fromLongLat(13.39, 2.95),
            GeoPointImpl.fromLongLat(-87.82, 38.58),
            GeoPointImpl.fromLongLat(23.66, 56.42),
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
            )
        }

    DemoMapPageScaffold(
        menuItems = DefaultMapViewItems(viewModel.initCameraPosition),
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = viewModel::onMapViewChanged,
    ) { paddingValues ->
        val mapViewState = viewModel.mapViewState.collectAsState()
        val marker = viewModel.markerState.collectAsState()

        mapViewState.value?.let {
            MapViewContainer(
                state = it,
                onPolygonClick = viewModel::onPolygonClicked,
            ) {
                Polygon(polylineState)
                Polygon(geodesicPolylineState)

                marker.value?.let { markerState ->
                    Marker(markerState)
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
                This example shows the ability of the polygon click detection.
                Place a green marker if you tap on the green polygon,
                and place a blue marker on the blue polygon if you tap on it.
                """.trimIndent(),
            )
        }
    }
}
