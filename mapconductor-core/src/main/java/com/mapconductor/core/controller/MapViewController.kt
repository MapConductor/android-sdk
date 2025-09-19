package com.mapconductor.core.controller

import com.mapconductor.core.map.MapCameraPositionImpl
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
        position: MapCameraPositionImpl,
        listener: MapViewState.MoveCameraCallback? = null,
    )

    fun animateCamera(
        position: MapCameraPositionImpl,
        duration: Long,
        listener: MapViewState.MoveCameraCallback? = null,
    )
}
