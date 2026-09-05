package com.mapconductor.example.pages.marker.streettree

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold
import com.mapconductor.utils.LoadingDialog
import com.mapconductor.streettree.StreetTreeDataLoader

/**
 * 144,183 street trees, one colour per species, drawn as raster tiles.
 *
 * The point of the page is scale: the same abstraction that carries a handful
 * of markers carries this, because past a threshold the markers stop being
 * individual overlays and become tiles the backend sees as an ordinary raster
 * layer.
 */
@Composable
fun StreetTreePage(
    modifier: Modifier = Modifier,
    onToggleSidebar: () -> Unit = {},
) {
    val context = LocalContext.current
    val dataLoader = remember { StreetTreeDataLoader(context) }

    val viewModel: StreetTreeViewModel =
        viewModel(
            factory =
                object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T =
                        StreetTreeViewModel(dataLoader) as T
                },
        )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val selectedMarker = viewModel.selectedMarker.collectAsState().value
        val markers = viewModel.markerList.collectAsState().value
        val mapViewState = viewModel.mapViewState.collectAsState().value
        val isMapLoaded = viewModel.isMapLoaded.collectAsState().value
        val isDataLoading = viewModel.isDataLoading.collectAsState().value

        DemoMapPageScaffold(
            menuItems = DefaultMapViewItems(viewModel.initCameraPosition),
            onToggleSidebar = onToggleSidebar,
            onMapViewStateChanged = viewModel::onMapViewChanged,
        ) { paddingValues ->
            mapViewState?.let { state ->
                StreetTreeMapComponent(
                    markerTiling = viewModel.markerTiling,
                    mapViewState = state,
                    selectedMarker = selectedMarker,
                    markers = markers,
                    modifier = modifier.padding(paddingValues),
                    onMapLoaded = viewModel::onMapLoaded,
                    onMapClick = viewModel::onMapClick,
                )
            }
        }

        if (!isMapLoaded || isDataLoading) {
            LoadingDialog(
                title = "Loading Street Trees",
                message = if (!isMapLoaded) "Preparing map..." else "144,183 trees...",
            )
        }
    }
}
