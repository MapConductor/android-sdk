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
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.heatmap.HeatmapPointCollector
import com.mapconductor.core.heatmap.HeatmapPointState
import com.mapconductor.core.heatmap.LocalHeatmapPointCollector
import com.mapconductor.core.map.LocalMapViewController
import com.mapconductor.core.raster.RasterLayer
import com.mapconductor.core.raster.RasterLayerState
import com.mapconductor.core.raster.RasterSource
import com.mapconductor.core.raster.TileScheme
import com.mapconductor.core.tileserver.TileServerRegistry
import com.mapconductor.settings.Settings
import java.util.UUID
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce

@OptIn(FlowPreview::class)
@Composable
fun MapViewScope.HeatmapOverlay(
    radiusPx: Int = HeatmapDefaults.DEFAULT_RADIUS_PX,
    opacity: Double = HeatmapDefaults.DEFAULT_OPACITY,
    gradient: HeatmapGradient = HeatmapGradient.DEFAULT,
    maxIntensity: Double? = null,
    weightProvider: (HeatmapPointState) -> Double = { state -> state.weight },
    content: @Composable () -> Unit,
) {
    val pointCollector = remember { HeatmapPointCollector() }
    val groupId = remember { UUID.randomUUID().toString() }
    val tileServer = remember { TileServerRegistry.get() }
    val renderer = remember { HeatmapTileRenderer() }
    val mapController = LocalMapViewController.current
    val cameraController = remember(renderer) { HeatmapCameraController(renderer) }
    var version by remember { mutableStateOf(0L) }

    val rasterLayerState =
        remember(groupId, tileServer, renderer) {
            RasterLayerState(
                id = "heatmap-$groupId",
                source =
                    RasterSource.UrlTemplate(
                        template = tileServer.urlTemplate(groupId, version),
                        tileSize = renderer.tileSize,
                        scheme = TileScheme.XYZ,
                    ),
                opacity = opacity.toFloat().coerceIn(0.0f, 1.0f),
                visible = true,
                extra = version,
            )
        }

    LaunchedEffect(opacity) {
        rasterLayerState.opacity = opacity.toFloat().coerceIn(0.0f, 1.0f)
    }

    DisposableEffect(groupId, tileServer, renderer) {
        tileServer.register(groupId, renderer)
        onDispose {
            tileServer.unregister(groupId)
        }
    }

    DisposableEffect(mapController, cameraController) {
        mapController.registerOverlayController(cameraController)
        onDispose {
            cameraController.destroy()
        }
    }

    val points = pointCollector.flow.collectAsState()
    val updateToken = remember { mutableStateOf(0L) }

    points.value.values.forEach { pointState ->
        LaunchedEffect(pointState.id, weightProvider) {
            pointState
                .asFlow()
                .debounce(Settings.Default.composeEventDebounce)
                .collectLatest {
                    updateToken.value += 1
                }
        }
    }

    LaunchedEffect(points.value, updateToken.value, radiusPx, gradient, maxIntensity, weightProvider) {
        val heatmapPoints =
            points.value.values.mapNotNull { pointState ->
                val weight = weightProvider(pointState)
                if (weight.isNaN() || weight <= 0.0) {
                    null
                } else {
                    HeatmapPoint(
                        position = pointState.position,
                        weight = weight,
                    )
                }
            }
        renderer.update(
            points = heatmapPoints,
            radiusPx = radiusPx,
            gradient = gradient,
            maxIntensity = maxIntensity,
        )
        version += 1
        rasterLayerState.source =
            RasterSource.UrlTemplate(
                template = tileServer.urlTemplate(groupId, version),
                tileSize = renderer.tileSize,
                scheme = TileScheme.XYZ,
            )
        rasterLayerState.extra = version
    }

    RasterLayer(state = rasterLayerState)

    CompositionLocalProvider(LocalHeatmapPointCollector provides pointCollector) {
        content()
    }
}
