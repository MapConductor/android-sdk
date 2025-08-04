package com.mapconductor.example.pages.polyline

import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mapconductor.example.toast.ToastHost
import com.mapconductor.example.ui.DemoMapPageScaffold
import com.mapconductor.example.ui.MapViewStatePanel
import com.mapconductor.example.ui.MessageCard

@Composable
fun PolylineMapPage(
    viewModel: PolylinePageViewModel = PolylinePageViewModelImpl(),
    onToggleSidebar: () -> Unit = {},
) {
    DemoMapPageScaffold(
        initCameraPosition = viewModel.initCameraPosition,
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = viewModel::onMapViewChanged,
    ) { paddingValues ->
        val mapViewState = viewModel.mapViewState.collectAsState()

        PolylineMapComponent(
            mapViewState = mapViewState.value,
            viewModel = viewModel,
            onMapClickHandler = viewModel::onMapClick,
            onMarkerClickHandler = viewModel::onMarkerClick,
            onPolylineClickHandler = viewModel::onPolylineClick,
            onMarkerDrag = viewModel::onMarkerDrag,
        )

        MessageCard(
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        bottom = paddingValues.calculateBottomPadding() + 16.dp,
                        start = paddingValues.calculateStartPadding(LayoutDirection.Ltr) + 16.dp,
                        end = paddingValues.calculateEndPadding(LayoutDirection.Ltr) + 16.dp,
                    ),
            title = "Messages",
        ) {
            MapViewStatePanel(
                mapViewState.value
                    ?.mapCameraPosition
                    ?.collectAsState()
                    ?.value,
            )
        }

        ToastHost(
            messages = viewModel.messages.collectAsState().value,
            onDismiss = { viewModel.removeToast(it) },
        )
    }
}