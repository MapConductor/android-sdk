package com.mapconductor.maplibre

import androidx.compose.ui.geometry.Offset
import com.mapconductor.core.circle.CircleEvent
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.circle.OnCircleEventHandler
import com.mapconductor.core.controller.BaseMapViewController
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.groundimage.GroundImageEvent
import com.mapconductor.core.groundimage.GroundImageState
import com.mapconductor.core.groundimage.OnGroundImageEventHandler
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapCameraPositionInterface
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
import com.mapconductor.maplibre.circle.MapLibreCircleController
import com.mapconductor.maplibre.groundimage.MapLibreGroundImageController
import com.mapconductor.maplibre.marker.DefaultMapLibreMarkerEventController
import com.mapconductor.maplibre.marker.MapLibreMarkerController
import com.mapconductor.maplibre.marker.MapLibreMarkerEventControllerInterface
import com.mapconductor.maplibre.marker.MapLibreMarkerOverlayRenderer
import com.mapconductor.maplibre.marker.MarkerDragLayer
import com.mapconductor.maplibre.marker.MarkerLayer
import com.mapconductor.maplibre.marker.StrategyMapLibreMarkerEventController
import com.mapconductor.maplibre.polygon.MapLibrePolygonConductor
import com.mapconductor.maplibre.polyline.MapLibrePolylineController
import com.mapconductor.maplibre.raster.MapLibreRasterLayerController
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.gestures.MoveGestureDetector
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.Layer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import java.util.UUID
import android.graphics.PointF
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

typealias MapLibreDesignTypeChangeHandler = (MapLibreMapDesignTypeInterface) -> Unit

