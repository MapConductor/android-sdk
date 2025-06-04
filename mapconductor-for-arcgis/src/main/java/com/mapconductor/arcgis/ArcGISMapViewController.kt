package com.mapconductor.arcgis

import androidx.core.graphics.drawable.toDrawable
import com.arcgismaps.mapping.symbology.PictureMarkerSymbol
import com.arcgismaps.mapping.view.Graphic
import com.arcgismaps.mapping.view.GraphicsOverlay
import com.arcgismaps.mapping.view.SurfacePlacement
import com.mapconductor.core.Offset
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.controller.BaseMapViewController
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.MarkerEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

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
    eventHandler: IArcGISMapEventHandler?,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : IArcGISMapViewController {
    lateinit var markerLayer: GraphicsOverlay

    private val eventHandlerRef = WeakReference(eventHandler)

    private val baseMapViewController =
        BaseMapViewController<Graphic>(
            coroutine = coroutine,
            onMarkerRemove = { id, marker ->
                this.markerLayer.graphics.remove(marker)

                coroutine.launch {
                    eventHandlerRef.get()?.onMarkerRemove(id)
                }
            },
            onMarkerAdd = { newMarkers ->
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

                return@BaseMapViewController markers
            },
            onMarkerChanged = { changes ->
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
    }

    override suspend fun addMarkers(markerList: List<MarkerEntry>) = baseMapViewController.addMarkers(markerList)

    override suspend fun clearOverlays() = baseMapViewController.clearOverlays()

    override fun toScreenOffset(position: IGeoPoint): Offset? {
        val result =
            this.holder.map.locationToScreen(
                point = GeoPoint.from(position).toPoint(),
            )
        return result?.let {
            Offset(it.screenPoint.x, it.screenPoint.y)
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
