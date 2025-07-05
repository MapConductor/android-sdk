package com.mapconductor.here

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import com.here.sdk.animation.AnimationState
import com.here.sdk.core.GeoOrientation
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
import com.mapconductor.core.marker.MarkerRenderer
import com.mapconductor.core.marker.MarkerRendererFactory
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.projection.WebMercator
import com.mapconductor.here.marker.DefaultHereMapMarkerRenderer
import com.mapconductor.here.marker.HereMapMarkerRenderer
import com.mapconductor.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
    override val hexGeocell: HexGeocell =
        HexGeocell(
            projection = WebMercator,
            baseHexSideLength = 100000, // 100km - 中ズームレベルに適した値
        ),
    private val overlayManagerFactory: MarkerRendererFactory<MapMarker> = DefaultHereMapMarkerRenderer(),
    private val markerRenderer: MarkerRenderer<MapMarker> = HereMapMarkerRenderer(
        holder = holder,
        coroutine = coroutine,
    ),
) : BaseMapViewController<MapCamera.State, MapMarker>(),
    IHereMapViewController,
    MapCameraListener,
    TapListener,
    LongPressListener {
    private var selectedMarker: MarkerEntity<MapMarker>? = null

    override val markerOverlayManager by lazy {
        return@lazy overlayManagerFactory.create(
            hexGeocell = hexGeocell,
            onIconAdd = markerRenderer::addIcons,
            onIconRemove = markerRenderer::removeIcons,
            onIconChange = markerRenderer::changeIcons,
            onAnimate = { entity ->
                when (entity.state.animation) {
                    MarkerAnimation.Drop -> animateMarkerDrop(entity)
                    MarkerAnimation.Bounce -> animateMarkerBounce(entity)
                    else -> throw IllegalArgumentException("No animation is available: ${entity.state.animation}")
                }
            }
        )
    }

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
