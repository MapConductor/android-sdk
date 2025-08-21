package com.mapconductor.core.controller

import com.mapconductor.core.circle.CircleOverlayManager
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.circle.OnCircleEventHandler
import com.mapconductor.core.map.MapViewHolder
import com.mapconductor.core.map.OnCameraMoveHandler
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.core.polygon.PolygonOverlayManager
import com.mapconductor.core.polyline.OnPolylineEventHandler
import com.mapconductor.core.polyline.PolylineOverlayManager
import com.mapconductor.core.polyline.PolylineState
import kotlinx.coroutines.CoroutineScope

interface MapViewController<ActualCircle, ActualPolyline, ActualPolygon> {
    val holder: MapViewHolder<*, *>
    val coroutine: CoroutineScope
    val polylineOverlayManager: PolylineOverlayManager<ActualPolyline>
    val circleOverlayManager: CircleOverlayManager<ActualCircle>
    val polygonOverlayManager: PolygonOverlayManager<ActualPolygon>

    suspend fun addPolylines(data: List<PolylineState>)

    suspend fun updatePolyline(state: PolylineState)

    suspend fun addCircles(data: List<CircleState>)

    suspend fun updateCircle(state: CircleState)

    suspend fun clearOverlays()

    fun setCameraMoveListener(listener: OnCameraMoveHandler?)

    fun setMapClickListener(listener: OnMapEventHandler?)

    fun setMapLongClickListener(listener: OnMapEventHandler?)

    fun setCircleClickListener(listener: OnCircleEventHandler?)

    fun setPolylineClickListener(listener: OnPolylineEventHandler?)
}

typealias MapViewControllerAlias = MapViewController<*, *, *>
