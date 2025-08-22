package com.mapconductor.core.controller

import com.mapconductor.core.circle.CircleOverlayManager
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.circle.OnCircleEventHandler
import com.mapconductor.core.map.MapViewHolder
import com.mapconductor.core.map.OnCameraMoveHandler
import com.mapconductor.core.map.OnMapEventHandler
import kotlinx.coroutines.CoroutineScope

interface MapViewController<ActualCircle> {
    val holder: MapViewHolder<*, *>
    val coroutine: CoroutineScope
    val circleOverlayManager: CircleOverlayManager<ActualCircle>

    suspend fun addCircles(data: List<CircleState>)

    suspend fun updateCircle(state: CircleState)

    suspend fun clearOverlays()

    fun setCameraMoveListener(listener: OnCameraMoveHandler?)

    fun setMapClickListener(listener: OnMapEventHandler?)

    fun setMapLongClickListener(listener: OnMapEventHandler?)

    fun setCircleClickListener(listener: OnCircleEventHandler?)
}

typealias MapViewControllerAlias = MapViewController<*>
