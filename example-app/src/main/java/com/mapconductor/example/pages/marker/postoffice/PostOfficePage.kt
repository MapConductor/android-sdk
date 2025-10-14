package com.mapconductor.example.pages.marker.postoffice

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
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
import com.mapconductor.marker.nativestrategy.NativeParallelMarkerStrategy
import com.mapconductor.marker.nativestrategy.NativeSpatialMarkerRenderingStrategy
import com.mapconductor.marker.nativestrategy.spatial.NativeRemoteSpatialMarkerStrategy

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
//            val google = NativeParallelMarkerStrategy<GoogleMapActualMarker>()
            val mapbox = NativeParallelMarkerStrategy<MapboxActualMarker>()
            val here = NativeParallelMarkerStrategy<HereActualMarker>()
            val arcgis = NativeSpatialMarkerRenderingStrategy<ArcGISActualMarker>()
            Strategies(
                google = google,
                mapbox = mapbox,
                here = here,
                arcgis = arcgis,
            )
        }

    val viewModel: PostOfficeViewModel =
        viewModel<PostOfficeViewModelImpl>(
            factory =
                object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        if (modelClass.isAssignableFrom(PostOfficeViewModelImpl::class.java)) {
                            @Suppress("UNCHECKED_CAST")
                            return PostOfficeViewModelImpl(
                                strategies = strategies,
                                postOfficeIcon = postOfficeIcon,
                                dataLoader = dataLoader,
                            ) as T
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
        val selectedMarker = viewModel.selectedMarker.collectAsState().value
        val markers = viewModel.markerList.collectAsState().value
        val mapViewState = viewModel.mapViewState.collectAsState().value
        val isMapLoaded = viewModel.isMapLoaded.collectAsState().value

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
                    onMarkerClick = viewModel::onMarkerClick,
                    onInfoWndClick = viewModel::onInfoClick,
                )
            }
        }

        if (!isMapLoaded) {
            viewModel.loadPostOfficeData()
            CircularProgressIndicator()
        }
    }
}
