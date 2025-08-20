package com.mapconductor.core.controller

import com.mapconductor.core.circle.CircleOverlayManager
import com.mapconductor.core.circle.CircleRenderer
import com.mapconductor.core.circle.OnCircleEventHandler
import com.mapconductor.core.map.OnCameraMoveHandler
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.core.marker.MarkerOverlayManager
import com.mapconductor.core.marker.MarkerRenderer
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.polygon.PolygonOverlayManager
import com.mapconductor.core.polygon.PolygonRenderer
import com.mapconductor.core.polyline.OnPolylineEventHandler
import com.mapconductor.core.polyline.PolylineOverlayManager
import com.mapconductor.core.polyline.PolylineRenderer

abstract class BaseMapViewController<ActualMarker, ActualCircle, ActualPolyline, ActualPolygon> :
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

    protected var cameraMoveCallback: OnCameraMoveHandler? = null
    protected var mapClickCallback: OnMapEventHandler? = null
    protected var mapLongClickCallback: OnMapEventHandler? = null
    protected var markerClickCallback: OnMarkerEventHandler? = null
    protected var markerDragStartCallback: OnMarkerEventHandler? = null
    protected var markerDragCallback: OnMarkerEventHandler? = null
    protected var markerDragEndCallback: OnMarkerEventHandler? = null
    protected var circleClickCallback: OnCircleEventHandler? = null
    protected var polylineClickCallback: OnPolylineEventHandler? = null

    abstract fun setupListeners()

    override fun setCameraMoveListener(listener: OnCameraMoveHandler?) {
        this.cameraMoveCallback = listener
    }

    override fun setMapClickListener(listener: OnMapEventHandler?) {
        this.mapClickCallback = listener
    }

    override fun setMapLongClickListener(listener: OnMapEventHandler?) {
        this.mapClickCallback = listener
    }

    override fun setMarkerClickListener(listener: OnMarkerEventHandler?) {
        this.markerClickCallback = listener
    }

    override fun setMarkerDragStartListener(listener: OnMarkerEventHandler?) {
        this.markerDragStartCallback = listener
    }

    override fun setMarkerDragListener(listener: OnMarkerEventHandler?) {
        this.markerDragCallback = listener
    }

    override fun setMarkerDragEndListener(listener: OnMarkerEventHandler?) {
        this.markerDragEndCallback = listener
    }

    override fun setCircleClickListener(listener: OnCircleEventHandler?) {
        this.circleClickCallback = listener
    }

    override fun setPolylineClickListener(listener: OnPolylineEventHandler?) {
        this.polylineClickCallback = listener
    }

    override fun setOnMarkerAnimationStart(listener: OnMarkerEventHandler?) =
        markerRenderer
            .setOnMarkerAnimationStart(listener)

    override fun setOnMarkerAnimationEnd(listener: OnMarkerEventHandler?) =
        markerRenderer
            .setOnMarkerAnimationEnd(listener)
}
