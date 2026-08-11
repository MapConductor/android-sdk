package com.mapconductor.example.pages.groundimage

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapconductor.compose.groundimage.GroundImage
import com.mapconductor.compose.marker.Marker
import com.mapconductor.compose.polyline.Polyline
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.example.MapViewContainer

@Composable
fun GroundImageMapComponent(
    mapViewState: MapViewStateInterface<*>?,
    viewModel: GroundImageMapPageViewModelInterface,
    modifier: Modifier = Modifier,
) {
    mapViewState?.let { mapViewState ->
        MapViewContainer(
            modifier = modifier,
            state = mapViewState,
        ) {
            // GroundImage
            GroundImage(viewModel.groundImageState)

            // 画像の外周をなぞる矩形。マーカーより先に置いて下に敷く。
            Polyline(viewModel.framePolyline)

            // BoundsMarkers
            viewModel.markers.forEach { marker ->
                Marker(marker)
            }
        }
    }
}
