package com.mapconductor.example.pages.groundimage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import com.mapconductor.core.groundimage.GroundImage
import com.mapconductor.core.groundimage.OnGroundImageClickEventHandler
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.example.MapViewContainer

@Composable
fun GroundImageMapComponent(
    mapViewState: MapViewState<*>?,
    viewModel: GroundImageMapPageViewModel,
    modifier: Modifier = Modifier,
    onMapClick: OnMapEventHandler = {},
    onGroundImageClick: OnGroundImageClickEventHandler = {},
) {
    mapViewState?.let { mapViewState ->
        MapViewContainer(
            modifier = modifier,
            state = mapViewState,
            onMapClick = onMapClick,
            onGroundImageClick = onGroundImageClick,
        ) {
            // GroundImage
            key(viewModel.groundImageState.id) {
                GroundImage(viewModel.groundImageState)
            }
        }
    }
}
