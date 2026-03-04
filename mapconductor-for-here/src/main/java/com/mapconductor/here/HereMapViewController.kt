package com.mapconductor.here

import HereMapDesignTypeChangeHandler
import HereMapViewControllerInterface
import androidx.compose.ui.geometry.Offset
import com.here.sdk.animation.AnimationState
import com.here.sdk.core.GeoOrientation
import com.here.sdk.core.Point2D
import com.here.sdk.gestures.GestureState
import com.here.sdk.gestures.LongPressListener
import com.here.sdk.gestures.TapListener
import com.here.sdk.mapview.MapCamera
import com.here.sdk.mapview.MapCameraAnimationFactory
import com.here.sdk.mapview.MapCameraListener
import com.here.sdk.mapview.MapMeasure
import com.here.time.Duration
import com.mapconductor.core.circle.CircleCapableInterface
import com.mapconductor.core.circle.CircleEvent
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.circle.OnCircleEventHandler
import com.mapconductor.core.controller.BaseMapViewController
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.groundimage.GroundImageEvent
import com.mapconductor.core.groundimage.GroundImageState
import com.mapconductor.core.groundimage.OnGroundImageEventHandler
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapCameraPositionInterface
import com.mapconductor.core.map.MapPaddingsInterface
import com.mapconductor.core.map.VisibleRegion
import com.mapconductor.core.marker.MarkerEventControllerInterface
import com.mapconductor.core.marker.MarkerOverlayRendererInterface
import com.mapconductor.core.marker.MarkerRenderingStrategyInterface
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.MarkerTileRasterLayerCallback
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.marker.StrategyMarkerController
import com.mapconductor.core.polygon.OnPolygonEventHandler
import com.mapconductor.core.polygon.PolygonEvent
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.core.polyline.OnPolylineEventHandler
import com.mapconductor.core.polyline.PolylineEvent
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.core.raster.RasterLayerState
import com.mapconductor.here.circle.HereCircleController
import com.mapconductor.here.groundimage.HereGroundImageController
import com.mapconductor.here.marker.DefaultHereMarkerEventController
import com.mapconductor.here.marker.HereMarkerController
import com.mapconductor.here.marker.HereMarkerEventControllerInterface
import com.mapconductor.here.marker.HereMarkerRenderer
import com.mapconductor.here.marker.StrategyHereMarkerEventController
import com.mapconductor.here.polygon.HerePolygonController
import com.mapconductor.here.polyline.HerePolylineController
import com.mapconductor.here.raster.HereRasterLayerController
import com.mapconductor.here.zoom.ZoomAltitudeConverter
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HereMapViewController(
    private val markerController: HereMarkerController,
    private val polylineController: HerePolylineController,
    private val polygonController: HerePolygonController,
    private val groundImageController: HereGroundImageController,
    private val circleController: HereCircleController,
    private val rasterLayerController: HereRasterLayerController,
    override val holder: HereViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
    val backCoroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : BaseMapViewController(),
    CircleCapableInterface,
    HereMapViewControllerInterface,
    MapCameraListener,
    TapListener,
    LongPressListener {
    private val zoomConverter = ZoomAltitudeConverter()

    private val markerEventControllers = mutableListOf<HereMarkerEventControllerInterface>()
    private var activeDragController: HereMarkerEventControllerInterface? = null
    private var markerClickListener: OnMarkerEventHandler? = null
    private var markerDragStartListener: OnMarkerEventHandler? = null
    private var markerDragListener: OnMarkerEventHandler? = null
    private var markerDragEndListener: OnMarkerEventHandler? = null
    private var markerAnimateStartListener: OnMarkerEventHandler? = null
    private var markerAnimateEndListener: OnMarkerEventHandler? = null
    private var lastRequestedCameraPosition: MapCameraPosition? = null
    private val cameraRequestGeneration = AtomicLong(0L)

    // HERE's MapCameraListener provides only continuous updates. Synthesize a "move end" after an idle window
    // so app code can treat HERE similarly to other SDKs (e.g., for camera sync).
    private var cameraMoveEndJob: Job? = null
    private var cameraMoveInProgress: Boolean = false
    private var isAnimatingCamera: Boolean = false
    private var lastCameraPosition: MapCameraPosition? = null

    private companion object {
        private const val CAMERA_MOVE_END_IDLE_MS = 120L
    }

    override suspend fun clearOverlays() {
        markerController.clear()
        polylineController.clear()
        polygonController.clear()
        groundImageController.clear()
        circleController.clear()
        rasterLayerController.clear()
    }

    override suspend fun compositionMarkers(data: List<MarkerState>) = markerController.add(data)

    override suspend fun updateMarker(state: MarkerState) = markerController.update(state)

    override suspend fun compositionGroundImages(data: List<GroundImageState>) = groundImageController.add(data)

    override suspend fun updateGroundImage(state: GroundImageState) = groundImageController.update(state)

    override fun hasMarker(state: MarkerState): Boolean = this.markerController.markerManager.hasEntity(state.id)

    override fun hasPolyline(state: PolylineState): Boolean =
        this.polylineController.polylineManager
            .hasEntity(state.id)

    override fun hasPolygon(state: PolygonState): Boolean = this.polygonController.polygonManager.hasEntity(state.id)

    override fun hasCircle(state: CircleState): Boolean = this.circleController.circleManager.hasEntity(state.id)

    override fun hasGroundImage(state: GroundImageState): Boolean =
        this.groundImageController.groundImageManager.hasEntity(state.id)

    override fun hasRasterLayer(state: RasterLayerState): Boolean =
        this.rasterLayerController.rasterLayerManager.hasEntity(state.id)

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

    override suspend fun compositionCircles(data: List<CircleState>) = circleController.add(data)

    override suspend fun updateCircle(state: CircleState) = circleController.update(state)

    @Deprecated("Use CircleState.onClick instead.")
    override fun setOnCircleClickListener(listener: OnCircleEventHandler?) {
        this.circleController.clickListener = listener
    }

    @Deprecated("Use GroundImageState.onClick instead.")
    override fun setOnGroundImageClickListener(listener: OnGroundImageEventHandler?) {
        this.groundImageController.clickListener = listener
    }

    override suspend fun compositionPolylines(data: List<PolylineState>) = polylineController.add(data)

    override suspend fun updatePolyline(state: PolylineState) = polylineController.update(state)

    override suspend fun compositionPolygons(data: List<PolygonState>) = polygonController.add(data)

    override suspend fun updatePolygon(state: PolygonState) = polygonController.update(state)

    override suspend fun compositionRasterLayers(data: List<RasterLayerState>) = rasterLayerController.add(data)

    override suspend fun updateRasterLayer(state: RasterLayerState) = rasterLayerController.update(state)

    init {
        setupListeners()
        registerController(markerController)
        registerController(polygonController)
        registerController(polylineController)
        registerController(groundImageController)
        registerController(circleController)
        registerController(rasterLayerController)
        registerMarkerEventController(DefaultHereMarkerEventController(markerController))

        markerController.setRasterLayerCallback(
            MarkerTileRasterLayerCallback { state ->
                if (state != null) {
                    rasterLayerController.upsert(state)
                } else {
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
        holder.mapView.camera.removeListener(this)
        holder.mapView.camera.addListener(this)
        holder.mapView.gestures.tapListener = this
        holder.mapView.gestures.longPressListener = this
    }

    override fun moveCamera(position: MapCameraPosition) {
        lastRequestedCameraPosition = position
        val request = cameraRequestGeneration.incrementAndGet()
        val camera = this.holder.mapView.camera
        val adjustCameraUpdate = position.toMapCameraUpdate()

        camera.applyUpdate(adjustCameraUpdate)

        // If this runs before first layout, HERE may ignore it; retry once after layout.
        if (holder.mapView.width == 0 || holder.mapView.height == 0) {
            holder.mapView.post {
                if (cameraRequestGeneration.get() == request) {
                    camera.applyUpdate(adjustCameraUpdate)
                }
            }
        }
    }

    override fun animateCamera(
        position: MapCameraPosition,
        duration: Long,
    ) {
        lastRequestedCameraPosition = position
        cameraRequestGeneration.incrementAndGet()
        val camera = this.holder.mapView.camera
        val update = position.toMapCameraUpdate()

        val hereCameraZoom = position.zoom

//      bowFactor > 0: 最初にズームアウト → 到達時にズームイン
//      bowFactor < 0: 最初にズームイン → 到達時にズームアウト（ややレア）
//      bowFactor = 0: 常に同じズーム（直線的）
        val bowFactor = 1.0
        val animation =
            MapCameraAnimationFactory.flyTo(
                GeoPoint.from(position.position).toGeoCoordinates().toUpdate(),
                GeoOrientation(position.bearing, position.tilt).toUpdate(),
                MapMeasure(MapMeasure.Kind.ZOOM_LEVEL, hereCameraZoom),
                bowFactor,
                Duration.ofMillis(duration),
            )
        coroutine.launch {
            isAnimatingCamera = true
            camera.startAnimation(animation) { animState ->
                when (animState) {
                    // Do nothing here
                    AnimationState.STARTED -> {
                        getMapCameraPosition(holder.mapView.camera.state)?.let {
                            cameraMoveStartCallback?.invoke(it)
                        }
                    }
                    AnimationState.COMPLETED -> {
                        isAnimatingCamera = false
                        cameraMoveEndCallback?.invoke(position)
                    }
                    AnimationState.CANCELLED -> {
                        isAnimatingCamera = false
                        getMapCameraPosition(holder.mapView.camera.state)?.let {
                            cameraMoveEndCallback?.invoke(it)
                        }
                    }
                }
            }
        }
    }

    override fun onMapCameraUpdated(cameraState: MapCamera.State) {
        // Must run on main thread: HERE MapView coordinate conversion APIs are not thread-safe.
        coroutine.launch {
            val mapCameraPosition = getMapCameraPosition(cameraState) ?: return@launch
            lastCameraPosition = mapCameraPosition

            // This will call registered overlay controllers and cameraMoveCallback.
            notifyMapCameraPosition(mapCameraPosition)

            // animateCamera() already provides a reliable end callback.
            if (isAnimatingCamera) return@launch

            if (!cameraMoveInProgress) {
                cameraMoveInProgress = true
                cameraMoveStartCallback?.invoke(mapCameraPosition)
            }

            cameraMoveEndJob?.cancel()
            cameraMoveEndJob =
                coroutine.launch {
                    delay(CAMERA_MOVE_END_IDLE_MS)
                    val last = lastCameraPosition ?: return@launch
                    cameraMoveInProgress = false
                    cameraMoveEndCallback?.invoke(last)
                }
        }
    }

    private fun getMapCameraPosition(cameraState: MapCamera.State): MapCameraPosition? {
        return holder.mapView.camera.boundingBox?.let { boundingBox ->
            val mapWidth = holder.mapView.width.toFloat()
            val mapHeight = holder.mapView.height.toFloat()
            val leftTop = Offset(0.0f, 0.0f)
            val rightTop = Offset(mapWidth, 0.0f)
            val leftBottom = Offset(0.0f, holder.mapView.height.toFloat())
            val rightBottom = Offset(mapWidth, mapHeight)
            val bounds = boundingBox.toGeoRectBounds()
            val visibleRegion =
                VisibleRegion(
                    bounds = bounds,
                    nearLeft = holder.fromScreenOffsetSync(leftBottom),
                    nearRight = holder.fromScreenOffsetSync(rightBottom),
                    farLeft = holder.fromScreenOffsetSync(leftTop),
                    farRight = holder.fromScreenOffsetSync(rightTop),
                )

            val cameraPosition =
                MapCameraPosition.from(
                    object : MapCameraPositionInterface {
                        override val position: GeoPointInterface = cameraState.targetCoordinates.toGeoPoint()
                        override val zoom: Double = cameraState.toMapCameraPosition().zoom
                        override val bearing: Double = cameraState.orientationAtTarget.bearing
                        override val tilt: Double = cameraState.orientationAtTarget.tilt
                        override val paddings: MapPaddingsInterface? = null
                        override val visibleRegion: VisibleRegion? = visibleRegion
                    },
                )
            return@let cameraPosition
        }
    }

    override fun onTap(point: Point2D) {
        val touchPosition = this.getGeoPointFromPoint(point) ?: return

        markerEventControllers.forEach { controller ->
            controller.find(touchPosition)?.let { entity ->
                controller.dispatchClick(entity.state)
                return
            }
        }

        circleController.find(touchPosition)?.let { entity ->
            val event =
                CircleEvent(
                    state = entity.state,
                    clicked = touchPosition,
                )
            circleController.dispatchClick(event)
            return
        }

        groundImageController.find(touchPosition)?.let { entity ->
            val event =
                GroundImageEvent(
                    state = entity.state,
                    clicked = touchPosition,
                )
            groundImageController.dispatchClick(event)
            return
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
            return
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
            return
        }

        // If no overlay is processed, process the tap as onMapClick
        mapClickCallback?.invoke(touchPosition)
    }

    override fun onLongPress(
        gesture: GestureState,
        point: Point2D,
    ) {
        val position = this.getGeoPointFromPoint(point) ?: return

        when (gesture.value) {
            GestureState.BEGIN.value -> {
                markerEventControllers.forEach { controller ->
                    controller.find(position)?.let { entity ->
                        if (entity.state.draggable) {
                            entity.state.position = position
                            activeDragController = controller
                            controller.setSelectedMarker(entity)
                            controller.dispatchDragStart(entity.state)
                            return
                        }
                    }
                }
                mapLongClickCallback?.invoke(position)
            }

            GestureState.UPDATE.value -> {
                val controller = activeDragController ?: return
                controller.getSelectedMarker()?.also { selected ->
                    holder.mapView.viewToGeoCoordinates(point)?.also { coordinates ->
                        selected.marker?.coordinates = coordinates
                        selected.state.position = coordinates.toGeoPoint()
                    }
                    controller.dispatchDrag(selected.state)
                }
            }

            GestureState.END.value, GestureState.CANCEL.value -> {
                val controller = activeDragController ?: return
                controller.getSelectedMarker()?.also { selected ->
                    controller.dispatchDragEnd(selected.state)
                    controller.setSelectedMarker(null)
                    activeDragController = null
                }
            }
        }
    }

    private fun getGeoPointFromPoint(point: Point2D): GeoPoint? =
        holder.mapView
            .viewToGeoCoordinates(point)
            ?.toGeoPoint()

    @Deprecated("Use PolylineState.onClick instead.")
    override fun setOnPolylineClickListener(listener: OnPolylineEventHandler?) {
        polylineController.clickListener = listener
    }

    @Deprecated("Use PolygonState.onClick instead.")
    override fun setOnPolygonClickListener(listener: OnPolygonEventHandler?) {
        polygonController.clickListener = listener
    }

    private var mapDesignType: HereMapDesignType = HereMapDesign.NormalDay
    private var mapDesignTypeChangeListener: HereMapDesignTypeChangeHandler? = null

    override fun setMapDesignType(value: HereMapDesignType) {
        val scene = value.getValue()
        coroutine.launch {
            holder.mapView.mapScene.loadScene(scene) {
                mapDesignType = value

                // loadScene can reset camera; restore the last requested camera to prevent jumping.
                lastRequestedCameraPosition?.let { cameraPosition ->
                    holder.mapView.post { moveCamera(cameraPosition) }
                }

                mapLoadedCallback?.invoke()
                mapLoadedCallback = null

                mapDesignTypeChangeListener?.invoke(value)
            }
        }
    }

    override fun setMapDesignTypeChangeListener(listener: HereMapDesignTypeChangeHandler) {
        mapDesignTypeChangeListener = listener
        listener(mapDesignType)
    }

    internal fun registerMarkerEventController(controller: HereMarkerEventControllerInterface) {
        if (markerEventControllers.contains(controller)) return
        markerEventControllers.add(controller)
        controller.setClickListener(markerClickListener)
        controller.setDragStartListener(markerDragStartListener)
        controller.setDragListener(markerDragListener)
        controller.setDragEndListener(markerDragEndListener)
        controller.setAnimateStartListener(markerAnimateStartListener)
        controller.setAnimateEndListener(markerAnimateEndListener)
    }

    fun createMarkerRenderer(
        strategy: MarkerRenderingStrategyInterface<HereActualMarker>,
    ): MarkerOverlayRendererInterface<HereActualMarker> = HereMarkerRenderer(holder = holder)

    fun createMarkerEventController(
        controller: StrategyMarkerController<HereActualMarker>,
        renderer: MarkerOverlayRendererInterface<HereActualMarker>,
    ): MarkerEventControllerInterface<HereActualMarker> = StrategyHereMarkerEventController(controller)

    fun registerMarkerEventController(controller: MarkerEventControllerInterface<HereActualMarker>) {
        val typed = controller as? HereMarkerEventControllerInterface ?: return
        registerMarkerEventController(typed)
    }
}
