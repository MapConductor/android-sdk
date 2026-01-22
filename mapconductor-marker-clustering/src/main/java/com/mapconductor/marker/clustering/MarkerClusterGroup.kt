package com.mapconductor.marker.clustering

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.map.LocalMapServiceRegistry
import com.mapconductor.core.map.LocalMapViewController
import com.mapconductor.core.marker.LocalMarkerCollector
import com.mapconductor.core.marker.MarkerCollector
import com.mapconductor.core.marker.MarkerIconInterface
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.Markers
import com.mapconductor.core.marker.MarkerRenderingSupport
import com.mapconductor.core.marker.MarkerRenderingSupportKey
import com.mapconductor.core.marker.MarkerRenderingStrategyInterface
import com.mapconductor.core.marker.StrategyMarkerController
import com.mapconductor.core.polygon.LocalPolygonCollector
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.settings.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce

@Composable
fun <ActualMarker> MapViewScope.MarkerClusterGroup(
    state: MarkerClusterGroupState<ActualMarker>,
    markers: List<MarkerState>,
    content: @Composable () -> Unit = {},
) {
    MarkerClusterGroup(state = state) {
        Markers(markers)
        content()
    }
}

@Composable
fun <ActualMarker> MapViewScope.MarkerClusterGroup(
    state: MarkerClusterGroupState<ActualMarker>,
    content: @Composable () -> Unit,
) {
    val strategy =
        remember(
            state.clusterRadiusPx,
            state.minClusterSize,
            state.expandMargin,
            state.clusterIconProvider,
            state.onClusterClick,
            state.enableZoomAnimation,
            state.enablePanAnimation,
            state.zoomAnimationDurationMillis,
            state.debugHullPolygons,
            state.cameraIdleDebounceMillis,
            state.tileSize,
        ) {
            MarkerClusterStrategy<ActualMarker>(
                clusterRadiusPx = state.clusterRadiusPx,
                minClusterSize = state.minClusterSize,
                expandMargin = state.expandMargin,
                clusterIconProvider = state.clusterIconProvider,
                onClusterClick = state.onClusterClick,
                enableZoomAnimation = state.enableZoomAnimation,
                enablePanAnimation = state.enablePanAnimation,
                zoomAnimationDurationMillis = state.zoomAnimationDurationMillis,
                debugHullPolygons = state.debugHullPolygons,
                cameraIdleDebounceMillis = state.cameraIdleDebounceMillis,
                tileSize = state.tileSize,
            )
        }

    MarkerRenderingGroup(strategy = strategy) {
        if (state.debugHullPolygons) {
            val polygonCollector = LocalPolygonCollector.current
            val debugInfos by strategy.debugInfoFlow.collectAsState()
            val colorsByCell = remember(debugInfos) { assignDistinctDebugColors(debugInfos) }
            var activeHullIds by remember { mutableStateOf<Set<String>>(emptySet()) }

            LaunchedEffect(
                debugInfos,
                state.debugHullStrokeWidth,
                state.debugHullStrokeAlpha,
                state.debugHullFillAlpha,
            ) {
                val nextStates =
                    withContext(Dispatchers.Default) {
                        debugInfos
                            .asSequence()
                            .filter { it.hullPoints.size >= 3 }
                            .map { info ->
                                val baseColor = colorsByCell[DebugCellKey(info.cellX, info.cellY)] ?: Color.Magenta
                                val fill = baseColor.copy(alpha = state.debugHullFillAlpha)
                                val stroke = baseColor.copy(alpha = state.debugHullStrokeAlpha)
                                PolygonState(
                                    id = "cluster-hull-${info.id}",
                                    points = info.hullPoints,
                                    strokeColor = stroke,
                                    strokeWidth = state.debugHullStrokeWidth,
                                    fillColor = fill,
                                    geodesic = false,
                                    zIndex = 9,
                                    extra = null,
                                    onClick = null,
                                )
                            }.toList()
                    }

                val nextIds = nextStates.map { it.id }.toSet()
                (activeHullIds - nextIds).forEach { polygonCollector.remove(it) }
                nextStates.forEach { polygonCollector.add(it) }
                activeHullIds = nextIds
            }

            DisposableEffect(Unit) {
                onDispose {
                    activeHullIds.forEach { polygonCollector.remove(it) }
                    activeHullIds = emptySet()
                }
            }
        }

        content()
    }
}

