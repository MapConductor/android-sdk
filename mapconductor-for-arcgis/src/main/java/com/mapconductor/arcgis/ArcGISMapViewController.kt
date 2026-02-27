package com.mapconductor.arcgis.map

import androidx.compose.ui.geometry.Offset
import com.arcgismaps.mapping.Basemap
import com.arcgismaps.mapping.view.Camera
import com.arcgismaps.mapping.view.LongPressEvent
import com.arcgismaps.mapping.view.PanChangeEvent
import com.arcgismaps.mapping.view.SingleTapConfirmedEvent
import com.arcgismaps.mapping.view.UpEvent
import com.arcgismaps.mapping.view.extensions.motionEvent
import com.mapconductor.arcgis.ArcGISActualMarker
import com.mapconductor.arcgis.calculateCameraForOrbitParameters
import com.mapconductor.arcgis.circle.ArcGISCircleOverlayController
import com.mapconductor.arcgis.fromLongLat
import com.mapconductor.arcgis.groundimage.ArcGISGroundImageController
import com.mapconductor.arcgis.marker.ArcGISMarkerController
import com.mapconductor.arcgis.marker.ArcGISMarkerEventControllerInterface
import com.mapconductor.arcgis.marker.ArcGISMarkerRenderer
import com.mapconductor.arcgis.marker.DefaultArcGISMarkerEventController
import com.mapconductor.arcgis.marker.StrategyArcGISMarkerEventController
import com.mapconductor.arcgis.polygon.ArcGISPolygonOverlayController
import com.mapconductor.arcgis.polyline.ArcGISPolylineOverlayController
import com.mapconductor.arcgis.raster.ArcGISRasterLayerController
import com.mapconductor.arcgis.toGeoPoint
import com.mapconductor.arcgis.toPoint
import com.mapconductor.arcgis.zoom.ZoomAltitudeConverter
import com.mapconductor.core.circle.CircleEvent
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.circle.OnCircleEventHandler
import com.mapconductor.core.controller.BaseMapViewController
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.groundimage.GroundImageEvent
import com.mapconductor.core.groundimage.GroundImageState
import com.mapconductor.core.groundimage.OnGroundImageEventHandler
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapPaddings
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
import com.mapconductor.settings.Settings
import android.view.MotionEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ArcGISMapViewController(
    override val holder: ArcGISMapViewHolder,
    private val markerController: ArcGISMarkerController,
    private val polylineController: ArcGISPolylineOverlayController,
    private val polygonController: ArcGISPolygonOverlayController,
    private val circleController: ArcGISCircleOverlayController,
    private val groundImageController: ArcGISGroundImageController,
    private val rasterLayerController: ArcGISRasterLayerController,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : BaseMapViewController(),
    ArcGISMapViewControllerInterface {
    private val markerEventControllers = mutableListOf<ArcGISMarkerEventControllerInterface>()
    private var activeDragController: ArcGISMarkerEventControllerInterface? = null
    private var markerClickListener: OnMarkerEventHandler? = null
    private var markerDragStartListener: OnMarkerEventHandler? = null
    private var markerDragListener: OnMarkerEventHandler? = null
    private var markerDragEndListener: OnMarkerEventHandler? = null
    private var markerAnimateStartListener: OnMarkerEventHandler? = null
    private var markerAnimateEndListener: OnMarkerEventHandler? = null

    // ArcGIS updates the viewpoint asynchronously; firing "move end" immediately after setViewpointCamera()
    // can read a stale camera and cause feedback loops in camera sync scenarios.
    private var cameraMoveEndJob: Job? = null
    private val cameraMoveEndDebounceMs = 180L

    init {
        holder.map.graphicsOverlays.clear()
        holder.map.graphicsOverlays.add(circleController.renderer.circleLayer)
        holder.map.graphicsOverlays.add(polygonController.renderer.polygonLayer)
        holder.map.graphicsOverlays.add(polylineController.renderer.polylineLayer)
        holder.map.graphicsOverlays.add(markerController.renderer.markerLayer)
        setupListeners()
        registerController(markerController)
        registerController(polygonController)
        registerController(polylineController)
        registerController(circleController)
        registerController(groundImageController)
        registerController(rasterLayerController)
        registerMarkerEventController(DefaultArcGISMarkerEventController(markerController))

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
        coroutine.launch {
            holder.map.onSingleTapConfirmed.collect { onMapTap(it) }
        }
        coroutine.launch {
            holder.map.viewpointChanged.collect { onViewpointChange() }
        }
        coroutine.launch {
            holder.map.onInteractiveZooming.collect { invokeCameraMoveCallback() }
        }
        coroutine.launch {
            holder.map.onRotate.collect { invokeCameraMoveCallback() }
        }
        coroutine.launch {
            holder.map.onLongPress.collect { onMapLongPress(it) }
        }
        coroutine.launch {
            holder.map.onUp.collect { onMapUp(it) }
        }
        coroutine.launch {
            holder.map.onPan.collect { onMapPan(it) }
        }
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

    private suspend fun invokeCameraMoveStartCallback() {
        cameraMoveStartCallback?.let { cb ->
            getMapCameraPosition()?.let { mapCameraPosition ->
                cb(mapCameraPosition)
            }
        }
    }

    private suspend fun invokeCameraMoveCallback() {
        cameraMoveCallback?.let { cb ->
            getMapCameraPosition()?.let { mapCameraPosition ->
                cb(mapCameraPosition)
            }
        }
        scheduleCameraMoveEndCallback()
    }

    private suspend fun invokeCameraMoveEndCallback() {
        cameraMoveEndCallback?.let { cb ->
            getMapCameraPosition()?.let { mapCameraPosition ->
                cb(mapCameraPosition)
            }
        }
    }

    private fun scheduleCameraMoveEndCallback() {
        if (cameraMoveEndCallback == null) return
        cameraMoveEndJob?.cancel()
        cameraMoveEndJob =
            coroutine.launch {
                delay(cameraMoveEndDebounceMs)
                invokeCameraMoveEndCallback()
            }
    }

    private fun currentViewportSizeInDp(): Pair<Int, Int> {
        val density = holder.mapView.resources.displayMetrics.density.coerceAtLeast(0.1f)
        val widthDp = (holder.map.width / density).toInt().coerceAtLeast(1)
        val heightDp = (holder.map.height / density).toInt().coerceAtLeast(1)
        return Pair(widthDp, heightDp)
    }

    private suspend fun onViewpointChange() {
        mapLoadedCallback?.invoke()
        mapLoadedCallback = null

        getMapCameraPosition()?.let { mapCameraPosition ->
            notifyMapCameraPosition(mapCameraPosition)
            scheduleCameraMoveEndCallback()
        }
    }

    private suspend fun getMapCameraPosition(): MapCameraPosition? {
        val mapWidth = holder.map.width.toFloat() - 1.0f
        val mapHeight = holder.map.height.toFloat() - 1.0f
        val nearLeft =
            holder.fromScreenOffset(
                Offset(1.0f, mapHeight),
            ) ?: return null

        val nearRight =
            holder.fromScreenOffsetSync(
                Offset(mapWidth, mapHeight),
            ) ?: return null
        val farLeft =
            holder.fromScreenOffsetSync(
                Offset(1.0f, 1.0f),
            ) ?: return null
        val farRight =
            holder.fromScreenOffsetSync(
                Offset(mapWidth, 1.0f),
            ) ?: return null

        val bounds = GeoRectBounds()
        bounds.extend(nearLeft)
        bounds.extend(nearRight)
        bounds.extend(farLeft)
        bounds.extend(farRight)

        val visibleRegion =
            VisibleRegion(
                bounds = bounds,
                nearLeft = nearLeft,
                nearRight = nearRight,
                farLeft = farLeft,
                farRight = farRight,
            )

        val arcCamera = holder.map.getCurrentViewpointCamera()
        val lat = arcCamera.location.y
        val lon = arcCamera.location.x
        val alt = arcCamera.location.z ?: 0.0
        val tilt = arcCamera.pitch
        val bearing = ((arcCamera.heading % 360) + 360) % 360

        val conv = ZoomAltitudeConverter()
        val (viewportWidthDp, viewportHeightDp) = currentViewportSizeInDp()
        val zoom =
            conv.altitudeToZoomLevel(
                altitude = alt,
                latitude = lat,
                tilt = tilt,
                viewportWidthPx = viewportWidthDp,
                viewportHeightPx = viewportHeightDp,
            )

        val camera =
            MapCameraPosition(
                position =
                    GeoPoint
                        .fromLongLat(lon, lat, alt),
                zoom = zoom,
                bearing = bearing,
                tilt = tilt,
                paddings = MapPaddings.Zeros,
                visibleRegion = visibleRegion,
            )
        return camera
    }

    private suspend fun onMapPan(event: PanChangeEvent) {
        val controller = activeDragController
        if (controller != null) {
            val screenPoint = event.screenCoordinate
            val point = holder.map.screenToLocation(screenPoint).getOrNull() ?: return
            val position = point.toGeoPoint()
            controller.updateDrag(point, position)
            controller.getSelectedState()?.let { state ->
                controller.dispatchDrag(state)
            }
        }
        invokeCameraMoveCallback()
    }

    private suspend fun onMapUp(event: UpEvent) {
        val controller = activeDragController
        if (controller != null) {
            val screenPoint = event.screenCoordinate
            val point = holder.map.screenToLocation(screenPoint).getOrNull() ?: return
            val position = point.toGeoPoint()
            val selectedState = controller.getSelectedState()
            controller.endDrag(point, position)
            selectedState?.let { state ->
                controller.dispatchDragEnd(state)
            }
            activeDragController = null

            with(holder.map) {
                interactionOptions.isPanEnabled = true
                interactionOptions.isRotateEnabled = true
                interactionOptions.isZoomEnabled = true
            }
        }
    }

    private suspend fun onMapLongPress(event: LongPressEvent) {
        if (event.motionEvent.action != MotionEvent.ACTION_MOVE) return

        val screenPoint = event.screenCoordinate
        val point = holder.map.screenToLocation(screenPoint).getOrNull() ?: return
        val position = point.toGeoPoint()
        val identifyResult =
            holder.map.identifyGraphicsOverlay(
                graphicsOverlay = markerController.renderer.markerLayer,
                screenCoordinate = screenPoint,
                tolerance =
                    Settings.Default.tapTolerance.value
                        .toDouble(),
                returnPopupsOnly = false,
            )
        val graphics = identifyResult.getOrNull()?.graphics
        graphics?.firstOrNull()?.let { graphic ->
            (graphic.attributes.get("id") as? String)?.let { markerId ->
                markerController.markerManager.getEntity(markerId)?.let { entity ->
                    if (entity.state.draggable) {
                        activeDragController = markerEventControllers.firstOrNull()
                        activeDragController?.startDrag(entity)
                        // 3Dナビゲーションを無効化
                        with(holder.map) {
                            interactionOptions.isPanEnabled = false
                            interactionOptions.isRotateEnabled = false
                            interactionOptions.isZoomEnabled = false
                        }
                        activeDragController?.dispatchDragStart(entity.state)
                        return
                    }
                }
            }
        }
        markerEventControllers
            .drop(1)
            .forEach { controller ->
                controller.find(position)?.let { entity ->
                    if (entity.state.draggable) {
                        activeDragController = controller
                        controller.startDrag(entity)
                        with(holder.map) {
                            interactionOptions.isPanEnabled = false
                            interactionOptions.isRotateEnabled = false
                            interactionOptions.isZoomEnabled = false
                        }
                        controller.dispatchDragStart(entity.state)
                        return
                    }
                }
            }
        mapLongClickCallback?.invoke(position)
    }

    private suspend fun onMapTap(event: SingleTapConfirmedEvent) {
        val screenPoint = event.screenCoordinate
        val touchPosition =
            holder.map
                .screenToLocation(screenPoint)
                .getOrNull()
                ?.toGeoPoint() ?: return

        markerEventControllers.forEach { controller ->
            controller.find(touchPosition)?.let { markerEntity ->
                controller.dispatchClick(markerEntity.state)
                return
            }
        }

        circleController.find(touchPosition)?.let { circleEntity ->
            val event =
                CircleEvent(
                    state = circleEntity.state,
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
            polylineController.dispatchClick(event)
            return
        }

        polygonController.find(touchPosition)?.let { polygonEntity ->
            val event =
                PolygonEvent(
                    state = polygonEntity.state,
                    clicked = touchPosition,
                )
            polygonController.dispatchClick(event)
            return
        }

        holder.map.screenToLocation(screenPoint).getOrNull()?.also {
            mapClickCallback?.invoke(it.toGeoPoint())
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

    override suspend fun compositionPolylines(data: List<PolylineState>) = polylineController.add(data)

    override suspend fun updatePolyline(state: PolylineState) = polylineController.update(state)

    override suspend fun compositionPolygons(data: List<PolygonState>) = polygonController.add(data)

    override suspend fun updatePolygon(state: PolygonState) = polygonController.update(state)

    override suspend fun compositionCircles(data: List<CircleState>) = circleController.add(data)

    override suspend fun updateCircle(state: CircleState) = circleController.update(state)

    override suspend fun compositionGroundImages(data: List<GroundImageState>) = groundImageController.add(data)

    override suspend fun updateGroundImage(state: GroundImageState) = groundImageController.update(state)

    override suspend fun compositionRasterLayers(data: List<RasterLayerState>) = rasterLayerController.add(data)

    override suspend fun updateRasterLayer(state: RasterLayerState) = rasterLayerController.update(state)

    @Deprecated("Use CircleState.onClick instead.")
    override fun setOnCircleClickListener(listener: OnCircleEventHandler?) {
        this.circleController.clickListener = listener
    }

    @Deprecated("Use GroundImageState.onClick instead.")
    override fun setOnGroundImageClickListener(listener: OnGroundImageEventHandler?) {
        this.groundImageController.clickListener = listener
    }

    override fun moveCamera(position: MapCameraPosition) {
        val dstCameraPosition = toCameraWithView(position)

        coroutine.launch {
            withContext(Dispatchers.Main) {
                if (!holder.mapView.isAttachedToWindow) return@withContext
                holder.map.setViewpointCamera(camera = dstCameraPosition)
            }
        }
    }

    override fun animateCamera(
        position: MapCameraPosition,
        duration: Long,
    ) {
        val dstCameraPosition = toCameraWithView(position)

        coroutine.launch {
            invokeCameraMoveStartCallback()
            withContext(Dispatchers.Main) {
                if (!holder.mapView.isAttachedToWindow) return@withContext
//                if (cameraRequestGeneration.get() == request) {
                holder.map.setViewpointCameraAnimated(
                    camera = dstCameraPosition,
                    duration = duration.toFloat() / 1000.0f,
                )
//                }
            }
            scheduleCameraMoveEndCallback()
        }
    }

    private fun toCameraWithView(position: MapCameraPosition): Camera {
        val targetPoint =
            GeoPoint
                .from(position.position)
                .toPoint()
        val conv = ZoomAltitudeConverter()
        val (viewportWidthDp, viewportHeightDp) = currentViewportSizeInDp()
        val distance =
            conv.zoomLevelToDistance(
                zoomLevel = position.zoom,
                latitude = position.position.latitude,
                viewportWidthPx = viewportWidthDp,
                viewportHeightPx = viewportHeightDp,
            )
        return calculateCameraForOrbitParameters(
            targetPoint = targetPoint,
            distance = distance,
            cameraHeadingOffset = position.bearing + 180,
            cameraPitchOffset = position.tilt,
        )
    }

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

    @Deprecated("Use PolylineState.onClick instead.")
    override fun setOnPolylineClickListener(listener: OnPolylineEventHandler?) {
        this.polylineController.clickListener = listener
    }

    @Deprecated("Use PolygonState.onClick instead.")
    override fun setOnPolygonClickListener(listener: OnPolygonEventHandler?) {
        this.polygonController.clickListener = listener
    }

    private var mapDesignType: ArcGISDesignTypeInterface = ArcGISDesign.Streets
    private var mapDesignTypeChangeListener: ArcGISDesignTypeChangeHandler? = null

    override fun setMapDesignType(value: ArcGISDesignTypeInterface) {
        holder.map.scene?.let { scene ->
            val baseMapStyle = ArcGISDesign.toBasemapStyle(value)
            val baseMap = Basemap(baseMapStyle)
            coroutine.launch {
                scene.setBasemap(baseMap)
                // Basemap changes can reset the viewpoint; mark the current request as pending so that
                // the next viewpointChanged can restore the last requested camera if needed.
//                pendingCameraRestoreRequest = cameraRequestGeneration.get()
            }
        }
    }

    override fun setMapDesignTypeChangeListener(listener: ArcGISDesignTypeChangeHandler) {
        mapDesignTypeChangeListener = listener
        listener(mapDesignType)
    }

    // Trigger an initial camera update after the view and scene are ready
    fun sendInitialCameraUpdate() {
        coroutine.launch {
            val mapWidth = holder.map.width
            val mapHeight = holder.map.height
            if (mapWidth <= 0 || mapHeight <= 0) return@launch
            getMapCameraPosition()?.let { mapCameraPosition ->
                notifyMapCameraPosition(mapCameraPosition)
            }
        }
    }

    internal fun registerMarkerEventController(controller: ArcGISMarkerEventControllerInterface) {
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
        strategy: MarkerRenderingStrategyInterface<ArcGISActualMarker>,
    ): MarkerOverlayRendererInterface<ArcGISActualMarker> {
        val markerLayer =
            com.arcgismaps.mapping.view
                .GraphicsOverlay()
        registerMarkerOverlayLayer(markerLayer)
        return ArcGISMarkerRenderer(
            markerLayer = markerLayer,
            holder = holder,
        )
    }

    fun createMarkerEventController(
        controller: StrategyMarkerController<ArcGISActualMarker>,
        renderer: MarkerOverlayRendererInterface<ArcGISActualMarker>,
    ): MarkerEventControllerInterface<ArcGISActualMarker> = StrategyArcGISMarkerEventController(controller)

    fun registerMarkerEventController(controller: MarkerEventControllerInterface<ArcGISActualMarker>) {
        val typed = controller as? ArcGISMarkerEventControllerInterface ?: return
        registerMarkerEventController(typed)
    }

    internal fun registerMarkerOverlayLayer(layer: com.arcgismaps.mapping.view.GraphicsOverlay) {
        if (holder.map.graphicsOverlays.contains(layer)) return
        holder.map.graphicsOverlays.add(layer)
    }
}
