package com.mapconductor.example.pages.rasterlayer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold
import com.mapconductor.example.ui.MessageCard
import com.mapconductor.tomtom.TomTomMapViewState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RasterLayerMapPage(
    onToggleSidebar: () -> Unit = {},
    layers: List<GsiLayer> = DefaultGsiLayers.all,
    initialLayer: GsiLayer = DefaultGsiLayers.nasa,
) {
    val viewModelFactory =
        remember(layers, initialLayer) {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(RasterLayerPageViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return RasterLayerPageViewModel(
                            layers = layers,
                            initialLayer = initialLayer,
                        ) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    val viewModel: RasterLayerPageViewModelInterface =
        viewModel<RasterLayerPageViewModel>(factory = viewModelFactory)

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
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                ) {
                    TextField(
                        modifier = Modifier.menuAnchor(),
                        value = viewModel.selectedLayer.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("GSI layer") },
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        viewModel.availableLayers.forEach { layer ->
                            DropdownMenuItem(
                                text = { Text(layer.displayName) },
                                onClick = {
                                    viewModel.selectLayer(layer)
                                    expanded = false
                                },
                            )
                        }
                    }
                }
                // TomTom はラスターの opacity をランタイムで変更する API が無く（変更＝スタイル全体の
                // 再ロードでビューポートが空白化する）、ライブ調整に非対応。スライダーを無効化する。
                val isTomTom = mapViewState.value is TomTomMapViewState
                if (isTomTom) {
                    Text("Opacity: not available for TomTom")
                } else {
                    Text("Opacity: ${String.format("%.1f", viewModel.opacity)}")
                }
                Slider(
                    value = viewModel.opacity,
                    onValueChange = { viewModel.opacity = it },
                    valueRange = 0f..1f,
                    enabled = !isTomTom,
                    colors =
                        SliderDefaults.colors(),
                )
            }
        }
    }
}
