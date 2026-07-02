package com.mapconductor.example.pages.map.fitbounds

import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
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
fun FitBoundsMapPage(onToggleSidebar: () -> Unit = {}) {
    val viewModel: FitBoundsPageViewModelInterface = remember { FitBoundsPageViewModel() }

    DemoMapPageScaffold(
        menuItems = DefaultMapViewItems(viewModel.initCameraPosition),
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = viewModel::onMapViewChanged,
    ) { paddingValues ->
        val mapViewState = viewModel.mapViewState.collectAsState()
        val boundsPolygon = viewModel.boundsPolygon.collectAsState()

        FitBoundsMapComponent(
            mapViewState = mapViewState.value,
            marker = viewModel.marker,
            boundsPolygon = boundsPolygon.value,
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
            title = "Fit Bounds",
        ) {
            androidx.compose.material3.Text("マーカーをドラッグして範囲を指定し、ドロップすると fitBounds で地図が移動します。")
        }
    }
}
