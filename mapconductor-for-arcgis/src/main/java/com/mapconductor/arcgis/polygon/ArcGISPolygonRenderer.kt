package com.mapconductor.arcgis.polygon

import com.arcgismaps.geometry.Geometry
import com.arcgismaps.geometry.PolygonBuilder
import com.arcgismaps.mapping.symbology.SimpleFillSymbol
import com.arcgismaps.mapping.symbology.SimpleFillSymbolStyle
import com.arcgismaps.mapping.symbology.SimpleLineSymbol
import com.arcgismaps.mapping.symbology.SimpleLineSymbolStyle
import com.arcgismaps.mapping.view.Graphic
import com.arcgismaps.mapping.view.GraphicsOverlay
import com.mapconductor.arcgis.ArcGISActualPolygon
import com.mapconductor.arcgis.ArcGISMapViewHolder
import com.mapconductor.arcgis.toArcGISColor
import com.mapconductor.arcgis.toPoint
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.polygon.AbstractPolygonRenderer
import com.mapconductor.core.polygon.PolygonEntity
import com.mapconductor.core.polygon.PolygonOverlayManager
import com.mapconductor.core.polygon.PolygonOverlayManagerImpl
import com.mapconductor.core.polygon.PolygonRenderer.UpdateParams
import com.mapconductor.core.polygon.PolygonRendererFactory
import com.mapconductor.core.polygon.PolygonState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DefaultArcGISPolygonRenderer : PolygonRendererFactory<ArcGISActualPolygon> {
    override fun create(
        onAdd: suspend (List<PolygonState>) -> List<ArcGISActualPolygon?>,
        onChange: suspend (List<UpdateParams<ArcGISActualPolygon>>) -> List<ArcGISActualPolygon?>,
        onRemove: suspend (List<PolygonEntity<ArcGISActualPolygon>>) -> Unit,
        onPostProcess: (suspend () -> Unit)?,
    ): PolygonOverlayManager<ArcGISActualPolygon> =
        PolygonOverlayManagerImpl(
            onRemove = onRemove,
            onAdd = onAdd,
            onChange = onChange,
            onPostProcess = onPostProcess,
        )
}

class ArcGISPolygonRenderer(
    val polygonLayer: GraphicsOverlay,
    override val holder: ArcGISMapViewHolder,
    override val coroutine: CoroutineScope,
) : AbstractPolygonRenderer<ArcGISActualPolygon>() {
    override suspend fun addPolygons(newPolygons: List<PolygonState>): List<ArcGISActualPolygon?> {
        return withContext(coroutine.coroutineContext) {
            return@withContext newPolygons.map { state ->

                val geometry = createGeometry(state)

                val outlineSymbol =
                    SimpleLineSymbol().apply {
                        style = SimpleLineSymbolStyle.Solid
                        color = state.strokeColor.toArcGISColor()
                        width = state.strokeWidth.value.toFloat()
                    }

                val fillSymbol =
                    SimpleFillSymbol().apply {
                        style = SimpleFillSymbolStyle.Solid
                        color = state.fillColor.toArcGISColor()
                        outline = outlineSymbol
                    }

                val graphic =
                    Graphic(geometry, fillSymbol).also {
                        it.attributes.set("id", state.id)
                    }

                polygonLayer.graphics.add(graphic)

                return@map graphic
            }
        }
    }

    override suspend fun removePolygons(removeEntities: List<PolygonEntity<ArcGISActualPolygon>>) {
        val polygons = removeEntities.map { it.polygon }
        coroutine.launch {
            polygonLayer.graphics.removeAll(polygons)
        }
    }

    override suspend fun changePolygon(changes: List<UpdateParams<ArcGISActualPolygon>>): List<ArcGISActualPolygon> {
        return withContext(coroutine.coroutineContext) {
            return@withContext changes.map { params ->
                val finger = params.entity.state.fingerPrint()
                val prevFinger = params.prevEntity.state.fingerPrint()
                if (finger.points != prevFinger.points) {
                    params.entity.polygon.geometry = createGeometry(params.entity.state)
                }

                (params.entity.polygon.symbol as SimpleFillSymbol).let { symbol ->
                    if (finger.fillColor != prevFinger.fillColor) {
                        symbol.color = params.entity.state.fillColor.toArcGISColor()
                    }
                    symbol.outline?.let { outline ->
                        if (finger.strokeColor != prevFinger.strokeColor) {
                            outline.color = params.entity.state.strokeColor.toArcGISColor()
                        }
                        if (finger.strokeWidth != prevFinger.strokeWidth) {
                            outline.width = ResourceProvider.dpToPx(params.entity.state.strokeWidth).toFloat()
                        }
                    }
                }
                return@map params.entity.polygon
            }
        }
    }

    private fun createGeometry(state: PolygonState): Geometry {
        val polygonBuilder =
            PolygonBuilder().also { builder ->
                state.points.forEach {
                    builder.addPoint(GeoPoint.from(it).toPoint())
                }
            }
        return polygonBuilder.toGeometry()
    }
}
