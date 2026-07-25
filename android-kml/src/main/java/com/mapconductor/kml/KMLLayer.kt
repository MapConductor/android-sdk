package com.mapconductor.kml

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.mapconductor.compose.MapViewScope
import com.mapconductor.compose.raster.RasterLayer
import com.mapconductor.core.ChildCollectorImpl
import com.mapconductor.core.raster.RasterLayerSource
import com.mapconductor.core.raster.RasterLayerState
import com.mapconductor.core.raster.TileScheme
import com.mapconductor.core.tileserver.TileServerRegistry
import com.mapconductor.settings.Settings
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun MapViewScope.KMLLayer(
    state: KMLLayerState = remember { KMLLayerState() },
    features: List<KMLFeature> = emptyList(),
    tileSize: Int = KMLDefaults.DEFAULT_TILE_SIZE,
    disableTileServerCache: Boolean = false,
    content: @Composable () -> Unit = {},
) {
    val featureCollector =
        remember {
            ChildCollectorImpl<KMLFeatureState, KMLFeatureFingerPrint>(
                fingerPrintOf = { it.fingerPrint() },
                updateDebounce = Settings.Default.composeEventDebounce,
            )
        }
    val groupId = remember { UUID.randomUUID().toString() }
    val tileServer =
        remember(disableTileServerCache) {
            TileServerRegistry.get(forceNoStoreCache = disableTileServerCache)
        }
    val renderer = remember(tileSize) { KMLTileRenderer(tileSize = tileSize) }

    var isTileServerRegistered by remember { mutableStateOf(false) }
    var hasRenderedOnce by remember { mutableStateOf(false) }

    fun tileTemplate(cacheBuster: Long): String {
        val base = tileServer.urlTemplate(groupId, renderer.tileSize)
        return "$base?cb=$cacheBuster"
    }

    val rasterLayerState =
        remember(groupId, tileServer, renderer) {
            RasterLayerState(
                id = "kml-$groupId",
                source =
                    RasterLayerSource.UrlTemplate(
                        template = tileTemplate(0),
                        tileSize = renderer.tileSize,
                        maxZoom = KMLDefaults.DEFAULT_MAX_ZOOM,
                        scheme = TileScheme.XYZ,
                    ),
                opacity = state.opacity.coerceIn(0f, 1f),
                visible = state.visible,
            )
        }

    LaunchedEffect(state.opacity, state.visible) {
        rasterLayerState.opacity = state.opacity.coerceIn(0f, 1f)
        rasterLayerState.visible = state.visible
    }

    DisposableEffect(groupId, tileServer, renderer) {
        tileServer.register(groupId, renderer)
        isTileServerRegistered = true
        onDispose {
            tileServer.unregister(groupId)
            isTileServerRegistered = false
        }
    }

    DisposableEffect(renderer, state) {
        state.renderer = renderer
        onDispose {
            state.renderer = null
        }
    }

    val composedFeatures = featureCollector.flow.collectAsState()
    var updateToken by remember { mutableStateOf(0L) }

    LaunchedEffect(
        features,
        composedFeatures.value,
        state.strokeColor,
        state.fillColor,
        state.strokeWidth,
        state.pointRadius,
        state.styleProvider,
    ) {
        val layerStyle =
            KMLTileRenderer.LayerStyle(
                strokeColor = state.strokeColor,
                fillColor = state.fillColor,
                strokeWidth = state.strokeWidth,
                pointRadius = state.pointRadius,
            )
        val dynamicList = composedFeatures.value.values.toList()
        if (features.isEmpty() && dynamicList.isEmpty()) {
            hasRenderedOnce = false
            withContext(Dispatchers.Default) {
                renderer.update(emptyList<KMLFeature>(), emptyList(), layerStyle, state.styleProvider)
            }
            return@LaunchedEffect
        }
        withContext(Dispatchers.Default) {
            renderer.update(features, dynamicList, layerStyle, state.styleProvider)
        }
        hasRenderedOnce = true
        updateToken += 1
        rasterLayerState.source =
            RasterLayerSource.UrlTemplate(
                template = tileTemplate(updateToken),
                tileSize = renderer.tileSize,
                maxZoom = KMLDefaults.DEFAULT_MAX_ZOOM,
                scheme = TileScheme.XYZ,
            )
    }

    CompositionLocalProvider(LocalKMLFeatureCollector provides featureCollector) {
        if (isTileServerRegistered && hasRenderedOnce) {
            RasterLayer(state = rasterLayerState)
        }
        content()
    }
}