@Composable
fun <ActualMarker> MapViewScope.MarkerClusterGroup(
    clusterRadiusPx: Double = MarkerClusterStrategy.DEFAULT_CLUSTER_RADIUS_PX,
    minClusterSize: Int = MarkerClusterStrategy.DEFAULT_MIN_CLUSTER_SIZE,
    expandMargin: Double = MarkerClusterStrategy.DEFAULT_EXPAND_MARGIN,
    clusterIconProvider: (Int) -> MarkerIconInterface = MarkerClusterStrategy.DEFAULT_ICON_PROVIDER,
    onClusterClick: ((MarkerCluster) -> Unit)? = null,
    clusterRadiusStrokeColor: Color = Color.Red,
    clusterRadiusStrokeWidth: Dp = 1.dp,
    clusterRadiusFillColor: Color = Color.Transparent,
    enableZoomAnimation: Boolean = false,
    enablePanAnimation: Boolean = false,
    zoomAnimationDurationMillis: Long = MarkerClusterStrategy.DEFAULT_ZOOM_ANIMATION_DURATION_MILLIS,
    debugIncludeRenderCount: Boolean = false,
    debugHullPolygons: Boolean = false,
    debugHullStrokeWidth: Dp = 2.dp,
    debugHullStrokeAlpha: Float = 0.8f,
    debugHullFillAlpha: Float = 0.18f,
    cameraIdleDebounceMillis: Long = MarkerClusterStrategy.DEFAULT_CAMERA_DEBOUNCE_MILLIS,
    tileSize: Double = MarkerClusterStrategy.DEFAULT_TILE_SIZE,
    markers: List<MarkerState>,
    content: @Composable () -> Unit = {},
) {
    MarkerClusterGroup<ActualMarker> (
        clusterRadiusPx = clusterRadiusPx,
        minClusterSize = minClusterSize,
        expandMargin = expandMargin,
        clusterIconProvider = clusterIconProvider,
        onClusterClick = onClusterClick,
        clusterRadiusStrokeColor = clusterRadiusStrokeColor,
        clusterRadiusStrokeWidth = clusterRadiusStrokeWidth,
        clusterRadiusFillColor = clusterRadiusFillColor,
        enableZoomAnimation = enableZoomAnimation,
        enablePanAnimation = enablePanAnimation,
        zoomAnimationDurationMillis = zoomAnimationDurationMillis,
        debugIncludeRenderCount = debugIncludeRenderCount,
        debugHullPolygons = debugHullPolygons,
        debugHullStrokeWidth = debugHullStrokeWidth,
        debugHullStrokeAlpha = debugHullStrokeAlpha,
        debugHullFillAlpha = debugHullFillAlpha,
        cameraIdleDebounceMillis = cameraIdleDebounceMillis,
        tileSize = tileSize,
    ) {
        Markers(markers)
        content()
    }
}

@Composable
fun <ActualMarker> MapViewScope.MarkerClusterGroup(
    clusterRadiusPx: Double = MarkerClusterStrategy.DEFAULT_CLUSTER_RADIUS_PX,
    minClusterSize: Int = MarkerClusterStrategy.DEFAULT_MIN_CLUSTER_SIZE,
    expandMargin: Double = MarkerClusterStrategy.DEFAULT_EXPAND_MARGIN,
    clusterIconProvider: (Int) -> MarkerIconInterface = MarkerClusterStrategy.DEFAULT_ICON_PROVIDER,
    onClusterClick: ((MarkerCluster) -> Unit)? = null,
    clusterRadiusStrokeColor: Color = Color.Red,
    clusterRadiusStrokeWidth: Dp = 1.dp,
    clusterRadiusFillColor: Color = Color.Transparent,
    enableZoomAnimation: Boolean = false,
    enablePanAnimation: Boolean = false,
    zoomAnimationDurationMillis: Long = MarkerClusterStrategy.DEFAULT_ZOOM_ANIMATION_DURATION_MILLIS,
    debugIncludeRenderCount: Boolean = false,
    debugHullPolygons: Boolean = false,
    debugHullStrokeWidth: Dp = 2.dp,
    debugHullStrokeAlpha: Float = 0.8f,
    debugHullFillAlpha: Float = 0.18f,
    cameraIdleDebounceMillis: Long = MarkerClusterStrategy.DEFAULT_CAMERA_DEBOUNCE_MILLIS,
    tileSize: Double = MarkerClusterStrategy.DEFAULT_TILE_SIZE,
    content: @Composable () -> Unit,
) {
    val state =
        remember(
            clusterRadiusPx,
            minClusterSize,
            expandMargin,
            clusterIconProvider,
            onClusterClick,
            clusterRadiusStrokeColor,
            clusterRadiusStrokeWidth,
            clusterRadiusFillColor,
            enableZoomAnimation,
            enablePanAnimation,
            zoomAnimationDurationMillis,
            debugIncludeRenderCount,
            debugHullPolygons,
            debugHullStrokeWidth,
            debugHullStrokeAlpha,
            debugHullFillAlpha,
            cameraIdleDebounceMillis,
            tileSize,
        ) {
            MarkerClusterGroupState<ActualMarker>(
                clusterRadiusPx = clusterRadiusPx,
                minClusterSize = minClusterSize,
                expandMargin = expandMargin,
                clusterIconProvider = clusterIconProvider,
                onClusterClick = onClusterClick,
                enableZoomAnimation = enableZoomAnimation,
                enablePanAnimation = enablePanAnimation,
                zoomAnimationDurationMillis = zoomAnimationDurationMillis,
                debugHullPolygons = debugHullPolygons,
                debugHullStrokeWidth = debugHullStrokeWidth,
                debugHullStrokeAlpha = debugHullStrokeAlpha,
                debugHullFillAlpha = debugHullFillAlpha,
                cameraIdleDebounceMillis = cameraIdleDebounceMillis,
                tileSize = tileSize,
            )
        }
    MarkerClusterGroup(state = state, content = content)
}

