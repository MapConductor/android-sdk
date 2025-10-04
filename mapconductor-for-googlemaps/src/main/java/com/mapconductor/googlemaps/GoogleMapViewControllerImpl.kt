package com.mapconductor.googlemaps

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.CancelableCallback
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener
import com.google.android.gms.maps.GoogleMap.OnCameraMoveCanceledListener
import com.google.android.gms.maps.GoogleMap.OnCameraMoveListener
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener
import com.google.android.gms.maps.GoogleMap.OnMapClickListener
import com.google.android.gms.maps.model.LatLng
import com.mapconductor.core.circle.CircleEvent
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.circle.OnCircleEventHandler
import com.mapconductor.core.controller.BaseMapViewController
import com.mapconductor.core.groundimage.GroundImageEvent
import com.mapconductor.core.groundimage.GroundImageState
import com.mapconductor.core.groundimage.OnGroundImageEventHandler
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.VisibleRegion
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.polygon.OnPolygonEventHandler
import com.mapconductor.core.polygon.PolygonEvent
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.core.polyline.OnPolylineEventHandler
import com.mapconductor.core.polyline.PolylineEvent
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.googlemaps.circle.GoogleMapCircleController
import com.mapconductor.googlemaps.groundimage.GoogleMapGroundImageController
import com.mapconductor.googlemaps.marker.GoogleMapMarkerController
import com.mapconductor.googlemaps.polygon.GoogleMapPolygonController
import com.mapconductor.googlemaps.polyline.GoogleMapPolylineController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GoogleMapViewControllerImpl(
    override val holder: GoogleMapViewHolder,
    private val markerController: GoogleMapMarkerController,
    private val polylineController: GoogleMapPolylineController,
    private val polygonController: GoogleMapPolygonController,
    private val groundImageController: GoogleMapGroundImageController,
    private val circleController: GoogleMapCircleController,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
    val backCoroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : BaseMapViewController(),
    GoogleMapViewController,
    OnCameraMoveStartedListener,
    OnCameraMoveCanceledListener,
    OnCameraMoveListener,
    OnCameraIdleListener,
    OnMapClickListener,
    GoogleMap.OnMapLoadedCallback {
    init {
        setupListeners()
        registerController(markerController)
        registerController(polygonController)
        registerController(polylineController)
        registerController(circleController)
    }

    fun setupListeners() {
        holder.map.setOnCameraMoveStartedListener(this)
        holder.map.setOnCameraMoveCanceledListener(this)
        holder.map.setOnCameraMoveListener(this)
        holder.map.setOnCameraIdleListener(this)
        holder.map.setOnMapClickListener(this)
        holder.map.setOnMapLoadedCallback(this)
    }

    override fun moveCamera(
        position: MapCameraPositionImpl,
        listener: MapViewState.MoveCameraCallback?,
    ) {
        coroutine.launch {
            val dstCameraPosition = position.toCameraPosition()
            val cameraUpdate = CameraUpdateFactory.newCameraPosition(dstCameraPosition)
            holder.map.moveCamera(cameraUpdate)
            listener?.onComplete()
        }
    }

    override fun animateCamera(
        position: MapCameraPositionImpl,
        duration: Long,
        listener: MapViewState.MoveCameraCallback?,
    ) {
        val dstCameraPosition = position.toCameraPosition()
        coroutine.launch {
            val cameraUpdate = CameraUpdateFactory.newCameraPosition(dstCameraPosition)
            holder.map.animateCamera(
                cameraUpdate,
                duration.toInt(),
                object : CancelableCallback {
                    override fun onCancel() {
                        listener?.onComplete()
                    }

                    override fun onFinish() {
                        listener?.onComplete()
                    }
                },
            )
        }
    }

    override suspend fun clearOverlays() {
        markerController.clear()
        groundImageController.clear()
        polylineController.clear()
        polygonController.clear()
        circleController.clear()
    }

    override suspend fun compositionMarkers(data: List<MarkerState>) = markerController.add(data)

    override suspend fun updateMarker(state: MarkerState) = markerController.update(state)

    override suspend fun compositionCircles(data: List<CircleState>) = circleController.add(data)

    override suspend fun updateCircle(state: CircleState) = circleController.update(state)

    override fun setOnCircleClickListener(listener: OnCircleEventHandler?) {
        this.circleController.clickListener = listener
    }

    override suspend fun compositionPolylines(data: List<PolylineState>) = polylineController.add(data)

    override suspend fun updatePolyline(state: PolylineState) = polylineController.update(state)

    override fun onCameraMove() {
        val mapCameraPosition = getMapCameraPosition()
        backCoroutine.launch {
            notifyMapCameraPosition(mapCameraPosition)
        }
    }

    override fun onCameraIdle() {
        val mapCameraPosition = getMapCameraPosition()
        backCoroutine.launch {
            markerController.onCameraChanged(mapCameraPosition)
        }
        cameraMoveCallback?.let { callBack ->
            callBack(mapCameraPosition)
        }
    }

    override fun onCameraMoveStarted(p0: Int) {
        cameraMoveCallback?.let { callBack ->
            val mapCameraPosition = getMapCameraPosition()
            callBack(mapCameraPosition)
        }
    }

    override fun onCameraMoveCanceled() {
        cameraMoveCallback?.let { callBack ->
            val mapCameraPosition = getMapCameraPosition()
            callBack(mapCameraPosition)
        }
    }

    private fun getMapCameraPosition(): MapCameraPositionImpl {
        val camera = holder.map.cameraPosition.toMapCameraPosition()
        holder.map.projection.visibleRegion.let {
            val visibleRegion =
                VisibleRegion(
                    bounds = it.latLngBounds.toGeoRectBounds(),
                    nearLeft = it.nearLeft.toGeoPoint(),
                    nearRight = it.nearRight.toGeoPoint(),
                    farLeft = it.farLeft.toGeoPoint(),
                    farRight = it.farRight.toGeoPoint(),
                )
            return camera.copy(visibleRegion = visibleRegion)
        }
    }

    override fun onMapClick(position: LatLng) {
        backCoroutine.launch {
            val touchPosition = position.toGeoPoint()

            circleController.find(touchPosition)?.let { entity ->
                val event =
                    CircleEvent(
                        state = entity.state,
                        clicked = touchPosition,
                    )
                coroutine.launch {
                    circleController.clickListener?.invoke(event)
                }
                return@launch
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
                return@launch
            }

            polylineController.findWithClosestPoint(touchPosition)?.let { hitResult ->
                val event =
                    PolylineEvent(
                        state = hitResult.entity.state,
                        clicked = hitResult.closestPoint,
                    )
                coroutine.launch {
                    polylineController.clickListener?.invoke(event)
                }
                return@launch
            }

            polygonController.find(touchPosition)?.let { entity ->
                val event =
                    PolygonEvent(
                        state = entity.state,
                        clicked = touchPosition,
                    )
                coroutine.launch {
                    polygonController.clickListener?.invoke(event)
                }
                return@launch
            }

            mapClickCallback?.let {
                coroutine.launch { it(position.toGeoPoint()) }
            }
        }
    }

    override suspend fun compositionGroundImages(data: List<GroundImageState>) = groundImageController.add(data)

    override suspend fun updateGroundImage(state: GroundImageState) = groundImageController.update(state)

    override suspend fun compositionPolygons(data: List<PolygonState>) = polygonController.add(data)

    override suspend fun updatePolygon(state: PolygonState) = polygonController.update(state)

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

    override fun hasMarker(state: MarkerState): Boolean = this.markerController.markerManager.hasEntity(state.id)

    override fun hasPolyline(state: PolylineState): Boolean =
        this.polylineController.polylineManager
            .hasEntity(state.id)

    override fun hasPolygon(state: PolygonState): Boolean = this.polygonController.polygonManager.hasEntity(state.id)

    override fun hasCircle(state: CircleState): Boolean = this.circleController.circleManager.hasEntity(state.id)

    override fun hasGroundImage(state: GroundImageState): Boolean =
        this.groundImageController.groundImageManager
            .hasEntity(state.id)

    override fun setOnGroundImageClickListener(listener: OnGroundImageEventHandler?) {
        this.groundImageController.clickListener = listener
    }

    override fun setOnPolylineClickListener(listener: OnPolylineEventHandler?) {
        this.polylineController.clickListener = listener
    }

    override fun setOnPolygonClickListener(listener: OnPolygonEventHandler?) {
        this.polygonController.clickListener = listener
    }

    private var mapDesignType: GoogleMapDesignType = GoogleMapDesign.None
    private var mapDesignTypeChangeListener: GoogleMapDesignTypeChangeHandler? = null

    override fun setMapDesignType(value: GoogleMapDesignType) {
        coroutine.launch {
            holder.map.mapType = value.getValue()
        }
        mapDesignType = value
        mapDesignTypeChangeListener?.invoke(value)
    }

    override fun setMapDesignTypeChangeListener(listener: GoogleMapDesignTypeChangeHandler) {
        mapDesignTypeChangeListener = listener
        listener(mapDesignType)
    }

    override fun onMapLoaded() {
        mapLoadedCallback?.invoke()
        mapLoadedCallback = null

        val mapDesignType = GoogleMapDesign.toMapDesignType(holder.map.mapType)
        mapDesignTypeChangeListener?.invoke(mapDesignType)
    }
}
