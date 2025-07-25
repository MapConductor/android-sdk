package com.mapconductor.example.pages.stores

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mapconductor.core.marker.DefaultIcon
import com.mapconductor.core.marker.MarkerAnimation
import com.mapconductor.example.MapArea
import com.mapconductor.example.toast.ToastHost

@Composable
fun StoreMapPage(viewModel: StoreMapPageViewModel) {
    val markerList = remember {
        viewModel.markerList.subList(0, 5).map {
            it.copy(draggable = true)
        }
    }
    val mapViewState = viewModel.mapViewState.collectAsState().value

    Box(modifier = Modifier.fillMaxSize()) {
        MapArea(
            mapViewState = mapViewState,
            markers = markerList,
            onDirectionButtonClick = { state ->
                state.icon?.let {
                    state.icon = (it as? DefaultIcon)?.copy(
                        fillColor = Color.Blue,
                    ) ?: it
                }
                state.animation = MarkerAnimation.Bounce
            },
            infoBubbleState = viewModel.infoBubbleState,
            onMapClickHandler = viewModel::onMapClick,
            onMarkerClickHandler = viewModel::onMarkerClick,
            onCircleClickHandler = viewModel::onCircleClick,
            selectedMarker = viewModel.selectedMarker,
        )

        ToastHost(
            messages = viewModel.messages.collectAsState().value,
            onDismiss = { viewModel.removeToast(it) },
        )
    }
}
