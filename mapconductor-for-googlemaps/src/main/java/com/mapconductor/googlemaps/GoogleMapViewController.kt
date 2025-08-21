package com.mapconductor.googlemaps

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap.CancelableCallback
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener
import com.google.android.gms.maps.GoogleMap.OnCameraMoveCanceledListener
import com.google.android.gms.maps.GoogleMap.OnCameraMoveListener
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener
import com.google.android.gms.maps.GoogleMap.OnMapClickListener
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.Polyline
import com.mapconductor.core.circle.CircleClickEvent
import com.mapconductor.core.circle.CircleOverlayManager
import com.mapconductor.core.circle.CircleRenderer
import com.mapconductor.core.circle.CircleRendererFactory
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.controller.BaseMapViewController
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.groundimage.GroundImageCapable
import com.mapconductor.core.groundimage.GroundImageController
import com.mapconductor.core.groundimage.GroundImageEvent
import com.mapconductor.core.groundimage.GroundImageState
import com.mapconductor.core.groundimage.OnGroundImageEventHandler
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.MarkerCapable
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.polygon.PolygonOverlayManager
import com.mapconductor.core.polygon.PolygonRenderer
import com.mapconductor.core.polygon.PolygonRendererFactory
import com.mapconductor.core.polyline.PolylineOverlayManager
import com.mapconductor.core.polyline.PolylineRenderer
import com.mapconductor.core.polyline.PolylineRendererFactory
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.googlemaps.circle.DefaultGoogleMapCircleRenderer
import com.mapconductor.googlemaps.circle.GoogleMapCircleRenderer
import com.mapconductor.googlemaps.marker.GoogleMapMarkerController
import com.mapconductor.googlemaps.polygon.DefaultGoogleMapPolygonRenderer
import com.mapconductor.googlemaps.polygon.GoogleMapPolygonRenderer
import com.mapconductor.googlemaps.polyline.DefaultGoogleMapPolylineRenderer
import com.mapconductor.googlemaps.polyline.GoogleMapPolylineRenderer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface IGoogleMapViewController :
    MapViewController<Circle, Polyline, Polygon>,
    GroundImageCapable,
    MarkerCapable<GoogleMapActualMarker> {
    fun moveCamera(
        dstPosition: MapCameraPosition,
        listener: MapViewState.MoveCameraCallback? = null,
    )

    fun animateCamera(
        dstPosition: MapCameraPosition,
        duration: Int,
        listener: MapViewState.MoveCameraCallback? = null,
    )
}

