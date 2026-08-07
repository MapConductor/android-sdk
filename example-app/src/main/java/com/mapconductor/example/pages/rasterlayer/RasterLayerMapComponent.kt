package com.mapconductor.example.pages.rasterlayer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapconductor.compose.raster.RasterLayer
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.raster.RasterLayerState
import com.mapconductor.example.MapViewContainer

@Composable
fun RasterLayerMapComponent(
    mapViewState: MapViewStateInterface<*>?,
    rasterLayerState: RasterLayerState,
    modifier: Modifier = Modifier,
) {
    mapViewState?.let { it ->
        MapViewContainer(
            modifier = modifier,
            state = it,
        ) {
            RasterLayer(rasterLayerState)
        }
    }
}
