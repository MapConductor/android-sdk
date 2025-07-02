package com.mapconductor.here

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import com.here.sdk.animation.AnimationState
import com.here.sdk.core.GeoOrientation
import com.here.sdk.core.Metadata
import com.here.sdk.core.Point2D
import com.here.sdk.gestures.GestureState
import com.here.sdk.gestures.LongPressListener
import com.here.sdk.gestures.TapListener
import com.here.sdk.mapview.HereMap
import com.here.sdk.mapview.MapCamera
import com.here.sdk.mapview.MapCameraAnimationFactory
import com.here.sdk.mapview.MapCameraListener
import com.here.sdk.mapview.MapMarker
import com.here.sdk.mapview.MapMeasure
import com.here.sdk.mapview.MapView
import com.here.time.Duration
import com.mapconductor.core.calculateZIndex
import com.mapconductor.core.controller.BaseMapViewController
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewHolder
import com.mapconductor.core.map.MapViewState.MoveCameraCallback
import com.mapconductor.core.marker.MarkerAnimation
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerOverlayManagerImpl
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.projection.WebMercator
import com.mapconductor.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface IHereMapViewController : MapViewController<MapMarker> {
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
    override val holder: MapViewHolder<MapView, HereMap>,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
    override val hexCell: HexGeocell =
        HexGeocell(
            projection = WebMercator,
            baseHexSideLength = 100000, // 100km - 中ズームレベルに適した値
        ),
) : BaseMapViewController<MapCamera.State, MapMarker>(),
    IHereMapViewController,
    MapCameraListener,
    TapListener,
    LongPressListener {
    private var selectedMarker: MarkerEntity<MapMarker>? = null
    override val markerOverlayManager =
        MarkerOverlayManagerImpl<MapMarker>(
            markerManager = MarkerManager(hexCell),
            onRemove = { removes ->
                coroutine.launch {
                    val markers: List<MapMarker> = removes.map { params -> params.marker }
                    holder.mapView.mapScene.removeMapMarkers(markers)
                }
            },
            onAdd = { newMarkers ->
                val markers =
                    withContext(coroutine.coroutineContext) {
                        newMarkers.map { params ->
                            val marker =
                                MapMarker(
                                    GeoPoint.from(params.first.position).toGeoCoordinates(),
                                    params.second.toMapImage(),
                                    params.second.toAnchor2D(),
                                ).apply {
                                    drawOrder = calculateZIndex(params.first.position).toInt()
                                    metadata =
                                        Metadata().apply {
                                            setString("id", params.first.id)
                                        }
                                }
                            return@map marker
                        }
                    }

                coroutine.launch {
                    holder.mapView.mapScene.addMapMarkers(markers)
                }
                return@MarkerOverlayManagerImpl markers
            },
            onChange = { changes ->
                changes.map { params ->
                    if (params.entity.state.position != params.prevEntity.state.position) {
                        params.entity.marker.coordinates =
                            params.entity.state.position
                                .toGeoCoordinates()
                    }
                    if (params.entity.state.icon != params.prevEntity.state.icon) {
                        params.entity.marker.image = params.bitmapIcon.toMapImage()
                        params.entity.marker.anchor = params.bitmapIcon.toAnchor2D()
                    }

                    // Hereはマーカーを再作成しなくてよいので、同じマーカーのインスタンスを返す
                    params.entity.marker
                }
            },
            onPostProcess = {
                // Do nothing here
            },
            onAnimate = {
                when (it.state.animation) {
                    MarkerAnimation.Drop -> this.animateMarkerDrop(it)
                    MarkerAnimation.Bounce -> this.animateMarkerBounce(it)
                    else -> throw IllegalArgumentException("No animation is available: ${it.state.animation}")
                }
            },
        )

    override suspend fun addMarkers(markerList: List<MarkerState>) = markerOverlayManager.addMarkers(markerList)

    override suspend fun clearOverlays() = markerOverlayManager.clearOverlays()

    override suspend fun updateMarker(state: MarkerState) = markerOverlayManager.updateMarker(state)

    override fun toScreenOffset(position: IGeoPoint): Offset? {
        val result =
            holder.mapView.geoToViewCoordinates(
                GeoPoint.from(position).toGeoCoordinates(),
            ) ?: return null

        return Offset(
            x = result.x.toFloat(),
            y = result.y.toFloat(),
        )
    }

    override suspend fun fromScreenOffset(offset: Offset): GeoPoint? =
        holder.mapView
            .viewToGeoCoordinates(
                Point2D(offset.x.toDouble(), offset.y.toDouble()),
            )?.toGeoPoint()

    init {
        setupListeners()
    }

/*
    private fun markerDropAnimation(params: MarkerModifyParams<MapMarker>) {
        val markerLatLng = params.marker.coordinates
        val interpolator = LinearInterpolator()
        val markerPoint = holder.mapView.geoToViewCoordinates(markerLatLng) ?: return
        val startPoint = Point2D(markerPoint.x, 0.0)
        val duration = Settings.Default.markerDropAnimateDuration

        markerAnimateStartListener?.invoke(params.state)

        flow{
            val startTime = SystemClock.uptimeMillis()
            while (true){
                val elapsed = SystemClock.uptimeMillis() - startTime
                val t = min(1f, elapsed.toFloat() / duration)
                emit(interpolator.getInterpolation(t))
                if (t >= 1f) break
                delay(16)
            }
        }.onEach { t ->
            val startLatLng = holder.mapView.viewToGeoCoordinates(startPoint) ?: return@onEach
            val lng = t * markerLatLng.longitude + (1 - t) * startLatLng.longitude
            val lat = t * markerLatLng.latitude + (1 - t) * startLatLng.latitude
            params.marker.coordinates = GeoCoordinates(lat, lng)
        }.onCompletion {
            params.marker.coordinates = markerLatLng
            params.state.animation = null
            markerAnimateEndListener?.let { it(params.state) }
        }.launchIn(coroutine)
    }

    private fun markerBounceAnimation(params: MarkerModifyParams<MapMarker>) {
        val startTime = SystemClock.uptimeMillis()
        val duration = Settings.Default.markerBounceAnimateDuration
        val interpolator: Interpolator = BounceInterpolator()
        val markerLatLng = params.marker.coordinates
        val markerPoint = holder.mapView.geoToViewCoordinates(markerLatLng) ?: return
        val startPoint = Point2D(0.0, -200.0)

        markerAnimateStartListener?.let { it(params.state) }

        flow {
            while (true) {
                val elapsed = SystemClock.uptimeMillis() - startTime
                val t = interpolator.getInterpolation(min(1f, elapsed.toFloat() / duration))
                emit(t)
                if (t >= 1f) break
                delay(16L)
            }
        }.onEach { t ->
            val startLatLng = holder.mapView.viewToGeoCoordinates(startPoint) ?: return@onEach
            val lng = markerLatLng.longitude
            val lat = t * markerLatLng.latitude + (1 - t) * startLatLng.latitude
            params.marker.coordinates = GeoCoordinates(lat, lng)
        }.onCompletion {
            params.marker.coordinates = markerLatLng
            params.state.animation = null

            markerAnimateEndListener?.let { it(params.state) }
        }.launchIn(coroutine)
    }
*/
    private fun setupListeners() {
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
        camera.applyUpdate(
            dstPosition.toMapCameraUpdate(),
        )
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
                MapMeasure(MapMeasure.Kind.ZOOM_LEVEL, dstPosition.zoom),
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
        cameraMoveListener?.invoke(cameraState)
    }

    override fun onTap(point: Point2D) {
        val position = this.getGeoPointFromPoint(point) ?: return

        val entity =
            this.findNearestMarker(
                position = position,
                tolerance = Settings.Default.tapTolerance,
            )
        if (entity != null) {
            markerClickListener?.invoke(entity.state)
            return
        }

        // TODO: Implement click handling for other overlays

        // If no overlay is processed, process the tap as onMapClick
        mapClickListener?.let { it(position) }
    }

    override fun onLongPress(
        gesture: GestureState,
        point: Point2D,
    ) {
        val position = this.getGeoPointFromPoint(point) ?: return

        when (gesture.value) {
            GestureState.BEGIN.value -> {
                val entity =
                    this.findNearestMarker(
                        position = position,
                        tolerance = Settings.Default.tapTolerance,
                    ) ?: return

                entity.state.position = position
                selectedMarker = entity

                // Suppress the recomposition for the position property
                setDraggingState(entity.state, true)

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
                    setDraggingState(selected.state, false)

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

    private fun findNearestMarker(
        position: IGeoPoint,
        tolerance: Dp,
    ): MarkerEntity<MapMarker>? {
        val zoom = holder.mapView.camera.state.zoomLevel
        val acceptDPI = tolerance.value.toFloat() * holder.mapView.context.resources.displayMetrics.density

        return findMarkerFromPoint(
            position = position,
            zoom = zoom,
            tolerance = acceptDPI.toDouble(),
        )
    }

    override fun setMarkerPosition(
        markerEntity: MarkerEntity<MapMarker>,
        position: GeoPoint,
    ) {
        markerEntity.marker.coordinates = position.toGeoCoordinates()
    }

    override fun clearPolyline() {
        TODO("Not yet implemented")
    }

    override fun drawPolyline(geoPoints: List<IGeoPoint>) {
        TODO("Not yet implemented")
    }
}
