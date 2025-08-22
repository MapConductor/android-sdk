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
import com.mapconductor.core.controller.OverlayRenderer
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.polygon.PolygonEntity
import com.mapconductor.core.polygon.PolygonState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ArcGISPolygonRenderer(
    val polygonLayer: GraphicsOverlay,
    val holder: ArcGISMapViewHolder,
    val coroutine: CoroutineScope,
) : OverlayRenderer<ArcGISActualPolygon, PolygonState, PolygonEntity<ArcGISActualPolygon>> {
    override suspend fun onAdd(data: List<PolygonState>): List<ArcGISActualPolygon?> {
        return withContext(coroutine.coroutineContext) {
            return@withContext data.map { state ->

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

    override suspend fun onRemove(data: List<PolygonEntity<ArcGISActualPolygon>>) {
        val polygons = data.map { it.polygon }
        coroutine.launch {
            polygonLayer.graphics.removeAll(polygons)
        }
    }

    override suspend fun onPostProcess() {
        // Do nothing here
    }

    override suspend fun onChange(
        data: List<OverlayRenderer.ChangeParams<PolygonEntity<ArcGISActualPolygon>>>,
    ): List<ArcGISActualPolygon?> {
        return withContext(coroutine.coroutineContext) {
            return@withContext data.map { params ->
                val finger = params.current.fingerPrint
                val prevFinger = params.prev.fingerPrint
                if (finger.points != prevFinger.points) {
                    params.current.polygon.geometry = createGeometry(params.current.state)
                }

                (params.current.polygon.symbol as SimpleFillSymbol).let { symbol ->
                    if (finger.fillColor != prevFinger.fillColor) {
                        symbol.color =
                            params.current.state.fillColor
                                .toArcGISColor()
                    }
                    symbol.outline?.let { outline ->
                        if (finger.strokeColor != prevFinger.strokeColor) {
                            outline.color =
                                params.current.state.strokeColor
                                    .toArcGISColor()
                        }
                        if (finger.strokeWidth != prevFinger.strokeWidth) {
                            outline.width = ResourceProvider.dpToPx(params.current.state.strokeWidth).toFloat()
                        }
                    }
                }
                return@map params.current.polygon
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
