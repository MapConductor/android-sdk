package com.mapconductor.arcgis.circle

import com.arcgismaps.Color
import com.arcgismaps.geometry.GeometryEngine
import com.arcgismaps.geometry.LinearUnit
import com.arcgismaps.geometry.LinearUnitId
import com.arcgismaps.mapping.symbology.SimpleFillSymbol
import com.arcgismaps.mapping.symbology.SimpleFillSymbolStyle
import com.arcgismaps.mapping.symbology.SimpleLineSymbol
import com.arcgismaps.mapping.symbology.SimpleLineSymbolStyle
import com.arcgismaps.mapping.view.Graphic
import com.arcgismaps.mapping.view.GraphicsOverlay
import com.mapconductor.arcgis.ArcGISMapViewHolder
import com.mapconductor.arcgis.toPoint
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.circle.AbstractCircleRenderer
import com.mapconductor.core.circle.CircleEntity
import com.mapconductor.core.circle.CircleOverlayManager
import com.mapconductor.core.circle.CircleOverlayManagerImpl
import com.mapconductor.core.circle.CircleRenderer.UpdateParams
import com.mapconductor.core.circle.CircleRendererFactory
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.features.GeoPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DefaultArcGISCircleRenderer : CircleRendererFactory<Graphic> {
    override fun create(
        onAdd: suspend (List<CircleState>) -> List<Graphic?>,
        onChange: suspend (List<UpdateParams<Graphic>>) -> List<Graphic?>,
        onRemove: suspend (List<CircleEntity<Graphic>>) -> Unit,
        onPostProcess: (suspend () -> Unit)?,
    ): CircleOverlayManager<Graphic> =
        CircleOverlayManagerImpl(
            onRemove = onRemove,
            onAdd = onAdd,
            onChange = onChange,
            onPostProcess = onPostProcess,
        )
}

class ArcGISCircleRenderer(
    val circleLayer: GraphicsOverlay,
    override val holder: ArcGISMapViewHolder,
    override val coroutine: CoroutineScope,
) : AbstractCircleRenderer<Graphic>() {
    override suspend fun addCircles(newCircles: List<CircleState>): List<Graphic?> {
        return withContext(coroutine.coroutineContext) {
            return@withContext newCircles.map { state ->
                val centerPoint = GeoPoint.from(state.center).toPoint()
                val circle = GeometryEngine.bufferOrNull(
                    centerPoint,
                    state.radiusMeters,
                    LinearUnit(LinearUnitId.Meters)
                )

                circle?.let { geometry ->
                    val outline =
                        SimpleLineSymbol().apply {
                            style = SimpleLineSymbolStyle.Solid
                            color = createStrokeColor(state)
                            width = ResourceProvider.dpToPx(state.strokeWidth).toFloat()
                        }

                    val fillSymbol =
                        SimpleFillSymbol().apply {
                            style = SimpleFillSymbolStyle.Solid
                            color = createFillColor(state)
                            outline = outline
                        }

                    val graphic =
                        Graphic(geometry, fillSymbol).also {
                            it.attributes.set("id", state.id)
                        }

                    circleLayer.graphics.add(graphic)
                    graphic
                }
            }
        }
    }

    override suspend fun removeCircles(removeEntities: List<CircleEntity<Graphic>>) {
        val circles = removeEntities.map { it.circle }
        coroutine.launch {
            circleLayer.graphics.removeAll(circles)
        }
    }

    override suspend fun changeCircle(changes: List<UpdateParams<Graphic>>): List<Graphic> {
        return withContext(coroutine.coroutineContext) {
            return@withContext changes.map { params ->
                val finger = params.entity.state.fingerPrint
                val prevFinger = params.prevEntity.state.fingerPrint
                val graphic = params.entity.circle

                if (finger.center != prevFinger.center || finger.radius != prevFinger.radius) {
                    val centerPoint = GeoPoint.from(params.entity.state.center).toPoint()
                    val newGeometry = GeometryEngine.bufferOrNull(
                        centerPoint,
                        params.entity.state.radiusMeters,
                        LinearUnit(LinearUnitId.Meters)
                    )
                    newGeometry?.let {
                        graphic.geometry = it
                    }
                }

                (graphic.symbol as SimpleFillSymbol).let { symbol ->
                    if (finger.fillColor != prevFinger.fillColor) {
                        symbol.color = createFillColor(params.entity.state)
                    }
                    symbol.outline?.let { outline ->
                        if (finger.strokeColor != prevFinger.strokeColor) {
                            outline.color = createStrokeColor(params.entity.state)
                        }
                        if (finger.strokeWidth != prevFinger.strokeWidth) {
                            outline.width = ResourceProvider.dpToPx(params.entity.state.strokeWidth).toFloat()
                        }
                    }
                }
                graphic
            }
        }
    }

    private fun createStrokeColor(state: CircleState): Color =
        Color.fromRgba(
            r = (state.strokeColor.red * 255).toInt(),
            g = (state.strokeColor.green * 255).toInt(),
            b = (state.strokeColor.blue * 255).toInt(),
            a = (state.strokeColor.alpha * 255).toInt(),
        )

    private fun createFillColor(state: CircleState): Color =
        Color.fromRgba(
            r = (state.fillColor.red * 255).toInt(),
            g = (state.fillColor.green * 255).toInt(),
            b = (state.fillColor.blue * 255).toInt(),
            a = (state.fillColor.alpha * 255).toInt(),
        )
}