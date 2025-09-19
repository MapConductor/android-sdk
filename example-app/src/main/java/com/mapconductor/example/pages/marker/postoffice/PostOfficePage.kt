package com.mapconductor.example.pages.marker.postoffice

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mapconductor.core.marker.ImageIcon
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold
import kotlinx.coroutines.delay

@Composable
fun PostOfficeMapPage(
    postOfficeIcon: ImageIcon,
    onToggleSidebar: () -> Unit = {},
) {
    val context = LocalContext.current
    val dataLoader = remember { PostOfficeDataLoader(context) }

    val viewModel: PostOfficeViewModel =
        viewModel<PostOfficeViewModelImpl>(
            factory =
                object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        if (modelClass.isAssignableFrom(PostOfficeViewModelImpl::class.java)) {
                            @Suppress("UNCHECKED_CAST")
                            return PostOfficeViewModelImpl(context, postOfficeIcon, dataLoader) as T
                        }
                        throw IllegalArgumentException("Unknown ViewModel class")
                    }
                },
        )

// Show loading indicator while map is loading or data is being loaded
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        val selectedMarker = viewModel.selectedMarker
        val markers = viewModel.markerList.collectAsState()

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
                    markers = markers.value,
                    onMapLoaded = viewModel::onMapLoaded,
                    onMapClick = viewModel::onMapClick,
                    onMarkerClick = viewModel::onMarkerClick,
                    onInfoWndClick = viewModel::onInfoClick,
                )
            }
        }

        if (!viewModel.isMapLoaded.value) {
            viewModel.loadPostOfficeData()
            CircularProgressIndicator()
        }
    }
}
