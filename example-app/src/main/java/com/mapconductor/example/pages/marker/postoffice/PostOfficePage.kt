package com.mapconductor.example.pages.marker.postoffice

import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.mapconductor.core.marker.ImageIcon
import com.mapconductor.example.R
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold

@Composable
fun PostOfficeMapPage(onToggleSidebar: () -> Unit = {}) {
    val context = LocalContext.current
    val viewModel =
        remember {
            val icon = AppCompatResources.getDrawable(context, R.drawable.postoffice)!!
            val imageIcon =
                ImageIcon(
                    drawable = icon,
                    scale = 0.3f,
                )
            PostOfficeViewModelImpl(imageIcon, TokyoPostOffices)
        }
    val selectedMarker = viewModel.selectedMarker

    DemoMapPageScaffold(
        menuItems = DefaultMapViewItems(viewModel.initCameraPosition),
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = viewModel::onMapViewChanged,
    ) {
        viewModel.mapViewState.value?.let { mapViewState ->
            PostOfficeMapComponent(
                mapViewState = mapViewState,
                renderingStrategy = viewModel.renderingStrategy.value,
                selectedMarker = selectedMarker.value,
                markers = viewModel.markerList,
                onMapClick = viewModel::onMapClick,
                onMarkerClick = viewModel::onMarkerClick,
            )
        }
    }
}
