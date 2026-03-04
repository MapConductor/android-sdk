package com.mapconductor.marker.clustering

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
    enableZoomAnimation: Boolean = false,
    enablePanAnimation: Boolean = false,
    zoomAnimationDurationMillis: Long = MarkerClusterStrategy.DEFAULT_ZOOM_ANIMATION_DURATION_MILLIS,
    debugHullPolygons: Boolean = false,
    debugHullStrokeWidth: Dp = 2.dp,
    debugHullStrokeAlpha: Float = 0.8f,
    debugHullFillAlpha: Float = 0.18f,
    cameraIdleDebounceMillis: Long = MarkerClusterStrategy.DEFAULT_CAMERA_DEBOUNCE_MILLIS,
    tileSize: Double = MarkerClusterStrategy.DEFAULT_TILE_SIZE,
) {
    var clusterRadiusPx by mutableStateOf(clusterRadiusPx)
    var minClusterSize by mutableStateOf(minClusterSize)
    var expandMargin by mutableStateOf(expandMargin)
    var clusterIconProvider by mutableStateOf(clusterIconProvider)
    var onClusterClick by mutableStateOf(onClusterClick)
    var debugClusterTurnLabel by mutableStateOf(debugClusterTurnLabel)
    var enableZoomAnimation by mutableStateOf(enableZoomAnimation)
    var enablePanAnimation by mutableStateOf(enablePanAnimation)
    var zoomAnimationDurationMillis by mutableStateOf(zoomAnimationDurationMillis)
    var debugHullPolygons by mutableStateOf(debugHullPolygons)
    var debugHullStrokeWidth by mutableStateOf(debugHullStrokeWidth)
    var debugHullStrokeAlpha by mutableStateOf(debugHullStrokeAlpha)
    var debugHullFillAlpha by mutableStateOf(debugHullFillAlpha)
    var cameraIdleDebounceMillis by mutableStateOf(cameraIdleDebounceMillis)
    var tileSize by mutableStateOf(tileSize)
}
