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
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield

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

        // Progressive loading: process markers in chunks to prevent ANR
        val allMarkerStates = mutableListOf<MarkerState>()
        val chunks = postOffices.chunked(50) // Process 50 markers at a time

        chunks.forEach { chunk ->
            val chunkMarkerStates =
                chunk.map {
                    MarkerState(
                        position = it.position,
                        id = it.hashCode().toString(),
                        icon = icon,
                        extra = it,
                    )
                }
            allMarkerStates.addAll(chunkMarkerStates)

            // Update state progressively
            postOfficesState.value = allMarkerStates.toList()

            // Yield to allow other coroutines and UI updates
            yield()

            // Small delay to prevent overwhelming the UI thread
            delay(16) // One frame delay (60fps = 16ms per frame)
        }
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
            PostOfficeViewModelImpl(context, icons, postOfficesState.value!!)
        }
    val selectedMarker = viewModel.selectedMarker

    DemoMapPageScaffold(
        initSelect = 2,
        menuItems = DefaultMapViewItems(viewModel.initCameraPosition),
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = viewModel::onMapViewChanged,
    ) {
        viewModel.mapViewState.value?.let { mapViewState ->
            PostOfficeMapComponent(
                mapViewState = mapViewState,
                renderingStrategy = viewModel.renderingStrategy.value,
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
