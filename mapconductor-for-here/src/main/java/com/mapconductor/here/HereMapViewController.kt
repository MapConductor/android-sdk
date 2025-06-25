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
import com.mapconductor.core.MarkerManager
import com.mapconductor.core.calculateZIndex
import com.mapconductor.core.controller.BaseMapViewController
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.controller.MarkerModifyParams
import com.mapconductor.core.controller.MarkerOverlayManagerImpl
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewHolder
import com.mapconductor.core.map.MapViewState.MoveCameraCallback
import com.mapconductor.core.marker.MarkerAnimation
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.projection.WebMercator
import com.mapconductor.settings.Settings
import kotlin.math.min
import android.os.SystemClock
import android.view.animation.BounceInterpolator
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.here.sdk.core.GeoCoordinates

interface IHereMapViewController : MapViewController {
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
) : BaseMapViewController<MapCamera.State>(),
    IHereMapViewController,
    MapCameraListener,
    TapListener,
    LongPressListener {
    private data class SelectedMarker(
        val state: MarkerState,
        val overlay: MapMarker,
    )

    private var selectedMarker: SelectedMarker? = null
    override val markerOverlayManager =
        MarkerOverlayManagerImpl<MapMarker>(
            markerManager = MarkerManager(HexGeocell(WebMercator, 1)),
            onRemove = { removes ->
                withContext(coroutine.coroutineContext) {
                    val markers: List<MapMarker> = removes.map { params -> params.marker }
                    holder.mapView.mapScene.removeMapMarkers(markers)
                }
            },
            onAdd = { newMarkers ->
                val markers = withContext(coroutine.coroutineContext) {
                    newMarkers.map { params ->
                        val marker =
                            MapMarker(
                                GeoPoint.from(params.state.position).toGeoCoordinates(),
                                params.icon.toMapImage(),
                                params.icon.toAnchor2D(),
                            ).apply {
                                drawOrder = calculateZIndex(params.state.position).toInt()
                                metadata =
                                    Metadata().apply {
                                        setString("id", params.state.id)
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
                withContext(coroutine.coroutineContext) {
                    changes.map { params ->
                        // TODO: アイコンに変更があったかどうかを比較
                        params.marker.image = params.icon.toMapImage()
                        params.marker.coordinates = GeoPoint.from(params.state.position).toGeoCoordinates()
                        params.marker.anchor = params.icon.toAnchor2D()

                        // Hereはマーカーを再作成しなくてよいので、同じマーカーのインスタンスを返す
                        params.marker
                    }
                }
            },
            onIconChange = { marker, icon ->
                withContext(coroutine.coroutineContext) {
                    marker.image = icon.toMapImage()
                }
            },
            onAnimation = {
                when (it.state.animation) {
                    MarkerAnimation.Drop -> this.markerDropAnimation(it)
                    MarkerAnimation.Bounce -> this.markerBounceAnimation(it)
                    else -> { /* Do nothing here */ }
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

    private fun markerDropAnimation(params: MarkerModifyParams<MapMarker>) {
        val markerGeo = params.marker.coordinates
        val interpolator = LinearInterpolator()
        val duration = Settings.Default.markerDropAnimateDuration

        val screenPoint = holder.mapView.geoToViewCoordinates(markerGeo) ?: return
        val startPoint = Point2D(screenPoint.x, 0.0)

        markerAnimateStartListener?.invoke(params.state)

        flow {
            val startTime = SystemClock.uptimeMillis()
            while (true) {
                val elapsed = SystemClock.uptimeMillis() - startTime
                val t = min(1f, elapsed.toFloat() / duration)
                emit(interpolator.getInterpolation(t))
                if (t >= 1f) break
                delay(16)
            }
        }.onEach { t ->
            val startGeo = holder.mapView.viewToGeoCoordinates(startPoint) ?: return@onEach
            val lat = t * markerGeo.latitude + (1 - t) * startGeo.latitude
            val lon = t * markerGeo.longitude + (1 - t) * startGeo.longitude
            params.marker.coordinates = GeoCoordinates(lat, lon)
        }.onCompletion {
            params.marker.coordinates = markerGeo
            params.state.animation = null
            markerAnimateEndListener?.invoke(params.state)
        }.launchIn(coroutine)
    }

    private fun markerBounceAnimation(params: MarkerModifyParams<MapMarker>) {
        val markerGeo = params.marker.coordinates
        val duration = Settings.Default.markerBounceAnimateDuration
        val interpolator: Interpolator = BounceInterpolator()

        val screenPoint = holder.mapView.geoToViewCoordinates(markerGeo) ?: return
        val startPoint = Point2D(screenPoint.x, screenPoint.y - 200)

        markerAnimateStartListener?.invoke(params.state)

        flow {
            val startTime = SystemClock.uptimeMillis()
            while (true) {
                val elapsed = SystemClock.uptimeMillis() - startTime
                val t = interpolator.getInterpolation(min(1f, elapsed.toFloat() / duration))
                emit(t)
                if (t >= 1f) break
                delay(16)
            }
        }.onEach { t ->
            val startGeo = holder.mapView.viewToGeoCoordinates(startPoint) ?: return@onEach
            val lat = t * markerGeo.latitude + (1 - t) * startGeo.latitude
            val lon = markerGeo.longitude
            params.marker.coordinates = GeoCoordinates(lat, lon)
        }.onCompletion {
            params.marker.coordinates = markerGeo
            params.state.animation = null
            markerAnimateEndListener?.invoke(params.state)
        }.launchIn(coroutine)
    }

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
        cameraMoveListener?.let {
            coroutine.launch {
                it(cameraState)
            }
        }
    }

    override fun onTap(point: Point2D) {
        val position = this.getGeoPointFromPoint(point) ?: return

        val state =
            this.findNearestMarker(
                position = position,
                tolerance = Settings.Default.tapTolerance,
            )
        if (state != null) {
            markerClickListener?.let {
                coroutine.launch {
                    it(state)
                }
            }
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
                val state =
                    this.findNearestMarker(
                        position = position,
                        tolerance = Settings.Default.tapTolerance,
                    ) ?: return

                val overlay = this.markerOverlayManager.markerManager.getMarker(state.id) ?: return

                state.position = position
                selectedMarker =
                    SelectedMarker(
                        state = state,
                        overlay = overlay,
                    )

                // Suppress the recomposition for the position property
                setDraggingState(state, true)

                markerDragStartListener?.let {
                    coroutine.launch { it.invoke(state) }
                }
            }

            GestureState.UPDATE.value -> {
                selectedMarker?.also { selected ->
                    holder.mapView.viewToGeoCoordinates(point)?.also { coordinates ->
                        selected.overlay.coordinates = coordinates
                        selected.state.position = coordinates.toGeoPoint()
                    }
                    markerDragListener?.let {
                        coroutine.launch { it.invoke(selected.state) }
                    }
                }
            }

            GestureState.END.value, GestureState.CANCEL.value -> {
                selectedMarker?.also { selected ->
                    markerOverlayManager.markerManager.updateState(selected.state)

                    // Restore the recomposition for the position property
                    setDraggingState(selected.state, false)

                    markerDragEndListener?.let {
                        coroutine.launch { it.invoke(selected.state) }
                    }
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
    ): MarkerState? {
        val zoom = holder.mapView.camera.state.zoomLevel
        val acceptDPI = tolerance.value.toFloat() * holder.mapView.context.resources.displayMetrics.density

        return findMarkerFromPoint(
            markerOverlayManager = markerOverlayManager,
            position = position,
            zoom = zoom,
            tolerance = acceptDPI.toDouble(),
        )
    }
}
