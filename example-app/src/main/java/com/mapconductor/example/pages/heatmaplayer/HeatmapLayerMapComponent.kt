package com.mapconductor.example.pages.heatmaplayer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.map.OnMapLoadedHandler
import com.mapconductor.example.MapViewContainer
import com.mapconductor.heatmap.HeatmapOverlay
import com.mapconductor.heatmap.HeatmapPoint
import com.mapconductor.postoffice.PostOffice

@Composable
fun HeatmapLayerMapComponent(
    mapViewState: MapViewStateInterface<*>?,
    points: List<PostOffice>,
    modifier: Modifier = Modifier,
    onMapLoaded: OnMapLoadedHandler? = null,
) {
    mapViewState?.let { it ->
        MapViewContainer(
            modifier = modifier,
            state = it,
            onMapLoaded = onMapLoaded,
        ) {
            HeatmapOverlay {
                points.forEach { point ->
                    HeatmapPoint(
                        position = point.position
                    )
                }
            }

        }
    }
}
