package com.mapconductor.example.pages.polygon

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mapconductor.example.toast.ToastHost
import com.mapconductor.example.ui.DemoMapPageScaffold
import com.mapconductor.example.ui.MessageCard
import com.mapconductor.example.ui.PolygonCapableMapViewItems

@Composable
fun PolygonMapPage(onToggleSidebar: () -> Unit = {}) {
    val viewModel: PolygonMapPageViewModel =
        viewModel<PolygonMapPageViewModelImpl>(
            factory =
                object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        if (modelClass.isAssignableFrom(PolygonMapPageViewModelImpl::class.java)) {
                            @Suppress("UNCHECKED_CAST")
                            return PolygonMapPageViewModelImpl() as T
                        }
                        throw IllegalArgumentException("Unknown ViewModel class")
                    }
                },
        )

    DemoMapPageScaffold(
        menuItems = PolygonCapableMapViewItems(viewModel.initCameraPosition),
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = viewModel::onMapViewChanged,
    ) { paddingValues ->
        val mapViewState = viewModel.mapViewState.collectAsState()

        Column {
            // Polygon Controls
            Column(
                modifier =
                    Modifier.padding(
                        start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                        end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                        top = paddingValues.calculateTopPadding(),
                    ),
            ) {
                MessageCard(
                    title = "Polygon Example",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(
                        "This example demonstrates polygon rendering with draggable vertices. " +
                            "Drag the markers to change the polygon shape.",
                    )
                }

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
                            SliderDefaults.colors(
                                thumbColor = Color.Blue,
                                activeTrackColor = Color.Blue,
                            ),
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
                        valueRange = 1f..10f,
                        colors =
                            SliderDefaults.colors(
                                thumbColor = Color.Blue,
                                activeTrackColor = Color.Blue,
                            ),
                    )
                }
            }

            // Map Component
            PolygonMapComponent(
                mapViewState = mapViewState.value,
                viewModel = viewModel,
                modifier =
                    Modifier.padding(
                        bottom = paddingValues.calculateBottomPadding(),
                    ),
                onPolygonClick = viewModel::onPolygonClick,
                onMarkerDrag = viewModel::onMarkerDrag,
            )
        }

        // Toast messages
        ToastHost(
            messages = viewModel.messages.collectAsState().value,
            onDismiss = viewModel::removeToast,
        )
    }
}