class GoogleMapViewController(
    override val holder: GoogleMapViewHolder,
    private val markerController: GoogleMapMarkerController,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
    private val polylineRendererFactory: PolylineRendererFactory<Polyline> = DefaultGoogleMapPolylineRenderer(),
    private val polygonRendererFactory: PolygonRendererFactory<Polygon> = DefaultGoogleMapPolygonRenderer(),
    private val circleRendererFactory: CircleRendererFactory<Circle> = DefaultGoogleMapCircleRenderer(),
    private val groundImageController: GroundImageController<GoogleMapActualGroundImage>,
) : BaseMapViewController<
        GoogleMapActualCircle,
        GoogleMapActualPolyline,
        GoogleMapActualPolygon,
    >(),
    IGoogleMapViewController,
    OnCameraMoveStartedListener,
    OnCameraMoveCanceledListener,
    OnCameraMoveListener,
    OnCameraIdleListener,
    OnMapClickListener {
    override val polylineRenderer: PolylineRenderer<Polyline> =
        GoogleMapPolylineRenderer(
            holder = holder,
            coroutine = coroutine,
        )

    override fun createPolylineOverlayManager(): PolylineOverlayManager<Polyline> =
        polylineRendererFactory.create(
            onAdd = polylineRenderer::addPolylines,
            onChange = polylineRenderer::changePolylines,
            onRemove = polylineRenderer::removePolylines,
        )

    override val polygonRenderer: PolygonRenderer<Polygon> =
        GoogleMapPolygonRenderer(
            holder = holder,
            coroutine = coroutine,
        )

    override fun createPolygonOverlayManager(): PolygonOverlayManager<Polygon> =
        polygonRendererFactory.create(
            onAdd = polygonRenderer::addPolygons,
            onChange = polygonRenderer::changePolygon,
            onRemove = polygonRenderer::removePolygons,
        )

    override fun onCircleOverlayManagerInitialized(overlayManager: CircleOverlayManager<Circle>) {
    }

    override fun onPolygonOverlayManagerInitialized(overlayManager: PolygonOverlayManager<Polygon>) {
    }

    override fun onPolylineOverlayManagerInitialized(overlayManager: PolylineOverlayManager<Polyline>) {
    }

    override val circleRenderer: CircleRenderer<Circle> =
        GoogleMapCircleRenderer(
            holder = holder,
            coroutine = coroutine,
        )

    override fun createCircleOverlayManager(): CircleOverlayManager<Circle> =
        circleRendererFactory.create(
            onAdd = circleRenderer::addCircles,
            onChange = circleRenderer::changeCircle,
            onRemove = circleRenderer::removeCircles,
        )

    init {
        setupListeners()
    }

    override fun setupListeners() {
        holder.map.setOnCameraMoveStartedListener(this)
        holder.map.setOnCameraMoveCanceledListener(this)
        holder.map.setOnCameraMoveListener(this)
        holder.map.setOnCameraIdleListener(this)
        holder.map.setOnMapClickListener(this)
    }

    override fun moveCamera(
        position: MapCameraPosition,
        listener: MapViewState.MoveCameraCallback?,
    ) {
        coroutine.launch {
            val dstCameraPosition = position.toCameraPosition()
            val cameraUpdate = CameraUpdateFactory.newCameraPosition(dstCameraPosition)
            holder.map.moveCamera(cameraUpdate)
            listener?.onComplete(true)
        }
    }

    override fun animateCamera(
        position: MapCameraPosition,
        duration: Int,
        listener: MapViewState.MoveCameraCallback?,
    ) {
        val dstCameraPosition = position.toCameraPosition()
        coroutine.launch {
            val cameraUpdate = CameraUpdateFactory.newCameraPosition(dstCameraPosition)
            holder.map.animateCamera(
                cameraUpdate,
                duration,
                object : CancelableCallback {
                    override fun onCancel() {
                        listener?.onComplete(false)
                    }

                    override fun onFinish() {
                        listener?.onComplete(true)
                    }
                },
            )
        }
    }

    override suspend fun clearOverlays() {
        markerController.clear()
        groundImageController.clear()
        circleOverlayManager.clearOverlays()
        polylineOverlayManager.clearOverlays()
        polygonOverlayManager.clearOverlays()
    }

    override suspend fun compositionMarkers(data: List<MarkerState>) = markerController.add(data)

    override suspend fun updateMarker(state: MarkerState) = markerController.update(state)

    override suspend fun addCircles(data: List<CircleState>) = circleOverlayManager.addCircles(data)

    override suspend fun updateCircle(state: CircleState) = circleOverlayManager.updateCircle(state)

    override suspend fun addPolylines(data: List<PolylineState>) = polylineOverlayManager.addPolylines(data)

    override suspend fun updatePolyline(state: PolylineState) = polylineOverlayManager.updatePolyline(state)

    override fun onCameraMove() {
        cameraMoveCallback?.let {
            val mapCameraPosition = holder.map.cameraPosition.toMapCameraPosition()
            coroutine.launch { it(mapCameraPosition) }
        }
    }

    override fun onCameraIdle() {
        cameraMoveCallback?.let {
            val mapCameraPosition = holder.map.cameraPosition.toMapCameraPosition()
            coroutine.launch { it(mapCameraPosition) }
        }
    }

    override fun onCameraMoveStarted(p0: Int) {
        cameraMoveCallback?.let {
            val mapCameraPosition = holder.map.cameraPosition.toMapCameraPosition()
            coroutine.launch { it(mapCameraPosition) }
        }
    }

    override fun onCameraMoveCanceled() {
        cameraMoveCallback?.let {
            val mapCameraPosition = holder.map.cameraPosition.toMapCameraPosition()
            coroutine.launch { it(mapCameraPosition) }
        }
    }

    override fun onMapClick(position: LatLng) {
        val touchPosition = position.toGeoPoint()

        circleOverlayManager.find(touchPosition)?.let { entity ->
            val event =
                CircleClickEvent(
                    state = entity.state,
                    position = touchPosition,
                )
            circleClickCallback?.invoke(event)
            return
        }

        groundImageController.find(touchPosition)?.let { entity ->
            val event =
                GroundImageEvent(
                    state = entity.state,
                    clicked = touchPosition,
                )
            coroutine.launch {
                groundImageController.clickListener?.invoke(event)
            }
            return
        }

        mapClickCallback?.let {
            coroutine.launch { it(position.toGeoPoint()) }
        }
    }

    override suspend fun compositionGroundImages(data: List<GroundImageState>) = groundImageController.add(data)

    override suspend fun updateGroundImage(state: GroundImageState) = groundImageController.update(state)

    override fun setOnMarkerDragStart(listener: OnMarkerEventHandler?) {
        this.markerController.dragStartListener = listener
    }

    override fun setOnMarkerDrag(listener: OnMarkerEventHandler?) {
        this.markerController.dragListener = listener
    }

    override fun setOnMarkerDragEnd(listener: OnMarkerEventHandler?) {
        this.markerController.dragEndListener = listener
    }

    override fun setOnMarkerAnimateStart(listener: OnMarkerEventHandler?) {
        this.markerController.renderer.animateStartListener = listener
    }

    override fun setOnMarkerAnimateEnd(listener: OnMarkerEventHandler?) {
        this.markerController.renderer.animateEndListener = listener
    }

    override fun setOnMarkerClickListener(listener: OnMarkerEventHandler?) {
        this.markerController.clickListener = listener
    }

    override fun setOnGroundImageClickListener(listener: OnGroundImageEventHandler?) {
        this.groundImageController.clickListener = listener
    }
}
