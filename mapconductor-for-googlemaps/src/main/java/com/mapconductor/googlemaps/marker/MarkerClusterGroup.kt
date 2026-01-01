package com.mapconductor.googlemaps.marker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mapconductor.core.circle.Circle
import com.mapconductor.core.marker.ColorDefaultIcon
import com.mapconductor.core.marker.MarkerIcon
import com.mapconductor.googlemaps.GoogleMapActualMarker
import com.mapconductor.googlemaps.GoogleMapViewScope
import com.mapconductor.marker.clustering.MarkerCluster
import com.mapconductor.marker.clustering.MarkerClusterStrategy
import android.util.Log

@Composable
fun GoogleMapViewScope.MarkerClusterGroup(
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
    Log.d("DEBUG", "----->MarkerClusterGroup() start")
    val iconProviderWithTurn =
        remember(clusterIconProvider, debugClusterTurnLabel) {
            Log.d("DEBUG", "iconProviderWithTurn: clusterIconProvider=${clusterIconProvider}, debugClusterTurnLabel=${debugClusterTurnLabel}")
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
            Log.d("DEBUG", "strategy: clusterRadiusPx=${clusterRadiusPx},clusterRadiusPx=${clusterRadiusPx}")
            MarkerClusterStrategy<GoogleMapActualMarker>(
                clusterRadiusPx = clusterRadiusPx,
                minClusterSize = minClusterSize,
                expandMargin = expandMargin,
                clusterIconProvider = clusterIconProvider,
                clusterIconProviderWithTurn = iconProviderWithTurn,
                includeTurnInClusterId = debugClusterTurnLabel,
                onClusterClick = onClusterClick,
            )
        }

    if (showClusterRadiusCircle) {
        val debugInfos by strategy.debugInfoFlow.collectAsState()
        debugInfos.forEach { info ->
            Circle(
                center = info.center,
                radiusMeters = info.radiusMeters,
                id = "cluster-circle-${info.id}",
                strokeColor = clusterRadiusStrokeColor,
                strokeWidth = clusterRadiusStrokeWidth,
                fillColor = clusterRadiusFillColor,
                extra = info,
                onClick = null,
            )
        }
    }

    MarkerRenderingGroup(strategy = strategy, content = content)
    Log.d("DEBUG", "----->MarkerClusterGroup() end")
}
