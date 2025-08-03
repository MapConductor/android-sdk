package com.mapconductor.example.pages.stores

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.mapconductor.example.ui.DemoMapPageScaffold

@Composable
fun StoreMapPage(
    viewModel: StoreMapPageViewModel = StoreMapPageViewModelImpl(),
    onToggleSidebar: () -> Unit = {},
) {

    DemoMapPageScaffold(
        initCameraPosition = viewModel.initCameraPosition,
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = viewModel::onMapViewChanged,
    ) { paddings ->

        val selectedMarker = viewModel.selectedMarker.collectAsState()
        val mapViewState = viewModel.mapViewState.collectAsState()

        StoreMapComponent(
            mapViewState = mapViewState.value,
            markers = viewModel.markerList,
            onDirectionButtonClick = viewModel::onDirectionButtonClick,
            onMapClickHandler = viewModel::onMapClick,
            onMarkerClickHandler = viewModel::onMarkerClick,
            selectedMarker = selectedMarker.value,
        )
    }
}
