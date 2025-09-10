package com.mapconductor.example.pages.marker.postoffice

import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import com.mapconductor.core.marker.ImageIcon
import com.mapconductor.example.R
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold

@Composable
fun PostOfficeMapPage(onToggleSidebar: () -> Unit = {}) {
    val context = LocalContext.current
    val viewModel = remember {
        val icon = AppCompatResources.getDrawable(context, R.drawable.postoffice)!!
        val imageIcon = ImageIcon(
            drawable = icon,
            scale = 0.3f,
        )
        PostOfficeViewModelImpl(imageIcon, TokyoPostOffices)
    }
    val mapViewState = viewModel.mapViewState.collectAsState()
    val selectedMarker = viewModel.selectedMarker.collectAsState()

    DemoMapPageScaffold(
        menuItems = DefaultMapViewItems(viewModel.initCameraPosition),
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = viewModel::onMapViewChanged,
    ) {
        mapViewState.value?.let {
            PostOfficeMapComponent(
                mapViewState = it,
                selectedMarker = selectedMarker.value,
                markers = viewModel.markerList,
                onMarkerClick = viewModel::onMarkerClick,
                onMapClick = viewModel::onMapClick,
            )
        }
    }
}
