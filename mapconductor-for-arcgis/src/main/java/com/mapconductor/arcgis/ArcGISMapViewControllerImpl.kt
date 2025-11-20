package com.mapconductor.arcgis

import androidx.compose.ui.geometry.Offset
import com.arcgismaps.mapping.Basemap
import com.arcgismaps.mapping.view.LongPressEvent
import com.arcgismaps.mapping.view.PanChangeEvent
import com.arcgismaps.mapping.view.SingleTapConfirmedEvent
import com.arcgismaps.mapping.view.UpEvent
import com.arcgismaps.mapping.view.extensions.motionEvent
import com.mapconductor.arcgis.circle.ArcGISCircleOverlayController
import com.mapconductor.arcgis.marker.ArcGISMarkerController
import com.mapconductor.arcgis.marker.SelectedMarker
import com.mapconductor.arcgis.polygon.ArcGISPolygonOverlayController
import com.mapconductor.arcgis.polyline.ArcGISPolylineOverlayController
import com.mapconductor.arcgis.zoom.ZoomAltitudeConverter
import com.mapconductor.core.circle.CircleEvent
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.circle.OnCircleEventHandler
import com.mapconductor.core.controller.BaseMapViewController
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.map.VisibleRegion
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.polygon.OnPolygonEventHandler
import com.mapconductor.core.polygon.PolygonEvent
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.core.polyline.OnPolylineEventHandler
import com.mapconductor.core.polyline.PolylineEvent
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.settings.Settings
import android.view.MotionEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ArcGISMapViewControllerImpl(
    override val holder: ArcGISMapViewHolder,
    private val markerController: ArcGISMarkerController,
    private val polylineController: ArcGISPolylineOverlayController,
    private val polygonController: ArcGISPolygonOverlayController,
    private val circleController: ArcGISCircleOverlayController,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : BaseMapViewController(),
    ArcGISMapViewController {
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

    private suspend fun invokeCameraMoveStartCallback() {
        cameraMoveCallback?.let {
            getMapCameraPosition()?.let { mapCameraPosition ->
                cameraMoveStartCallback?.invoke(mapCameraPosition)
            }
        }
    }

    private suspend fun invokeCameraMoveCallback() {
        cameraMoveCallback?.let {
            getMapCameraPosition()?.let { mapCameraPosition ->
                cameraMoveCallback?.invoke(mapCameraPosition)
            }
        }
    }

    private suspend fun invokeCameraMoveEndCallback() {
        cameraMoveEndCallback?.let {
            getMapCameraPosition()?.let { mapCameraPosition ->
                cameraMoveEndCallback?.invoke(mapCameraPosition)
            }
        }
    }

    private suspend fun onViewpointChange() {
        mapLoadedCallback?.invoke()
        mapLoadedCallback = null

        getMapCameraPosition()?.let { mapCameraPosition ->
            notifyMapCameraPosition(mapCameraPosition)
        }
    }

    private suspend fun getMapCameraPosition(): MapCameraPositionImpl? {
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
        val bearing = (360 - arcCamera.heading) % 360

        // Use calibrated constant instead of dynamic calculation
        val conv = ZoomAltitudeConverter()
        val zoom = conv.altitudeToZoomLevel(alt, lat, tilt)

        val camera =
            MapCameraPositionImpl(
                position =
                    com.mapconductor.core.features.GeoPointImpl
                        .fromLongLat(lon, lat, alt),
                zoom = zoom,
                bearing = bearing,
                tilt = tilt,
                paddings = com.mapconductor.core.map.MapPaddingsImpl.Zeros,
                visibleRegion = visibleRegion,
            )
        return camera
    }

    private suspend fun onMapPan(event: PanChangeEvent) {
        markerController.selectedMarker?.also {
            val screenPoint = event.screenCoordinate
            val point = holder.map.screenToLocation(screenPoint).getOrNull() ?: return
            val position = point.toGeoPoint()
            it.graphic.geometry = point
            it.state.position = position
            markerController.dragListener?.invoke(it.state)
        }
        invokeCameraMoveCallback()
    }

    private suspend fun onMapUp(event: UpEvent) {
        markerController.selectedMarker?.also {
            val screenPoint = event.screenCoordinate
            val point = holder.map.screenToLocation(screenPoint).getOrNull() ?: return
            val position = point.toGeoPoint()
            it.graphic.geometry = point
            it.state.position = position

            markerController.selectedMarker = null
            markerController.dragEndListener?.invoke(it.state)

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
                        markerController.selectedMarker =
                            SelectedMarker(
                                state = entity.state,
                                graphic = graphic,
                            )
                        // 3Dナビゲーションを無効化
                        with(holder.map) {
                            interactionOptions.isPanEnabled = false
                            interactionOptions.isRotateEnabled = false
                            interactionOptions.isZoomEnabled = false
                        }
                        markerController.dragStartListener?.invoke(entity.state)
                        return
                    }
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

        markerController.find(touchPosition)?.let { markerEntity ->
            markerController.clickListener?.invoke(markerEntity.state)
            return
        }

        circleController.find(touchPosition)?.let { circleEntity ->
            val event =
                CircleEvent(
                    state = circleEntity.state,
                    clicked = touchPosition,
                )
            circleController.clickListener?.invoke(event)
            return
        }

        polylineController.findWithClosestPoint(touchPosition)?.let { hitResult ->
            val event =
                PolylineEvent(
                    state = hitResult.entity.state,
                    clicked = hitResult.closestPoint,
                )
            polylineController.clickListener?.invoke(event)
            return
        }

        polygonController.find(touchPosition)?.let { polygonEntity ->
            val event =
                PolygonEvent(
                    state = polygonEntity.state,
                    clicked = touchPosition,
                )
            polygonController.clickListener?.invoke(event)
            return
        }

        holder.map.screenToLocation(screenPoint).getOrNull()?.also {
            mapClickCallback?.invoke(it.toGeoPoint())
        }
    }

    override suspend fun clearOverlays() {
        markerController.clear()
        polylineController.clear()
        polygonController.clear()
    }

    override suspend fun compositionMarkers(data: List<MarkerState>) = markerController.add(data)

    override suspend fun updateMarker(state: MarkerState) = markerController.update(state)

    override suspend fun compositionPolylines(data: List<PolylineState>) = polylineController.add(data)

    override suspend fun updatePolyline(state: PolylineState) = polylineController.update(state)

    override suspend fun compositionPolygons(data: List<PolygonState>) = polygonController.add(data)

    override suspend fun updatePolygon(state: PolygonState) = polygonController.update(state)

    override suspend fun compositionCircles(data: List<CircleState>) = circleController.add(data)

    override suspend fun updateCircle(state: CircleState) = circleController.update(state)

    override fun setOnCircleClickListener(listener: OnCircleEventHandler?) {
        this.circleController.clickListener = listener
    }

    override fun moveCamera(position: MapCameraPositionImpl) {
        val dstCameraPosition = toCameraWithView(position)

        holder.map.setViewpointCamera(
            camera = dstCameraPosition,
        )
        coroutine.launch {
            invokeCameraMoveEndCallback()
        }
    }

    override fun animateCamera(
        position: MapCameraPositionImpl,
        duration: Long,
    ) {
        val dstCameraPosition = toCameraWithView(position)

        coroutine.launch {
            invokeCameraMoveStartCallback()
            holder.map.setViewpointCameraAnimated(
                camera = dstCameraPosition,
                duration = duration.toFloat() / 1000.0f,
            )
            invokeCameraMoveEndCallback()
        }
    }

    private fun computeZoom0DistanceForCurrentView(): Double {
        val view = holder.map
        val w = view.width.coerceAtLeast(1)
        val h = view.height.coerceAtLeast(1)
        val aspect = w.toDouble() / h.toDouble()
        val fovHorizontalDegrees = 55.0 // default assumption
        val fovVerticalDegrees = ZoomAltitudeConverter.verticalFovFromHorizontal(fovHorizontalDegrees, aspect)
        return ZoomAltitudeConverter.computeZoom0DistanceForView(h, fovVerticalDegrees)
    }

    private fun toCameraWithView(position: MapCameraPositionImpl): com.arcgismaps.mapping.view.Camera {
        val targetPoint =
            com.mapconductor.core.features.GeoPointImpl
                .from(position.position)
                .toPoint()
        // Use calibrated constant instead of dynamic calculation
        val conv = ZoomAltitudeConverter()
        val distance = conv.zoomLevelToDistance(position.zoom, position.position.latitude)
        return calculateCameraForOrbitParameters(
            targetPoint = targetPoint,
            distance = distance,
            cameraHeadingOffset = 360 - (position.bearing + 180),
            cameraPitchOffset = position.tilt,
        )
    }

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

    override fun setOnPolylineClickListener(listener: OnPolylineEventHandler?) {
        this.polylineController.clickListener = listener
    }

    override fun setOnPolygonClickListener(listener: OnPolygonEventHandler?) {
        this.polygonController.clickListener = listener
    }

    private var mapDesignType: ArcGISDesignType = ArcGISDesign.Streets
    private var mapDesignTypeChangeListener: ArcGISDesignTypeChangeHandler? = null

    override fun setMapDesignType(value: ArcGISDesignType) {
        holder.map.scene?.let { scene ->
            val baseMapStyle = ArcGISDesign.toBasemapStyle(value)
            val baseMap = Basemap(baseMapStyle)
            coroutine.launch {
                scene.setBasemap(baseMap)
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
}
