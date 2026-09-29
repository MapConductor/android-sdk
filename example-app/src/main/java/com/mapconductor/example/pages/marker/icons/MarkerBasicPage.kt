package com.mapconductor.example.pages.marker.icons

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold

@Composable
fun MarkerBasicPage(
    modifier: Modifier = Modifier,
    onToggleSidebar: () -> Unit = {},
) {
    val initCameraPosition =
        remember {
            MapCameraPosition(
                position = GeoPoint(0.014, 0.008),
                zoom = 15.0,
            )
        }

    var mapViewState by remember { mutableStateOf<MapViewStateInterface<*>?>(null) }

    DemoMapPageScaffold(
        menuItems = DefaultMapViewItems(initCameraPosition),
        modifier = modifier,
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = { newMapViewState ->
            mapViewState = newMapViewState
        },
    ) {
        mapViewState?.let {
            MarkerBasicMapComponent(
                mapViewState = it,
            )
        }
    }
}
