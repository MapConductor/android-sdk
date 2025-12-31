package com.mapconductor.googlemaps.marker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mapconductor.core.marker.ColorDefaultIcon
import com.mapconductor.core.marker.MarkerIcon
import com.mapconductor.googlemaps.GoogleMapActualMarker
import com.mapconductor.googlemaps.GoogleMapViewScope
import com.mapconductor.marker.clustering.MarkerCluster
import com.mapconductor.marker.clustering.MarkerClusterStrategy

@Composable
fun GoogleMapViewScope.MarkerClusterGroup(
    clusterRadiusPx: Double = MarkerClusterStrategy.DEFAULT_CLUSTER_RADIUS_PX,
    minClusterSize: Int = MarkerClusterStrategy.DEFAULT_MIN_CLUSTER_SIZE,
    expandMargin: Double = MarkerClusterStrategy.DEFAULT_EXPAND_MARGIN,
    clusterIconProvider: (Int) -> MarkerIcon = MarkerClusterStrategy.DEFAULT_ICON_PROVIDER,
    onClusterClick: ((MarkerCluster) -> Unit)? = null,
    debugClusterTurnLabel: Boolean = false,
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
            MarkerClusterStrategy<GoogleMapActualMarker>(
                clusterRadiusPx = clusterRadiusPx,
                minClusterSize = minClusterSize,
                expandMargin = expandMargin,
                clusterIconProvider = clusterIconProvider,
                clusterIconProviderWithTurn = iconProviderWithTurn,
                onClusterClick = onClusterClick,
            )
        }

    MarkerRenderingGroup(strategy = strategy, content = content)
}
