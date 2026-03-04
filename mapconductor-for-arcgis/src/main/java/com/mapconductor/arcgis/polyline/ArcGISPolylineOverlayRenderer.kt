package com.mapconductor.arcgis.polyline

import com.arcgismaps.geometry.Geometry
import com.arcgismaps.geometry.PolylineBuilder
import com.arcgismaps.mapping.symbology.SimpleLineSymbol
import com.arcgismaps.mapping.symbology.SimpleLineSymbolStyle
import com.arcgismaps.mapping.view.Graphic
import com.arcgismaps.mapping.view.GraphicsOverlay
import com.mapconductor.arcgis.ArcGISActualPolyline
import com.mapconductor.arcgis.map.ArcGISMapViewHolder
import com.mapconductor.arcgis.toArcGISColor
import com.mapconductor.arcgis.toPoint
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.polyline.AbstractPolylineOverlayRenderer
import com.mapconductor.core.polyline.PolylineEntityInterface
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.core.spherical.Spherical
import kotlin.collections.set
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ArcGISPolylineOverlayRenderer(
    val polylineLayer: GraphicsOverlay,
    override val holder: ArcGISMapViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : AbstractPolylineOverlayRenderer<ArcGISActualPolyline>() {
    override suspend fun createPolyline(state: PolylineState): ArcGISActualPolyline? =
        withContext(coroutine.coroutineContext) {
            val geometry = createGeometry(state)

            val lineSymbol =
                SimpleLineSymbol().apply {
                    style = SimpleLineSymbolStyle.Solid
                    color = state.strokeColor.toArcGISColor()
                    width = state.strokeWidth.value.toFloat()
                }

            val graphic =
                Graphic(geometry, lineSymbol).also {
                    it.attributes.set("id", state.id)
                }

            polylineLayer.graphics.add(graphic)
            graphic
        }

    override suspend fun updatePolylineProperties(
        polyline: ArcGISActualPolyline,
        current: PolylineEntityInterface<ArcGISActualPolyline>,
        prev: PolylineEntityInterface<ArcGISActualPolyline>,
    ): ArcGISActualPolyline? =
        withContext(coroutine.coroutineContext) {
            val finger = current.fingerPrint
            val prevFinger = prev.fingerPrint

            if (finger.points != prevFinger.points || finger.geodesic != prevFinger.geodesic) {
                polyline.geometry = createGeometry(current.state)
            }

            (polyline.symbol as SimpleLineSymbol).let { symbol ->
                if (finger.strokeColor != prevFinger.strokeColor) {
                    symbol.color = current.state.strokeColor.toArcGISColor()
                }
                if (finger.strokeWidth != prevFinger.strokeWidth) {
                    symbol.width = ResourceProvider.dpToPx(current.state.strokeWidth).toFloat()
                }
            }

            polyline
        }

    override suspend fun removePolyline(entity: PolylineEntityInterface<ArcGISActualPolyline>) {
        coroutine.launch {
            polylineLayer.graphics.remove(entity.polyline)
        }
    }

    private fun createGeometry(state: PolylineState): Geometry {
        val polylineBuilder =
            PolylineBuilder().also { builder ->
                if (state.geodesic) {
                    state.points.forEach {
                        builder.addPoint(GeoPoint.from(it).toPoint())
                    }
                    return@also
                }

                builder.addPoint(GeoPoint.from(state.points[0]).toPoint())
                for (i in 1 until state.points.size) {
                    var fraction = 0.0
                    while (fraction <= 1.0) {
                        val point =
                            Spherical.linearInterpolate(
                                from = state.points[i - 1],
                                to = state.points[i],
                                fraction = fraction,
                            )
                        builder.addPoint(point.toPoint())
                        fraction += 0.01
                    }
                    builder.addPoint(GeoPoint.from(state.points[i]).toPoint())
                }
            }
        return polylineBuilder.toGeometry()
    }
}
