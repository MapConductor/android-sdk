package com.mapconductor.maplibre

import androidx.compose.ui.geometry.Offset
import com.mapconductor.core.circle.CircleEvent
import com.mapconductor.core.controller.BaseMapViewController
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.VisibleRegion
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.polygon.PolygonEvent
import com.mapconductor.core.polyline.PolylineEvent
import com.mapconductor.maplibre.marker.MapLibreMarkerController
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.gestures.MoveGestureDetector
import org.maplibre.android.maps.MapLibreMap
import android.graphics.PointF
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


typealias MapLibreDesignTypeChangeHandler = (MapLibreMapDesignType) -> Unit

class MapLibreViewControllerImpl(
    override val holder: MapLibreViewHolder,
    private val markerController: MapLibreMarkerController,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
    val backCoroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : BaseMapViewController(),
    MapLibreViewController,
    MapLibreMap.OnMapClickListener,
    MapLibreMap.OnMapLongClickListener,
    MapLibreMap.OnMoveListener,
    MapLibreMap.OnCameraMoveListener,
    MapLibreMap.OnCameraIdleListener {

    init {
        holder.map.getStyle { style ->
            // Marker
            style.addSource(markerController.renderer.markerLayer.source)
            style.addLayer(markerController.renderer.markerLayer.layer)
            style.addSource(markerController.renderer.dragLayer.source)
            style.addLayer(markerController.renderer.dragLayer.layer)
        }
        setupListeners()
        registerController(markerController)
//        registerController(polygonController)
//        registerController(polylineController)
//        registerController(circleController)
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
        TODO("Not yet implemented")
    }

    override fun moveCamera(
        position: MapCameraPositionImpl,
        listener: MapViewState.MoveCameraCallback?
    ) {
        coroutine.launch {
            val cameraUpdate = CameraUpdateFactory
                .newCameraPosition(position.toCameraPosition())
            holder.map.moveCamera(cameraUpdate)
            listener?.onComplete()
        }
    }

    override fun animateCamera(
        position: MapCameraPositionImpl,
        duration: Long,
        listener: MapViewState.MoveCameraCallback?
    ) {
        coroutine.launch {
            val cameraUpdate = CameraUpdateFactory
                .newCameraPosition(position.toCameraPosition())
            holder.map.animateCamera(cameraUpdate, duration.toInt())
            listener?.onComplete()
        }
    }

    private var mapDesignType: MapLibreMapDesignType = MapLibreMapDesign.DemoTiles

    private var mapDesignTypeChangeListener: MapLibreDesignTypeChangeHandler? = null

    override fun setMapDesignType(value: MapLibreMapDesignType) {
        coroutine.launch {
            holder.map.setStyle(value.styleJsonURL)
        }
    }

    override fun setMapDesignTypeChangeListener(listener: MapLibreDesignTypeChangeHandler) {
        mapDesignTypeChangeListener = listener
        listener(mapDesignType)
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

    override fun hasMarker(state: MarkerState): Boolean = this.markerController.markerManager.hasEntity(state.id)
    override fun onMapClick(point: LatLng): Boolean {
        val touchPosition = point.toGeoPoint()

        markerController.find(touchPosition)?.let { entity ->
            markerController.clickListener?.invoke(entity.state)
            return true
        }

//        circleController.find(touchPosition)?.let { entity ->
//            val event =
//                CircleEvent(
//                    state = entity.state,
//                    clicked = touchPosition,
//                )
//            circleController.clickListener?.invoke(event)
//            return true
//        }
//
//        polylineController.findWithClosestPoint(touchPosition)?.let { hitResult ->
//            val event =
//                PolylineEvent(
//                    state = hitResult.entity.state,
//                    clicked = hitResult.closestPoint,
//                )
//            coroutine.launch {
//                polylineController.clickListener?.invoke(event)
//            }
//            return true
//        }
//
//        polygonController.find(touchPosition)?.let { polygonEntity ->
//            val event =
//                PolygonEvent(
//                    state = polygonEntity.state,
//                    clicked = touchPosition,
//                )
//            polygonController.clickListener?.invoke(event)
//            return true
//        }

        mapClickCallback?.invoke(touchPosition)
        return true
    }

    override fun onMapLongClick(point: LatLng): Boolean {
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

    override fun onMoveBegin(detector: MoveGestureDetector) {
        // Do nothing here
    }

    override fun onMove(detector: MoveGestureDetector) {
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
        }
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
}
