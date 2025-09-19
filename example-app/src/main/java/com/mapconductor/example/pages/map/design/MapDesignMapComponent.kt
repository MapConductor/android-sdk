package com.mapconductor.example.pages.map.design

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapconductor.core.map.MapViewState
import com.mapconductor.example.MapViewContainer

@Composable
fun MapDesignMapComponent(
    mapViewState: MapViewState<*>?,
    modifier: Modifier = Modifier,
) {
    mapViewState?.let { state ->

        MapViewContainer(
            modifier = modifier,
            state = state,
        )
    }
}
