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
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.controller.MarkerOverlayManager
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.map.MapViewHolder
import com.mapconductor.core.map.MapViewState.MoveCameraCallback
import com.mapconductor.core.map.OnCameraMoveHandler
import com.mapconductor.core.map.OnMapClickHandler
import com.mapconductor.core.marker.MarkerEntry
import com.mapconductor.core.projection.WebMercator
import com.mapconductor.core.spherical.haversineDistance
import com.mapconductor.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.pow

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

internal class HereMapController(
    override val holder: MapViewHolder<MapView, HereMap>,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
    val onCameraMove: (OnCameraMoveHandler<MapCamera.State>)? = null,
    val onMapClick: OnMapClickHandler? = null,
) : IHereMapViewController,
    MapCameraListener,
    TapListener,
    LongPressListener {
    private val markerOverlayManager =
        MarkerOverlayManager<MapMarker>(
            markerManager = MarkerManager(HexGeocell(WebMercator, 1)),
            onRemove = { removes ->
                val markers: List<MapMarker> = removes.map { params -> params.marker }
                holder.mapView.mapScene.removeMapMarkers(markers)
            },
            onAdd = { newMarkers ->
                val markers =
                    newMarkers.map { params ->
                        val marker =
                            MapMarker(
                                GeoPoint.from(params.entry.state.position).toGeoCoordinates(),
                                params.icon.toMapImage(),
                                params.icon.toAnchor2D(),
                            ).apply {
                                drawOrder = calculateZIndex(params.entry.state.position).toInt()
                                metadata =
                                    Metadata().apply {
                                        setString("id", params.entry.state.id)
                                    }
                            }
                        return@map marker
                    }

                holder.mapView.mapScene.addMapMarkers(markers)
                return@MarkerOverlayManager markers
            },
            onChange = { changes ->
                changes.forEach { params ->
                    // TODO: アイコンに変更があったかどうかを比較
                    params.marker.image = params.icon.toMapImage()
                    params.marker.coordinates = GeoPoint.from(params.entry.state.position).toGeoCoordinates()
                    params.marker.anchor = params.icon.toAnchor2D()
                }
            },
        )

    override suspend fun addMarkers(markerList: List<MarkerEntry>) = markerOverlayManager.addMarkers(markerList)

    override suspend fun clearOverlays() = markerOverlayManager.clearOverlays()

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
        onCameraMove?.let {
            coroutine.launch {
                it(cameraState)
            }
        }
    }

    override fun onTap(touchPoint: Point2D) {
        val touchPosition = this.getGeoPointFromPoint(touchPoint) ?: return

        val markerEntry = this.findMarkerFromPoint(
            position = touchPosition,
            tolerance = Settings.Default.tapTolerance,
        )
        if (markerEntry != null) {
            markerEntry.handlers.onClick?.let {
                coroutine.launch {
                    it(markerEntry.state)
                }
            }
            return
        }

        // TODO: Implement click handling for other overlays

        // If no overlay is processed, process the tap as onMapClick
        onMapClick?.let { it(touchPosition) }
    }

    private fun zoomToIdPrefixLevel(zoom: Double): Int =
        when {
            zoom <= 5 -> 2 // 数10km 単位でまとめる
            zoom <= 8 -> 3 // 数km 単位
            zoom <= 10 -> 4 // 500m ～ 1km
            zoom <= 12 -> 5 // 100～300m
            zoom <= 14 -> 6 // 50～100m
            zoom <= 16 -> 7 // 20～50m
            zoom <= 18 -> 8 // 5～20m
            else -> 9 // ~1mまで細分化
        }

    private fun hereZoomToMetersPerPixel(zoom: Double): Double {
        val earthCircumference = 40075016.686
        val tileSize = 256
        return earthCircumference / (tileSize * 2.0.pow(zoom))
    }

    override fun onLongPress(
        state: GestureState,
        point: Point2D,
    ) {
        TODO("Not yet implemented")
    }

    private fun getGeoPointFromPoint(point: Point2D): GeoPoint? =
        holder.mapView
            .viewToGeoCoordinates(point)
            ?.toGeoPoint()

    private fun findMarkerFromPoint(
        position: IGeoPoint,
        tolerance: Dp,
    ): MarkerEntry? {
        val zoom = holder.mapView.camera.state.zoomLevel
        val meterInMapPixel = hereZoomToMetersPerPixel(zoom)
        val acceptDPI = tolerance.value.toFloat() * holder.mapView.context.resources.displayMetrics.density
        val radius = acceptDPI * meterInMapPixel

        val entry = markerOverlayManager.markerManager.findNearest(position) ?: return null

        val distance = haversineDistance(position, entry.point)
        return if (distance <= radius) {
            entry
        } else {
            null
        }
    }
}
