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
import com.here.sdk.mapview.MapMeasure
import com.here.sdk.mapview.MapPolygon
import com.here.sdk.mapview.MapScene
import com.here.sdk.mapview.MapView
import com.here.time.Duration
import com.mapconductor.core.circle.CircleClickEvent
import com.mapconductor.core.circle.CircleOverlayManager
import com.mapconductor.core.circle.CircleRendererFactory
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.controller.BaseMapViewController
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewHolder
import com.mapconductor.core.map.MapViewState.MoveCameraCallback
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
import com.mapconductor.here.circle.DefaultHereMapCircleRenderer
import com.mapconductor.here.circle.HereMapCircleRenderer
import com.mapconductor.here.marker.HereMarkerController
import com.mapconductor.here.polygon.HerePolygonController
import com.mapconductor.here.polyline.HerePolylineController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface HereMapViewController :
    MapViewController<HereActualCircle>,
    MarkerCapable<HereActualMarker>,
    PolygonCapable,
    PolylineCapable {
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

class HereMapViewControllerImpl(
    private val markerController: HereMarkerController,
    private val polylineController: HerePolylineController,
    private val polygonController: HerePolygonController,
    override val holder: MapViewHolder<MapView, MapScene>,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val circleRendererFactory: CircleRendererFactory<HereActualCircle> = DefaultHereMapCircleRenderer(),
) : BaseMapViewController<HereActualCircle>(),
    HereMapViewController,
    MapCameraListener,
    TapListener,
    LongPressListener {
    companion object {
        private const val ZOOM_ADJUST_VALUE = 0.1 // バイナリテストで確定
    }

    override val circleRenderer =
        HereMapCircleRenderer(
            holder = holder,
            coroutine = coroutine,
        )

    override fun onCircleOverlayManagerInitialized(overlayManager: CircleOverlayManager<HereActualCircle>) {
    }

    override fun createCircleOverlayManager(): CircleOverlayManager<MapPolygon> =
        circleRendererFactory.create(
            onAdd = circleRenderer::addCircles,
            onChange = circleRenderer::changeCircle,
            onRemove = circleRenderer::removeCircles,
        )

    override suspend fun clearOverlays() {
        markerController.clear()
        polylineController.clear()
        polygonController.clear()
        circleOverlayManager.clearOverlays()
    }

    override suspend fun compositionMarkers(data: List<MarkerState>) = markerController.add(data)

    override suspend fun updateMarker(state: MarkerState) = markerController.update(state)

    override fun setOnMarkerDragStart(listener: OnMarkerEventHandler?) {
        markerController.dragStartListener = listener
    }

    override fun setOnMarkerDrag(listener: OnMarkerEventHandler?) {
        markerController.dragListener = listener
    }

    override fun setOnMarkerDragEnd(listener: OnMarkerEventHandler?) {
        markerController.dragEndListener = listener
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

    override suspend fun addCircles(data: List<CircleState>) = circleOverlayManager.addCircles(data)

    override suspend fun updateCircle(state: CircleState) = circleOverlayManager.updateCircle(state)

    override suspend fun compositionPolylines(data: List<PolylineState>) = polylineController.add(data)

    override suspend fun updatePolyline(state: PolylineState) = polylineController.update(state)

    override suspend fun compositionPolygons(data: List<PolygonState>) = polygonController.add(data)

    override suspend fun updatePolygon(state: PolygonState) = polygonController.update(state)

    init {
        setupListeners()
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

        cameraMoveCallback?.let {
            val mapCameraPosition = correctCameraState.toMapCameraPosition()
            it(mapCameraPosition)
        }
    }

    override fun onTap(point: Point2D) {
        val touchPosition = this.getGeoPointFromPoint(point) ?: return

        markerController.find(touchPosition)?.let { entity ->
            markerController.clickListener?.invoke(entity.state)
            return
        }

        circleOverlayManager.find(touchPosition)?.let { entity ->
            val event =
                CircleClickEvent(
                    state = entity.state,
                    position = touchPosition,
                )
            circleClickCallback?.invoke(event)
            return
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
                markerController.find(position)?.let { entity ->
                    if (entity.state.draggable) {
                        entity.state.position = position
                        markerController.selectedMarker = entity
                        markerController.dragStartListener?.invoke(entity.state)
                        return
                    }
                }
                mapLongClickCallback?.invoke(position)
            }

            GestureState.UPDATE.value -> {
                markerController.selectedMarker?.also { selected ->
                    holder.mapView.viewToGeoCoordinates(point)?.also { coordinates ->
                        selected.marker.coordinates = coordinates
                        selected.state.position = coordinates.toGeoPoint()
                    }
                    markerController.dragListener?.invoke(selected.state)
                }
            }

            GestureState.END.value, GestureState.CANCEL.value -> {
                markerController.selectedMarker?.also { selected ->
                    markerController.markerManager.updateEntity(selected)
                    markerController.dragEndListener?.invoke(selected.state)
                    markerController.selectedMarker = null
                    markerController.selectedMarker = null
                }
            }
        }
    }

    private fun getGeoPointFromPoint(point: Point2D): GeoPoint? =
        holder.mapView
            .viewToGeoCoordinates(point)
            ?.toGeoPoint()

    override fun setOnPolylineClickListener(listener: OnPolylineEventHandler?) {
        polylineController.clickListener = listener
    }

    override fun setOnPolygonClickListener(listener: OnPolygonEventHandler?) {
        polygonController.clickListener = listener
    }
}
