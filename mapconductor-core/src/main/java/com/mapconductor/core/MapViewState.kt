package com.mapconductor.core

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.StateFlow

interface MapViewState {
    val mapCameraPosition: StateFlow<MapCameraPositionImpl?>
    fun attachTo(container: ViewGroup)
    fun detach(owner: LifecycleOwner? = null)
    fun onResume(owner: LifecycleOwner? = null)
    fun onPause(owner: LifecycleOwner? = null)
    fun destroy(owner: LifecycleOwner? = null)
    fun moveCameraTo(dstPosition: MapCameraPositionImpl, durationMs: Long = 0): Boolean
    fun moveCameraTo(geoPoint: GeoPointInterface, durationMs: Long = 0): Boolean
    fun addMarkers(markerDataList: List<MarkerDataWithHandler>)
}