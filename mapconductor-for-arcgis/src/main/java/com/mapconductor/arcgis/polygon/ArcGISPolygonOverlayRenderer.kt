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
import com.mapconductor.core.polygon.AbstractPolygonOverlayRenderer
import com.mapconductor.core.polygon.PolygonEntity
import com.mapconductor.core.polygon.PolygonState
import kotlin.collections.set
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ArcGISPolygonOverlayRenderer(
    val polygonLayer: GraphicsOverlay,
    override val holder: ArcGISMapViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : AbstractPolygonOverlayRenderer<ArcGISActualPolygon>() {
    override suspend fun createPolygon(state: PolygonState): ArcGISActualPolygon? =
        withContext(coroutine.coroutineContext) {
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
            graphic
        }

    override suspend fun updatePolygonProperties(
        polygon: ArcGISActualPolygon,
        current: PolygonEntity<ArcGISActualPolygon>,
        prev: PolygonEntity<ArcGISActualPolygon>,
    ): ArcGISActualPolygon? =
        withContext(coroutine.coroutineContext) {
            val finger = current.fingerPrint
            val prevFinger = prev.fingerPrint
            if (finger.points != prevFinger.points) {
                current.polygon.geometry = createGeometry(current.state)
            }

            (current.polygon.symbol as SimpleFillSymbol).let { symbol ->
                if (finger.fillColor != prevFinger.fillColor) {
                    symbol.color =
                        current.state.fillColor
                            .toArcGISColor()
                }
                symbol.outline?.let { outline ->
                    if (finger.strokeColor != prevFinger.strokeColor) {
                        outline.color =
                            current.state.strokeColor
                                .toArcGISColor()
                    }
                    if (finger.strokeWidth != prevFinger.strokeWidth) {
                        outline.width = ResourceProvider.dpToPx(current.state.strokeWidth).toFloat()
                    }
                }
            }
            polygon
        }

    override suspend fun removePolygon(entity: PolygonEntity<ArcGISActualPolygon>) {
        coroutine.launch {
            polygonLayer.graphics.remove(entity.polygon)
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
