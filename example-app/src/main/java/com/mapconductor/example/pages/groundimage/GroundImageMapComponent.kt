package com.mapconductor.example.pages.groundimage

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapconductor.core.groundimage.GroundImage
import com.mapconductor.core.groundimage.OnGroundImageEventHandler
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.example.MapViewContainer

@Composable
fun GroundImageMapComponent(
    mapViewState: MapViewState<*>?,
    viewModel: GroundImageMapPageViewModel,
    modifier: Modifier = Modifier,
) {
    mapViewState?.let { mapViewState ->
        MapViewContainer(
            modifier = modifier,
            state = mapViewState,
        ) {
            // GroundImage
            GroundImage(viewModel.groundImageState)

            // BoundsMarkers
            viewModel.markers.forEach { marker ->
                Marker(marker)
            }
        }
    }
}
