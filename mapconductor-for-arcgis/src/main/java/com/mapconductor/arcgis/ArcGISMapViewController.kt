package com.mapconductor.arcgis

import androidx.compose.ui.geometry.Offset
import androidx.core.graphics.drawable.toDrawable
import com.arcgismaps.geometry.GeometryEngine
import com.arcgismaps.geometry.Point
import com.arcgismaps.geometry.SpatialReference
import com.arcgismaps.mapping.symbology.PictureMarkerSymbol
import com.arcgismaps.mapping.view.Camera
import com.arcgismaps.mapping.view.Graphic
import com.arcgismaps.mapping.view.GraphicsOverlay
import com.arcgismaps.mapping.view.SingleTapConfirmedEvent
import com.arcgismaps.mapping.view.SurfacePlacement
import com.mapconductor.core.MarkerManager
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.controller.MarkerOverlayManager
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.OnCameraMoveHandler
import com.mapconductor.core.map.OnMapClickHandler
import com.mapconductor.core.marker.MarkerEntry
import com.mapconductor.core.projection.WebMercator
import com.mapconductor.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

class ArcGISMapViewController(
    override val holder: ArcGISMapViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
    val onCameraMove: (OnCameraMoveHandler<Camera>)? = null,
    val onMapClick: OnMapClickHandler? = null,
) : IArcGISMapViewController {
    lateinit var markerLayer: GraphicsOverlay

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
                                    params.entry.state.position
                                        .toPoint(),
                                symbol = pictureSymbolFuture,
                            )
                        marker.attributes.set("id", params.entry.id)
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
                        params.entry.state.position
                            .toPoint()
                    params.marker.symbol = pictureSymbolFuture
                }
            },
        )

    init {

        this.markerLayer =
            GraphicsOverlay().apply {
                sceneProperties.surfacePlacement = SurfacePlacement.Relative
            }
        holder.map.graphicsOverlays.clear()
        holder.map.graphicsOverlays.add(markerLayer)
        coroutine.launch {
            holder.map.onSingleTapConfirmed.collect { onMapTap(it) }
        }
        coroutine.launch {
            onCameraMove?.let { callback ->
                holder.map.viewpointChanged.collect { onViewpointChange(callback) }
            }
        }
    }

    private fun onViewpointChange(callback: OnCameraMoveHandler<Camera>) {
        callback(holder.map.getCurrentViewpointCamera())
    }

    private suspend fun onMapTap(event: SingleTapConfirmedEvent) {
        val screenPoint = event.screenCoordinate
        val identifyResult = holder.map.identifyGraphicsOverlay(
            graphicsOverlay = markerLayer,
            screenCoordinate = screenPoint,
            tolerance = Settings.Default.tapTolerance.value.toDouble(),
            returnPopupsOnly = false,
        )
        identifyResult.getOrNull()?.graphics?.firstOrNull()?.also { graphic ->
            (graphic.attributes.get("id") as? String)?.also { markerId ->
                markerOverlayManager.getMarkerEntry(markerId)?.also { entry ->
                    entry.handlers.onClick?.also { onMarkerClick ->
                        return@onMapTap onMarkerClick(entry.state)
                    }
                }
            }

            holder.map.screenToLocation(screenPoint).getOrNull()?.also {
                onMapClick?.invoke(it.toGeoPoint())
            }
        }
    }

    override suspend fun addMarkers(markerList: List<MarkerEntry>) = markerOverlayManager.addMarkers(markerList)

    override suspend fun clearOverlays() = markerOverlayManager.clearOverlays()

    override fun toScreenOffset(position: IGeoPoint): Offset? {
        val result = holder.map.locationToScreen(
            point = GeoPoint.from(position).toPoint(),
        )
        return result?.let {
            Offset(it.screenPoint.x.toFloat(), it.screenPoint.y.toFloat())
        }
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
