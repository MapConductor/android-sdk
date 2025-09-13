package com.mapconductor.example.pages.marker.postoffice

import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.mapconductor.core.marker.ImageIcon
import com.mapconductor.example.R
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold

@Composable
fun PostOfficeMapPage(onToggleSidebar: () -> Unit = {}) {
    val context = LocalContext.current
    val postOfficesState = remember { mutableStateOf<List<PostOffice>?>(null) }
    val dataLoader = remember { PostOfficeDataLoader(context) }

    // Load post office data asynchronously
    LaunchedEffect(Unit) {
        val postOffices = dataLoader.loadAllPostOffices()
        postOfficesState.value = postOffices
    }

    val postOffices = postOfficesState.value

    if (postOffices == null || postOffices.isEmpty()) {
        // Show loading indicator while data is being loaded
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val viewModel =
        remember(postOffices) {
            val icon = AppCompatResources.getDrawable(context, R.drawable.postoffice)!!
            val tinyIcon =
                ImageIcon(
                    drawable = icon,
                    scale = 0.15f,
                )
            val smallIcon =
                ImageIcon(
                    drawable = icon,
                    scale = 0.3f,
                )
            val regularIcon =
                ImageIcon(
                    drawable = icon,
                    scale = 0.6f,
                )
            val icons = listOf(tinyIcon, smallIcon, regularIcon)
            PostOfficeViewModelImpl(icons, postOffices)
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
                onCameraChanged = viewModel::onCameraChanged,
            )
        }
    }
}
