package com.mapconductor.example.pages.groundimage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
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
    onGroundImageClick: OnGroundImageEventHandler = {},
    onMarkerDrag: OnMarkerEventHandler = {},
) {
    mapViewState?.let { mapViewState ->
        MapViewContainer(
            modifier = modifier,
            state = mapViewState,
            onGroundImageClick = onGroundImageClick,
            onMarkerDrag = onMarkerDrag,
        ) {
            // GroundImage
            key(viewModel.groundImageState.id) {
                GroundImage(viewModel.groundImageState)
            }

            // BoundsMarkers
            viewModel.markers.forEach { marker ->
                key(marker.fingerPrint()) {
                    Marker(marker)
                }
            }
        }
    }
}
