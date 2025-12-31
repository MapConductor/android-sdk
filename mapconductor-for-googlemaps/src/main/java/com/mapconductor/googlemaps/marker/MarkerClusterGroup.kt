package com.mapconductor.googlemaps.marker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mapconductor.core.marker.MarkerIcon
import com.mapconductor.marker.clustering.MarkerCluster
import com.mapconductor.marker.clustering.MarkerClusterStrategy
import com.mapconductor.googlemaps.GoogleMapActualMarker

@Composable
fun MarkerClusterGroup(
    clusterRadiusPx: Double = MarkerClusterStrategy.DEFAULT_CLUSTER_RADIUS_PX,
    minClusterSize: Int = MarkerClusterStrategy.DEFAULT_MIN_CLUSTER_SIZE,
    expandMargin: Double = MarkerClusterStrategy.DEFAULT_EXPAND_MARGIN,
    clusterIconProvider: (Int) -> MarkerIcon = MarkerClusterStrategy.DEFAULT_ICON_PROVIDER,
    onClusterClick: ((MarkerCluster) -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val strategy =
        remember(
            clusterRadiusPx,
            minClusterSize,
            expandMargin,
            clusterIconProvider,
            onClusterClick,
        ) {
            MarkerClusterStrategy<GoogleMapActualMarker>(
                clusterRadiusPx = clusterRadiusPx,
                minClusterSize = minClusterSize,
                expandMargin = expandMargin,
                clusterIconProvider = clusterIconProvider,
                onClusterClick = onClusterClick,
            )
        }

    MarkerRenderingGroup(strategy = strategy, content = content)
}
