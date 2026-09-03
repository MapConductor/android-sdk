package com.mapconductor.example.pages.vectortile

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.mapconductor.core.OnMapLoadedHandler
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.example.MapViewContainer
import com.mapconductor.vectortile.VectorTileLayer

@Composable
fun VectorTileMapComponent(
    mapViewState: MapViewStateInterface<*>?,
    styleJson: String?,
    opacity: Float,
    tileSize: Int = 512,
    modifier: Modifier = Modifier,
    onMapLoaded: OnMapLoadedHandler? = null,
    onDiagnostics: (List<String>) -> Unit = {},
) {
    val diskCacheDir = LocalContext.current.cacheDir.resolve("vectortile")
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
                    tileSize = tileSize,
                    opacity = opacity,
                    diskCacheDir = diskCacheDir,
                    onDiagnostics = onDiagnostics,
                )
            }
        }
    }
}
