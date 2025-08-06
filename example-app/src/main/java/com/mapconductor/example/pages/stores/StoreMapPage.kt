package com.mapconductor.example.pages.stores

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import com.mapconductor.example.ui.DemoMapPageScaffold

@Composable
fun StoreMapPage(
    viewModel: StoreMapPageViewModel = StoreMapPageViewModelImpl(),
    onToggleSidebar: () -> Unit = {},
) {
    val context = LocalContext.current

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
            onDirectionButtonClick = { state ->
                val intent = viewModel.onDirectionButtonClick(state)
                context.startActivity(intent)
            },
            onMapClick = viewModel::onMapClick,
            onMarkerClick = viewModel::onMarkerClick,
            selectedMarker = selectedMarker.value,
        )
    }
}
