package com.mapconductor.core.controller

import com.mapconductor.core.circle.CircleOverlayManager
import com.mapconductor.core.circle.CircleRenderer
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.circle.OnCircleEventHandler
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.map.MapViewHolder
import com.mapconductor.core.map.OnCameraMoveHandler
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.core.marker.MarkerOverlayManager
import com.mapconductor.core.marker.MarkerRenderer
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.polygon.PolygonOverlayManager
import com.mapconductor.core.polygon.PolygonRenderer
import com.mapconductor.core.polyline.OnPolylineEventHandler
import com.mapconductor.core.polyline.PolylineOverlayManager
import com.mapconductor.core.polyline.PolylineRenderer
import com.mapconductor.core.polyline.PolylineState
import kotlinx.coroutines.CoroutineScope

interface MapViewController<ActualMarker, ActualCircle, ActualPolyline, ActualPolygon> {
    val holder: MapViewHolder<*, *>
    val coroutine: CoroutineScope
    val markerOverlayManager: MarkerOverlayManager<ActualMarker>
    val hexGeocell: HexGeocell
    val polylineOverlayManager: PolylineOverlayManager<ActualPolyline>
    val circleOverlayManager: CircleOverlayManager<ActualCircle>
    val polygonOverlayManager: PolygonOverlayManager<ActualPolygon>

    suspend fun addMarkers(data: List<MarkerState>)

    suspend fun updateMarker(state: MarkerState)

    suspend fun addPolylines(data: List<PolylineState>)

    suspend fun updatePolyline(state: PolylineState)

    suspend fun addCircles(data: List<CircleState>)

    suspend fun updateCircle(state: CircleState)

    suspend fun clearOverlays()

    fun setCameraMoveListener(listener: OnCameraMoveHandler?)

    fun setMapClickListener(listener: OnMapEventHandler?)

    fun setMapLongClickListener(listener: OnMapEventHandler?)

    fun setMarkerClickListener(listener: OnMarkerEventHandler?)

    fun setMarkerDragStartListener(listener: OnMarkerEventHandler?)

    fun setMarkerDragListener(listener: OnMarkerEventHandler?)

    fun setMarkerDragEndListener(listener: OnMarkerEventHandler?)

    fun setCircleClickListener(listener: OnCircleEventHandler?)

    fun setPolylineClickListener(listener: OnPolylineEventHandler?)

    fun setOnMarkerAnimationStart(listener: OnMarkerEventHandler?)

    fun setOnMarkerAnimationEnd(listener: OnMarkerEventHandler?)
}
typealias MapViewControllerAlias = MapViewController<*, *, *, *>
