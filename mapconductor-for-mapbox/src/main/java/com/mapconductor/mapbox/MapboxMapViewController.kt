package com.mapconductor.mapbox

import androidx.compose.ui.geometry.Offset
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraChanged
import com.mapbox.maps.CameraChangedCallback
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.CameraState
import com.mapbox.maps.ScreenCoordinate
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.OnMapLongClickListener
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.addOnMapLongClickListener
import com.mapbox.maps.plugin.gestures.addOnMoveListener
import com.mapbox.maps.plugin.gestures.removeOnMapClickListener
import com.mapbox.maps.plugin.gestures.removeOnMapLongClickListener
import com.mapbox.maps.plugin.gestures.removeOnMoveListener
import com.mapconductor.core.circle.CircleClickEvent
import com.mapconductor.core.circle.CircleOverlayManager
import com.mapconductor.core.circle.CircleRendererFactory
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.controller.BaseMapViewController
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.MarkerCapable
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.polygon.PolygonOverlayManager
import com.mapconductor.core.polygon.PolygonRenderer
import com.mapconductor.core.polyline.PolylineOverlayManager
import com.mapconductor.core.polyline.PolylineRendererFactory
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.mapbox.circle.DefaultMapboxCircleRenderer
import com.mapconductor.mapbox.circle.MapboxCircleLayer
import com.mapconductor.mapbox.circle.MapboxCircleRenderer
import com.mapconductor.mapbox.marker.MapboxMarkerController
import com.mapconductor.mapbox.polygon.MapboxPolygonLayer
import com.mapconductor.mapbox.polygon.MapboxPolygonRenderer
import com.mapconductor.mapbox.polyline.DefaultMapboxPolylineRenderer
import com.mapconductor.mapbox.polyline.MapboxPolylineLayer
import com.mapconductor.mapbox.polyline.MapboxPolylineRenderer
import android.animation.Animator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface IMapboxMapViewController :
    MapViewController<
        MapboxActualCircle,
        MapboxActualPolyline,
        MapboxActualPolygon,
    >,
    MarkerCapable<MapboxActualMarker> {
    fun moveCamera(
        dstPosition: MapCameraPosition,
        listener: MapViewState.MoveCameraCallback? = null,
    )

    fun animateCamera(
        dstPosition: MapCameraPosition,
        duration: Long,
        listener: MapViewState.MoveCameraCallback? = null,
    )
}

