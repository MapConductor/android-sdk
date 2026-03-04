package com.mapconductor.example.pages.map.design

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.example.MapViewContainer

@Composable
fun MapDesignMapComponent(
    mapViewState: MapViewStateInterface<*>?,
    modifier: Modifier = Modifier,
) {
    mapViewState?.let { state ->

        MapViewContainer(
            modifier = modifier,
            state = state,
        )
    }
}
