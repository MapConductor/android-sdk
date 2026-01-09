package com.mapconductor.core.controller

import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewHolderInterface
import com.mapconductor.core.map.OnCameraMoveHandler
import com.mapconductor.core.map.OnMapEventHandler
import kotlinx.coroutines.CoroutineScope

interface MapViewControllerInterface {
    val holder: MapViewHolderInterface<*, *>
    val coroutine: CoroutineScope

    suspend fun clearOverlays()

    fun setCameraMoveStartListener(listener: OnCameraMoveHandler?)

    fun setCameraMoveListener(listener: OnCameraMoveHandler?)

    fun setCameraMoveEndListener(listener: OnCameraMoveHandler?)

    fun setMapClickListener(listener: OnMapEventHandler?)

    fun setMapLongClickListener(listener: OnMapEventHandler?)

    fun moveCamera(position: MapCameraPosition)

    fun animateCamera(
        position: MapCameraPosition,
        duration: Long,
    )

    fun registerOverlayController(controller: OverlayControllerInterface<*, *, *>) {}
}
