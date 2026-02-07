package com.mapconductor.heatmap

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.mapconductor.core.ChildCollectorImpl
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.map.LocalMapViewController
import com.mapconductor.core.raster.RasterLayer
import com.mapconductor.core.raster.RasterLayerSource
import com.mapconductor.core.raster.RasterLayerState
import com.mapconductor.core.raster.TileScheme
import com.mapconductor.core.tileserver.TileServerRegistry
import com.mapconductor.settings.Settings
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun MapViewScope.HeatmapOverlay(
    state: HeatmapOverlayState,
    content: @Composable () -> Unit,
) {
    HeatmapOverlay(
        radiusPx = state.radiusPx,
        opacity = state.opacity,
        gradient = state.gradient,
        maxIntensity = state.maxIntensity,
        weightProvider = state.weightProvider,
        content = content,
    )
}

@Composable
fun MapViewScope.HeatmapOverlay(
    radiusPx: Int = HeatmapDefaults.DEFAULT_RADIUS_PX,
    opacity: Double = HeatmapDefaults.DEFAULT_OPACITY,
    gradient: HeatmapGradient = HeatmapGradient.DEFAULT,
    maxIntensity: Double? = null,
    weightProvider: (HeatmapPointState) -> Double = { state -> state.weight },
    tileSize: Int = HeatmapTileRenderer.DEFAULT_TILE_SIZE,
    trackPointUpdates: Boolean = false,
    disableTileServerCache: Boolean = false,
    content: @Composable () -> Unit,
) {
    val pointCollector =
        remember {
            ChildCollectorImpl<HeatmapPointState, HeatmapPointFingerPrint>(
                asFlow = { it.asFlow() },
                updateDebounce = Settings.Default.composeEventDebounce,
            )
        }
    val groupId = remember { UUID.randomUUID().toString() }
    val tileServer =
        remember(disableTileServerCache) {
            TileServerRegistry.get(forceNoStoreCache = disableTileServerCache)
        }
    val renderer =
        remember(tileSize) {
            HeatmapTileRenderer(
                tileSize = tileSize,
            )
        }
    val mapController = LocalMapViewController.current
    val cameraController = remember(renderer) { HeatmapCameraController(renderer) }
    var isTileServerRegistered by remember { mutableStateOf(false) }
    var hasRenderedOnce by remember { mutableStateOf(false) }

    fun tileTemplate(cacheBuster: Long): String {
        val base = tileServer.urlTemplate(groupId, renderer.tileSize)
        return "$base?cb=$cacheBuster"
    }

    val rasterLayerState =
        remember(groupId, tileServer, renderer) {
            RasterLayerState(
                id = "heatmap-$groupId",
                source =
                    RasterLayerSource.UrlTemplate(
                        template = tileTemplate(0),
                        tileSize = renderer.tileSize,
                        maxZoom = HeatmapDefaults.DEFAULT_MAX_ZOOM,
                        scheme = TileScheme.XYZ,
                    ),
                opacity = opacity.toFloat().coerceIn(0.0f, 1.0f),
                visible = true,
            )
        }

    LaunchedEffect(opacity) {
        rasterLayerState.opacity = opacity.toFloat().coerceIn(0.0f, 1.0f)
    }

    DisposableEffect(groupId, tileServer, renderer) {
        tileServer.register(groupId, renderer)
        isTileServerRegistered = true
        onDispose {
            tileServer.unregister(groupId)
            isTileServerRegistered = false
        }
    }

    DisposableEffect(mapController, cameraController) {
        mapController.registerOverlayController(cameraController)
        onDispose {
            cameraController.destroy()
        }
    }

    val points = pointCollector.flow.collectAsState()
    var updateToken by remember { mutableStateOf(0L) }

    DisposableEffect(pointCollector) {
        if (trackPointUpdates) {
            pointCollector.setUpdateHandler {
                updateToken += 1
            }
        } else {
            pointCollector.setUpdateHandler(null)
        }
        onDispose {
            pointCollector.setUpdateHandler(null)
        }
    }

    LaunchedEffect(points.value, updateToken, radiusPx, gradient, maxIntensity, weightProvider) {
        val heatmapPoints =
            points.value.values.mapNotNull { pointState ->
                val weight = weightProvider(pointState)
                if (weight.isNaN() || weight <= 0.0) {
                    null
                } else {
                    com.mapconductor.heatmap.HeatmapPoint(
                        position = pointState.position,
                        weight = weight,
                    )
                }
            }
        if (heatmapPoints.isEmpty()) {
            hasRenderedOnce = false
            withContext(Dispatchers.Default) {
                renderer.update(
                    points = emptyList(),
                    radiusPx = radiusPx,
                    gradient = gradient,
                    maxIntensity = maxIntensity,
                )
            }
            return@LaunchedEffect
        }
        withContext(Dispatchers.Default) {
            renderer.update(
                points = heatmapPoints,
                radiusPx = radiusPx,
                gradient = gradient,
                maxIntensity = maxIntensity,
            )
        }
        hasRenderedOnce = true
        rasterLayerState.source =
            RasterLayerSource.UrlTemplate(
                template = tileTemplate(updateToken),
                tileSize = renderer.tileSize,
                maxZoom = HeatmapDefaults.DEFAULT_MAX_ZOOM,
                scheme = TileScheme.XYZ,
            )
    }

    CompositionLocalProvider(LocalHeatmapPointCollector provides pointCollector) {
        // Avoid returning NO_TILE for the initial viewport (Google Maps may cache it).
        if (isTileServerRegistered && hasRenderedOnce) {
            RasterLayer(state = rasterLayerState)
        }
        content()
    }
}
