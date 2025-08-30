package com.mapconductor.example.pages.polyline

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold

@Composable
fun PolylineMapPage(
    onToggleSidebar: () -> Unit = {},
) {
    val viewModel = remember { PolylinePageViewModelImpl() }
    DemoMapPageScaffold(
        menuItems = DefaultMapViewItems(viewModel.initCameraPosition),
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = viewModel::onMapViewChanged,
    ) { paddingValues ->
        val mapViewState = viewModel.mapViewState.collectAsState()

        PolylineMapComponent(
            polylineState = viewModel.polylineState,
            wayPointMarkers = viewModel.wayPointMarkers,
            mapViewState = mapViewState.value,
            onMarkerDrag = viewModel::onMarkerDrag,
        )
    }
}
