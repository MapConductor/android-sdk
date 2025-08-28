package com.mapconductor.arcgis

import com.arcgismaps.mapping.view.GraphicsOverlay
import com.arcgismaps.mapping.view.LongPressEvent
import com.arcgismaps.mapping.view.PanChangeEvent
import com.arcgismaps.mapping.view.SingleTapConfirmedEvent
import com.arcgismaps.mapping.view.SurfacePlacement
import com.arcgismaps.mapping.view.UpEvent
import com.arcgismaps.mapping.view.extensions.motionEvent
import com.mapconductor.arcgis.circle.ArcGISCircleRenderer
import com.mapconductor.arcgis.circle.DefaultArcGISCircleRenderer
import com.mapconductor.arcgis.marker.ArcGISMarkerController
import com.mapconductor.arcgis.marker.SelectedMarker
import com.mapconductor.arcgis.polygon.ArcGISPolygonController
import com.mapconductor.arcgis.polyline.ArcGISPolylineController
import com.mapconductor.core.circle.CircleClickEvent
import com.mapconductor.core.circle.CircleOverlayManager
import com.mapconductor.core.circle.CircleRenderer
import com.mapconductor.core.circle.CircleRendererFactory
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.controller.BaseMapViewController
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.MarkerCapable
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.polygon.OnPolygonEventHandler
import com.mapconductor.core.polygon.PolygonCapable
import com.mapconductor.core.polygon.PolygonEvent
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.core.polyline.OnPolylineEventHandler
import com.mapconductor.core.polyline.PolylineCapable
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.settings.Settings
import android.view.MotionEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface ArcGISMapViewController :
    MapViewController<
        ArcGISActualCircle,
    >,
    MarkerCapable<ArcGISActualMarker>,
    PolylineCapable,
    PolygonCapable {
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

class ArcGISMapViewControllerImpl(
    override val holder: ArcGISMapViewHolder,
    private val polylineController: ArcGISPolylineController,
    private val polygonController: ArcGISPolygonController,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val circleLayer: GraphicsOverlay =
        GraphicsOverlay().apply {
            sceneProperties.surfacePlacement = SurfacePlacement.DrapedFlat
        },
    private val circleRendererFactory: CircleRendererFactory<ArcGISActualCircle> =
        DefaultArcGISCircleRenderer(),
    private val markerController: ArcGISMarkerController,
) : BaseMapViewController<
        ArcGISActualCircle,
    >(),
    ArcGISMapViewController {
    override fun createCircleOverlayManager(): CircleOverlayManager<ArcGISActualCircle> =
        circleRendererFactory.create(
            onAdd = circleRenderer::addCircles,
            onChange = circleRenderer::changeCircle,
            onRemove = circleRenderer::removeCircles,
        )

    override val circleRenderer: CircleRenderer<ArcGISActualCircle> =
        ArcGISCircleRenderer(
            circleLayer = circleLayer,
            holder = holder,
            coroutine = coroutine,
        )

    override fun onCircleOverlayManagerInitialized(overlayManager: CircleOverlayManager<ArcGISActualCircle>) {
    }

    init {
        holder.map.graphicsOverlays.clear()
        holder.map.graphicsOverlays.add(circleLayer)
        holder.map.graphicsOverlays.add(polygonController.renderer.polygonLayer)
        holder.map.graphicsOverlays.add(polylineController.renderer.polylineLayer)
        holder.map.graphicsOverlays.add(markerController.renderer.markerLayer)
        setupListeners()
    }

    override fun setupListeners() {
        coroutine.launch {
            holder.map.onSingleTapConfirmed.collect { onMapTap(it) }
        }
        coroutine.launch {
            holder.map.viewpointChanged.collect { onViewpointChange() }
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

    private fun onViewpointChange() {
        this.cameraMoveCallback?.let {
            val mapCamera = holder.map.getCurrentViewpointCamera().toMapCameraPosition()
            it(mapCamera)
        }
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

        val circleEntity = circleOverlayManager.find(touchPosition)
        circleEntity?.let {
            val event =
                CircleClickEvent(
                    state = circleEntity.state,
                    position = touchPosition,
                )
            circleClickCallback?.invoke(event)
            return
        }

        polygonController.find(touchPosition)?.let { polygonEntity ->
            val event =
                PolygonEvent(
                    state = polygonEntity.state,
                    clicked = touchPosition,
                )
            polygonClickCallback?.invoke(event)
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

    override suspend fun addCircles(data: List<CircleState>) = circleOverlayManager.addCircles(data)

    override suspend fun updateCircle(state: CircleState) = circleOverlayManager.updateCircle(state)

    override fun moveCamera(
        dstPosition: MapCameraPosition,
        listener: MapViewState.MoveCameraCallback?,
    ) {
        val dstCameraPosition = dstPosition.toCamera()

        holder.map.setViewpointCamera(
            camera = dstCameraPosition,
        )
        listener?.onComplete(true)
    }

    override fun animateCamera(
        dstPosition: MapCameraPosition,
        duration: Int,
        listener: MapViewState.MoveCameraCallback?,
    ) {
        val dstCameraPosition = dstPosition.toCamera()

        coroutine.launch {
            val result =
                holder.map.setViewpointCameraAnimated(
                    camera = dstCameraPosition,
                    duration = duration.toFloat() / 1000.0f,
                )
            listener?.onComplete(result.isSuccess)
        }
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
        this.polygonClickCallback = listener
    }
}
