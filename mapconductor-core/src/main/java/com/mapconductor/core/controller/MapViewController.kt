package com.mapconductor.core.controller

import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.map.MapViewHolder
import com.mapconductor.core.map.OnCameraMoveHandler
import com.mapconductor.core.map.OnMapEventHandler
import kotlinx.coroutines.CoroutineScope

interface MapViewController {
    val holder: MapViewHolder<*, *>
    val coroutine: CoroutineScope

    suspend fun clearOverlays()

    fun setCameraMoveStartListener(listener: OnCameraMoveHandler?)

    fun setCameraMoveListener(listener: OnCameraMoveHandler?)

    fun setCameraMoveEndListener(listener: OnCameraMoveHandler?)

    fun setMapClickListener(listener: OnMapEventHandler?)

    fun setMapLongClickListener(listener: OnMapEventHandler?)

    fun moveCamera(position: MapCameraPositionImpl)

    fun animateCamera(
        position: MapCameraPositionImpl,
        duration: Long,
    )
}
