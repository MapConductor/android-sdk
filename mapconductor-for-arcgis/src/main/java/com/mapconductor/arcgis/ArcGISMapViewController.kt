package com.mapconductor.arcgis

import com.arcgismaps.mapping.view.Camera
import com.arcgismaps.mapping.view.Graphic
import com.arcgismaps.mapping.view.GraphicsOverlay
import com.arcgismaps.mapping.view.LongPressEvent
import com.arcgismaps.mapping.view.PanChangeEvent
import com.arcgismaps.mapping.view.SingleTapConfirmedEvent
import com.arcgismaps.mapping.view.SurfacePlacement
import com.arcgismaps.mapping.view.UpEvent
import com.arcgismaps.mapping.view.extensions.motionEvent
import com.mapconductor.arcgis.circle.ArcGISCircleRenderer
import com.mapconductor.arcgis.circle.DefaultArcGISCircleRenderer
import com.mapconductor.arcgis.marker.ArcGISMarkerRenderer
import com.mapconductor.arcgis.marker.DefaultArcGISMarkerRender
import com.mapconductor.arcgis.polygon.ArcGISPolygonRenderer
import com.mapconductor.arcgis.polygon.DefaultArcGISPolygonRenderer
import com.mapconductor.arcgis.polyline.ArcGISPolylineRenderer
import com.mapconductor.arcgis.polyline.DefaultArcGISPolylineRenderer
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.circle.CircleClickEvent
import com.mapconductor.core.circle.CircleOverlayManager
import com.mapconductor.core.circle.CircleRenderer
import com.mapconductor.core.circle.CircleRendererFactory
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.controller.BaseMapViewController
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.MarkerOverlayManager
import com.mapconductor.core.marker.MarkerRenderer
import com.mapconductor.core.marker.MarkerRendererFactory
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polygon.PolygonOverlayManager
import com.mapconductor.core.polygon.PolygonRenderer
import com.mapconductor.core.polygon.PolygonRendererFactory
import com.mapconductor.core.polyline.PolylineOverlayManager
import com.mapconductor.core.polyline.PolylineRenderer
import com.mapconductor.core.polyline.PolylineRendererFactory
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.core.projection.WebMercator
import com.mapconductor.settings.Settings
import android.view.MotionEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface IArcGISMapViewController :
    MapViewController<
        ArcGISActualMarker,
        ArcGISActualCircle,
        ArcGISActualPolyline,
        ArcGISActualPolygon,
    > {
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

internal data class SelectedMarker(
    val state: MarkerState,
    val graphic: Graphic,
)

class ArcGISMapViewController(
    override val holder: ArcGISMapViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
    override val hexGeocell: HexGeocell =
        HexGeocell(
            projection = WebMercator,
            baseHexSideLength = 100000, // 100km - 中ズームレベルに適した値
        ),
    private val markerLayer: GraphicsOverlay =
        GraphicsOverlay().apply {
            sceneProperties.surfacePlacement = SurfacePlacement.Relative
        },
    private val circleLayer: GraphicsOverlay =
        GraphicsOverlay().apply {
            sceneProperties.surfacePlacement = SurfacePlacement.DrapedFlat
        },
    private val polylineLayer: GraphicsOverlay =
        GraphicsOverlay().apply {
            sceneProperties.surfacePlacement = SurfacePlacement.DrapedBillboarded
        },
    private val polygonLayer: GraphicsOverlay =
        GraphicsOverlay().apply {
            sceneProperties.surfacePlacement = SurfacePlacement.DrapedBillboarded
        },
    private val markerRendererFactory: MarkerRendererFactory<ArcGISActualMarker> = DefaultArcGISMarkerRender(),
    private val polylineRendererFactory: PolylineRendererFactory<ArcGISActualPolyline> =
        DefaultArcGISPolylineRenderer(),
    private val polygonRendererFactory: PolygonRendererFactory<ArcGISActualPolygon> =
        DefaultArcGISPolygonRenderer(),
    private val circleRendererFactory: CircleRendererFactory<ArcGISActualCircle> =
        DefaultArcGISCircleRenderer(),
) : BaseMapViewController<
        Camera,
        ArcGISActualMarker,
        ArcGISActualCircle,
        ArcGISActualPolyline,
        ArcGISActualPolygon,
    >(),
    IArcGISMapViewController {
    override val markerRenderer: MarkerRenderer<ArcGISActualMarker> =
        ArcGISMarkerRenderer(
            markerLayer = markerLayer,
            holder = holder,
            coroutine = coroutine,
        )

    private var selectedMarker: SelectedMarker? = null

    override fun createMarkerOverlayManager(): MarkerOverlayManager<ArcGISActualMarker> =
        markerRendererFactory.create(
            hexGeocell = hexGeocell,
            onIconAdd = markerRenderer::addIcons,
            onIconRemove = markerRenderer::removeIcons,
            onIconChange = markerRenderer::changeIcons,
            onAnimate = markerRenderer::animate,
        )

    override fun createPolylineOverlayManager(): PolylineOverlayManager<ArcGISActualPolyline> =
        polylineRendererFactory.create(
            onAdd = polylineRenderer::addLines,
            onChange = polylineRenderer::changeLine,
            onRemove = polylineRenderer::removeLines,
        )

    override val polylineRenderer: PolylineRenderer<ArcGISActualPolyline> =
        ArcGISPolylineRenderer(
            polylineLayer = polylineLayer,
            holder = holder,
            coroutine = coroutine,
        )

    override fun createPolygonOverlayManager(): PolygonOverlayManager<ArcGISActualPolygon> =
        polygonRendererFactory.create(
            onAdd = polygonRenderer::addPolygons,
            onChange = polygonRenderer::changePolygon,
            onRemove = polygonRenderer::removePolygons,
        )

    override val polygonRenderer: PolygonRenderer<ArcGISActualPolygon> =
        ArcGISPolygonRenderer(
            polygonLayer = polygonLayer,
            holder = holder,
            coroutine = coroutine,
        )

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

    override fun onPolygonOverlayManagerInitialized(overlayManager: PolygonOverlayManager<ArcGISActualPolygon>) {
    }

    override fun onPolylineOverlayManagerInitialized(overlayManager: PolylineOverlayManager<ArcGISActualPolyline>) {
    }

    override fun onMarkerOverlayManagerInitialized(overlayManager: MarkerOverlayManager<ArcGISActualMarker>) {
    }

    init {
        markerRenderer.init(markerOverlayManager)
        holder.map.graphicsOverlays.clear()
        holder.map.graphicsOverlays.add(circleLayer)
        holder.map.graphicsOverlays.add(polylineLayer)
        holder.map.graphicsOverlays.add(markerLayer)
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
        this.cameraMoveListener?.invoke(holder.map.getCurrentViewpointCamera())
    }

    private suspend fun onMapPan(event: PanChangeEvent) {
        selectedMarker?.also {
            val screenPoint = event.screenCoordinate
            val point = holder.map.screenToLocation(screenPoint).getOrNull() ?: return
            val position = point.toGeoPoint()
            it.graphic.geometry = point
            it.state.position = position
            markerDragListener?.invoke(it.state)
        }
    }

    private suspend fun onMapUp(event: UpEvent) {
        selectedMarker?.also {
            val screenPoint = event.screenCoordinate
            val point = holder.map.screenToLocation(screenPoint).getOrNull() ?: return
            val position = point.toGeoPoint()
            it.graphic.geometry = point
            it.state.position = position

            // Restore the recomposition for the position property
            markerRenderer.setDraggingState(it.state, false)

            markerDragEndListener?.invoke(it.state)
            with(holder.map) {
                interactionOptions.isPanEnabled = true
                interactionOptions.isRotateEnabled = true
                interactionOptions.isZoomEnabled = true
            }
        }
        selectedMarker = null
    }

    private suspend fun onMapLongPress(event: LongPressEvent) {
        if (event.motionEvent.action != MotionEvent.ACTION_MOVE) return

        val screenPoint = event.screenCoordinate
        val point = holder.map.screenToLocation(screenPoint).getOrNull() ?: return
        val position = point.toGeoPoint()
        val identifyResult =
            holder.map.identifyGraphicsOverlay(
                graphicsOverlay = markerLayer,
                screenCoordinate = screenPoint,
                tolerance =
                    Settings.Default.tapTolerance.value
                        .toDouble(),
                returnPopupsOnly = false,
            )
        val graphics = identifyResult.getOrNull()?.graphics
        val graphic = graphics?.firstOrNull()
        if (graphic == null) {
            mapLongClickListener?.invoke(position)
            return
        }
        val markerId = (graphic.attributes.get("id") as? String) ?: return
        val state = markerOverlayManager.getMarkerState(markerId) ?: return
        selectedMarker =
            SelectedMarker(
                state = state,
                graphic = graphic,
            )
        // 3Dナビゲーションを無効化
        with(holder.map) {
            interactionOptions.isPanEnabled = false
            interactionOptions.isRotateEnabled = false
            interactionOptions.isZoomEnabled = false
        }

        // Suppress the recomposition for the position property
        markerRenderer.setDraggingState(state, true)

        markerDragStartListener?.invoke(state)
    }

    private suspend fun onMapTap(event: SingleTapConfirmedEvent) {
        val screenPoint = event.screenCoordinate
        val touchPosition =
            holder.map
                .screenToLocation(screenPoint)
                .getOrNull()
                ?.toGeoPoint() ?: return

        val markerEntity =
            markerRenderer.findNearestMarker(
                position = touchPosition,
                tolerance =
                    Settings.Default.tapTolerance.value
                        .toDouble() * ResourceProvider.getDensity(),
                zoom = holder.map.getCurrentViewpointCamera().getZoomLevel(),
            )
        if (markerEntity != null) {
            markerClickListener?.invoke(markerEntity.state)
            return
        }

        val circleEntity = circleOverlayManager.find(touchPosition)
        circleEntity?.let {
            val event =
                CircleClickEvent(
                    state = circleEntity.state,
                    position = touchPosition,
                )
            circleClickListener?.invoke(event)
        }

        holder.map.screenToLocation(screenPoint).getOrNull()?.also {
            mapClickListener?.invoke(it.toGeoPoint())
        }
    }

    override suspend fun clearOverlays() {
        markerOverlayManager.clearOverlays()
        polylineOverlayManager.clearOverlays()
    }

    override suspend fun addMarkers(markerList: List<MarkerState>) = markerOverlayManager.addMarkers(markerList)

    override suspend fun updateMarker(state: MarkerState) = markerOverlayManager.updateMarker(state)

    override suspend fun addPolylines(data: List<PolylineState>) = polylineOverlayManager.addPolylines(data)

    override suspend fun updatePolyline(state: PolylineState) = polylineOverlayManager.updatePolyline(state)

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
}
