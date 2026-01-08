package com.mapconductor.marker.clustering

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.marker.ColorDefaultIcon
import com.mapconductor.core.marker.MarkerIconInterface

@Composable
fun <ActualMarker> MapViewScope.MarkerClusterGroup(
    state: MarkerClusterGroupState<ActualMarker>,
    content: @Composable () -> Unit,
) {
    MarkerClusterGroup<ActualMarker>(
        clusterRadiusPx = state.clusterRadiusPx,
        minClusterSize = state.minClusterSize,
        expandMargin = state.expandMargin,
        clusterIconProvider = state.clusterIconProvider,
        onClusterClick = state.onClusterClick,
        showClusterRadiusCircle = state.showClusterRadiusCircle,
        clusterRadiusStrokeColor = state.clusterRadiusStrokeColor,
        clusterRadiusStrokeWidth = state.clusterRadiusStrokeWidth,
        clusterRadiusFillColor = state.clusterRadiusFillColor,
        enableZoomAnimation = state.enableZoomAnimation,
        zoomAnimationDurationMillis = state.zoomAnimationDurationMillis,
        debugIncludeRenderCount = state.debugIncludeRenderCount,
        cameraIdleDebounceMillis = state.cameraIdleDebounceMillis,
        content = content,
    )
}

@Composable
fun <ActualMarker> MapViewScope.MarkerClusterGroup(
    clusterRadiusPx: Double = MarkerClusterStrategy.DEFAULT_CLUSTER_RADIUS_PX,
    minClusterSize: Int = MarkerClusterStrategy.DEFAULT_MIN_CLUSTER_SIZE,
    expandMargin: Double = MarkerClusterStrategy.DEFAULT_EXPAND_MARGIN,
    clusterIconProvider: (Int) -> MarkerIconInterface = MarkerClusterStrategy.DEFAULT_ICON_PROVIDER,
    onClusterClick: ((MarkerCluster) -> Unit)? = null,
    showClusterRadiusCircle: Boolean = false,
    clusterRadiusStrokeColor: Color = Color.Red,
    clusterRadiusStrokeWidth: Dp = 1.dp,
    clusterRadiusFillColor: Color = Color.Transparent,
    enableZoomAnimation: Boolean = false,
    zoomAnimationDurationMillis: Long = MarkerClusterStrategy.DEFAULT_ZOOM_ANIMATION_DURATION_MILLIS,
    debugIncludeRenderCount: Boolean = false,
    cameraIdleDebounceMillis: Long = MarkerClusterStrategy.DEFAULT_CAMERA_DEBOUNCE_MILLIS,
    content: @Composable () -> Unit,
) {
    val strategy =
        remember(
            clusterRadiusPx,
            minClusterSize,
            expandMargin,
            clusterIconProvider,
            onClusterClick,
            enableZoomAnimation,
            zoomAnimationDurationMillis,
            debugIncludeRenderCount,
            cameraIdleDebounceMillis,
        ) {
            MarkerClusterStrategy<ActualMarker>(
                clusterRadiusPx = clusterRadiusPx,
                minClusterSize = minClusterSize,
                expandMargin = expandMargin,
                clusterIconProvider = clusterIconProvider,
                onClusterClick = onClusterClick,
                enableZoomAnimation = enableZoomAnimation,
                zoomAnimationDurationMillis = zoomAnimationDurationMillis,
                debugIncludeRenderCount = debugIncludeRenderCount,
                cameraIdleDebounceMillis = cameraIdleDebounceMillis,
            )
        }

    val debugInfos by strategy.debugInfoFlow.collectAsState()
    LaunchedEffect(
        showClusterRadiusCircle,
        clusterRadiusStrokeColor,
        clusterRadiusStrokeWidth,
        clusterRadiusFillColor,
        debugInfos,
    ) {
        val prefix = CLUSTER_CIRCLE_ID_PREFIX
        val nextMap = circleFlow.value.toMutableMap()
        nextMap.keys.filter { it.startsWith(prefix) }.forEach { nextMap.remove(it) }
        if (showClusterRadiusCircle) {
            debugInfos.forEach { info ->
                val circleState =
                    CircleState(
                        center = info.center,
                        radiusMeters = info.radiusMeters,
                        clickable = false,
                        strokeColor = clusterRadiusStrokeColor,
                        strokeWidth = clusterRadiusStrokeWidth,
                        fillColor = clusterRadiusFillColor,
                        id = "$prefix${info.id}",
                        extra = info,
                        onClick = null,
                    )
                nextMap[circleState.id] = circleState
            }
        }
        circleFlow.value = nextMap
    }

    MarkerRenderingGroup(strategy = strategy, content = content)
}

private const val CLUSTER_CIRCLE_ID_PREFIX = "cluster-circle-"
