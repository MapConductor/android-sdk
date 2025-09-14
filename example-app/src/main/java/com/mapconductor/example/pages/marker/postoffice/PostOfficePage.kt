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
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.example.R
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold

@Composable
fun PostOfficeMapPage(onToggleSidebar: () -> Unit = {}) {
    val context = LocalContext.current
    val postOfficesState = remember { mutableStateOf<List<MarkerState>?>(null) }
    val dataLoader = remember { PostOfficeDataLoader(context) }
    LaunchedEffect(Unit) {
        val baseicon = AppCompatResources.getDrawable(context, R.drawable.postoffice)!!
        val icon =
            ImageIcon(
                drawable = baseicon,
                scale = 0.3f,
            )
        val postOffices = dataLoader.loadAllPostOffices()
        val markerStates =
            postOffices.map {
                MarkerState(
                    position = it.position,
                    id = it.hashCode().toString(),
//                icon = icon,
//                extra = it,
                )
            }
        postOfficesState.value = markerStates
    }

    if ((postOfficesState.value ?: emptyList()).isEmpty()) {
        // Show loading indicator while data is being loaded
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val viewModel =
        remember {
//            val icon = AppCompatResources.getDrawable(context, R.drawable.postoffice)!!
//            val tinyIcon =
//                ImageIcon(
//                    drawable = icon,
//                    scale = 0.15f,
//                )
//            val smallIcon =
//                ImageIcon(
//                    drawable = icon,
//                    scale = 0.3f,
//                )
//            val regularIcon =
//                ImageIcon(
//                    drawable = icon,
//                    scale = 0.6f,
//                )
//            val icons = listOf(tinyIcon, smallIcon, regularIcon)
            PostOfficeViewModelImpl(postOfficesState.value!!)
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
                renderingStrategy = viewModel.renderingStrategy,
                selectedMarker = selectedMarker.value,
                markers = viewModel.markerList.value,
                onMapLoaded = viewModel::onMapLoaded,
                onMapClick = viewModel::onMapClick,
                onMarkerClick = viewModel::onMarkerClick,
                onCameraChanged = viewModel::onCameraChanged,
            )
        }
    }
}
