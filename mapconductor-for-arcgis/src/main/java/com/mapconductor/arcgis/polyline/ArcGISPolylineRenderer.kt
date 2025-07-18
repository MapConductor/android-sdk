package com.mapconductor.arcgis.polyline

import com.arcgismaps.Color
import com.arcgismaps.geometry.Geometry
import com.arcgismaps.geometry.PolylineBuilder
import com.arcgismaps.mapping.symbology.SimpleLineSymbol
import com.arcgismaps.mapping.symbology.SimpleLineSymbolStyle
import com.arcgismaps.mapping.view.Graphic
import com.arcgismaps.mapping.view.GraphicsOverlay
import com.mapconductor.arcgis.ArcGISMapViewHolder
import com.mapconductor.arcgis.toPoint
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.polyline.AbstractPolylineRenderer
import com.mapconductor.core.polyline.PolylineEntity
import com.mapconductor.core.polyline.PolylineOverlayManager
import com.mapconductor.core.polyline.PolylineOverlayManagerImpl
import com.mapconductor.core.polyline.PolylineRenderer.UpdateParams
import com.mapconductor.core.polyline.PolylineRendererFactory
import com.mapconductor.core.polyline.PolylineState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DefaultArcGISPolylineRenderer : PolylineRendererFactory<Graphic> {
    override fun create(
        onAdd: suspend (List<PolylineState>) -> List<Graphic?>,
        onChange: suspend (List<UpdateParams<Graphic>>) -> List<Graphic?>,
        onRemove: suspend (List<PolylineEntity<Graphic>>) -> Unit,
        onPostProcess: (suspend () -> Unit)?,
    ): PolylineOverlayManager<Graphic> =
        PolylineOverlayManagerImpl(
            onRemove = onRemove,
            onAdd = onAdd,
            onChange = onChange,
            onPostProcess = onPostProcess,
        )
}

class ArcGISPolylineRenderer(
    val polylineLayer: GraphicsOverlay,
    override val holder: ArcGISMapViewHolder,
    override val coroutine: CoroutineScope,
) : AbstractPolylineRenderer<Graphic>() {
    override suspend fun addLines(newLines: List<PolylineState>): List<Graphic?> {
        return withContext(coroutine.coroutineContext) {
            return@withContext newLines.map { state ->

                val geometry = createGeometry(state)

                val lineSymbol =
                    SimpleLineSymbol().apply {
                        style = SimpleLineSymbolStyle.Solid
                        color = createStrokeColor(state)
                        width = state.strokeWidth.value.toFloat() // 線の太さ
                    }

                val graphic =
                    Graphic(geometry, lineSymbol).also {
                        it.attributes.set("id", state.id)
                    }

                polylineLayer.graphics.add(graphic)

                return@map graphic
            }
        }
    }

    override suspend fun removeLines(removeEntities: List<PolylineEntity<Graphic>>) {
        val polylines = removeEntities.map { it.polyline }
        coroutine.launch {
            polylineLayer.graphics.removeAll(polylines)
        }
    }

    override suspend fun changeLine(changes: List<UpdateParams<Graphic>>): List<Graphic> {
        return withContext(coroutine.coroutineContext) {
            return@withContext changes.map { params ->
                val finger = params.entity.state.fingerPrint()
                val prevFinger = params.prevEntity.state.fingerPrint()
                if (finger.points != prevFinger.points) {
                    params.entity.polyline.geometry = createGeometry(params.entity.state)
                }

                (params.entity.polyline.symbol as SimpleLineSymbol).let { symbol ->
                    if (finger.strokeColor != prevFinger.strokeColor) {
                        symbol.color = createStrokeColor(params.entity.state)
                    }
                    if (finger.strokeWidth != prevFinger.strokeWidth) {
                        symbol.width = ResourceProvider.dpToPx(params.prevEntity.state.strokeWidth).toFloat()
                    }
                }
                return@map params.prevEntity.polyline
            }
        }
    }

    private fun createStrokeColor(state: PolylineState): Color =
        Color.fromRgba(
            r = (state.strokeColor.red * 255).toInt(),
            g = (state.strokeColor.green * 255).toInt(),
            b = (state.strokeColor.blue * 255).toInt(),
            a = (state.strokeColor.alpha * 255).toInt(),
        )

    private fun createGeometry(state: PolylineState): Geometry {
        val polylineBuilder =
            PolylineBuilder().also { builder ->
                state.points.forEach {
                    builder.addPoint(GeoPoint.from(it).toPoint())
                }
            }
        return polylineBuilder.toGeometry()
    }
}
