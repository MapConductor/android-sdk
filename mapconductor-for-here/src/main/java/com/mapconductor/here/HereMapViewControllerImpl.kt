package com.mapconductor.here

import HereMapDesignTypeChangeHandler
import HereMapViewController
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
import com.here.sdk.mapview.MapCameraUpdateFactory
import com.here.sdk.mapview.MapMeasure
import com.here.sdk.mapview.MapScene
import com.here.sdk.mapview.MapView
import com.here.time.Duration
import com.mapconductor.core.circle.CircleCapable
import com.mapconductor.core.circle.CircleEvent
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.circle.OnCircleEventHandler
import com.mapconductor.core.controller.BaseMapViewController
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.map.MapViewHolder
import com.mapconductor.core.map.VisibleRegion
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.polygon.OnPolygonEventHandler
import com.mapconductor.core.polygon.PolygonEvent
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.core.polyline.OnPolylineEventHandler
import com.mapconductor.core.polyline.PolylineEvent
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.here.circle.HereCircleController
import com.mapconductor.here.marker.HereMarkerController
import com.mapconductor.here.polygon.HerePolygonController
import com.mapconductor.here.polyline.HerePolylineController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HereMapViewControllerImpl(
    private val markerController: HereMarkerController,
    private val polylineController: HerePolylineController,
    private val polygonController: HerePolygonController,
    private val circleController: HereCircleController,
    override val holder: MapViewHolder<MapView, MapScene>,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
    val backCoroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : BaseMapViewController(),
    CircleCapable,
    HereMapViewController,
    MapCameraListener,
    TapListener,
    LongPressListener {
    companion object {
        internal const val ZOOM_ADJUST_VALUE = 0.1 // バイナリテストで確定
    }

    override suspend fun clearOverlays() {
        markerController.clear()
        polylineController.clear()
        polygonController.clear()
        circleController.clear()
    }

    override suspend fun compositionMarkers(data: List<MarkerState>) = markerController.add(data)

    override suspend fun updateMarker(state: MarkerState) = markerController.update(state)

    override fun hasMarker(state: MarkerState): Boolean = this.markerController.markerManager.hasEntity(state.id)

    override fun hasPolyline(state: PolylineState): Boolean =
        this.polylineController.polylineManager
            .hasEntity(state.id)

    override fun hasPolygon(state: PolygonState): Boolean = this.polygonController.polygonManager.hasEntity(state.id)

    override fun hasCircle(state: CircleState): Boolean = this.circleController.circleManager.hasEntity(state.id)

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
        markerController.animateStartListener = listener
    }

    override fun setOnMarkerAnimateEnd(listener: OnMarkerEventHandler?) {
        markerController.animateEndListener = listener
    }

    override fun setOnMarkerClickListener(listener: OnMarkerEventHandler?) {
        markerController.clickListener = listener
    }

    override suspend fun compositionCircles(data: List<CircleState>) = circleController.add(data)

    override suspend fun updateCircle(state: CircleState) = circleController.update(state)

    override fun setOnCircleClickListener(listener: OnCircleEventHandler?) {
        this.circleController.clickListener = listener
    }

    override suspend fun compositionPolylines(data: List<PolylineState>) = polylineController.add(data)

    override suspend fun updatePolyline(state: PolylineState) = polylineController.update(state)

    override suspend fun compositionPolygons(data: List<PolygonState>) = polygonController.add(data)

    override suspend fun updatePolygon(state: PolygonState) = polygonController.update(state)

    init {
        setupListeners()
        registerController(markerController)
        registerController(polygonController)
        registerController(polylineController)
        registerController(circleController)
    }

    fun setupListeners() {
        holder.mapView.camera.removeListener(this)
        holder.mapView.camera.addListener(this)
        holder.mapView.gestures.tapListener = this
        holder.mapView.gestures.longPressListener = this
    }

    override fun moveCamera(position: MapCameraPositionImpl) {
        val camera = this.holder.mapView.camera
        val adjustCameraUpdate =
            MapCameraUpdateFactory.lookAt(
                GeoPointImpl.from(position.position).toGeoCoordinates().toUpdate(),
                GeoOrientation(position.bearing, position.tilt).toUpdate(),
                MapMeasure(MapMeasure.Kind.ZOOM_LEVEL, position.zoom + ZOOM_ADJUST_VALUE),
            )

        camera.applyUpdate(adjustCameraUpdate)
    }

    override fun animateCamera(
        position: MapCameraPositionImpl,
        durationMills: Long,
    ) {
        val camera = this.holder.mapView.camera

//      bowFactor > 0: 最初にズームアウト → 到達時にズームイン
//      bowFactor < 0: 最初にズームイン → 到達時にズームアウト（ややレア）
//      bowFactor = 0: 常に同じズーム（直線的）
        val bowFactor = 1.0
        val animation =
            MapCameraAnimationFactory.flyTo(
                GeoPointImpl.from(position.position).toGeoCoordinates().toUpdate(),
                GeoOrientation(position.bearing, position.tilt).toUpdate(),
                MapMeasure(MapMeasure.Kind.ZOOM_LEVEL, position.zoom + ZOOM_ADJUST_VALUE),
                bowFactor,
                Duration.ofMillis(durationMills),
            )
        coroutine.launch {
            camera.startAnimation(animation) { animState ->
                when (animState) {
                    // Do nothing here
                    AnimationState.STARTED ->
                        cameraMoveStartCallback?.invoke(
                            getMapCameraPosition(holder.mapView.camera.state)!!,
                        )
                    AnimationState.COMPLETED -> cameraMoveEndCallback?.invoke(position)
                    AnimationState.CANCELLED ->
                        cameraMoveEndCallback?.invoke(
                            getMapCameraPosition(holder.mapView.camera.state)!!,
                        )
                }
            }
        }
    }

    override fun onMapCameraUpdated(cameraState: MapCamera.State) {
        backCoroutine.launch {
            getMapCameraPosition(cameraState)?.let { mapCameraPosition ->
                cameraMoveCallback?.invoke(mapCameraPosition)
                notifyMapCameraPosition(mapCameraPosition)
            }
        }
    }

    private fun getMapCameraPosition(cameraState: MapCamera.State): MapCameraPositionImpl? {
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

            val distanceToTargetInMeters =
                GeoOrientation(
                    cameraState.orientationAtTarget.bearing,
                    cameraState.orientationAtTarget.tilt,
                )
            val zoomLevel = 0.0
            val correctCameraState =
                MapCamera.State(
                    cameraState.targetCoordinates,
                    distanceToTargetInMeters,
                    zoomLevel,
                    cameraState.zoomLevel - ZOOM_ADJUST_VALUE,
                )
            val adjustedMapCameraPosition = correctCameraState.toMapCameraPosition()
            return@let adjustedMapCameraPosition.copy(visibleRegion = visibleRegion)
        }
    }

    override fun onTap(point: Point2D) {
        val touchPosition = this.getGeoPointFromPoint(point) ?: return

        markerController.find(touchPosition)?.let { entity ->
            markerController.dispatchClick(entity.state)
            return
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
                markerController.find(position)?.let { entity ->
                    if (entity.state.draggable) {
                        entity.state.position = position
                        markerController.selectedMarker = entity
                        markerController.dispatchDragStart(entity.state)
                        return
                    }
                }
                mapLongClickCallback?.invoke(position)
            }

            GestureState.UPDATE.value -> {
                markerController.selectedMarker?.also { selected ->
                    holder.mapView.viewToGeoCoordinates(point)?.also { coordinates ->
                        selected.marker?.coordinates = coordinates
                        selected.state.position = coordinates.toGeoPoint()
                    }
                    markerController.dispatchDrag(selected.state)
                }
            }

            GestureState.END.value, GestureState.CANCEL.value -> {
                markerController.selectedMarker?.also { selected ->
                    markerController.markerManager.updateEntity(selected)
                    markerController.dispatchDragEnd(selected.state)
                    markerController.selectedMarker = null
                    markerController.selectedMarker = null
                }
            }
        }
    }

    private fun getGeoPointFromPoint(point: Point2D): GeoPointImpl? =
        holder.mapView
            .viewToGeoCoordinates(point)
            ?.toGeoPoint()

    override fun setOnPolylineClickListener(listener: OnPolylineEventHandler?) {
        polylineController.clickListener = listener
    }

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
}
