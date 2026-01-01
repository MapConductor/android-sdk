package com.mapconductor.here.marker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.marker.ColorDefaultIcon
import com.mapconductor.core.marker.MarkerIcon
import com.mapconductor.here.HereActualMarker
import com.mapconductor.here.HereViewScope
import com.mapconductor.marker.clustering.MarkerCluster
import com.mapconductor.marker.clustering.MarkerClusterStrategy

@Composable
fun HereViewScope.MarkerClusterGroup(
    clusterRadiusPx: Double = MarkerClusterStrategy.DEFAULT_CLUSTER_RADIUS_PX,
    minClusterSize: Int = MarkerClusterStrategy.DEFAULT_MIN_CLUSTER_SIZE,
    expandMargin: Double = MarkerClusterStrategy.DEFAULT_EXPAND_MARGIN,
    clusterIconProvider: (Int) -> MarkerIcon = MarkerClusterStrategy.DEFAULT_ICON_PROVIDER,
    onClusterClick: ((MarkerCluster) -> Unit)? = null,
    debugClusterTurnLabel: Boolean = false,
    showClusterRadiusCircle: Boolean = false,
    clusterRadiusStrokeColor: Color = Color.Red,
    clusterRadiusStrokeWidth: Dp = 1.dp,
    clusterRadiusFillColor: Color = Color.Transparent,
    content: @Composable () -> Unit,
) {
    val iconProviderWithTurn =
        remember(clusterIconProvider, debugClusterTurnLabel) {
            if (debugClusterTurnLabel) {
                { _: Int, turn: Int -> ColorDefaultIcon(label = turn.toString()) }
            } else {
                null
            }
        }
    val strategy =
        remember(
            clusterRadiusPx,
            minClusterSize,
            expandMargin,
            clusterIconProvider,
            onClusterClick,
            debugClusterTurnLabel,
        ) {
            MarkerClusterStrategy<HereActualMarker>(
                clusterRadiusPx = clusterRadiusPx,
                minClusterSize = minClusterSize,
                expandMargin = expandMargin,
                clusterIconProvider = clusterIconProvider,
                clusterIconProviderWithTurn = iconProviderWithTurn,
                includeTurnInClusterId = debugClusterTurnLabel,
                onClusterClick = onClusterClick,
            )
        }

    val debugInfos by strategy.debugInfoFlow.collectAsState()
    LaunchedEffect(showClusterRadiusCircle, debugInfos) {
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
