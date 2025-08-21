package com.mapconductor.example.pages.polyline

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold

@Composable
fun PolylineMapPage(
    viewModel: PolylinePageViewModel = PolylinePageViewModelImpl(),
    onToggleSidebar: () -> Unit = {},
) {
    DemoMapPageScaffold(
        menuItems = DefaultMapViewItems(viewModel.initCameraPosition),
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = viewModel::onMapViewChanged,
    ) { paddingValues ->
        val mapViewState = viewModel.mapViewState.collectAsState()

        PolylineMapComponent(
            polylineState = viewModel.polylineState,
            wayPointMarkers = viewModel.wayPointMarkers,
            mapViewState = mapViewState.value,
            onPolylineClick = viewModel::onPolylineClick,
            onMarkerDrag = viewModel::onMarkerDrag,
        )

//        MessageCard(
//            modifier =
//                Modifier
//                    .align(Alignment.BottomStart)
//                    .padding(
//                        bottom = paddingValues.calculateBottomPadding() + 16.dp,
//                        start = paddingValues.calculateStartPadding(LayoutDirection.Ltr) + 16.dp,
//                        end = paddingValues.calculateEndPadding(LayoutDirection.Ltr) + 16.dp,
//                    ),
//            title = "Messages",
//        ) {
//            MapViewStatePanel(
//                mapViewState.value
//                    ?.mapCameraPosition
//                    ?.collectAsState()
//                    ?.value,
//            )
//        }
//
//        ToastHost(
//            messages = viewModel.messages.collectAsState().value,
//            onDismiss = { viewModel.removeToast(it) },
//        )
    }
}
