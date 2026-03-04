package com.mapconductor.example.pages.map.basic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold

@Composable
fun StoreMapPage(onToggleSidebar: () -> Unit = {}) {
    val viewModel = remember { StoreMapPageViewModel() }
    val context = LocalContext.current

    DemoMapPageScaffold(
        menuItems = DefaultMapViewItems(viewModel.initCameraPosition),
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = viewModel::onMapViewChanged,
    ) {
        val selectedMarker = viewModel.selectedMarker.collectAsState()
        val mapViewState = viewModel.mapViewState.collectAsState()

        StoreMapComponent(
            mapViewState = mapViewState.value,
            markers = viewModel.markerList,
            onDirectionButtonClick = { state ->
                val intent = viewModel.onDirectionButtonClick(state)
                context.startActivity(intent)
            },
            onMapClick = viewModel::onMapClick,
            selectedMarker = selectedMarker.value,
        )
    }
}
