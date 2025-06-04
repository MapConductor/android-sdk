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
    onCameraMove: (OnCameraMoveHandler<Camera>)? = null,
    onMapClick: OnMapClickHandler? = null,
) : IArcGISMapViewController {
    lateinit var markerLayer: GraphicsOverlay

    private val markerOverlayManager =
        MarkerOverlayManager<Graphic>(
            coroutine = coroutine,
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
                    // TODO: アイコンに変更があったかどうかを比較
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

        onMapClick?.let { clickHandler ->
            coroutine.launch {
                holder.map.onSingleTapConfirmed.collect { event ->

                    event.mapPoint?.let {
                        clickHandler(it.toGeoPoint())
                    }
                }
            }
        }

        onCameraMove?.let {
            coroutine.launch {
                it(holder.map.getCurrentViewpointCamera())
            }
        }
    }

    override suspend fun addMarkers(markerList: List<MarkerEntry>) = markerOverlayManager.addMarkers(markerList)

    override suspend fun clearOverlays() = markerOverlayManager.clearOverlays()

    override fun toScreenOffset(position: IGeoPoint): Offset? {
        val spatial = SpatialReference.webMercator()
        val mapPoint = GeoPoint.from(position).toPoint(spatial)

        // 2. MapView の座標系に変換
        val mapPointInMap =
            holder.map.spatialReference?.let { spatialRef ->
                GeometryEngine.projectOrNull(mapPoint, spatial) as? Point
            } ?: return Offset(-9999f, -9999f)

        val result =
            this.holder.map.locationToScreen(
                point = mapPointInMap,
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
