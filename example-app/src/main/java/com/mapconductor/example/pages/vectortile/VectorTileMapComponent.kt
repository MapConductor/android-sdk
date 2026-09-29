package com.mapconductor.example.pages.vectortile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.mapconductor.arcgis.ArcGISDesign
import com.mapconductor.arcgis.ArcGISMapViewStateInterface
import com.mapconductor.core.OnMapLoadedHandler
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.example.MapViewContainer
import com.mapconductor.googlemaps.GoogleMapDesign
import com.mapconductor.googlemaps.GoogleMapViewStateInterface
import com.mapconductor.longdo.LongdoDesign
import com.mapconductor.longdo.LongdoViewStateInterface
import com.mapconductor.mapbox.MapboxMapDesign
import com.mapconductor.mapbox.MapboxViewStateInterface
import com.mapconductor.maptiler.MapTilerDesign
import com.mapconductor.maptiler.MapTilerViewStateInterface
import com.mapconductor.openmobilemaps.OpenMobileMapsDesign
import com.mapconductor.openmobilemaps.OpenMobileMapsViewStateInterface
import com.mapconductor.tomtom.TomTomMapDesign
import com.mapconductor.tomtom.TomTomMapViewStateInterface
import com.mapconductor.maplibre.MapLibreDesign
import com.mapconductor.maplibre.MapLibreViewStateInterface
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
        when (state) {
            is MapLibreViewStateInterface ->
                state.mapDesignType =
                    if (asBasemap) {
                        MapLibreDesign(id = "blank", styleJsonURL = "asset://blank-style.json")
                    } else {
                        MapLibreDesign.OsmBrightJa
                    }
            is GoogleMapViewStateInterface ->
                state.mapDesignType =
                    if (asBasemap) GoogleMapDesign.None else GoogleMapDesign.Normal
            is ArcGISMapViewStateInterface ->
                state.mapDesignType =
                    if (asBasemap) ArcGISDesign.None else ArcGISDesign.OsmStandard
            is MapboxViewStateInterface ->
                state.mapDesignType =
                    if (asBasemap) MapboxMapDesign.None else MapboxMapDesign.Standard
            is TomTomMapViewStateInterface ->
                state.mapDesignType =
                    if (asBasemap) TomTomMapDesign.None else TomTomMapDesign.Standard
            is MapTilerViewStateInterface ->
                state.mapDesignType =
                    if (asBasemap) MapTilerDesign.None else MapTilerDesign.Streets
            is LongdoViewStateInterface ->
                state.mapDesignType =
                    if (asBasemap) LongdoDesign.None else LongdoDesign.Normal
            is OpenMobileMapsViewStateInterface ->
                state.mapDesignType =
                    if (asBasemap) OpenMobileMapsDesign.None else OpenMobileMapsDesign.OpenStreetMap
            // HERE and Mappls have no design that draws nothing; the opaque
            // tiles cover their basemap, which is still fetched.
            else -> Unit
        }
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
                )
            }
        }
    }
}
