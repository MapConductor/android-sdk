package com.mapconductor.here

import com.here.sdk.animation.AnimationState
import com.here.sdk.core.GeoOrientation
import com.here.sdk.core.Point2D
import com.here.sdk.gestures.GestureState
import com.here.sdk.gestures.LongPressListener
import com.here.sdk.gestures.TapListener
import com.here.sdk.mapview.MapCamera
import com.here.sdk.mapview.MapCameraAnimationFactory
import com.here.sdk.mapview.MapCameraListener
import com.here.sdk.mapview.MapCameraUpdateFactory
import com.here.sdk.mapview.MapImage
import com.here.sdk.mapview.MapMarker
import com.here.sdk.mapview.MapMeasure
import com.here.sdk.mapview.MapPolygon
import com.here.sdk.mapview.MapPolyline
import com.here.sdk.mapview.MapScene
import com.here.sdk.mapview.MapView
import com.here.time.Duration
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.circle.CircleClickEvent
import com.mapconductor.core.circle.CircleOverlayManager
import com.mapconductor.core.circle.CircleRendererFactory
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.controller.BaseMapViewController
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.groundimage.GroundImageOverlayManager
import com.mapconductor.core.groundimage.GroundImageRenderer
import com.mapconductor.core.groundimage.GroundImageRendererFactory
import com.mapconductor.core.groundimage.GroundImageState
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewHolder
import com.mapconductor.core.map.MapViewState.MoveCameraCallback
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerOverlayManager
import com.mapconductor.core.marker.MarkerRenderer
import com.mapconductor.core.marker.MarkerRendererFactory
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polygon.PolygonOverlayManager
import com.mapconductor.core.polygon.PolygonRendererFactory
import com.mapconductor.core.polyline.PolylineOverlayManager
import com.mapconductor.core.polyline.PolylineRenderer
import com.mapconductor.core.polyline.PolylineRendererFactory
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.core.projection.WebMercator
import com.mapconductor.here.circle.DefaultHereMapCircleRenderer
import com.mapconductor.here.circle.HereMapCircleRenderer
import com.mapconductor.here.groundimage.DefaultHereMapGroundImageRenderer
import com.mapconductor.here.groundimage.HereMapGroundImageRenderer
import com.mapconductor.here.marker.DefaultHereMapMarkerRenderer
import com.mapconductor.here.marker.HereMapMarkerRenderer
import com.mapconductor.here.polygon.DefaultHereMapPolygonRenderer
import com.mapconductor.here.polygon.HereMapPolygonRenderer
import com.mapconductor.here.polyline.DefaultHereMapPolylineRenderer
import com.mapconductor.here.polyline.HereMapPolylineRenderer
import com.mapconductor.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface IHereMapViewController : MapViewController<MapMarker, MapPolygon, MapPolyline, MapPolygon, MapImage> {
    fun moveCamera(
        dstPosition: MapCameraPosition,
        listener: MoveCameraCallback? = null,
    )

    fun animateCamera(
        dstPosition: MapCameraPosition,
        durationMs: Long,
        listener: MoveCameraCallback? = null,
    )
}

