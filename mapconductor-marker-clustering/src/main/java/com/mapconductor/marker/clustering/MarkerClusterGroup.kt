package com.mapconductor.marker.clustering

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.circle.CircleState
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
    val circleCollector = com.mapconductor.core.circle.LocalCircleCollector.current
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
            state.debugIncludeRenderCount,
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
                debugIncludeRenderCount = state.debugIncludeRenderCount,
                cameraIdleDebounceMillis = state.cameraIdleDebounceMillis,
                tileSize = state.tileSize,
            )
        }

    MarkerRenderingGroup(strategy = strategy, content = content)
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
                debugIncludeRenderCount = debugIncludeRenderCount,
                cameraIdleDebounceMillis = cameraIdleDebounceMillis,
                tileSize = tileSize,
            )
        }
    MarkerClusterGroup(state = state, content = content)
}

private const val CLUSTER_CIRCLE_ID_PREFIX = "cluster-circle-"

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
