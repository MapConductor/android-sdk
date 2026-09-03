package com.mapconductor.example.pages.vectortile

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapconductor.core.OnMapLoadedHandler
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.example.MapViewContainer
import com.mapconductor.vectortile.VectorTileLayer

@Composable
fun VectorTileMapComponent(
    mapViewState: MapViewStateInterface<*>?,
    styleJson: String?,
    opacity: Float,
    modifier: Modifier = Modifier,
    onMapLoaded: OnMapLoadedHandler? = null,
    onDiagnostics: (List<String>) -> Unit = {},
) {
    mapViewState?.let { state ->
        MapViewContainer(
            modifier = modifier,
            state = state,
            onMapLoaded = onMapLoaded,
        ) {
            // The layer is what any backend sees: an ordinary raster layer,
            // whose tiles happen to be rendered on-device from a vector style.
            if (styleJson != null) {
                VectorTileLayer(
                    styleJson = styleJson,
                    opacity = opacity,
                    onDiagnostics = onDiagnostics,
                )
            }
        }
    }
}
