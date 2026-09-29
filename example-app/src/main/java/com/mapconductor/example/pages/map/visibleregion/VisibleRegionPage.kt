package com.mapconductor.example.pages.map.visibleregion

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold

@Composable
fun VisibleRegionPage(modifier: Modifier = Modifier, onToggleSidebar: () -> Unit = {}) {
    // react-sdk / ios の同ページと同じカメラ。並べて数値を突き合わせるため揃えてある。
    val initCameraPosition =
        MapCameraPosition(
            position = GeoPoint(latitude = 21.3069, longitude = -157.8583),
            zoom = 10.0,
        )

    val viewModel = remember { VisibleRegionViewModel() }

    DemoMapPageScaffold(
        initSelect = 0,
        menuItems = DefaultMapViewItems(initCameraPosition),
        modifier = modifier,
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = viewModel::onMapViewStateChanged,
    ) {
        viewModel.mapViewState.value?.let { mapViewState ->
            VisibleRegionMapComponent(
                mapViewState = mapViewState,
                onMapLoaded = viewModel::onMapLoaded,
                onCameraChanged = viewModel::onCameraChanged,
            )
        }
    }
}
