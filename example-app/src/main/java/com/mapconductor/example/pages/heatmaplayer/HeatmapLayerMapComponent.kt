package com.mapconductor.example.pages.heatmaplayer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.map.OnMapLoadedHandler
import com.mapconductor.example.MapViewContainer
import com.mapconductor.heatmap.HeatmapOverlay
import com.mapconductor.heatmap.HeatmapPointState
import com.mapconductor.heatmap.HeatmapPoints
import com.mapconductor.postoffice.PostOffice

@Composable
fun HeatmapLayerMapComponent(
    mapViewState: MapViewStateInterface<*>?,
    points: List<HeatmapPointState>,
    modifier: Modifier = Modifier,
    onMapLoaded: OnMapLoadedHandler? = null,
) {
    val heatmapPoints =
        remember(points) {
            points.map {
                HeatmapPointState(
                    position = it.position,
                )
            }
        }
    mapViewState?.let { it ->
        MapViewContainer(
            modifier = modifier,
            state = it,
            onMapLoaded = onMapLoaded,
        ) {
            HeatmapOverlay {
                HeatmapPoints(heatmapPoints)
            }
        }
    }
}