class MapLibreViewController(
    override val holder: MapLibreMapViewHolderInterface,
    private val markerController: MapLibreMarkerController,
    private val polylineController: MapLibrePolylineController,
    private val polygonController: MapLibrePolygonConductor,
    private val groundImageController: MapLibreGroundImageController,
    private val circleController: MapLibreCircleController,
    private val rasterLayerController: MapLibreRasterLayerController,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
    val backCoroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : BaseMapViewController(),
    MapLibreViewControllerInterface,
    MapLibreMap.OnMapClickListener,
    MapLibreMap.OnMapLongClickListener,
    MapLibreMap.OnMoveListener,
    MapLibreMap.OnCameraMoveListener,
    MapLibreMap.OnCameraIdleListener {
    // Keep reference to the style instance to avoid getting a new one
    private var styleInstance: Style? = null
    private var wasScrollEnabledBeforeDrag: Boolean? = null
    private var dragTouchInterceptor: View.OnTouchListener? = null
    private val polygonZLayers: MutableSet<Int> = mutableSetOf()
    private val markerEventControllers = mutableListOf<MapLibreMarkerEventControllerInterface>()
    private var activeDragController: MapLibreMarkerEventControllerInterface? = null
    private var markerClickListener: OnMarkerEventHandler? = null
    private var markerDragStartListener: OnMarkerEventHandler? = null
    private var markerDragListener: OnMarkerEventHandler? = null
    private var markerDragEndListener: OnMarkerEventHandler? = null
    private var markerAnimateStartListener: OnMarkerEventHandler? = null
    private var markerAnimateEndListener: OnMarkerEventHandler? = null

    private fun ensureGeoJsonSource(
        style: Style,
        sourceId: String,
    ) {
        if (style.getSource(sourceId) != null) return
        try {
            style.addSource(GeoJsonSource(sourceId))
        } catch (e: Exception) {
            Log.w("MapLibre", "Failed to add source: $sourceId (${e.message})")
        }
    }

    private fun addLayerSafely(
        style: Style,
        layer: Layer,
        layerId: String,
    ) {
        if (style.getLayer(layerId) != null) return
        try {
            style.addLayer(layer)
        } catch (e: Exception) {
            if (style.getLayer(layerId) == null) {
                Log.w("MapLibre", "Failed to add layer: $layerId (${e.message})")
            }
        }
    }

    private fun addLayerAboveSafely(
        style: Style,
        layer: Layer,
        layerId: String,
        aboveId: String,
    ) {
        if (style.getLayer(layerId) != null) return
        try {
            style.addLayerAbove(layer, aboveId)
        } catch (e: Exception) {
            if (style.getLayer(layerId) != null) return
            try {
                style.addLayer(layer)
            } catch (e2: Exception) {
                if (style.getLayer(layerId) == null) {
                    Log.w("MapLibre", "Failed to add layer: $layerId (${e2.message})")
                }
            }
        }
    }

    private fun setupStyle(style: Style) {
        // Store the style instance for future use
        styleInstance = style

        // Log existing layers
        val topLayerId = style.layers.lastOrNull()?.id

        // Ensure default icon image exists on this style
        markerController.renderer.ensureDefaultIcon(style)

        // Polygon sources only (layers will be added per zIndex)
        ensureGeoJsonSource(style, polygonController.polylineOverlay.layer.sourceId)
        ensureGeoJsonSource(style, polygonController.polygonOverlay.layer.sourceId)

        // Circle cts as anchor above polygons
        ensureGeoJsonSource(style, circleController.renderer.layer.sourceId)
        addLayerSafely(
            style = style,
            layer = circleController.renderer.layer.layer,
            layerId = circleController.renderer.layer.layerId,
        )

        // Polyline (general) acts as anchor above circles
        ensureGeoJsonSource(style, polylineController.renderer.layer.sourceId)
        addLayerSafely(
            style = style,
            layer = polylineController.renderer.layer.layer,
            layerId = polylineController.renderer.layer.layerId,
        )

        // Add z-indexed polygon layers below general polylines
        ensurePolygonZLayers(style)

        // Marker - add source and layer at the top
        ensureGeoJsonSource(style, markerController.renderer.markerLayer.sourceId)
        addLayerAboveSafely(
            style = style,
            layer = markerController.renderer.markerLayer.layer,
            layerId = markerController.renderer.markerLayer.layerId,
            aboveId = polylineController.renderer.layer.layerId,
        )
        markerController.renderer.redraw()

        // Drag layer above marker layer
        ensureGeoJsonSource(style, markerController.renderer.dragLayer.sourceId)
        addLayerAboveSafely(
            style = style,
            layer = markerController.renderer.dragLayer.layer,
            layerId = markerController.renderer.dragLayer.layerId,
            aboveId = markerController.renderer.markerLayer.layerId,
        )
        markerController.renderer.redraw()

        markerEventControllers
            .map { it.renderer }
            .filter { it != markerController.renderer }
            .forEach { renderer ->
                renderer.ensureDefaultIcon(style)
                ensureGeoJsonSource(style, renderer.markerLayer.sourceId)
                addLayerAboveSafely(
                    style = style,
                    layer = renderer.markerLayer.layer,
                    layerId = renderer.markerLayer.layerId,
                    aboveId = polylineController.renderer.layer.layerId,
                )
                ensureGeoJsonSource(style, renderer.dragLayer.sourceId)
                addLayerAboveSafely(
                    style = style,
                    layer = renderer.dragLayer.layer,
                    layerId = renderer.dragLayer.layerId,
                    aboveId = renderer.markerLayer.layerId,
                )
                renderer.redraw()
                renderer.drawDragLayer()
            }

        // Force redraw after adding layers
        markerController.renderer.redraw()
        polylineController.renderer.redraw()
//        polygonController.polygonOverlay.onPostProcess()
        coroutine.launch {
            groundImageController.reapplyStyle()
            rasterLayerController.reapplyStyle()
        }
    }

    init {
        // Style should already be loaded by holderProvider
        val style = holder.map.style
        if (style != null) {
            setupStyle(style)
            // Trigger initial camera update after style is ready
            sendInitialCameraUpdate()
        }

        setupListeners()
        registerController(markerController)
        registerController(polylineController)
        registerController(polygonController)
        registerController(groundImageController)
        registerController(circleController)
        registerController(rasterLayerController)
        registerMarkerEventController(DefaultMapLibreMarkerEventController(markerController))

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
        holder.map.addOnCameraMoveListener(this)
        holder.map.addOnCameraIdleListener(this)

        holder.map.removeOnMapClickListener(this)
        holder.map.addOnMapClickListener(this)

        holder.map.removeOnMapLongClickListener(this)
        holder.map.addOnMapLongClickListener(this)

        holder.map.removeOnMoveListener(this)
        holder.map.addOnMoveListener(this)
    }

    override suspend fun clearOverlays() {
        markerController.clear()
        polylineController.clear()
        polygonController.clear()
        groundImageController.clear()
        circleController.clear()
        rasterLayerController.clear()
    }

    override fun moveCamera(position: MapCameraPosition) {
        coroutine.launch {
            val cameraPos = position.toCameraPosition()
            val cameraUpdate =
                CameraUpdateFactory
                    .newCameraPosition(cameraPos)
            holder.map.moveCamera(cameraUpdate)
            cameraMoveEndCallback?.invoke(position)
        }
    }

    override fun animateCamera(
        position: MapCameraPosition,
        duration: Long,
    ) {
        coroutine.launch {
            val cameraPos = position.toCameraPosition()
            val cameraUpdate =
                CameraUpdateFactory
                    .newCameraPosition(cameraPos)
            holder.map.animateCamera(cameraUpdate, duration.toInt())
            cameraMoveEndCallback?.invoke(position)
        }
    }

    private var mapDesignType: MapLibreMapDesignTypeInterface = MapLibreDesign.DemoTiles

    private var mapDesignTypeChangeListener: MapLibreDesignTypeChangeHandler? = null

    override fun setMapDesignType(value: MapLibreMapDesignTypeInterface) {
        coroutine.launch {
            holder.map.setStyle(value.styleJsonURL) { newStyle ->
                Log.d("MapLibre", "Style changed to ${value.styleJsonURL}")
                setupStyle(newStyle)
            }
        }
    }

    // Provide access to the style instance
    fun getStyleInstance(): Style? = styleInstance

    override fun setMapDesignTypeChangeListener(listener: MapLibreDesignTypeChangeHandler) {
        mapDesignTypeChangeListener = listener
        // Don't call listener immediately - it may trigger style reload
        // listener(mapDesignType)
    }

    override suspend fun compositionMarkers(data: List<MarkerState>) = markerController.add(data)

    override suspend fun updateMarker(state: MarkerState) = markerController.update(state)

    override suspend fun compositionGroundImages(data: List<GroundImageState>) = groundImageController.add(data)

    override suspend fun updateGroundImage(state: GroundImageState) = groundImageController.update(state)

    override suspend fun compositionPolylines(data: List<PolylineState>) = polylineController.add(data)

    override suspend fun updatePolyline(state: PolylineState) = polylineController.update(state)

    override suspend fun compositionPolygons(data: List<PolygonState>) {
        polygonController.add(data)
        getStyleInstance()?.let { ensurePolygonZLayers(it) }
    }

    override suspend fun updatePolygon(state: PolygonState) {
        polygonController.update(state)
        getStyleInstance()?.let { ensurePolygonZLayers(it) }
    }

    override suspend fun compositionCircles(data: List<CircleState>) = circleController.add(data)

    override suspend fun updateCircle(state: CircleState) = circleController.update(state)

    override suspend fun compositionRasterLayers(data: List<RasterLayerState>) = rasterLayerController.add(data)

    override suspend fun updateRasterLayer(state: RasterLayerState) = rasterLayerController.update(state)

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

    @Deprecated("Use PolylineState.onClick instead.")
    override fun setOnPolylineClickListener(listener: OnPolylineEventHandler?) {
        polylineController.clickListener = listener
    }

    @Deprecated("Use PolygonState.onClick instead.")
    override fun setOnPolygonClickListener(listener: OnPolygonEventHandler?) {
        polygonController.clickListener = listener
    }

    @Deprecated("Use CircleState.onClick instead.")
    override fun setOnCircleClickListener(listener: OnCircleEventHandler?) {
        this.circleController.clickListener = listener
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

    override fun hasPolygon(state: PolygonState): Boolean =
        this.polygonController.polygonOverlay.polygonManager
            .hasEntity(state.id)

    override fun hasCircle(state: CircleState): Boolean = this.circleController.circleManager.hasEntity(state.id)

    override fun hasGroundImage(state: GroundImageState): Boolean =
        this.groundImageController.groundImageManager.hasEntity(state.id)

    override fun hasRasterLayer(state: RasterLayerState): Boolean =
        this.rasterLayerController.rasterLayerManager.hasEntity(state.id)

    @Deprecated("Use GroundImageState.onClick instead.")
    override fun setOnGroundImageClickListener(listener: OnGroundImageEventHandler?) {
        this.groundImageController.clickListener = listener
    }

    override fun onMapClick(point: LatLng): Boolean {
        val touchPosition = point.toGeoPoint()

        markerEventControllers.forEach { controller ->
            controller.find(touchPosition)?.let { entity ->
                controller.dispatchClick(entity.state)
                return true
            }
        }

        circleController.find(touchPosition)?.let { entity ->
            val event = CircleEvent(state = entity.state, clicked = touchPosition)
            circleController.dispatchClick(event)
            return true
        }

        groundImageController.find(touchPosition)?.let { entity ->
            val event = GroundImageEvent(state = entity.state, clicked = touchPosition)
            groundImageController.dispatchClick(event)
            return true
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
            return true
        }

        polygonController.find(touchPosition)?.let { polygonEntity ->
            val event =
                PolygonEvent(
                    state = polygonEntity.state,
                    clicked = touchPosition,
                )
            polygonController.dispatchClick(event)
            return true
        }

        mapClickCallback?.invoke(touchPosition)
        return true
    }

    override fun onMapLongClick(point: LatLng): Boolean {
        val touchPosition = point.toGeoPoint()
        markerEventControllers.forEach { controller ->
            controller.find(touchPosition)?.let { entity ->
                if (entity.state.draggable) {
                    // Disable map scroll while dragging a marker
                    try {
                        val ui = holder.map.uiSettings
                        wasScrollEnabledBeforeDrag = ui.isScrollGesturesEnabled
                        ui.isScrollGesturesEnabled = false
                    } catch (e: Exception) {
                        Log.w("MapLibre", "Failed to disable scroll gestures: ${e.message}")
                    }
                    activeDragController = controller
                    controller.setSelectedMarker(entity)
                    controller.dispatchDragStart(entity.state)
                    // Intercept touch to move marker without moving the map
                    installDragTouchInterceptor()
                    return true
                }
            }
        }

        mapLongClickCallback?.invoke(touchPosition)
        return true
    }

    override fun onMoveBegin(detector: MoveGestureDetector) {
        coroutine.launch {
            getMapCameraPosition(holder.map.cameraPosition.toMapCameraPosition())?.let { mapCameraPosition ->
                cameraMoveStartCallback?.invoke(mapCameraPosition)
            }
        }
    }

    override fun onMove(detector: MoveGestureDetector) {
        val controller = activeDragController ?: return
        controller.getSelectedMarker()?.let { entity ->

            val screenCoordinate =
                Offset(
                    detector.focalPoint.x,
                    detector.focalPoint.y,
                )

            holder.fromScreenOffsetSync(screenCoordinate)?.let {
                entity.state.position = it
                controller.renderer.dragLayer.updatePosition(it)
                controller.renderer.drawDragLayer()
            }

            controller.dispatchDrag(entity.state)
        }
    }

    override fun onMoveEnd(detector: MoveGestureDetector) {
        val controller = activeDragController ?: return
        controller.getSelectedMarker()?.let { entity ->
            val screenCoordinate =
                PointF(
                    detector.focalPoint.x,
                    detector.focalPoint.y,
                )
            val point = holder.map.projection.fromScreenLocation(screenCoordinate)
            controller.renderer.dragLayer.updatePosition(point.toGeoPoint())
            controller.setSelectedMarker(null)
            controller.dispatchDragEnd(entity.state)
            // Re-enable map scroll after dragging finishes
            try {
                val ui = holder.map.uiSettings
                ui.isScrollGesturesEnabled = wasScrollEnabledBeforeDrag == true
            } catch (e: Exception) {
                Log.w("MapLibre", "Failed to re-enable scroll gestures: ${e.message}")
            } finally {
                wasScrollEnabledBeforeDrag = null
            }
            removeDragTouchInterceptor()
            activeDragController = null
        }
    }

    private fun installDragTouchInterceptor() {
        if (dragTouchInterceptor != null) return
        val view = holder.mapView
        dragTouchInterceptor =
            View.OnTouchListener { _, event ->
                val controller = activeDragController ?: return@OnTouchListener false
                val selected = controller.getSelectedMarker() ?: return@OnTouchListener false
                when (event.actionMasked) {
                    MotionEvent.ACTION_MOVE -> {
                        val pos = holder.fromScreenOffsetSync(Offset(event.x, event.y))
                        if (pos != null) {
                            selected.state.position = pos
                            controller.renderer.dragLayer.updatePosition(pos)
                            controller.renderer.drawDragLayer()
                            controller.dispatchDrag(selected.state)
                        }
                        true // consume to prevent map panning
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        val point = holder.map.projection.fromScreenLocation(PointF(event.x, event.y))
                        controller.renderer.dragLayer.updatePosition(point.toGeoPoint())
                        controller.setSelectedMarker(null)
                        controller.dispatchDragEnd(selected.state)
                        try {
                            val ui = holder.map.uiSettings
                            ui.isScrollGesturesEnabled = wasScrollEnabledBeforeDrag == true
                        } catch (e: Exception) {
                            Log.w("MapLibre", "Failed to re-enable scroll gestures: ${e.message}")
                        } finally {
                            wasScrollEnabledBeforeDrag = null
                        }
                        removeDragTouchInterceptor()
                        activeDragController = null
                        true
                    }
                    else -> false
                }
            }
        view.setOnTouchListener(dragTouchInterceptor)
    }

    private fun removeDragTouchInterceptor() {
        val view = holder.mapView
        view.setOnTouchListener(null)
        dragTouchInterceptor = null
    }

    override fun onCameraMove() {
        coroutine.launch {
            getMapCameraPosition(holder.map.cameraPosition.toMapCameraPosition())?.let { mapCameraPosition ->
                backCoroutine.launch {
                    notifyMapCameraPosition(mapCameraPosition)
                }
                cameraMoveCallback?.invoke(mapCameraPosition)
            }
        }
    }

    override fun onCameraIdle() {
        coroutine.launch {
            getMapCameraPosition(holder.map.cameraPosition.toMapCameraPosition())?.let { mapCameraPosition ->
                backCoroutine.launch {
                    notifyMapCameraPosition(mapCameraPosition)
                }
                cameraMoveEndCallback?.invoke(mapCameraPosition)
            }
        }
    }

    private fun getMapCameraPosition(camera: MapCameraPositionInterface): MapCameraPosition? {
        val mapWidth = holder.mapView.width.toFloat()
        val mapHeight = holder.mapView.height.toFloat()
        val nearLeft =
            holder.fromScreenOffsetSync(
                Offset(0.0f, mapHeight),
            ) ?: return null
        val nearRight =
            holder.fromScreenOffsetSync(
                Offset(mapWidth, mapHeight),
            ) ?: return null
        val farLeft =
            holder.fromScreenOffsetSync(
                Offset(0.0f, 0.0f),
            ) ?: return null
        val farRight =
            holder.fromScreenOffsetSync(
                Offset(mapWidth, 0.0f),
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
        val mapCameraPosition =
            MapCameraPosition.Companion.from(camera).copy(
                visibleRegion = visibleRegion,
            )
        return mapCameraPosition
    }

    private fun ensurePolygonZLayers(style: Style) {
        val fillSourceId = polygonController.polygonOverlay.layer.sourceId
        val outlineSourceId = polygonController.polylineOverlay.layer.sourceId
        val anchorId = polylineController.renderer.layer.layerId

        val zSet =
            polygonController.polygonOverlay.polygonManager
                .allEntities()
                .map { it.state.zIndex }
                .toSet()

        // Remove stale z-indexed layers we previously created
        val toRemove = polygonZLayers.subtract(zSet)
        toRemove.forEach { z ->
            val fillId = "polygon-fill-layer-$z"
            val outlineId = "polygon-outline-layer-$z"
            try {
                style.removeLayer(outlineId)
            } catch (_: Exception) {
            }
            try {
                style.removeLayer(fillId)
            } catch (_: Exception) {
            }
        }

        val zList = zSet.toList().sorted()
        zList.forEach { z ->
            val fillId = "polygon-fill-layer-$z"
            val outlineId = "polygon-outline-layer-$z"

            if (style.getLayer(fillId) == null) {
                val fill =
                    FillLayer(fillId, fillSourceId).apply {
                        setFilter(
                            Expression.eq(
                                Expression
                                    .get("zIndex"),
                                Expression
                                    .literal(z),
                            ),
                        )
                        setProperties(
                            PropertyFactory.fillColor(
                                Expression
                                    .get("fillColor"),
                            ),
                        )
                    }
                try {
                    style.addLayerBelow(fill, anchorId)
                } catch (_: Exception) {
                    style.addLayer(fill)
                }
            }

            if (style.getLayer(outlineId) == null) {
                val outline =
                    LineLayer(outlineId, outlineSourceId).apply {
                        setFilter(
                            Expression.eq(
                                Expression
                                    .get("zIndex"),
                                Expression
                                    .literal(z),
                            ),
                        )
                        setProperties(
                            PropertyFactory
                                .lineJoin(Property.LINE_JOIN_ROUND),
                            PropertyFactory
                                .lineCap(Property.LINE_CAP_ROUND),
                            PropertyFactory.lineColor(
                                Expression
                                    .get("strokeColor"),
                            ),
                            PropertyFactory.lineWidth(
                                Expression
                                    .get("strokeWidth"),
                            ),
                        )
                    }
                try {
                    style.addLayerAbove(outline, fillId)
                } catch (_: Exception) {
                    style.addLayer(outline)
                }
            }
        }
        polygonZLayers.clear()
        polygonZLayers.addAll(zSet)
    }

    // Trigger an initial camera update after the view and style are ready
    fun sendInitialCameraUpdate() {
        coroutine.launch {
            val mapWidth = holder.mapView.width.toFloat()
            val mapHeight = holder.mapView.height.toFloat()
            if (mapWidth <= 0 || mapHeight <= 0) return@launch

            val camera = holder.map.cameraPosition.toMapCameraPosition()
            getMapCameraPosition(camera)?.let { mapCameraPosition ->
                backCoroutine.launch { notifyMapCameraPosition(mapCameraPosition) }
            }
        }
    }

    internal fun registerMarkerEventController(controller: MapLibreMarkerEventControllerInterface) {
        if (markerEventControllers.contains(controller)) return
        markerEventControllers.add(controller)
        controller.setClickListener(markerClickListener)
        controller.setDragStartListener(markerDragStartListener)
        controller.setDragListener(markerDragListener)
        controller.setDragEndListener(markerDragEndListener)
        controller.setAnimateStartListener(markerAnimateStartListener)
        controller.setAnimateEndListener(markerAnimateEndListener)

        styleInstance?.let { style ->
            controller.renderer.ensureDefaultIcon(style)
            ensureGeoJsonSource(style, controller.renderer.markerLayer.sourceId)
            addLayerAboveSafely(
                style = style,
                layer = controller.renderer.markerLayer.layer,
                layerId = controller.renderer.markerLayer.layerId,
                aboveId = polylineController.renderer.layer.layerId,
            )
            ensureGeoJsonSource(style, controller.renderer.dragLayer.sourceId)
            addLayerAboveSafely(
                style = style,
                layer = controller.renderer.dragLayer.layer,
                layerId = controller.renderer.dragLayer.layerId,
                aboveId = controller.renderer.markerLayer.layerId,
            )
            controller.renderer.redraw()
            controller.renderer.drawDragLayer()
        }
    }

    fun createMarkerRenderer(
        strategy: MarkerRenderingStrategyInterface<MapLibreActualMarker>,
    ): MarkerOverlayRendererInterface<MapLibreActualMarker> {
        val groupId = UUID.randomUUID().toString()
        val markerLayer =
            MarkerLayer(
                sourceId = "markers-source-$groupId",
                layerId = "markers-layer-$groupId",
            )
        val dragLayer =
            MarkerDragLayer(
                sourceId = "marker-drag-source-$groupId",
                layerId = "marker-drag-layer-$groupId",
            )
        return MapLibreMarkerOverlayRenderer(
            holder = holder,
            markerManager = strategy.markerManager,
            markerLayer = markerLayer,
            dragLayer = dragLayer,
        )
    }

    fun createMarkerEventController(
        controller: StrategyMarkerController<MapLibreActualMarker>,
        renderer: MarkerOverlayRendererInterface<MapLibreActualMarker>,
    ): MarkerEventControllerInterface<MapLibreActualMarker> =
        StrategyMapLibreMarkerEventController(
            controller = controller,
            renderer = renderer as MapLibreMarkerOverlayRenderer,
        )

    fun registerMarkerEventController(controller: MarkerEventControllerInterface<MapLibreActualMarker>) {
        val typed = controller as? MapLibreMarkerEventControllerInterface ?: return
        registerMarkerEventController(typed)
    }
}
