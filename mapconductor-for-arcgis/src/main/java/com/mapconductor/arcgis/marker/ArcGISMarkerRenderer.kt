package com.mapconductor.arcgis.marker

import androidx.core.graphics.drawable.toDrawable
import com.arcgismaps.mapping.symbology.PictureMarkerSymbol
import com.arcgismaps.mapping.view.Graphic
import com.arcgismaps.mapping.view.GraphicsOverlay
import com.mapconductor.arcgis.ArcGISMapViewHolder
import com.mapconductor.arcgis.toPoint
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.marker.AbstractMarkerRenderer
import com.mapconductor.core.marker.BitmapIcon
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerOverlayManager
import com.mapconductor.core.marker.MarkerOverlayManagerImpl
import com.mapconductor.core.marker.MarkerRendererFactory
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.UpdateParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DefaultArcGISMarkerRender : MarkerRendererFactory<Graphic> {
    override fun create(
        hexGeocell: HexGeocell,
        onIconAdd: suspend (List<Pair<MarkerState, BitmapIcon>>) -> List<Graphic?>,
        onIconRemove: suspend (List<MarkerEntity<Graphic>>) -> Unit,
        onIconChange: suspend (List<UpdateParams<Graphic>>) -> List<Graphic>,
        onAnimate: suspend (MarkerEntity<Graphic>) -> Unit,
        onPostProcess: (suspend () -> Unit)?,
    ): MarkerOverlayManager<Graphic> =
        MarkerOverlayManagerImpl(
            markerManager = MarkerManager(hexGeocell),
            onAdd = onIconAdd,
            onChange = onIconChange,
            onRemove = onIconRemove,
            onPostProcess = onPostProcess,
            onAnimate = onAnimate,
        )
}

class ArcGISMarkerRenderer(
    private val markerLayer: GraphicsOverlay,
    override val holder: ArcGISMapViewHolder,
    override val coroutine: CoroutineScope,
) : AbstractMarkerRenderer<Graphic>() {
    override fun setMarkerPosition(
        markerEntity: MarkerEntity<Graphic>,
        position: GeoPoint,
    ) {
        markerEntity.marker.geometry = position.toPoint()
    }

    override suspend fun addIcons(newMarkers: List<Pair<MarkerState, BitmapIcon>>): List<Graphic?> {
        return withContext(coroutine.coroutineContext) {
            newMarkers
                .map { params ->
                    val bitmapDrawable = params.second.bitmap.toDrawable(holder.mapView.context.resources)
                    val density = ResourceProvider.getDensity()
                    val width = (params.second.size.width / density) * (params.first.icon?.scale ?: 1.0f)
                    val height = (params.second.size.height / density) * (params.first.icon?.scale ?: 1.0f)
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
    }

    override suspend fun removeIcons(removeEntities: List<MarkerEntity<Graphic>>) {
        coroutine.launch {
            val elements = removeEntities.map { params -> params.marker }
            markerLayer.graphics.removeAll(elements)
        }
    }

    override suspend fun changeIcons(changes: List<UpdateParams<Graphic>>): List<Graphic> =
        withContext(coroutine.coroutineContext) {
            changes.map { params ->
                val prevFinger = params.prevEntity.fingerPrint
                val currFinger = params.entity.fingerPrint
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
}
