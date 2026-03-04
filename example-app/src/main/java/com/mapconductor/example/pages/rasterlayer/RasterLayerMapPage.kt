package com.mapconductor.example.pages.rasterlayer

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
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold
import com.mapconductor.example.ui.MessageCard

@Composable
fun RasterLayerMapPage(onToggleSidebar: () -> Unit = {}) {
    val viewModel = remember { RasterLayerPageViewModel() }
    DemoMapPageScaffold(
        menuItems = DefaultMapViewItems(viewModel.initCameraPosition),
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = viewModel::onMapViewChanged,
    ) { paddingValues ->
        val mapViewState = viewModel.mapViewState.collectAsState()

        RasterLayerMapComponent(
            mapViewState = mapViewState.value,
            rasterLayerState = viewModel.rasterLayerState,
        )

        MessageCard(
            title = "Raster Layer Example",
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
            // Opacity Control
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Opacity: ${String.format("%.1f", viewModel.opacity)}")
                Slider(
                    value = viewModel.opacity,
                    onValueChange = { viewModel.opacity = it },
                    valueRange = 0f..1f,
                    colors =
                        SliderDefaults.colors(),
                )
            }
        }
    }
}
