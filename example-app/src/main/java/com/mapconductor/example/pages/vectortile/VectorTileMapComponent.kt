package com.mapconductor.example.pages.vectortile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.mapconductor.core.OnMapLoadedHandler
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.map.VectorStyleSupportKey
import com.mapconductor.example.MapViewContainer
import com.mapconductor.vectortile.VectorTileLayer

@Composable
fun VectorTileMapComponent(
    mapViewState: MapViewStateInterface<*>?,
    styleJson: String?,
    opacity: Float,
    asBasemap: Boolean = false,
    tileSize: Int? = null,
    modifier: Modifier = Modifier,
    onMapLoaded: OnMapLoadedHandler? = null,
    onDiagnostics: (List<String>) -> Unit = {},
) {
    val diskCacheDir = LocalContext.current.cacheDir.resolve("vectortile")

    // Blanking the backend's own basemap is per-provider by construction: the
    // design type is provider-specific, and only some of them have a "draw
    // nothing" value. Where there is none, the opaque tiles cover it anyway —
    // the difference is whether the device also fetches a basemap nobody sees.
    LaunchedEffect(mapViewState, asBasemap) {
        val state = mapViewState ?: return@LaunchedEffect
        // A map that takes the style directly gets it *as* its design from
        // the layer; blanking first would only load one style to throw it
        // away. Going back to the provider's own basemap is still done here.
        if (asBasemap && state.serviceRegistry.has(VectorStyleSupportKey)) return@LaunchedEffect
        showProviderBasemap(state, visible = !asBasemap)
    }

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
                    asBasemap = asBasemap,
                )
            }
        }
    }
}
