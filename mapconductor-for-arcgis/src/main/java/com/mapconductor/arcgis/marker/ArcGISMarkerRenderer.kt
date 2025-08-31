package com.mapconductor.arcgis.marker

import androidx.core.graphics.drawable.toDrawable
import com.arcgismaps.mapping.symbology.PictureMarkerSymbol
import com.arcgismaps.mapping.view.Graphic
import com.arcgismaps.mapping.view.GraphicsOverlay
import com.mapconductor.arcgis.ArcGISActualMarker
import com.mapconductor.arcgis.ArcGISMapViewHolder
import com.mapconductor.arcgis.toPoint
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.marker.AbstractMarkerOverlayRenderer
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerOverlayRenderer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ArcGISMarkerRenderer(
    val markerLayer: GraphicsOverlay,
    holder: ArcGISMapViewHolder,
    coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractMarkerOverlayRenderer<ArcGISMapViewHolder, ArcGISActualMarker>(
        holder = holder,
        coroutine = coroutine,
    ) {
    override fun setMarkerPosition(
        markerEntity: MarkerEntity<Graphic>,
        position: GeoPoint,
    ) {
        markerEntity.marker.geometry = position.toPoint(holder.map.scene?.spatialReference)
    }

    override suspend fun onAdd(data: List<MarkerOverlayRenderer.AddParams>): List<Graphic?> {
        return withContext(coroutine.coroutineContext) {
            data
                .map { params ->
                    val bitmapDrawable = params.bitmapIcon.bitmap.toDrawable(holder.mapView.context.resources)
                    val density = ResourceProvider.getDensity()
//                    val width = ((params.bitmapIcon.size.width * (params.state.icon?.scale ?: 1.0f)) / density)
//                    val height = ((params.bitmapIcon.size.height * (params.state.icon?.scale ?: 1.0f)) / density)
                    val width = params.bitmapIcon.size.width / density
                    val height = params.bitmapIcon.size.height / density
                    val anchorX = (0.5 - params.bitmapIcon.anchor.x) * width
                    val anchorY = (params.bitmapIcon.anchor.y - 0.5) * height

                    val pictureSymbolFuture =
                        PictureMarkerSymbol.createWithImage(bitmapDrawable).also {
                            it.width = width.toFloat()
                            it.height = height.toFloat()
                            it.offsetX = anchorX.toFloat()
                            it.offsetY = anchorY.toFloat()
                        }

                    val marker =
                        Graphic(
                            geometry = GeoPoint.from(params.state.position).toPoint(holder.map.scene?.spatialReference),
                            symbol = pictureSymbolFuture,
                        ).also {
                            it.attributes.set("id", params.state.id)
                        }
                    return@map marker
                }.also {
                    markerLayer.graphics.addAll(it)
                }
        }
    }

    override suspend fun onRemove(data: List<MarkerEntity<ArcGISActualMarker>>) {
        coroutine.launch {
            val elements = data.map { params -> params.marker }
            markerLayer.graphics.removeAll(elements)
        }
    }

    override suspend fun onPostProcess() {
        // Do nothing here
    }

    override suspend fun onChange(
        data: List<MarkerOverlayRenderer.ChangeParams<ArcGISActualMarker>>,
    ): List<ArcGISActualMarker?> =
        withContext(coroutine.coroutineContext) {
            data.map { params ->
                val prevFinger = params.prev.fingerPrint
                val currFinger = params.current.fingerPrint
                if (currFinger.icon != prevFinger.icon) {
                    val bitmapDrawable = params.bitmapIcon.bitmap.toDrawable(holder.mapView.context.resources)
                    val density = ResourceProvider.getDensity()
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
                    params.current.marker.symbol = pictureSymbolFuture
                }

                if (params.current.state.position != params.prev.state.position) {
                    params.current.marker.geometry =
                        GeoPoint.from(params.current.state.position).toPoint()
                }

                // ArcGISはマーカーを再作成しなくてよいので、同じマーカーのインスタンスを返す
                params.current.marker
            }
        }
}
