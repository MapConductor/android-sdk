package com.mapconductor.arcgis

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.core.graphics.drawable.toDrawable
import com.arcgismaps.mapping.symbology.PictureMarkerSymbol
import com.arcgismaps.mapping.view.Camera
import com.arcgismaps.mapping.view.Graphic
import com.arcgismaps.mapping.view.GraphicsOverlay
import com.arcgismaps.mapping.view.LongPressEvent
import com.arcgismaps.mapping.view.PanChangeEvent
import com.arcgismaps.mapping.view.ScreenCoordinate
import com.arcgismaps.mapping.view.SingleTapConfirmedEvent
import com.arcgismaps.mapping.view.SurfacePlacement
import com.arcgismaps.mapping.view.UpEvent
import com.arcgismaps.mapping.view.extensions.motionEvent
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.controller.BaseMapViewController
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.MarkerAnimation
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerOverlayManagerImpl
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.projection.WebMercator
import com.mapconductor.settings.Settings
import android.view.MotionEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface IArcGISMapViewController : MapViewController<Graphic> {
    fun moveCamera(
        dstPosition: MapCameraPosition,
        listener: MapViewState.MoveCameraCallback? = null,
    )

    fun animateCamera(
        dstPosition: MapCameraPosition,
        duration: Int,
        listener: MapViewState.MoveCameraCallback? = null,
    )
}

internal data class SelectedMarker(
    val state: MarkerState,
    val graphic: Graphic,
)

