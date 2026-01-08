package com.mapconductor.marker.clustering

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mapconductor.core.marker.MarkerIconInterface

class MarkerClusterGroupState<ActualMarker>(
    clusterRadiusPx: Double = MarkerClusterStrategy.DEFAULT_CLUSTER_RADIUS_PX,
    minClusterSize: Int = MarkerClusterStrategy.DEFAULT_MIN_CLUSTER_SIZE,
    expandMargin: Double = MarkerClusterStrategy.DEFAULT_EXPAND_MARGIN,
    clusterIconProvider: (Int) -> MarkerIconInterface = MarkerClusterStrategy.DEFAULT_ICON_PROVIDER,
    onClusterClick: ((MarkerCluster) -> Unit)? = null,
    debugClusterTurnLabel: Boolean = false,
    showClusterRadiusCircle: Boolean = false,
    clusterRadiusStrokeColor: Color = Color.Red,
    clusterRadiusStrokeWidth: Dp = 1.dp,
    clusterRadiusFillColor: Color = Color.Transparent,
) {
    var clusterRadiusPx by mutableStateOf(clusterRadiusPx)
    var minClusterSize by mutableStateOf(minClusterSize)
    var expandMargin by mutableStateOf(expandMargin)
    var clusterIconProvider by mutableStateOf(clusterIconProvider)
    var onClusterClick by mutableStateOf(onClusterClick)
    var debugClusterTurnLabel by mutableStateOf(debugClusterTurnLabel)
    var showClusterRadiusCircle by mutableStateOf(showClusterRadiusCircle)
    var clusterRadiusStrokeColor by mutableStateOf(clusterRadiusStrokeColor)
    var clusterRadiusStrokeWidth by mutableStateOf(clusterRadiusStrokeWidth)
    var clusterRadiusFillColor by mutableStateOf(clusterRadiusFillColor)
}
