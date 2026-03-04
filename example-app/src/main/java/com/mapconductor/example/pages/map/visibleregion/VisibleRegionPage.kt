package com.mapconductor.example.pages.map.visibleregion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold

@Composable
fun VisibleRegionPage(onToggleSidebar: () -> Unit = {}) {
    val initCameraPosition =
        MapCameraPosition(
            position = GeoPoint(latitude = 35.6762, longitude = 139.6503),
            zoom = 10.0,
        )

    val viewModel = remember { VisibleRegionViewModel() }

    DemoMapPageScaffold(
        initSelect = 0,
        menuItems = DefaultMapViewItems(initCameraPosition),
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = viewModel::onMapViewStateChanged,
    ) {
        viewModel.mapViewState.value?.let { mapViewState ->
            VisibleRegionMapComponent(
                mapViewState = mapViewState,
                onMapLoaded = viewModel::onMapLoaded,
                onCameraChanged = viewModel::onCameraChanged,
            )
        }
    }
}
