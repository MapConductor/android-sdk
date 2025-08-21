package com.mapconductor.core.controller

import com.mapconductor.core.circle.CircleOverlayManager
import com.mapconductor.core.circle.CircleRenderer
import com.mapconductor.core.circle.OnCircleEventHandler
import com.mapconductor.core.map.OnCameraMoveHandler
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.core.polygon.PolygonOverlayManager
import com.mapconductor.core.polygon.PolygonRenderer
import com.mapconductor.core.polyline.OnPolylineEventHandler
import com.mapconductor.core.polyline.PolylineOverlayManager
import com.mapconductor.core.polyline.PolylineRenderer

abstract class BaseMapViewController<ActualCircle, ActualPolyline, ActualPolygon> :
    MapViewController<ActualCircle, ActualPolyline, ActualPolygon> {
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

    protected abstract fun onPolylineOverlayManagerInitialized(overlayManager: PolylineOverlayManager<ActualPolyline>)

    protected abstract fun onPolygonOverlayManagerInitialized(overlayManager: PolygonOverlayManager<ActualPolygon>)

    protected abstract fun onCircleOverlayManagerInitialized(overlayManager: CircleOverlayManager<ActualCircle>)

    protected abstract fun createPolylineOverlayManager(): PolylineOverlayManager<ActualPolyline>

    protected abstract fun createPolygonOverlayManager(): PolygonOverlayManager<ActualPolygon>

    protected abstract fun createCircleOverlayManager(): CircleOverlayManager<ActualCircle>

    protected var cameraMoveCallback: OnCameraMoveHandler? = null
    protected var mapClickCallback: OnMapEventHandler? = null
    protected var mapLongClickCallback: OnMapEventHandler? = null
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

    override fun setCircleClickListener(listener: OnCircleEventHandler?) {
        this.circleClickCallback = listener
    }

    override fun setPolylineClickListener(listener: OnPolylineEventHandler?) {
        this.polylineClickCallback = listener
    }
}
