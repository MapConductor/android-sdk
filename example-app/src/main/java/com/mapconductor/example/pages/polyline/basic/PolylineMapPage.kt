package com.mapconductor.example.pages.polyline

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold

@Composable
fun PolylineMapPage(
    modifier: Modifier = Modifier,
    onToggleSidebar: () -> Unit = {},
) {
    val viewModel = remember { PolylinePageViewModel() }
    DemoMapPageScaffold(
        menuItems = DefaultMapViewItems(viewModel.initCameraPosition),
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = viewModel::onMapViewChanged,
    ) { paddings ->
        val mapViewState = viewModel.mapViewState.collectAsState()

        PolylineMapComponent(
            polylineState = viewModel.polylineState,
            wayPointMarkers = viewModel.wayPointMarkers,
            mapViewState = mapViewState.value,
            modifier = modifier.padding(paddings),
        )
    }
}
