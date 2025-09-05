package com.mapconductor.core.controller

import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewHolder
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.OnCameraMoveHandler
import com.mapconductor.core.map.OnMapEventHandler
import kotlinx.coroutines.CoroutineScope

interface MapViewController {
    val holder: MapViewHolder<*, *>
    val coroutine: CoroutineScope

    suspend fun clearOverlays()

    fun setCameraMoveListener(listener: OnCameraMoveHandler?)

    fun setMapClickListener(listener: OnMapEventHandler?)

    fun setMapLongClickListener(listener: OnMapEventHandler?)

    fun moveCamera(
        dstPosition: MapCameraPosition,
        listener: MapViewState.MoveCameraCallback? = null,
    )

    fun animateCamera(
        dstPosition: MapCameraPosition,
        duration: Long,
        listener: MapViewState.MoveCameraCallback? = null,
    )
}
