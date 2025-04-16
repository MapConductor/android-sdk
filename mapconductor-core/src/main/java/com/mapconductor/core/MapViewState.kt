package com.mapconductor.core

import android.view.ViewGroup
import kotlinx.coroutines.flow.StateFlow

interface MapViewStateImpl {
    val mapCameraPosition: StateFlow<MapCameraPositionImpl?>
    fun attachTo(container: ViewGroup)
    fun detach()
    fun onResume()
    fun onPause()
    fun destroy()
    fun moveCameraTo(dstPosition: MapCameraPositionImpl, durationMs: Long = 0): Boolean
    fun moveCameraTo(geoPoint: GeoPointImpl, durationMs: Long = 0): Boolean
}