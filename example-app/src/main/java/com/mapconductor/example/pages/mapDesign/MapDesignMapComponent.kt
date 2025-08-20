package com.mapconductor.example.pages.mapDesign

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.example.MapViewContainer

@Composable
fun MapDesignMapComponent(
    mapViewState: MapViewState<*>?,
    modifier: Modifier = Modifier,
    onMapClick: OnMapEventHandler = {},
) {
    mapViewState?.let { state ->
        MapViewContainer(
            modifier = modifier,
            state = state,
            onMapClick = onMapClick,
        ) {}
    }
}
