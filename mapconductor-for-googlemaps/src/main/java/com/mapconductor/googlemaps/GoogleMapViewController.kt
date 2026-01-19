package com.mapconductor.googlemaps

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.CancelableCallback
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener
import com.google.android.gms.maps.GoogleMap.OnCameraMoveCanceledListener
import com.google.android.gms.maps.GoogleMap.OnCameraMoveListener
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener
import com.google.android.gms.maps.GoogleMap.OnMapClickListener
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener
import com.google.android.gms.maps.model.LatLng
import com.mapconductor.core.circle.CircleEvent
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.circle.OnCircleEventHandler
import com.mapconductor.core.controller.BaseMapViewController
import com.mapconductor.core.groundimage.GroundImageEvent
import com.mapconductor.core.groundimage.GroundImageState
import com.mapconductor.core.groundimage.OnGroundImageEventHandler
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.VisibleRegion
import com.mapconductor.core.marker.MarkerEventControllerInterface
import com.mapconductor.core.marker.MarkerOverlayRendererInterface
import com.mapconductor.core.marker.MarkerRenderingStrategyInterface
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.marker.StrategyMarkerController
import com.mapconductor.core.polygon.OnPolygonEventHandler
import com.mapconductor.core.polygon.PolygonEvent
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.core.polyline.OnPolylineEventHandler
import com.mapconductor.core.polyline.PolylineEvent
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.core.raster.RasterLayerState
import com.mapconductor.googlemaps.circle.GoogleMapCircleController
import com.mapconductor.googlemaps.groundimage.GoogleMapGroundImageController
import com.mapconductor.googlemaps.marker.DefaultGoogleMapMarkerEventController
import com.mapconductor.googlemaps.marker.GoogleMapMarkerController
import com.mapconductor.googlemaps.marker.GoogleMapMarkerEventControllerInterface
import com.mapconductor.googlemaps.marker.GoogleMapMarkerRenderer
import com.mapconductor.googlemaps.marker.MarkerTileRasterLayerCallback
import com.mapconductor.googlemaps.marker.StrategyGoogleMapMarkerEventController
import com.mapconductor.googlemaps.polygon.GoogleMapPolygonController
import com.mapconductor.googlemaps.polyline.GoogleMapPolylineController
import com.mapconductor.googlemaps.raster.GoogleMapRasterLayerController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GoogleMapViewController(
    override val holder: GoogleMapViewHolder,
    private val markerController: GoogleMapMarkerController,
    private val polylineController: GoogleMapPolylineController,
    private val polygonController: GoogleMapPolygonController,
    private val groundImageController: GoogleMapGroundImageController,
    private val circleController: GoogleMapCircleController,
    private val rasterLayerController: GoogleMapRasterLayerController,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
    val backCoroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : BaseMapViewController(),
    GoogleMapViewControllerInterface,
    OnCameraMoveStartedListener,
    OnCameraMoveCanceledListener,
    OnCameraMoveListener,
    OnCameraIdleListener,
    OnMapClickListener,
    OnMarkerClickListener,
    OnMarkerDragListener,
    GoogleMap.OnMapLoadedCallback {
    private val markerEventControllers = mutableListOf<GoogleMapMarkerEventControllerInterface>()
    private val _mapLoadedState = MutableStateFlow(false)
    val mapLoadedState: StateFlow<Boolean> = _mapLoadedState
    private var markerClickListener: OnMarkerEventHandler? = null
    private var markerDragStartListener: OnMarkerEventHandler? = null
    private var markerDragListener: OnMarkerEventHandler? = null
    private var markerDragEndListener: OnMarkerEventHandler? = null
    private var markerAnimateStartListener: OnMarkerEventHandler? = null
    private var markerAnimateEndListener: OnMarkerEventHandler? = null

    init {
        setupListeners()
        registerController(markerController)
        registerController(polygonController)
        registerController(polylineController)
        registerController(circleController)
        registerController(rasterLayerController)
        registerMarkerEventController(DefaultGoogleMapMarkerEventController(markerController))

        // Wire up the RasterLayer callback for marker tile rendering
        markerController.setRasterLayerCallback(
            MarkerTileRasterLayerCallback { state ->
                if (state != null) {
                    rasterLayerController.upsert(state)
                } else {
                    // Remove all marker tile layers
                    val markerTileLayers =
                        rasterLayerController.rasterLayerManager
                            .allEntities()
                            .filter { it.state.id.startsWith("marker-tile-") }
                    markerTileLayers.forEach { entity -> rasterLayerController.removeById(entity.state.id) }
                }
            },
        )
    }

    fun setupListeners() {
        holder.map.setOnCameraMoveStartedListener(this)
        holder.map.setOnCameraMoveCanceledListener(this)
        holder.map.setOnCameraMoveListener(this)
        holder.map.setOnCameraIdleListener(this)
        holder.map.setOnMapClickListener(this)
        holder.map.setOnMapLoadedCallback(this)
        holder.map.setOnMarkerClickListener(this)
        holder.map.setOnMarkerDragListener(this)
    }

    override fun moveCamera(position: MapCameraPosition) {
        coroutine.launch {
            val dstCameraPosition = position.toCameraPosition()
            val cameraUpdate = CameraUpdateFactory.newCameraPosition(dstCameraPosition)
            holder.map.moveCamera(cameraUpdate)
        }
    }

    override fun animateCamera(
        position: MapCameraPosition,
        duration: Long,
    ) {
        val dstCameraPosition = position.toCameraPosition()
        coroutine.launch {
            val cameraUpdate = CameraUpdateFactory.newCameraPosition(dstCameraPosition)
            holder.map.animateCamera(
                cameraUpdate,
                duration.toInt(),
                object : CancelableCallback {
                    override fun onCancel() {
                        cameraMoveEndCallback?.invoke(getMapCameraPosition())
                    }

                    override fun onFinish() {
                        cameraMoveEndCallback?.invoke(getMapCameraPosition())
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
        rasterLayerController.clear()
    }

    override suspend fun compositionMarkers(data: List<MarkerState>) = markerController.add(data)

    override suspend fun updateMarker(state: MarkerState) = markerController.update(state)

    override suspend fun compositionCircles(data: List<CircleState>) = circleController.add(data)

    override suspend fun updateCircle(state: CircleState) = circleController.update(state)

    override suspend fun compositionRasterLayers(data: List<RasterLayerState>) = rasterLayerController.add(data)

    override suspend fun updateRasterLayer(state: RasterLayerState) = rasterLayerController.update(state)

    @Deprecated("Use CircleState.onClick instead.")
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
        cameraMoveCallback?.invoke(getMapCameraPosition())
    }

    override fun onCameraIdle() {
        val mapCameraPosition = getMapCameraPosition()
        backCoroutine.launch { markerController.onCameraChanged(mapCameraPosition) }
        cameraMoveEndCallback?.invoke(getMapCameraPosition())
    }

    override fun onCameraMoveStarted(p0: Int) {
        cameraMoveStartCallback?.invoke(getMapCameraPosition())
    }

    override fun onCameraMoveCanceled() {
        cameraMoveEndCallback?.invoke(getMapCameraPosition())
    }

    private fun getMapCameraPosition(): MapCameraPosition {
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
        val touchPosition = position.toGeoPoint()
        val zoomSnapshot =
            holder.map.cameraPosition.zoom
                .toDouble()
        backCoroutine.launch {
            markerController.find(touchPosition, zoomSnapshot)?.let { entity ->
                if (!entity.state.clickable) return@launch
                coroutine.launch { markerController.dispatchClick(entity.state) }
                return@launch
            }

            circleController.find(touchPosition)?.let { entity ->
                val event =
                    CircleEvent(
                        state = entity.state,
                        clicked = touchPosition,
                    )
                coroutine.launch {
                    circleController.dispatchClick(event)
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
                    groundImageController.dispatchClick(event)
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
                    polylineController.dispatchClick(event)
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
                    polygonController.dispatchClick(event)
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

    @Deprecated("Use MarkerState.onDragStart instead.")
    override fun setOnMarkerDragStart(listener: OnMarkerEventHandler?) {
        markerDragStartListener = listener
        markerEventControllers.forEach { it.setDragStartListener(listener) }
    }

    @Deprecated("Use MarkerState.onDrag instead.")
    override fun setOnMarkerDrag(listener: OnMarkerEventHandler?) {
        markerDragListener = listener
        markerEventControllers.forEach { it.setDragListener(listener) }
    }

    @Deprecated("Use MarkerState.onDragEnd instead.")
    override fun setOnMarkerDragEnd(listener: OnMarkerEventHandler?) {
        markerDragEndListener = listener
        markerEventControllers.forEach { it.setDragEndListener(listener) }
    }

    @Deprecated("Use MarkerState.onAnimateStart instead.")
    override fun setOnMarkerAnimateStart(listener: OnMarkerEventHandler?) {
        markerAnimateStartListener = listener
        markerEventControllers.forEach { it.setAnimateStartListener(listener) }
    }

    @Deprecated("Use MarkerState.onAnimateEnd instead.")
    override fun setOnMarkerAnimateEnd(listener: OnMarkerEventHandler?) {
        markerAnimateEndListener = listener
        markerEventControllers.forEach { it.setAnimateEndListener(listener) }
    }

    @Deprecated("Use MarkerState.onClick instead.")
    override fun setOnMarkerClickListener(listener: OnMarkerEventHandler?) {
        markerClickListener = listener
        markerEventControllers.forEach { it.setClickListener(listener) }
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

    override fun hasRasterLayer(state: RasterLayerState): Boolean =
        this.rasterLayerController.rasterLayerManager.hasEntity(state.id)

    @Deprecated("Use GroundImageState.onClick instead.")
    override fun setOnGroundImageClickListener(listener: OnGroundImageEventHandler?) {
        this.groundImageController.clickListener = listener
    }

    @Deprecated("Use PolylineState.onClick instead.")
    override fun setOnPolylineClickListener(listener: OnPolylineEventHandler?) {
        this.polylineController.clickListener = listener
    }

    @Deprecated("Use PolygonState.onClick instead.")
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
        _mapLoadedState.value = true
        mapLoadedCallback?.invoke()
        mapLoadedCallback = null

        val mapDesignType = GoogleMapDesign.toMapDesignType(holder.map.mapType)
        mapDesignTypeChangeListener?.invoke(mapDesignType)
    }

    // Trigger an initial camera update after the view and map are ready
    private var initialCameraUpdateAttempts = 0

    fun sendInitialCameraUpdate() {
        val w = holder.mapView.width
        val h = holder.mapView.height
        if (w <= 0 || h <= 0) {
            if (initialCameraUpdateAttempts >= INITIAL_CAMERA_UPDATE_MAX_ATTEMPTS) return
            initialCameraUpdateAttempts += 1
            holder.mapView.post { sendInitialCameraUpdate() }
            return
        }
        initialCameraUpdateAttempts = 0
        val mapCameraPosition = getMapCameraPosition()
        backCoroutine.launch { notifyMapCameraPosition(mapCameraPosition) }
    }

    fun createMarkerRenderer(
        strategy: MarkerRenderingStrategyInterface<GoogleMapActualMarker>,
    ): MarkerOverlayRendererInterface<GoogleMapActualMarker> = GoogleMapMarkerRenderer(holder = holder)

    fun createMarkerEventController(
        controller: StrategyMarkerController<GoogleMapActualMarker>,
        renderer: MarkerOverlayRendererInterface<GoogleMapActualMarker>,
    ): MarkerEventControllerInterface<GoogleMapActualMarker> = StrategyGoogleMapMarkerEventController(controller)

    fun registerMarkerEventController(controller: MarkerEventControllerInterface<GoogleMapActualMarker>) {
        val typed = controller as? GoogleMapMarkerEventControllerInterface ?: return
        registerMarkerEventController(typed)
    }

    fun onMarkerRenderingReady() {
        sendInitialCameraUpdate()
    }

    companion object {
        private const val INITIAL_CAMERA_UPDATE_MAX_ATTEMPTS = 10
    }

    internal fun registerMarkerEventController(controller: GoogleMapMarkerEventControllerInterface) {
        if (markerEventControllers.contains(controller)) return
        markerEventControllers.add(controller)
        controller.setClickListener(markerClickListener)
        controller.setDragStartListener(markerDragStartListener)
        controller.setDragListener(markerDragListener)
        controller.setDragEndListener(markerDragEndListener)
        controller.setAnimateStartListener(markerAnimateStartListener)
        controller.setAnimateEndListener(markerAnimateEndListener)
    }

    override fun onMarkerClick(marker: GoogleMapActualMarker): Boolean {
        val stateId = marker.tag as? String ?: return false
        markerEventControllers.forEach { controller ->
            val entity = controller.getEntity(stateId) ?: return@forEach
            if (!entity.state.clickable) return true
            controller.dispatchClick(entity.state)
            return true
        }
        return false
    }

    override fun onMarkerDrag(marker: GoogleMapActualMarker) {
        val stateId = marker.tag as? String ?: return
        markerEventControllers.forEach { controller ->
            val entity = controller.getEntity(stateId) ?: return@forEach
            entity.state.position = marker.position.toGeoPoint()
            controller.dispatchDrag(entity.state)
            return
        }
    }

    override fun onMarkerDragEnd(marker: GoogleMapActualMarker) {
        val stateId = marker.tag as? String ?: return
        markerEventControllers.forEach { controller ->
            val entity = controller.getEntity(stateId) ?: return@forEach
            entity.state.position = marker.position.toGeoPoint()
            controller.dispatchDragEnd(entity.state)
            return
        }
    }

    override fun onMarkerDragStart(marker: GoogleMapActualMarker) {
        val stateId = marker.tag as? String ?: return
        markerEventControllers.forEach { controller ->
            val entity = controller.getEntity(stateId) ?: return@forEach
            entity.state.position = marker.position.toGeoPoint()
            controller.dispatchDragStart(entity.state)
            return
        }
    }
}
