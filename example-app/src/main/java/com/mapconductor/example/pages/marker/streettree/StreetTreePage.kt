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
import com.mapconductor.streettree.StreetTreeDataLoader
import com.mapconductor.utils.LoadingDialog
import android.app.Activity

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
    // 再現用の入口。`adb shell am start ... --es camera "35.698,139.766,17.5"` のように渡すと、
    // その場・そのズームで開く（ios-sdk の `MAPCONDUCTOR_SAMPLE_INIT_CAMERA` と同じ）。
    val initCamera = remember { (context as? Activity)?.intent?.getStringExtra("camera") }

    val viewModel: StreetTreeViewModel =
        viewModel(
            factory =
                object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T =
                        StreetTreeViewModel(dataLoader, initCamera = initCamera) as T
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
                    onCameraMoveEnd = viewModel::onCameraMoveEnd,
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
