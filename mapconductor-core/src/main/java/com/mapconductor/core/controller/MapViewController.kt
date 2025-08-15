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

    fun <ActualCamera> setCameraMoveListener(listener: OnCameraMoveHandler<ActualCamera>?)
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

abstract class BaseMapViewController<ActualCamera, ActualMarker, ActualCircle, ActualPolyline, ActualPolygon> :
    MapViewController<ActualMarker, ActualCircle, ActualPolyline, ActualPolygon> {
    abstract val markerRenderer: MarkerRenderer<ActualMarker>

    override val markerOverlayManager: MarkerOverlayManager<ActualMarker> by lazy {
        createMarkerOverlayManager().also { overlayManager ->
            markerRenderer.init(overlayManager)
            onMarkerOverlayManagerInitialized(overlayManager)
        }
    }

    abstract val polylineRenderer: PolylineRenderer<ActualPolyline>

    override val polylineOverlayManager: PolylineOverlayManager<ActualPolyline> by lazy {
        createPolylineOverlayManager().also { overlayManager ->
            polylineRenderer.init(overlayManager)
            onPolylineOverlayManagerInitialized(overlayManager)
        }
    }

    abstract val polygonRenderer: PolygonRenderer<ActualPolygon>

    override val polygonOverlayManager: PolygonOverlayManager<ActualPolygon> by lazy {
        createPolygonOverlayManager().also { overlayManager ->
            polygonRenderer.init(overlayManager)
            onPolygonOverlayManagerInitialized(overlayManager)
        }
    }

    abstract val circleRenderer: CircleRenderer<ActualCircle>

    override val circleOverlayManager: CircleOverlayManager<ActualCircle> by lazy {
        createCircleOverlayManager().also { overlayManager ->
            circleRenderer.init(overlayManager)
            onCircleOverlayManagerInitialized(overlayManager)
        }
    }

    protected abstract fun onMarkerOverlayManagerInitialized(overlayManager: MarkerOverlayManager<ActualMarker>)

    protected abstract fun onPolylineOverlayManagerInitialized(overlayManager: PolylineOverlayManager<ActualPolyline>)

    protected abstract fun onPolygonOverlayManagerInitialized(overlayManager: PolygonOverlayManager<ActualPolygon>)

    protected abstract fun onCircleOverlayManagerInitialized(overlayManager: CircleOverlayManager<ActualCircle>)

    protected abstract fun createMarkerOverlayManager(): MarkerOverlayManager<ActualMarker>

    protected abstract fun createPolylineOverlayManager(): PolylineOverlayManager<ActualPolyline>

    protected abstract fun createPolygonOverlayManager(): PolygonOverlayManager<ActualPolygon>

    protected abstract fun createCircleOverlayManager(): CircleOverlayManager<ActualCircle>

    protected var _cameraMoveListener: (OnCameraMoveHandler<ActualCamera>)? = null
    protected var _mapClickListener: OnMapEventHandler? = null
    protected var _mapLongClickListener: OnMapEventHandler? = null
    protected var _markerClickListener: OnMarkerEventHandler? = null
    protected var _markerDragStartListener: OnMarkerEventHandler? = null
    protected var _markerDragListener: OnMarkerEventHandler? = null
    protected var _markerDragEndListener: OnMarkerEventHandler? = null
    protected var _circleClickListener: OnCircleEventHandler? = null
    protected var _polylineClickListener: OnPolylineEventHandler? = null

    abstract fun setupListeners()

    override fun setMapClickListener(listener: OnMapEventHandler?) {
        this._mapClickListener = listener
    }
    override fun setMapLongClickListener(listener: OnMapEventHandler?) {
        this._mapClickListener = listener
    }
    override fun setMarkerClickListener(listener: OnMarkerEventHandler?) {
        this._markerClickListener = listener
    }
    override fun setMarkerDragStartListener(listener: OnMarkerEventHandler?) {
        this._markerDragStartListener = listener
    }
    override fun setMarkerDragListener(listener: OnMarkerEventHandler?) {
        this._markerDragListener = listener
    }
    override fun setMarkerDragEndListener(listener: OnMarkerEventHandler?) {
        this._markerDragEndListener = listener
    }
    override fun setCircleClickListener(listener: OnCircleEventHandler?) {
        this._circleClickListener = listener
    }
    override fun setPolylineClickListener(listener: OnPolylineEventHandler?) {
        this._polylineClickListener = listener
    }

    override fun setOnMarkerAnimationStart(listener: OnMarkerEventHandler?) =
        markerRenderer
            .setOnMarkerAnimationStart(listener)

    override fun setOnMarkerAnimationEnd(listener: OnMarkerEventHandler?) =
        markerRenderer
            .setOnMarkerAnimationEnd(listener)
}
