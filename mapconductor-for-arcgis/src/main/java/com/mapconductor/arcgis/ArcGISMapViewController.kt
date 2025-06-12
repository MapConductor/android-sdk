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
import com.mapconductor.core.MarkerManager
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.controller.BaseMapViewController
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.controller.MarkerOverlayManager
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.projection.WebMercator
import com.mapconductor.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.view.MotionEvent

interface IArcGISMapViewController : MapViewController {
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
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : BaseMapViewController<Camera>(),
    IArcGISMapViewController {
    val markerLayer: GraphicsOverlay =
        GraphicsOverlay().apply {
            sceneProperties.surfacePlacement = SurfacePlacement.Relative
        }

    private var selectedMarker: SelectedMarker? = null

    private val markerOverlayManager =
        MarkerOverlayManager<Graphic>(
            markerManager = MarkerManager(HexGeocell(WebMercator)),
            onRemove = { removes ->
                val elements: List<Graphic> = removes.map { params -> params.marker }
                this.markerLayer.graphics.removeAll(elements)
            },
            onAdd = { newMarkers ->
                val markers =
                    newMarkers.map { params ->
                        val bitmapDrawable = params.icon.bitmap.toDrawable(holder.mapView.context.resources)
                        val density = ResourceProvider.density
                        val width = (params.icon.size.width / density)
                        val height = (params.icon.size.height / density)
                        val anchorX = (params.icon.anchor.x - 0.5) * width
                        val anchorY = (params.icon.anchor.y - 0.5) * height

                        val pictureSymbolFuture =
                            PictureMarkerSymbol.createWithImage(bitmapDrawable).also {
                                it.width = width.toFloat()
                                it.height = height.toFloat()
                                it.offsetX = anchorX.toFloat()
                                it.offsetY = anchorY.toFloat()
                            }

                        val marker =
                            Graphic(
                                geometry =
                                    params.state.position
                                        .toPoint(),
                                symbol = pictureSymbolFuture,
                            )
                        marker.attributes.set("id", params.state.id)
                        return@map marker
                    }

                this.markerLayer.graphics.addAll(markers)

                return@MarkerOverlayManager markers
            },
            onChange = { changes ->
                changes.forEach { params ->
                    val bitmapDrawable = params.icon.bitmap.toDrawable(holder.mapView.context.resources)
                    val density = ResourceProvider.density
                    val width = (params.icon.size.width / density)
                    val height = (params.icon.size.height / density)
                    val anchorX = (params.icon.anchor.x - 0.5) * width
                    val anchorY = (params.icon.anchor.y - 0.5) * height

                    val pictureSymbolFuture =
                        PictureMarkerSymbol.createWithImage(bitmapDrawable).also {
                            it.width = width.toFloat()
                            it.height = height.toFloat()
                            it.offsetX = anchorX.toFloat()
                            it.offsetY = anchorY.toFloat()
                        }

                    params.marker.geometry =
                        params.state.position
                            .toPoint()
                    params.marker.symbol = pictureSymbolFuture
                }
            },
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
            mapLongClickListener?.also {
                it.invoke(position)
            }
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
    ): MarkerState? {
        val camera = holder.map.getCurrentViewpointCamera()
        val zoom = camera.toMapCameraPosition().zoom
        val acceptDPI = tolerance.value.toFloat() * holder.mapView.context.resources.displayMetrics.density

        return findMarkerFromPoint(
            markerOverlayManager = markerOverlayManager,
            position = position,
            zoom = zoom,
            tolerance = acceptDPI.toDouble(),
        )
    }

    override suspend fun addMarkers(markerList: List<MarkerState>) = markerOverlayManager.addMarkers(markerList)

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
}
