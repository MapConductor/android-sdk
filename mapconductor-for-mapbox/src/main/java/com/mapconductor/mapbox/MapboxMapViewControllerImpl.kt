package com.mapconductor.mapbox

import MapboxMapViewController
import androidx.compose.ui.geometry.Offset
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraChanged
import com.mapbox.maps.CameraChangedCallback
import com.mapbox.maps.ScreenCoordinate
import com.mapbox.maps.StyleLoaded
import com.mapbox.maps.StyleLoadedCallback
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
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.circle.CircleEvent
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.circle.OnCircleEventHandler
import com.mapconductor.core.controller.BaseMapViewController
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.VisibleRegion
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.polygon.OnPolygonEventHandler
import com.mapconductor.core.polygon.PolygonEvent
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.core.polyline.OnPolylineEventHandler
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.mapbox.circle.MapboxCircleController
import com.mapconductor.mapbox.marker.MapboxMarkerController
import com.mapconductor.mapbox.polygon.MapboxPolygonConductor
import com.mapconductor.mapbox.polyline.MapboxPolylineController
import android.animation.Animator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

typealias MapboxMapDesignTypeChangeHandler = (MapboxDesignType) -> Unit

internal class MapboxMapViewControllerImpl(
    override val holder: MapboxMapViewHolder,
    private val markerController: MapboxMarkerController,
    private val polylineController: MapboxPolylineController,
    private val polygonController: MapboxPolygonConductor,
    private val circleController: MapboxCircleController,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
    val backCoroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : BaseMapViewController(),
    MapboxMapViewController,
    CameraChangedCallback,
    StyleLoadedCallback,
    OnMapClickListener,
    OnMapLongClickListener,
    OnMoveListener {
    init {
        holder.map.getStyle { style ->
            // Circle
            style.addSource(circleController.renderer.layer.source)
            style.addLayer(circleController.renderer.layer.layer)

            // Polygon
            style.addSource(polygonController.polygonOverlay.layer.source)
            style.addLayer(polygonController.polygonOverlay.layer.layer)
            style.addSource(polygonController.polylineOverlay.layer.source)
            style.addLayer(polygonController.polylineOverlay.layer.layer)

            // Polyline
            style.addSource(polylineController.renderer.layer.source)
            style.addLayer(polylineController.renderer.layer.layer)

            // Marker
            style.addSource(markerController.renderer.markerLayer.source)
            style.addLayer(markerController.renderer.markerLayer.layer)
            style.addSource(markerController.renderer.dragLayer.source)
            style.addLayer(markerController.renderer.dragLayer.layer)
        }
        setupListeners()
        registerController(markerController)
    }

    fun setupListeners() {
        holder.map.subscribeCameraChanged(this)
        holder.map.subscribeStyleLoaded(this)
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

    override fun run(cameraChanged: CameraChanged) {
        coroutine.launch {
            getMapCameraPosition(cameraChanged)?.let { mapCameraPosition ->
                backCoroutine.launch {
                    notifyMapCameraPosition(mapCameraPosition)
                }
            }
        }
    }

    override fun hasMarker(state: MarkerState): Boolean = this.markerController.markerManager.hasEntity(state.id)

    override fun hasPolyline(state: PolylineState): Boolean =
        this.polylineController.polylineManager
            .hasEntity(state.id)

    override fun hasPolygon(state: PolygonState): Boolean =
        this.polygonController.polygonOverlay.polygonManager
            .hasEntity(state.id)

    override fun hasCircle(state: CircleState): Boolean = this.circleController.circleManager.hasEntity(state.id)

    private fun getMapCameraPosition(cameraChanged: CameraChanged): MapCameraPosition? {
        val options = cameraChanged.toMapCameraPosition()
        val camera = holder.map.cameraState.toMapCameraPosition()
        val currentBox = holder.map.coordinateBoundsForCameraUnwrapped(options)

        val mapSize = holder.map.getSize()
        val mapWidth = ResourceProvider.dpToPx(mapSize.width).toFloat()
        val mapHeight = ResourceProvider.dpToPx(mapSize.height).toFloat()
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

        val bounds = currentBox.toGeoRectBounds()
        val visibleRegion =
            VisibleRegion(
                bounds = bounds,
                nearLeft = nearLeft,
                nearRight = nearRight,
                farLeft = farLeft,
                farRight = farRight,
            )
        val mapCameraPosition =
            camera.copy(
                visibleRegion = visibleRegion,
            )
        return mapCameraPosition
    }

    override fun moveCamera(
        position: MapCameraPosition,
        listener: MapViewState.MoveCameraCallback?,
    ) {
        val cameraOptions = position.toCameraOptions()
        coroutine.launch {
            holder.map.setCamera(cameraOptions)
        }
        listener?.onComplete()
    }

    override fun animateCamera(
        position: MapCameraPosition,
        duration: Long,
        listener: MapViewState.MoveCameraCallback?,
    ) {
        val targetCamera = position.toCameraOptions()

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
                    listener?.onComplete()
                }

                override fun onAnimationCancel(animation: Animator) {
                    listener?.onComplete()
                }

                override fun onAnimationRepeat(animation: Animator) {
                    // Do nothing here
                }
            }

        coroutine.launch {
            holder.map.flyTo(
                cameraOptions = targetCamera,
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

        circleController.find(touchPosition)?.let { entity ->
            val event =
                CircleEvent(
                    state = entity.state,
                    clicked = touchPosition,
                )
            circleController.clickListener?.invoke(event)
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

    override fun setOnPolylineClickListener(listener: OnPolylineEventHandler?) {
        polylineController.clickListener = listener
    }

    override fun setOnPolygonClickListener(listener: OnPolygonEventHandler?) {
        polygonController.clickListener = listener
    }

    private var mapDesignType: MapboxDesignType = MapboxMapDesign.Standard

    private var mapDesignTypeChangeListener: MapboxMapDesignTypeChangeHandler? = null

    override fun setMapDesignType(value: MapboxDesignType) {
        coroutine.launch {
            holder.mapView.mapboxMap.loadStyle(value.getValue())
        }
    }

    override fun setMapDesignTypeChangeListener(listener: MapboxMapDesignTypeChangeHandler) {
        mapDesignTypeChangeListener = listener
        listener(mapDesignType)
    }

    override fun run(styleLoaded: StyleLoaded) {
        mapLoadedCallback?.invoke()
        mapLoadedCallback = null

        holder.map.style?.toMapDesignType()?.let { mapDesignType ->
            this@MapboxMapViewControllerImpl.mapDesignType = mapDesignType
            mapDesignTypeChangeListener?.invoke(mapDesignType)
        }
    }
}
