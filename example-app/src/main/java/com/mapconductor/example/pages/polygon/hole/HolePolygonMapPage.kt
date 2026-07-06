package com.mapconductor.example.pages.polygon.hole

import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mapconductor.compose.marker.Marker
import com.mapconductor.compose.polygon.Polygon
import com.mapconductor.example.MapViewContainer
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold
import com.mapconductor.example.ui.MessageCard

@Composable
fun HolePolygonMapPage(onToggleSidebar: () -> Unit = {}) {
    val viewModel = remember { HolePolygonMapPageViewModel() }

    DemoMapPageScaffold(
        menuItems = DefaultMapViewItems(viewModel.initCameraPosition),
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = viewModel::onMapViewChanged,
    ) { paddingValues ->
        val mapViewState = viewModel.mapViewState.collectAsState()

        mapViewState.value?.let {
            MapViewContainer(state = it) {
                Polygon(viewModel.polygonState)
                viewModel.holeVertexMarkers.forEach { marker ->
                    Marker(marker)
                }
            }
        }

        MessageCard(
            title = "Hole Polygon Example",
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
                A world-covering polygon with two triangular holes near Sapporo.
                Drag hole vertex markers to reshape the holes.
                """.trimIndent(),
            )
        }
    }
}
