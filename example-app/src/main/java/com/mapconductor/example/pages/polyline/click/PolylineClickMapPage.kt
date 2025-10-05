package com.mapconductor.example.pages.polyline

import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold
import com.mapconductor.example.ui.MessageCard

@Composable
fun PolylineClickMapPage(onToggleSidebar: () -> Unit = {}) {
    val viewModel = remember { PolylineClickPageViewModelImpl() }
    DemoMapPageScaffold(
        menuItems = DefaultMapViewItems(viewModel.initCameraPosition),
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = viewModel::onMapViewChanged,
    ) { paddingValues ->
        val mapViewState = viewModel.mapViewState.collectAsState()
        val markers = viewModel.markers.collectAsState()
        PolylineClickMapComponent(
            polylineState = viewModel.polylineState,
            markers = markers.value,
            mapViewState = mapViewState.value,
            onPolylineClick = viewModel::onPolylineClicked,
        )

        MessageCard(
            title = "Description",
            isCardOpen = true,
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        bottom = paddingValues.calculateBottomPadding() + 16.dp,
                        start = paddingValues.calculateStartPadding(LayoutDirection.Ltr) + 16.dp,
                        end = paddingValues.calculateEndPadding(LayoutDirection.Ltr) + 16.dp,
                    ),
        ) {
            Text("Tap on the curved polyline. A marker would be placed on the tapped polyline.")
        }
    }
}