internal class MapboxMapViewController(
    override val holder: MapboxMapViewHolder,
    private val markerController: MapboxMarkerController,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
    private val polylineRendererFactory: PolylineRendererFactory<MapboxActualPolyline> =
        DefaultMapboxPolylineRenderer(),
    private val circleLayer: MapboxCircleLayer =
        MapboxCircleLayer(
            sourceId = "circle-source",
            layerId = "circle-layer",
        ),
    private val polylineLayer: MapboxPolylineLayer =
        MapboxPolylineLayer(
            sourceId = "polyline-source",
            layerId = "polyline-layer",
        ),
    private val polygonLayer: MapboxPolygonLayer =
        MapboxPolygonLayer(
            sourceId = "polygon-source",
            layerId = "polygon-layer",
        ),
    private val circleRendererFactory: CircleRendererFactory<MapboxActualCircle> =
        DefaultMapboxCircleRenderer(),
) : BaseMapViewController<
        MapboxActualCircle,
        MapboxActualPolyline,
        MapboxActualPolygon,
    >(),
    IMapboxMapViewController,
    CameraChangedCallback,
    OnMapClickListener,
    OnMapLongClickListener,
    OnMoveListener {
    companion object {
        private const val ZOOM_ADJUST_VALUE = 1.0
    }

    override fun createPolylineOverlayManager(): PolylineOverlayManager<MapboxActualPolyline> =
        polylineRendererFactory.create(
            onAdd = polylineRenderer::addPolylines,
            onChange = polylineRenderer::changePolylines,
            onRemove = polylineRenderer::removePolylines,
            onPostProcess = polylineRenderer::redraw,
        )

    override fun createPolygonOverlayManager(): PolygonOverlayManager<MapboxActualPolygon> {
        TODO("Not yet implemented")
    }

    override fun createCircleOverlayManager(): CircleOverlayManager<MapboxActualCircle> =
        circleRendererFactory.create(
            onAdd = circleRenderer::addCircles,
            onChange = circleRenderer::changeCircle,
            onRemove = circleRenderer::removeCircles,
            onPostProcess = circleRenderer::redraw,
        )

    override val polylineRenderer: MapboxPolylineRenderer =
        MapboxPolylineRenderer(
            holder = holder,
            coroutine = coroutine,
            layer = polylineLayer,
        )

    override val polygonRenderer: PolygonRenderer<MapboxActualPolygon> =
        MapboxPolygonRenderer(
            holder = holder,
            coroutine = coroutine,
            layer = polygonLayer,
        )
    override val circleRenderer: MapboxCircleRenderer =
        MapboxCircleRenderer(
            holder = holder,
            coroutine = coroutine,
            layer = circleLayer,
        )

    override fun onCircleOverlayManagerInitialized(overlayManager: CircleOverlayManager<MapboxActualCircle>) {
    }

    override fun onPolygonOverlayManagerInitialized(overlayManager: PolygonOverlayManager<MapboxActualPolygon>) {
    }

    override fun onPolylineOverlayManagerInitialized(overlayManager: PolylineOverlayManager<MapboxActualPolyline>) {
    }

    init {
        holder.map.getStyle { style ->
            style.addSource(circleLayer.source)
            style.addLayer(circleLayer.layer)
            style.addSource(polylineLayer.source)
            style.addLayer(polylineLayer.layer)
            style.addSource(markerController.renderer.markerLayer.source)
            style.addLayer(markerController.renderer.markerLayer.layer)
            style.addSource(markerController.renderer.dragLayer.source)
            style.addLayer(markerController.renderer.dragLayer.layer)
        }
        setupListeners()
    }

    override fun setupListeners() {
        holder.map.subscribeCameraChanged(this)
        holder.map.removeOnMapClickListener(this)
        holder.map.addOnMapClickListener(this)

        holder.map.removeOnMapLongClickListener(this)
        holder.map.addOnMapLongClickListener(this)

        holder.map.removeOnMoveListener(this)
        holder.map.addOnMoveListener(this)
    }

    override suspend fun clearOverlays() {
        markerController.clear()
        polylineOverlayManager.clearOverlays()
    }

    override suspend fun compositionMarkers(data: List<MarkerState>) = markerController.add(data)

    override suspend fun updateMarker(state: MarkerState) = markerController.update(state)

    override suspend fun addPolylines(data: List<PolylineState>) = polylineOverlayManager.addPolylines(data)

    override suspend fun updatePolyline(state: PolylineState) = polylineOverlayManager.updatePolyline(state)

    override suspend fun addCircles(data: List<CircleState>) = circleOverlayManager.addCircles(data)

    override suspend fun updateCircle(state: CircleState) = circleOverlayManager.updateCircle(state)

    override fun run(cameraChanged: CameraChanged) {
        cameraMoveCallback?.let {
            val mapCameraPosition =
                CameraState(
                    cameraChanged.cameraState.center,
                    cameraChanged.cameraState.padding,
                    cameraChanged.cameraState.zoom + ZOOM_ADJUST_VALUE,
                    cameraChanged.cameraState.bearing,
                    cameraChanged.cameraState.pitch,
                ).toMapCameraPosition()

            it(mapCameraPosition)
        }
    }

    override fun moveCamera(
        dstPosition: MapCameraPosition,
        listener: MapViewState.MoveCameraCallback?,
    ) {
        val cameraOptions =
            CameraOptions
                .Builder()
                .center(dstPosition.position.toPoint())
                .zoom(dstPosition.zoom - ZOOM_ADJUST_VALUE)
                .pitch(dstPosition.tilt)
                .bearing(dstPosition.bearing)
                .build()

        coroutine.launch {
            holder.map.setCamera(cameraOptions)
        }
        listener?.onComplete(true)
    }

    override fun animateCamera(
        dstPosition: MapCameraPosition,
        duration: Long,
        listener: MapViewState.MoveCameraCallback?,
    ) {
        val targetCamera = dstPosition.toCameraOptions()
        val adjustCamera =
            CameraOptions
                .Builder()
                .center(targetCamera.center)
                .zoom(targetCamera.zoom!! + ZOOM_ADJUST_VALUE)
                .pitch(targetCamera.pitch)
                .bearing(targetCamera.pitch)
                .build()

        val animationOptions =
            MapAnimationOptions
                .Builder()
                .duration(duration)
                .build()

        val animatorListener =
            object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    // Do nothing here
                }

                override fun onAnimationEnd(animation: Animator) {
                    listener?.onComplete(true)
                }

                override fun onAnimationCancel(animation: Animator) {
                    listener?.onComplete(false)
                }

                override fun onAnimationRepeat(animation: Animator) {
                    // Do nothing here
                }
            }

        coroutine.launch {
            holder.map.flyTo(
                cameraOptions = adjustCamera,
                animationOptions = animationOptions,
                animatorListener = animatorListener,
            )
        }
    }

    override fun onMapLongClick(point: Point): Boolean {
        val touchPosition = point.toGeoPoint()
        markerController.find(touchPosition)?.let { entity ->
            if (entity.state.draggable) {
                markerController.selectedMarker = entity
                markerController.markerManager.removeEntity(entity.state.id)
                markerController.dragStartListener?.invoke(entity.state)
                return true
            }
        }

        mapLongClickCallback?.invoke(touchPosition)
        return true
    }

    override fun onMapClick(point: Point): Boolean {
        val touchPosition = point.toGeoPoint()

        markerController.find(touchPosition)?.let { entity ->
            markerController.clickListener?.invoke(entity.state)
            return true
        }

        val circleEntity = this.circleOverlayManager.find(touchPosition)
        circleEntity?.let {
            val event =
                CircleClickEvent(
                    state = circleEntity.state,
                    position = touchPosition,
                )
            circleClickCallback?.invoke(event)
            return true
        }

        mapClickCallback?.invoke(touchPosition)
        return true
    }

//    override fun drawPolyline(geoPoints: List<IGeoPoint>) {
//        val points =
//            geoPoints.map {
//                GeoPoint.from(it).toPoint()
//            }
//        lineSource.geometry(LineString.fromLngLats(points))
//    }

    override fun onMove(detector: MoveGestureDetector): Boolean {
        markerController.renderer.dragLayer.selected?.let { entity ->

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
            return true
        }
        return false
    }

    override fun onMoveBegin(detector: MoveGestureDetector) {
        // Do nothing here
    }

    override fun onMoveEnd(detector: MoveGestureDetector) {
        markerController.selectedMarker?.let { entity ->
            val screenCoordinate =
                ScreenCoordinate(
                    detector.focalPoint.x.toDouble(),
                    detector.focalPoint.y.toDouble(),
                )
            val point = holder.map.coordinateForPixel(screenCoordinate)
            markerController.renderer.dragLayer.updatePosition(point.toGeoPoint())
            markerController.selectedMarker = null
            markerController.dragEndListener?.invoke(entity.state)
        }
    }

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
}