class HereMapViewController(
    override val holder: MapViewHolder<MapView, MapScene>,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
    override val hexGeocell: HexGeocell =
        HexGeocell(
            projection = WebMercator,
            baseHexSideLength = 100000, // 100km - 中ズームレベルに適した値
        ),
    private val markerRendererFactory: MarkerRendererFactory<HereMapActualMarker> = DefaultHereMapMarkerRenderer(),
    private val polylineRendererFactory: PolylineRendererFactory<HereMapActualPolyline> =
        DefaultHereMapPolylineRenderer(),
    private val polygonRendererFactory: PolygonRendererFactory<HereMapActualPolygon> = DefaultHereMapPolygonRenderer(),
    private val circleRendererFactory: CircleRendererFactory<HereMapActualCircle> = DefaultHereMapCircleRenderer(),
    private val groundImageRendererFactory: GroundImageRendererFactory<HereMapActualGroundImage> =
        DefaultHereMapGroundImageRenderer(),
) : BaseMapViewController<
        MapCamera.State,
        HereMapActualMarker,
        HereMapActualCircle,
        HereMapActualPolyline,
        HereMapActualPolygon,
        HereMapActualGroundImage,
    >(),
    IHereMapViewController,
    MapCameraListener,
    TapListener,
    LongPressListener {
    companion object {
        private const val ZOOM_ADJUST_VALUE = 0.1 // バイナリテストで確定
    }

    private var selectedMarker: MarkerEntity<MapMarker>? = null

    override val markerRenderer: MarkerRenderer<MapMarker> =
        HereMapMarkerRenderer(
            holder = holder,
            coroutine = coroutine,
        )

    override fun createMarkerOverlayManager(): MarkerOverlayManager<MapMarker> =
        markerRendererFactory.create(
            hexGeocell = hexGeocell,
            onIconAdd = markerRenderer::addIcons,
            onIconRemove = markerRenderer::removeIcons,
            onIconChange = markerRenderer::changeIcons,
            onAnimate = markerRenderer::animate,
        )

    override val polylineRenderer: PolylineRenderer<MapPolyline> =
        HereMapPolylineRenderer(
            holder = holder,
            coroutine = coroutine,
        )

    override val polygonRenderer =
        HereMapPolygonRenderer(
            holder = holder,
            coroutine = coroutine,
        )

    override val circleRenderer =
        HereMapCircleRenderer(
            holder = holder,
            coroutine = coroutine,
        )

    override val groundImageRenderer: GroundImageRenderer<HereMapActualGroundImage> =
        HereMapGroundImageRenderer(
            holder = holder,
            coroutine = coroutine,
        )

    override fun onCircleOverlayManagerInitialized(overlayManager: CircleOverlayManager<HereMapActualCircle>) {
    }

    override fun onPolygonOverlayManagerInitialized(overlayManager: PolygonOverlayManager<HereMapActualPolygon>) {
    }

    override fun onPolylineOverlayManagerInitialized(overlayManager: PolylineOverlayManager<HereMapActualPolyline>) {
    }

    override fun onMarkerOverlayManagerInitialized(overlayManager: MarkerOverlayManager<HereMapActualMarker>) {
    }

    override fun onGroundImageOverlayManagerInitialized(
        overlayManager: GroundImageOverlayManager<HereMapActualGroundImage>,
    ) {
    }

    override fun createPolylineOverlayManager(): PolylineOverlayManager<MapPolyline> =
        polylineRendererFactory.create(
            onAdd = polylineRenderer::addPolylines,
            onChange = polylineRenderer::changePolylines,
            onRemove = polylineRenderer::removePolylines,
        )

    override fun createPolygonOverlayManager(): PolygonOverlayManager<MapPolygon> =
        polygonRendererFactory.create(
            onAdd = polygonRenderer::addPolygons,
            onChange = polygonRenderer::changePolygon,
            onRemove = polygonRenderer::removePolygons,
        )

    override fun createCircleOverlayManager(): CircleOverlayManager<MapPolygon> =
        circleRendererFactory.create(
            onAdd = circleRenderer::addCircles,
            onChange = circleRenderer::changeCircle,
            onRemove = circleRenderer::removeCircles,
        )

    override fun createGroundImageOverlayManager(): GroundImageOverlayManager<HereMapActualGroundImage> {
        groundImageRendererFactory.create(
            onAdd = groundImageRenderer::addGroundImages,
            onChange = groundImageRenderer::changeGroundImages,
            onRemove = groundImageRenderer::removeGroundImages,
        )
        TODO("Not yet implemented")
    }

    override suspend fun clearOverlays() {
        markerOverlayManager.clearOverlays()
        polylineOverlayManager.clearOverlays()
        polygonOverlayManager.clearOverlays()
        circleOverlayManager.clearOverlays()
    }

    override suspend fun addMarkers(markerList: List<MarkerState>) = markerOverlayManager.addMarkers(markerList)

    override suspend fun updateMarker(state: MarkerState) = markerOverlayManager.updateMarker(state)

    override suspend fun addCircles(data: List<CircleState>) = circleOverlayManager.addCircles(data)

    override suspend fun updateCircle(state: CircleState) = circleOverlayManager.updateCircle(state)

    override suspend fun addPolylines(data: List<PolylineState>) = polylineOverlayManager.addPolylines(data)

    override suspend fun updatePolyline(state: PolylineState) = polylineOverlayManager.updatePolyline(state)

    override suspend fun addGroundImages(data: List<GroundImageState>) {
        TODO("Not yet implemented")
    }

    override suspend fun updateGroundImage(state: GroundImageState) {
        TODO("Not yet implemented")
    }

    init {
        setupListeners()
        markerRenderer.init(markerOverlayManager)
        polygonRenderer.init(polygonOverlayManager)
        circleRenderer.init(circleOverlayManager)
    }

    override fun setupListeners() {
        holder.mapView.camera.removeListener(this)
        holder.mapView.camera.addListener(this)
        holder.mapView.gestures.tapListener = this
        holder.mapView.gestures.longPressListener = this
    }

    override fun moveCamera(
        dstPosition: MapCameraPosition,
        listener: MoveCameraCallback?,
    ) {
        val camera = this.holder.mapView.camera
        val adjustCameraUpdate =
            MapCameraUpdateFactory.lookAt(
                GeoPoint.from(dstPosition.position).toGeoCoordinates().toUpdate(),
                GeoOrientation(dstPosition.bearing, dstPosition.tilt).toUpdate(),
                MapMeasure(MapMeasure.Kind.ZOOM_LEVEL, dstPosition.zoom + ZOOM_ADJUST_VALUE),
            )

        camera.applyUpdate(adjustCameraUpdate)
        listener?.onComplete(true)
    }

    override fun animateCamera(
        dstPosition: MapCameraPosition,
        durationMs: Long,
        listener: MoveCameraCallback?,
    ) {
        val camera = this.holder.mapView.camera

//      bowFactor > 0: 最初にズームアウト → 到達時にズームイン
//      bowFactor < 0: 最初にズームイン → 到達時にズームアウト（ややレア）
//      bowFactor = 0: 常に同じズーム（直線的）
        val bowFactor = 1.0
        val animation =
            MapCameraAnimationFactory.flyTo(
                GeoPoint.from(dstPosition.position).toGeoCoordinates().toUpdate(),
                GeoOrientation(dstPosition.bearing, dstPosition.tilt).toUpdate(),
                MapMeasure(MapMeasure.Kind.ZOOM_LEVEL, dstPosition.zoom + ZOOM_ADJUST_VALUE),
                bowFactor,
                Duration.ofMillis(durationMs),
            )
        coroutine.launch {
            camera.startAnimation(animation) { animState ->
                when (animState) {
                    // Do nothing here
                    AnimationState.STARTED -> Unit
                    AnimationState.COMPLETED -> listener?.onComplete(true)
                    AnimationState.CANCELLED -> listener?.onComplete(false)
                }
            }
        }
    }

    override fun onMapCameraUpdated(cameraState: MapCamera.State) {
        val correctCameraState =
            MapCamera.State(
                cameraState.targetCoordinates,
                GeoOrientation(cameraState.orientationAtTarget.bearing, cameraState.orientationAtTarget.tilt),
                0.0,
                cameraState.zoomLevel - ZOOM_ADJUST_VALUE,
            )

        cameraMoveListener?.invoke(correctCameraState)
    }

    override fun onTap(point: Point2D) {
        val touchPosition = this.getGeoPointFromPoint(point) ?: return
        val zoom = holder.mapView.camera.state.zoomLevel - ZOOM_ADJUST_VALUE
        val tolerance =
            Settings.Default.tapTolerance.value
                .toDouble() * ResourceProvider.getDensity()

        val entity =
            markerRenderer.findNearestMarker(
                position = touchPosition,
                tolerance = tolerance,
                zoom = zoom,
            )
        if (entity != null) {
            markerClickListener?.invoke(entity.state)
            return
        }

        circleOverlayManager.find(touchPosition)?.let { entity ->
            val event =
                CircleClickEvent(
                    state = entity.state,
                    position = touchPosition,
                )
            circleClickListener?.invoke(event)
            return
        }

        // If no overlay is processed, process the tap as onMapClick
        mapClickListener?.let { it(touchPosition) }
    }

    override fun onLongPress(
        gesture: GestureState,
        point: Point2D,
    ) {
        val position = this.getGeoPointFromPoint(point) ?: return

        when (gesture.value) {
            GestureState.BEGIN.value -> {
                val zoom = holder.mapView.camera.state.zoomLevel - ZOOM_ADJUST_VALUE
                val tolerance =
                    Settings.Default.tapTolerance.value
                        .toDouble() * ResourceProvider.getDensity()

                val entity =
                    markerRenderer.findNearestMarker(
                        position = position,
                        tolerance = tolerance,
                        zoom = zoom,
                    ) ?: return

                entity.state.position = position
                selectedMarker = entity

                // Suppress the recomposition for the position property
                markerRenderer.setDraggingState(entity.state, true)

                markerDragStartListener?.invoke(entity.state)
            }

            GestureState.UPDATE.value -> {
                selectedMarker?.also { selected ->
                    holder.mapView.viewToGeoCoordinates(point)?.also { coordinates ->
                        selected.marker.coordinates = coordinates
                        selected.state.position = coordinates.toGeoPoint()
                    }
                    markerDragListener?.invoke(selected.state)
                }
            }

            GestureState.END.value, GestureState.CANCEL.value -> {
                selectedMarker?.also { selected ->
                    markerOverlayManager.markerManager.updateEntity(selected)

                    // Restore the recomposition for the position property
                    markerRenderer.setDraggingState(selected.state, false)

                    markerDragEndListener?.invoke(selected.state)
                    selectedMarker = null
                }
            }
        }
    }

    private fun getGeoPointFromPoint(point: Point2D): GeoPoint? =
        holder.mapView
            .viewToGeoCoordinates(point)
            ?.toGeoPoint()
}