private data class DebugCellKey(
    val x: Int,
    val y: Int,
)

private fun assignDistinctDebugColors(infos: List<MarkerClusterDebugInfo>): Map<DebugCellKey, Color> {
    if (infos.isEmpty()) return emptyMap()

    val palette =
        listOf(
            Color(0xFFE53935), // red
            Color(0xFFD81B60), // pink
            Color(0xFF8E24AA), // purple
            Color(0xFF5E35B1), // deep purple
            Color(0xFF3949AB), // indigo
            Color(0xFF1E88E5), // blue
            Color(0xFF039BE5), // light blue
            Color(0xFF00ACC1), // cyan
            Color(0xFF00897B), // teal
            Color(0xFF43A047), // green
            Color(0xFF7CB342), // light green
            Color(0xFFFDD835), // yellow
            Color(0xFFFFB300), // amber
            Color(0xFFFB8C00), // orange
        )

    val result = LinkedHashMap<DebugCellKey, Color>(infos.size * 2)
    val sorted = infos.sortedWith(compareBy<MarkerClusterDebugInfo> { it.cellX }.thenBy { it.cellY })

    fun neighborColors(key: DebugCellKey): Set<Color> {
        val used = mutableSetOf<Color>()
        for (dx in -1..1) {
            for (dy in -1..1) {
                if (dx == 0 && dy == 0) continue
                val c = result[DebugCellKey(key.x + dx, key.y + dy)] ?: continue
                used.add(c)
            }
        }
        return used
    }

    sorted.forEach { info ->
        val key = DebugCellKey(info.cellX, info.cellY)
        val used = neighborColors(key)
        val start = (info.id.hashCode() and 0x7fffffff) % palette.size
        var chosen: Color? = null
        for (i in palette.indices) {
            val candidate = palette[(start + i) % palette.size]
            if (candidate !in used) {
                chosen = candidate
                break
            }
        }
        result[key] = chosen ?: palette[start]
    }

    return result
}

@OptIn(FlowPreview::class)
@Composable
private fun <ActualMarker> MarkerRenderingGroup(
    strategy: MarkerRenderingStrategyInterface<ActualMarker>,
    content: @Composable () -> Unit,
) {
    val mapController = LocalMapViewController.current

    val services = LocalMapServiceRegistry.current
    @Suppress("UNCHECKED_CAST")
    val renderingSupport =
        services.get(MarkerRenderingSupportKey) as? MarkerRenderingSupport<ActualMarker> ?: return
    val markerCollector = remember { MarkerCollector() }
    val renderer =
        remember(mapController, strategy) {
            renderingSupport.createMarkerRenderer(strategy)
        }
    val markerController =
        remember(strategy, renderer) {
            StrategyMarkerController(
                strategy = strategy,
                renderer = renderer,
            )
        }
    val eventController =
        remember(markerController, renderer) {
            renderingSupport.createMarkerEventController(markerController, renderer)
        }

    var isRegistered by remember { mutableStateOf(false) }

    LaunchedEffect(mapController, markerController, eventController) {
        mapController.registerOverlayController(markerController)
        renderingSupport.registerMarkerEventController(eventController)
        isRegistered = true
    }

    DisposableEffect(markerCollector, markerController) {
        markerCollector.setUpdateHandler { markerState ->
            if (markerController.getEntity(markerState.id) != null) {
                withContext(Dispatchers.Default) {
                    markerController.update(markerState)
                }
            }
        }
        onDispose {
            markerCollector.setUpdateHandler(null)
        }
    }

    val mapLoaded = renderingSupport.mapLoadedState?.collectAsState()?.value ?: true
    var requestedInitialCameraUpdate by remember { mutableStateOf(false) }

    LaunchedEffect(mapLoaded, isRegistered) {
        if (!mapLoaded || !isRegistered || requestedInitialCameraUpdate) return@LaunchedEffect
        requestedInitialCameraUpdate = true
        renderingSupport.onMarkerRenderingReady()
    }

    LaunchedEffect(mapLoaded) {
        if (!mapLoaded) return@LaunchedEffect
        markerCollector.flow.collectLatest { markerMap ->
            // Avoid doing O(n) work on the UI thread for very large marker sets.
            val snapshot =
                withContext(Dispatchers.Default) {
                    markerMap.values.toList()
                }
            withContext(Dispatchers.Default) {
                markerController.add(snapshot)
            }
        }
    }

    CompositionLocalProvider(LocalMarkerCollector provides markerCollector) {
        content()
    }
}
