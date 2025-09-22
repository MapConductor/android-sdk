package com.mapconductor.example.pages.marker.icons

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.map.MapViewState
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold

@Composable
fun MarkerBasicPage(onToggleSidebar: () -> Unit = {}) {
    val initCameraPosition =
        remember {
            MapCameraPositionImpl(
                position = GeoPointImpl(0.014, 0.008),
                zoom = 15.0,
            )
        }

    var mapViewState by remember { mutableStateOf<MapViewState<*>?>(null) }

    DemoMapPageScaffold(
        menuItems = DefaultMapViewItems(initCameraPosition),
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = { newMapViewState ->
            mapViewState = newMapViewState
        },
    ) { paddingValues ->
        mapViewState?.let {
            MarkerBasicMapComponent(
                mapViewState = it,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
