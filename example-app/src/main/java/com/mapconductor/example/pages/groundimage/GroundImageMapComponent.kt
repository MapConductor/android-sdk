package com.mapconductor.example.pages.groundimage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import com.mapconductor.core.circle.Circle
import com.mapconductor.core.circle.OnCircleEventHandler
import com.mapconductor.core.groundimage.GroundImage
import com.mapconductor.core.groundimage.OnGroundImageEventHandler
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.example.MapViewContainer
import com.mapconductor.example.pages.circle.CirclePageViewModel

@Composable
fun GroundImageMapComponent(
    mapViewState: MapViewState<*>?,
    viewModel: GroundImageMapPageViewModel,
    modifier: Modifier = Modifier,
    onMapClick: OnMapEventHandler = {},
    onGroundImageClick: OnGroundImageEventHandler = {},
    onGroundImageChange: OnGroundImageEventHandler = {},
) {
    mapViewState?.let { mapViewState ->
        MapViewContainer(
            modifier = modifier,
            state = mapViewState,
            onMapClick = onMapClick,
            onGroundImageClick = onGroundImageClick,
            onGroundImageChange = onGroundImageChange,
        ) {
            // GroundImage
            key(viewModel.groundImageState.id) {
                GroundImage(viewModel.groundImageState)
            }
        }
    }
}
