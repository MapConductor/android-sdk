package com.mapconductor.maplibre

import androidx.compose.ui.geometry.Offset
import com.mapconductor.core.circle.CircleEvent
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.circle.OnCircleEventHandler
import com.mapconductor.core.controller.BaseMapViewController
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.map.MapCameraPosition
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
import com.mapconductor.maplibre.circle.MapLibreCircleController
import com.mapconductor.maplibre.marker.MapLibreMarkerController
import com.mapconductor.maplibre.polygon.MapLibrePolygonConductor
import com.mapconductor.maplibre.polyline.MapLibrePolylineController
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.gestures.MoveGestureDetector
import org.maplibre.android.maps.MapLibreMap
import android.graphics.PointF
import android.view.MotionEvent
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

typealias MapLibreDesignTypeChangeHandler = (MapLibreMapDesignType) -> Unit

class MapLibreViewControllerImpl(
    override val holder: MapLibreMapViewHolder,
    private val markerController: MapLibreMarkerController,
    private val polylineController: MapLibrePolylineController,
    private val polygonController: MapLibrePolygonConductor,
    private val circleController: MapLibreCircleController,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
    val backCoroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : BaseMapViewController(),
    MapLibreViewController,
    MapLibreMap.OnMapClickListener,
    MapLibreMap.OnMapLongClickListener,
    MapLibreMap.OnMoveListener,
    MapLibreMap.OnCameraMoveListener,
    MapLibreMap.OnCameraIdleListener {
    // Keep reference to the style instance to avoid getting a new one
    private var styleInstance: org.maplibre.android.maps.Style? = null
    private var wasScrollEnabledBeforeDrag: Boolean? = null
    private var dragTouchInterceptor: View.OnTouchListener? = null
    private val polygonZLayers: MutableSet<Int> = mutableSetOf()

    private fun setupStyle(style: org.maplibre.android.maps.Style) {
        // Store the style instance for future use
        styleInstance = style

        // Log existing layers
        val topLayerId = style.layers.lastOrNull()?.id

        // Ensure default icon image exists on this style
        markerController.renderer.ensureDefaultIcon(style)

        // Polygon sources only (layers will be added per zIndex)
        style.addSource(polygonController.polylineOverlay.layer.source)
        style.addSource(polygonController.polygonOverlay.layer.source)

        // Circle cts as anchor above polygons
        style.addSource(circleController.renderer.layer.source)
        style.addLayer(circleController.renderer.layer.layer)

        // Polyline (general) acts as anchor above circles
        style.addSource(polylineController.renderer.layer.source)
        style.addLayer(polylineController.renderer.layer.layer)

        // Add z-indexed polygon layers below general polylines
        ensurePolygonZLayers(style)

        // Marker - add source and layer at the top
        style.addSource(markerController.renderer.markerLayer.source)
        try {
            style.addLayerAbove(
                markerController.renderer.markerLayer.layer,
                polylineController.renderer.layer.layerId,
            )
        } catch (_: Exception) {
            // Fallback when anchor layer is not present yet
            style.addLayer(markerController.renderer.markerLayer.layer)
        }
        markerController.renderer.redraw()

        // Drag layer above marker layer
        style.addSource(markerController.renderer.dragLayer.source)
        try {
            style.addLayerAbove(
                markerController.renderer.dragLayer.layer,
                markerController.renderer.markerLayer.layerId,
            )
        } catch (_: Exception) {
            style.addLayer(markerController.renderer.dragLayer.layer)
        }
        markerController.renderer.redraw()

        // Force redraw after adding layers
        markerController.renderer.redraw()
        polylineController.renderer.redraw()
//        polygonController.polygonOverlay.onPostProcess()
    }

    init {
        // Style should already be loaded by holderProvider
        val style = holder.map.style
        if (style != null) {
            setupStyle(style)
        }

        setupListeners()
        registerController(markerController)
        registerController(polylineController)
        registerController(polygonController)
        registerController(circleController)
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
        circleController.clear()
    }

    override fun moveCamera(
        position: MapCameraPositionImpl,
        listener: MapViewState.MoveCameraCallback?,
    ) {
        coroutine.launch {
            val cameraUpdate =
                CameraUpdateFactory
                    .newCameraPosition(position.toCameraPosition())
            holder.map.moveCamera(cameraUpdate)
            listener?.onComplete()
        }
    }

    override fun animateCamera(
        position: MapCameraPositionImpl,
        duration: Long,
        listener: MapViewState.MoveCameraCallback?,
    ) {
        coroutine.launch {
            val cameraUpdate =
                CameraUpdateFactory
                    .newCameraPosition(position.toCameraPosition())
            holder.map.animateCamera(cameraUpdate, duration.toInt())
            listener?.onComplete()
        }
    }

    private var mapDesignType: MapLibreMapDesignType = MapLibreMapDesign.DemoTiles

    private var mapDesignTypeChangeListener: MapLibreDesignTypeChangeHandler? = null

    override fun setMapDesignType(value: MapLibreMapDesignType) {
        coroutine.launch {
            holder.map.setStyle(value.styleJsonURL) { newStyle ->
                android.util.Log.d("MapLibre", "Style changed to ${value.styleJsonURL}")
                setupStyle(newStyle)
            }
        }
    }

    // Provide access to the style instance
    fun getStyleInstance(): org.maplibre.android.maps.Style? = styleInstance

    override fun setMapDesignTypeChangeListener(listener: MapLibreDesignTypeChangeHandler) {
        mapDesignTypeChangeListener = listener
        // Don't call listener immediately - it may trigger style reload
        // listener(mapDesignType)
    }

    override suspend fun compositionMarkers(data: List<MarkerState>) = markerController.add(data)

    override suspend fun updateMarker(state: MarkerState) = markerController.update(state)

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

    override fun setOnMarkerDragStart(listener: OnMarkerEventHandler?) {
        markerController.dragStartListener = listener
    }

    override fun setOnMarkerDrag(listener: OnMarkerEventHandler?) {
        markerController.dragListener = listener
    }

    override fun setOnMarkerDragEnd(listener: OnMarkerEventHandler?) {
        markerController.dragEndListener = listener
    }

    override fun setOnPolylineClickListener(listener: OnPolylineEventHandler?) {
        polylineController.clickListener = listener
    }

    override fun setOnPolygonClickListener(listener: OnPolygonEventHandler?) {
        polygonController.clickListener = listener
    }

    override fun setOnCircleClickListener(listener: OnCircleEventHandler?) {
        this.circleController.clickListener = listener
    }

    override fun setOnMarkerAnimateStart(listener: OnMarkerEventHandler?) {
        markerController.renderer.animateStartListener = listener
    }

    override fun setOnMarkerAnimateEnd(listener: OnMarkerEventHandler?) {
        markerController.renderer.animateEndListener = listener
    }

    override fun setOnMarkerClickListener(listener: OnMarkerEventHandler?) {
        markerController.clickListener = listener
    }

    override fun hasMarker(state: MarkerState): Boolean = this.markerController.markerManager.hasEntity(state.id)

    override fun hasPolyline(state: PolylineState): Boolean =
        this.polylineController.polylineManager
            .hasEntity(state.id)

    override fun hasPolygon(state: PolygonState): Boolean =
        this.polygonController.polygonOverlay.polygonManager
            .hasEntity(state.id)

    override fun hasCircle(state: CircleState): Boolean = this.circleController.circleManager.hasEntity(state.id)

    override fun onMapClick(point: LatLng): Boolean {
        val touchPosition = point.toGeoPoint()

        markerController.find(touchPosition)?.let { entity ->
            markerController.clickListener?.invoke(entity.state)
            return true
        }

        circleController.find(touchPosition)?.let { entity ->
            val event = CircleEvent(state = entity.state, clicked = touchPosition)
            circleController.clickListener?.invoke(event)
            return true
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
            return true
        }

        polygonController.find(touchPosition)?.let { polygonEntity ->
            val event =
                PolygonEvent(
                    state = polygonEntity.state,
                    clicked = touchPosition,
                )
            polygonController.clickListener?.invoke(event)
            return true
        }

        mapClickCallback?.invoke(touchPosition)
        return true
    }

    override fun onMapLongClick(point: LatLng): Boolean {
        val touchPosition = point.toGeoPoint()
        markerController.find(touchPosition)?.let { entity ->
            if (entity.state.draggable) {
                // Disable map scroll while dragging a marker
                try {
                    val ui = holder.map.uiSettings
                    wasScrollEnabledBeforeDrag = ui.isScrollGesturesEnabled
                    ui.isScrollGesturesEnabled = false
                } catch (e: Exception) {
                    android.util.Log.w("MapLibre", "Failed to disable scroll gestures: ${e.message}")
                }
                markerController.selectedMarker = entity
                markerController.markerManager.removeEntity(entity.state.id)
                markerController.dragStartListener?.invoke(entity.state)
                // Intercept touch to move marker without moving the map
                installDragTouchInterceptor()
                return true
            }
        }

        mapLongClickCallback?.invoke(touchPosition)
        return true
    }

    override fun onMoveBegin(detector: MoveGestureDetector) {
        // Do nothing here
    }

    override fun onMove(detector: MoveGestureDetector) {
        markerController.selectedMarker?.let { entity ->

            val screenCoordinate =
                Offset(
                    detector.focalPoint.x,
                    detector.focalPoint.y,
                )

            holder.fromScreenOffsetSync(screenCoordinate)?.let {
                entity.state.position = it
                markerController.renderer.dragLayer.updatePosition(it)
                markerController.renderer.drawDragLayer()
            }

            markerController.dragListener?.invoke(entity.state)
        }
    }

    override fun onMoveEnd(detector: MoveGestureDetector) {
        markerController.selectedMarker?.let { entity ->
            val screenCoordinate =
                PointF(
                    detector.focalPoint.x,
                    detector.focalPoint.y,
                )
            val point = holder.map.projection.fromScreenLocation(screenCoordinate)
            markerController.renderer.dragLayer.updatePosition(point.toGeoPoint())
            markerController.selectedMarker = null
            markerController.dragEndListener?.invoke(entity.state)
            // Re-enable map scroll after dragging finishes
            try {
                val ui = holder.map.uiSettings
                ui.isScrollGesturesEnabled = wasScrollEnabledBeforeDrag == true
            } catch (e: Exception) {
                android.util.Log.w("MapLibre", "Failed to re-enable scroll gestures: ${e.message}")
            } finally {
                wasScrollEnabledBeforeDrag = null
            }
            removeDragTouchInterceptor()
        }
    }

    private fun installDragTouchInterceptor() {
        if (dragTouchInterceptor != null) return
        val view = holder.mapView
        dragTouchInterceptor =
            View.OnTouchListener { _, event ->
                val selected = markerController.selectedMarker ?: return@OnTouchListener false
                when (event.actionMasked) {
                    MotionEvent.ACTION_MOVE -> {
                        val pos = holder.fromScreenOffsetSync(Offset(event.x, event.y))
                        if (pos != null) {
                            selected.state.position = pos
                            markerController.renderer.dragLayer.updatePosition(pos)
                            markerController.renderer.drawDragLayer()
                            markerController.dragListener?.invoke(selected.state)
                        }
                        true // consume to prevent map panning
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        val point = holder.map.projection.fromScreenLocation(PointF(event.x, event.y))
                        markerController.renderer.dragLayer.updatePosition(point.toGeoPoint())
                        markerController.selectedMarker = null
                        markerController.dragEndListener?.invoke(selected.state)
                        try {
                            val ui = holder.map.uiSettings
                            ui.isScrollGesturesEnabled = wasScrollEnabledBeforeDrag == true
                        } catch (e: Exception) {
                            android.util.Log.w("MapLibre", "Failed to re-enable scroll gestures: ${e.message}")
                        } finally {
                            wasScrollEnabledBeforeDrag = null
                        }
                        removeDragTouchInterceptor()
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
            }
        }
    }

    override fun onCameraIdle() {
        coroutine.launch {
            getMapCameraPosition(holder.map.cameraPosition.toMapCameraPosition())?.let { mapCameraPosition ->
                backCoroutine.launch {
                    notifyMapCameraPosition(mapCameraPosition)
                }
            }
        }
    }

    private fun getMapCameraPosition(camera: MapCameraPosition): MapCameraPositionImpl? {
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
            MapCameraPositionImpl.from(camera).copy(
                visibleRegion = visibleRegion,
            )
        return mapCameraPosition
    }

    private fun ensurePolygonZLayers(style: org.maplibre.android.maps.Style) {
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
                    org.maplibre.android.style.layers.FillLayer(fillId, fillSourceId).apply {
                        setFilter(
                            org.maplibre.android.style.expressions.Expression.eq(
                                org.maplibre.android.style.expressions.Expression
                                    .get("zIndex"),
                                org.maplibre.android.style.expressions.Expression
                                    .literal(z),
                            ),
                        )
                        setProperties(
                            org.maplibre.android.style.layers.PropertyFactory.fillColor(
                                org.maplibre.android.style.expressions.Expression
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
                    org.maplibre.android.style.layers.LineLayer(outlineId, outlineSourceId).apply {
                        setFilter(
                            org.maplibre.android.style.expressions.Expression.eq(
                                org.maplibre.android.style.expressions.Expression
                                    .get("zIndex"),
                                org.maplibre.android.style.expressions.Expression
                                    .literal(z),
                            ),
                        )
                        setProperties(
                            org.maplibre.android.style.layers.PropertyFactory
                                .lineJoin(org.maplibre.android.style.layers.Property.LINE_JOIN_ROUND),
                            org.maplibre.android.style.layers.PropertyFactory
                                .lineCap(org.maplibre.android.style.layers.Property.LINE_CAP_ROUND),
                            org.maplibre.android.style.layers.PropertyFactory.lineColor(
                                org.maplibre.android.style.expressions.Expression
                                    .get("strokeColor"),
                            ),
                            org.maplibre.android.style.layers.PropertyFactory.lineWidth(
                                org.maplibre.android.style.expressions.Expression
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
}