class ArcGISMapViewController(
    override val holder: ArcGISMapViewHolder,
    override val coroutine: CoroutineScope =
        CoroutineScope(Dispatchers.Default),
    override val hexCell: HexGeocell =
        HexGeocell(
            projection = WebMercator,
            baseHexSideLength = 100000, // 100km - 中ズームレベルに適した値
        ),
) : BaseMapViewController<Camera, Graphic>(),
    IArcGISMapViewController {
    val markerLayer: GraphicsOverlay =
        GraphicsOverlay().apply {
            sceneProperties.surfacePlacement = SurfacePlacement.Relative
        }

    private var selectedMarker: SelectedMarker? = null

    override val markerOverlayManager =
        MarkerOverlayManagerImpl<Graphic>(
            markerManager = MarkerManager<Graphic>(hexCell),
            onRemove = { removes ->
                coroutine.launch {
                    val elements = removes.map { params -> params.marker }
                    markerLayer.graphics.removeAll(elements)
                }
            },
            onAdd = { newMarkers ->
                withContext(coroutine.coroutineContext) {
                    newMarkers
                        .map { params ->
                            val bitmapDrawable = params.second.bitmap.toDrawable(holder.mapView.context.resources)
                            val density = ResourceProvider.density
                            val width = (params.second.size.width / density)
                            val height = (params.second.size.height / density)
                            val anchorX = (params.second.anchor.x - 0.5) * width
                            val anchorY = (params.second.anchor.y - 0.5) * height

                            val pictureSymbolFuture =
                                PictureMarkerSymbol.createWithImage(bitmapDrawable).also {
                                    it.width = width.toFloat()
                                    it.height = height.toFloat()
                                    it.offsetX = anchorX.toFloat()
                                    it.offsetY = anchorY.toFloat()
                                }

                            val marker =
                                Graphic(
                                    geometry = params.first.position.toPoint(),
                                    symbol = pictureSymbolFuture,
                                )
                            marker.attributes.set("id", params.first.id)
                            return@map marker
                        }.also {
                            markerLayer.graphics.addAll(it)
                        }
                }
            },
            onChange = { changes ->
                withContext(coroutine.coroutineContext) {
                    changes.map { params ->
                        if (params.entity.state.icon != params.prevEntity.state.icon) {
                            val bitmapDrawable = params.bitmapIcon.bitmap.toDrawable(holder.mapView.context.resources)
                            val density = ResourceProvider.density
                            val width = (params.bitmapIcon.size.width / density)
                            val height = (params.bitmapIcon.size.height / density)
                            val anchorX = (params.bitmapIcon.anchor.x - 0.5) * width
                            val anchorY = (params.bitmapIcon.anchor.y - 0.5) * height

                            val pictureSymbolFuture =
                                PictureMarkerSymbol.createWithImage(bitmapDrawable).also {
                                    it.width = width.toFloat()
                                    it.height = height.toFloat()
                                    it.offsetX = anchorX.toFloat()
                                    it.offsetY = anchorY.toFloat()
                                }
                            params.entity.marker.symbol = pictureSymbolFuture
                        }

                        if (params.entity.state.position != params.prevEntity.state.position) {
                            params.entity.marker.geometry =
                                params.entity.state.position
                                    .toPoint()
                        }

                        // ArcGISはマーカーを再作成しなくてよいので、同じマーカーのインスタンスを返す
                        params.entity.marker
                    }
                }
            },

            onAnimate = {
                when (it.state.animation) {
                    MarkerAnimation.Drop -> this.animateMarkerDrop(it)
                    MarkerAnimation.Bounce -> this.animateMarkerBounce(it)
                    else -> throw IllegalArgumentException("Unimplemented animation is specified: ${it.state.animation}")
                }
            }
        )

    init {
        holder.map.graphicsOverlays.clear()
        holder.map.graphicsOverlays.add(markerLayer)
        coroutine.launch {
            holder.map.onSingleTapConfirmed.collect { onMapTap(it) }
        }
        coroutine.launch {
            holder.map.viewpointChanged.collect { onViewpointChange() }
        }
        coroutine.launch {
            holder.map.onLongPress.collect { onMapLongPress(it) }
        }
        coroutine.launch {
            holder.map.onUp.collect { onMapUp(it) }
        }
        coroutine.launch {
            holder.map.onPan.collect { onMapPan(it) }
        }
    }
/*
    private fun markerDropAnimation(params: MarkerModifyParams<Graphic>) {
        val markerLatLng = (params.marker.geometry as? Point)?.toGeoPoint() ?: return
        val interpolator = LinearInterpolator()
        val markerPoint = this.toScreenOffset(markerLatLng) ?: return
        val startPoint = Offset(markerPoint.x, 0f)
        val duration = Settings.Default.markerDropAnimateDuration

        markerAnimateStartListener?.let { it(params.state) }

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
            val startLatLng = this.fromScreenOffset(startPoint) ?: return@onEach
            val lng = t * markerLatLng.longitude + (1 - t) * startLatLng.longitude
            val lat = t * markerLatLng.latitude + (1 - t) * startLatLng.latitude
            params.marker.geometry = Point(lng, lat, SpatialReference.wgs84())
        }.onCompletion {
            params.marker.geometry = markerLatLng.toPoint()
            params.state.animation = null
            markerAnimateEndListener?.let { it(params.state) }
        }.launchIn(coroutine)
    }

    private fun markerBounceAnimation(params: MarkerModifyParams<Graphic>) {
        val startTime = SystemClock.uptimeMillis()
        val duration = Settings.Default.markerBounceAnimateDuration
        val interpolator: Interpolator = BounceInterpolator()
        val markerLatLng = (params.marker.geometry as? Point)?.toGeoPoint() ?: return
        val startPoint = Offset(0f , 0f)

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
            val startLatLng = this.fromScreenOffset(startPoint) ?: return@onEach
            val lng = markerLatLng.longitude
            val lat = t * markerLatLng.latitude + (1 - t) * startLatLng.latitude
            params.marker.geometry = Point(lng, lat, SpatialReference.wgs84())
        }.onCompletion {
            params.marker.geometry = markerLatLng.toPoint()
            params.state.animation = null

            markerAnimateEndListener?.let { it(params.state) }
        }.launchIn(coroutine)
    }
*/
    private fun onViewpointChange() {
        this.cameraMoveListener?.invoke(holder.map.getCurrentViewpointCamera())
    }

    private suspend fun onMapPan(event: PanChangeEvent) {
        selectedMarker?.also {
            val screenPoint = event.screenCoordinate
            val point = holder.map.screenToLocation(screenPoint).getOrNull() ?: return
            val position = point.toGeoPoint()
            it.graphic.geometry = point
            it.state.position = position
            markerDragListener?.invoke(it.state)
        }
    }

    private suspend fun onMapUp(event: UpEvent) {
        selectedMarker?.also {
            val screenPoint = event.screenCoordinate
            val point = holder.map.screenToLocation(screenPoint).getOrNull() ?: return
            val position = point.toGeoPoint()
            it.graphic.geometry = point
            it.state.position = position

            // Restore the recomposition for the position property
            setDraggingState(it.state, false)

            markerDragEndListener?.invoke(it.state)
            with(holder.map) {
                interactionOptions.isPanEnabled = true
                interactionOptions.isRotateEnabled = true
                interactionOptions.isZoomEnabled = true
            }
        }
        selectedMarker = null
    }

    private suspend fun onMapLongPress(event: LongPressEvent) {
        if (event.motionEvent.action != MotionEvent.ACTION_MOVE) return

        val screenPoint = event.screenCoordinate
        val point = holder.map.screenToLocation(screenPoint).getOrNull() ?: return
        val position = point.toGeoPoint()
        val identifyResult =
            holder.map.identifyGraphicsOverlay(
                graphicsOverlay = markerLayer,
                screenCoordinate = screenPoint,
                tolerance =
                    Settings.Default.tapTolerance.value
                        .toDouble(),
                returnPopupsOnly = false,
            )
        val graphics = identifyResult.getOrNull()?.graphics
        val graphic = graphics?.firstOrNull()
        if (graphic == null) {
            mapLongClickListener?.invoke(position)
            return
        }
        val markerId = (graphic.attributes.get("id") as? String) ?: return
        val state = markerOverlayManager.getMarkerState(markerId) ?: return
        selectedMarker =
            SelectedMarker(
                state = state,
                graphic = graphic,
            )
        // 3Dナビゲーションを無効化
        with(holder.map) {
            interactionOptions.isPanEnabled = false
            interactionOptions.isRotateEnabled = false
            interactionOptions.isZoomEnabled = false
        }

        // Suppress the recomposition for the position property
        setDraggingState(state, true)

        markerDragStartListener?.invoke(state)
    }

    private suspend fun onMapTap(event: SingleTapConfirmedEvent) {
        val screenPoint = event.screenCoordinate
//        val touchPosition = holder.map.screenToLocation(screenPoint).getOrNull()?.toGeoPoint() ?: return
//
//        val MarkerState = this.findNearestMarker(
//            position = touchPosition,
//            tolerance = Settings.Default.tapTolerance,
//        )
//        if (MarkerState != null) {
//            MarkerState.handlers.onClick?.let {
//                coroutine.launch {
//                    it(MarkerState.state)
//                }
//            }
//            return
//        }
        val identifyResult =
            holder.map.identifyGraphicsOverlay(
                graphicsOverlay = markerLayer,
                screenCoordinate = screenPoint,
                tolerance =
                    Settings.Default.tapTolerance.value
                        .toDouble(),
                returnPopupsOnly = false,
            )
        val graphics = identifyResult.getOrNull()?.graphics
        graphics?.firstOrNull()?.also { graphic ->
            (graphic.attributes.get("id") as? String)?.also { markerId ->
                markerOverlayManager.getMarkerState(markerId)?.also { state ->
                    markerClickListener?.also { onMarkerClick ->
                        return@onMapTap onMarkerClick(state)
                    }
                }
            }
        }

        holder.map.screenToLocation(screenPoint).getOrNull()?.also {
            mapClickListener?.invoke(it.toGeoPoint())
        }
    }

    private fun findNearestMarker(
        position: IGeoPoint,
        tolerance: Dp,
    ): MarkerEntity<Graphic>? {
        val camera = holder.map.getCurrentViewpointCamera()
        val zoom = camera.toMapCameraPosition().zoom
        val acceptDPI = tolerance.value.toFloat() * holder.mapView.context.resources.displayMetrics.density

        return findMarkerFromPoint(
            position = position,
            zoom = zoom,
            tolerance = acceptDPI.toDouble(),
        )
    }

    override suspend fun addMarkers(markerList: List<MarkerState>) = markerOverlayManager.addMarkers(markerList)

    override suspend fun updateMarker(state: MarkerState) = markerOverlayManager.updateMarker(state)

    override suspend fun clearOverlays() = markerOverlayManager.clearOverlays()

    override fun toScreenOffset(position: IGeoPoint): Offset? {
        val result =
            holder.map.locationToScreen(
                point = GeoPoint.from(position).toPoint(),
            )
        return result?.let {
            Offset(it.screenPoint.x.toFloat(), it.screenPoint.y.toFloat())
        }
    }

    override suspend fun fromScreenOffset(offset: Offset): GeoPoint? {
        val result =
            holder.map.screenToLocation(
                screenCoordinate =
                    ScreenCoordinate(
                        x = offset.x.toDouble(),
                        y = offset.y.toDouble(),
                    ),
            )
        return result.getOrNull()?.toGeoPoint()
    }

    override fun moveCamera(
        dstPosition: MapCameraPosition,
        listener: MapViewState.MoveCameraCallback?,
    ) {
        val dstCameraPosition = dstPosition.toCamera()

        holder.map.setViewpointCamera(
            camera = dstCameraPosition,
        )
        listener?.onComplete(true)
    }

    override fun animateCamera(
        dstPosition: MapCameraPosition,
        duration: Int,
        listener: MapViewState.MoveCameraCallback?,
    ) {
        val dstCameraPosition = dstPosition.toCamera()

        coroutine.launch {
            val result =
                holder.map.setViewpointCameraAnimated(
                    camera = dstCameraPosition,
                    duration = duration.toFloat() / 1000.0f,
                )
            listener?.onComplete(result.isSuccess)
        }
    }

    override fun setMarkerPosition(markerEntity: MarkerEntity<Graphic>, position: GeoPoint) {
        markerEntity.marker.geometry = position.toPoint()
    }

    override fun clearPolyline() {
        TODO("Not yet implemented")
    }

    override fun drawPolyline(geoPoints: List<IGeoPoint>) {
        TODO("Not yet implemented")
    }
}
