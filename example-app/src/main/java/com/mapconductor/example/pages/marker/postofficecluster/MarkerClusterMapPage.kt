package com.mapconductor.example.pages.marker.postofficecluster

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mapconductor.arcgis.ArcGISActualMarker
import com.mapconductor.arcgis.map.ArcGISMapViewStateInterface
import com.mapconductor.core.marker.ImageIcon
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold
import com.mapconductor.googlemaps.GoogleMapActualMarker
import com.mapconductor.googlemaps.GoogleMapViewStateInterface
import com.mapconductor.here.HereActualMarker
import com.mapconductor.here.HereViewStateInterface
import com.mapconductor.mapbox.MapboxActualMarker
import com.mapconductor.mapbox.MapboxViewStateInterface
import com.mapconductor.maplibre.MapLibreActualMarker
import com.mapconductor.maplibre.MapLibreViewStateInterface
import com.mapconductor.marker.clustering.MarkerClusterGroupState
import com.mapconductor.postoffice.PostOfficeDataLoader
import com.mapconductor.utils.LoadingDialog

@Composable
fun MarkerClusterMapPage(
    postOfficeIcon: ImageIcon,
    onToggleSidebar: () -> Unit = {},
) {
    val context = LocalContext.current
    val dataLoader = remember { PostOfficeDataLoader(context) }
    val googleClusterState =
        remember {
            MarkerClusterGroupState<GoogleMapActualMarker>(
                enableZoomAnimation = true,
                enablePanAnimation = true,
                debugHullPolygons = true,
            )
        }
    val mapboxClusterState =
        remember {
            MarkerClusterGroupState<MapboxActualMarker>(
                enableZoomAnimation = true,
                enablePanAnimation = true,
                debugHullPolygons = true,
            )
        }
    val hereClusterState =
        remember {
            MarkerClusterGroupState<HereActualMarker>(
                enableZoomAnimation = true,
                enablePanAnimation = true,
                debugHullPolygons = true,
            )
        }
    val arcgisClusterState =
        remember {
            MarkerClusterGroupState<ArcGISActualMarker>(
                enableZoomAnimation = true,
                enablePanAnimation = true,
                debugHullPolygons = true,
            )
        }
    val maplibreClusterState =
        remember {
            MarkerClusterGroupState<MapLibreActualMarker>(
                enableZoomAnimation = true,
                enablePanAnimation = true,
                debugHullPolygons = true,
            )
        }

    val viewModel: MarkerClusterMapPageViewModelInterface =
        viewModel<MarkerClusterMapPageViewModel>(
            factory =
                object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        if (modelClass.isAssignableFrom(MarkerClusterMapPageViewModel::class.java)) {
                            @Suppress("UNCHECKED_CAST")
                            return MarkerClusterMapPageViewModel(
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
                when (mapViewState) {
                    is GoogleMapViewStateInterface ->
                        MarkerClusterMapComponent<GoogleMapActualMarker>(
                            mapViewState = mapViewState,
                            selectedMarker = selectedMarker,
                            markers = markers,
                            onMapLoaded = viewModel::onMapLoaded,
                            onMapClick = viewModel::onMapClick,
                            onInfoWndClick = viewModel::onInfoClick,
                            clusterGroupState = googleClusterState,
                        )
                    is MapboxViewStateInterface ->
                        MarkerClusterMapComponent<MapboxActualMarker>(
                            mapViewState = mapViewState,
                            selectedMarker = selectedMarker,
                            markers = markers,
                            onMapLoaded = viewModel::onMapLoaded,
                            onMapClick = viewModel::onMapClick,
                            onInfoWndClick = viewModel::onInfoClick,
                            clusterGroupState = mapboxClusterState,
                        )
                    is HereViewStateInterface ->
                        MarkerClusterMapComponent<HereActualMarker>(
                            mapViewState = mapViewState,
                            selectedMarker = selectedMarker,
                            markers = markers,
                            onMapLoaded = viewModel::onMapLoaded,
                            onMapClick = viewModel::onMapClick,
                            onInfoWndClick = viewModel::onInfoClick,
                            clusterGroupState = hereClusterState,
                        )
                    is ArcGISMapViewStateInterface ->
                        MarkerClusterMapComponent<ArcGISActualMarker>(
                            mapViewState = mapViewState,
                            selectedMarker = selectedMarker,
                            markers = markers,
                            onMapLoaded = viewModel::onMapLoaded,
                            onMapClick = viewModel::onMapClick,
                            onInfoWndClick = viewModel::onInfoClick,
                            clusterGroupState = arcgisClusterState,
                        )
                    is MapLibreViewStateInterface ->
                        MarkerClusterMapComponent<MapLibreActualMarker>(
                            mapViewState = mapViewState,
                            selectedMarker = selectedMarker,
                            markers = markers,
                            onMapLoaded = viewModel::onMapLoaded,
                            onMapClick = viewModel::onMapClick,
                            onInfoWndClick = viewModel::onInfoClick,
                            clusterGroupState = maplibreClusterState,
                        )
                }
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

