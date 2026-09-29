package com.mapconductor.example.pages.map.basic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold

@Composable
fun StoreMapPage(modifier: Modifier = Modifier, onToggleSidebar: () -> Unit = {}) {
    val viewModel = remember { StoreMapPageViewModel() }
    val context = LocalContext.current

    DemoMapPageScaffold(
        menuItems = DefaultMapViewItems(viewModel.initCameraPosition),
        modifier = modifier,
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = viewModel::onMapViewChanged,
    ) {
        val selectedMarker = viewModel.selectedMarker.collectAsState()
        val mapViewState = viewModel.mapViewState.collectAsState()

        StoreMapComponent(
            mapViewState = mapViewState.value,
            markers = viewModel.markerList,
            selectedMarker = selectedMarker.value,
//            mapPaddings = MapPaddings(
//                top = paddings.calculateTopPadding().value.toDouble(),
//                bottom = paddings.calculateBottomPadding().value.toDouble(),
//                left = paddings.calculateLeftPadding(LayoutDirection.Ltr).value.toDouble(),
//                right = paddings.calculateRightPadding(LayoutDirection.Ltr).value.toDouble(),
//            ),
            onDirectionButtonClick = { state ->
                val intent = viewModel.onDirectionButtonClick(state)
                context.startActivity(intent)
            },
            onMapClick = viewModel::onMapClick,
        )
    }
}
