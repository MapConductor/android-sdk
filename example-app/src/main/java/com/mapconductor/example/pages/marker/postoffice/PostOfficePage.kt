package com.mapconductor.example.pages.marker.postoffice

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import com.mapconductor.arcgis.ArcGISActualMarker
import com.mapconductor.core.marker.ImageIcon
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold
import com.mapconductor.googlemaps.GoogleMapActualMarker
import com.mapconductor.here.HereActualMarker
import com.mapconductor.mapbox.MapboxActualMarker
import com.mapconductor.maplibre.MapLibreActualMarker
import com.mapconductor.marker.nativestrategy.spatial.NativeRemoteSpatialMarkerStrategy
import com.mapconductor.postoffice.PostOfficeDataLoader
import com.mapconductor.utils.LoadingDialog

@Composable
fun PostOfficeMapPage(
    postOfficeIcon: ImageIcon,
    onToggleSidebar: () -> Unit = {},
) {
    val context = LocalContext.current
    val dataLoader = remember { PostOfficeDataLoader(context) }
    val strategies =
        remember {
            val google = NativeRemoteSpatialMarkerStrategy<GoogleMapActualMarker>(context)
            val mapbox =
                NativeRemoteSpatialMarkerStrategy<MapboxActualMarker>(
                    context = context,
                    addOnlyMode = true,
                )
            val here = NativeRemoteSpatialMarkerStrategy<HereActualMarker>(context)
            val arcgis = NativeRemoteSpatialMarkerStrategy<ArcGISActualMarker>(context)
            val maplibre =
                NativeRemoteSpatialMarkerStrategy<MapLibreActualMarker>(
                    context = context,
                    addOnlyMode = true,
                )
            Strategies(
                google = google,
                mapbox = mapbox,
                here = here,
                arcgis = arcgis,
                maplibre = maplibre,
            )
        }

    val viewModel: PostOfficeViewModel =
        viewModel<PostOfficeViewModel>(
            factory =
                object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        if (modelClass.isAssignableFrom(PostOfficeViewModel::class.java)) {
                            @Suppress("UNCHECKED_CAST")
                            return PostOfficeViewModel(
                                strategies = strategies,
                                postOfficeIcon = postOfficeIcon,
                                dataLoader = dataLoader,
                            ) as T
                        }
                        throw IllegalArgumentException("Unknown ViewModel class")
                    }
                },
        )

    // Show loading dialog while map or data is loading; start data load once
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        val selectedMarker = viewModel.selectedMarker.collectAsState().value
        val markers = viewModel.markerList.collectAsState().value
        val mapViewState = viewModel.mapViewState.collectAsState().value
        val isMapLoaded = viewModel.isMapLoaded.collectAsState().value
        val isDataLoading = viewModel.isDataLoading.collectAsState().value

        LaunchedEffect(Unit) {
            viewModel.loadPostOfficeData()
        }

        DemoMapPageScaffold(
            menuItems = DefaultMapViewItems(viewModel.initCameraPosition),
            onToggleSidebar = onToggleSidebar,
            onMapViewStateChanged = viewModel::onMapViewChanged,
        ) {
            mapViewState?.let { mapViewState ->
                PostOfficeMapComponent(
                    mapViewState = mapViewState,
                    renderingStrategy = viewModel.renderingStrategy.value,
                    selectedMarker = selectedMarker,
                    markers = markers,
                    onMapLoaded = viewModel::onMapLoaded,
                    onMapClick = viewModel::onMapClick,
                    onInfoWndClick = viewModel::onInfoClick,
                )
            }
        }

        if (!isMapLoaded || isDataLoading) {
            LoadingDialog(
                title = "Loading Post Offices",
                message = if (!isMapLoaded) "Preparing map..." else "Generating markers...",
            )
        }
    }
}
