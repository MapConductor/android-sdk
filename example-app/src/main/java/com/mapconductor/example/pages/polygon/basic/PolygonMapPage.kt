package com.mapconductor.example.pages.polygon.basic

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mapconductor.example.ui.DemoMapPageScaffold
import com.mapconductor.example.ui.MessageCard
import com.mapconductor.example.ui.PolygonCapableMapViewItems

@Composable
fun PolygonMapPage(onToggleSidebar: () -> Unit = {}) {
    val viewModel = remember { PolygonMapPageViewModelImpl() }

    DemoMapPageScaffold(
        menuItems = PolygonCapableMapViewItems(viewModel.initCameraPosition),
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = viewModel::onMapViewChanged,
    ) { paddingValues ->
        val mapViewState = viewModel.mapViewState.collectAsState()

        // Map Component
        PolygonMapComponent(
            mapViewState = mapViewState.value,
            polygonState = viewModel.polygonState,
            polygonVertexMarkers = viewModel.polygonVertexMarkers,
            modifier =
                Modifier.padding(
                    bottom = paddingValues.calculateBottomPadding(),
                ),
            onMarkerDrag = viewModel::onMarkerDrag,
        )
        MessageCard(
            title = "Polygon Example",
            maxHeight = 250.dp,
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        bottom = paddingValues.calculateBottomPadding() + 16.dp,
                        start = paddingValues.calculateStartPadding(LayoutDirection.Ltr) + 16.dp,
                        end = paddingValues.calculateEndPadding(LayoutDirection.Ltr) + 16.dp,
                    ),
        ) {
            // Fill Opacity Control
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Fill Opacity: ${String.format("%.1f", viewModel.fillOpacity)}")
                Slider(
                    value = viewModel.fillOpacity,
                    onValueChange = { viewModel.fillOpacity = it },
                    valueRange = 0f..1f,
                    colors =
                        SliderDefaults.colors(),
                )
            }

            // Stroke Width Control
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Stroke Width: ${String.format("%.1f", viewModel.strokeWidth)}dp")
                Slider(
                    value = viewModel.strokeWidth,
                    onValueChange = { viewModel.strokeWidth = it },
                    valueRange = 0f..10f,
                    colors =
                        SliderDefaults.colors(),
                )
            }
        }
    }
}
