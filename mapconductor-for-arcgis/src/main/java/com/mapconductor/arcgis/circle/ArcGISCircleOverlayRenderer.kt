package com.mapconductor.arcgis.circle

import com.arcgismaps.geometry.GeodeticCurveType
import com.arcgismaps.geometry.GeometryEngine
import com.arcgismaps.geometry.LinearUnit
import com.arcgismaps.geometry.LinearUnitId
import com.arcgismaps.mapping.symbology.SimpleFillSymbol
import com.arcgismaps.mapping.symbology.SimpleFillSymbolStyle
import com.arcgismaps.mapping.symbology.SimpleLineSymbol
import com.arcgismaps.mapping.symbology.SimpleLineSymbolStyle
import com.arcgismaps.mapping.view.Graphic
import com.arcgismaps.mapping.view.GraphicsOverlay
import com.mapconductor.arcgis.ArcGISActualCircle
import com.mapconductor.arcgis.ArcGISMapViewHolder
import com.mapconductor.arcgis.toArcGISColor
import com.mapconductor.arcgis.toPoint
import com.mapconductor.core.circle.AbstractCircleOverlayRenderer
import com.mapconductor.core.circle.CircleEntity
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.features.GeoPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ArcGISCircleOverlayRenderer(
    val circleLayer: GraphicsOverlay,
    override val holder: ArcGISMapViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractCircleOverlayRenderer<ArcGISActualCircle>() {
    override suspend fun createCircle(state: CircleState): ArcGISActualCircle? =
        withContext(coroutine.coroutineContext) {
            val spec =
                holder.mapView.sceneView.scene
                    ?.spatialReference
            val centerPoint = GeoPoint.from(state.center).toPoint(spec)
            val circleGeometry =
                GeometryEngine.bufferGeodeticOrNull(
                    geometry = centerPoint,
                    distance = state.radiusMeters,
                    distanceUnit = LinearUnit(LinearUnitId.Meters),
                    maxDeviation = Double.NaN,
                    curveType = GeodeticCurveType.NormalSection,
                )
            val stroke =
                SimpleLineSymbol(
                    style = SimpleLineSymbolStyle.Solid,
                    color = state.strokeColor.toArcGISColor(),
                    width = state.strokeWidth.value,
                )
            val fillSymbol =
                SimpleFillSymbol(
                    style = SimpleFillSymbolStyle.Solid,
                    color = state.fillColor.toArcGISColor(),
                    outline = stroke,
                )
            val circle = Graphic(circleGeometry, fillSymbol)

            circleLayer.graphics.add(circle)
            circle
        }

    override suspend fun removeCircle(entity: CircleEntity<ArcGISActualCircle>) {
        coroutine.launch {
            val circles = listOf(entity.circle)
            circleLayer.graphics.removeAll(circles)
        }
    }

    override suspend fun updateCircleProperties(
        circle: ArcGISActualCircle,
        current: CircleEntity<ArcGISActualCircle>,
        prev: CircleEntity<ArcGISActualCircle>,
    ): ArcGISActualCircle? =
        withContext(coroutine.coroutineContext) {
            val spec =
                holder.mapView.sceneView.scene
                    ?.spatialReference
            val finger = current.fingerPrint
            val prevFinger = prev.fingerPrint
            val graphic = current.circle

            if (finger.center != prevFinger.center || finger.radiusMeters != prevFinger.radiusMeters) {
                val centerPoint = GeoPoint.from(current.state.center).toPoint(spec)

                val newGeometry =
                    GeometryEngine.bufferGeodeticOrNull(
                        geometry = centerPoint,
                        distance = current.state.radiusMeters,
                        distanceUnit = LinearUnit(LinearUnitId.Meters),
                        maxDeviation = Double.NaN,
                        curveType = GeodeticCurveType.NormalSection,
                    )
                newGeometry?.let {
                    graphic.geometry = it
                }
            }

            (graphic.symbol as SimpleFillSymbol).let { symbol ->
                if (finger.fillColor != prevFinger.fillColor) {
                    symbol.color =
                        current.state.fillColor.toArcGISColor()
                }
                symbol.outline?.let { outline ->
                    if (finger.strokeColor != prevFinger.strokeColor) {
                        outline.color =
                            current.state.strokeColor.toArcGISColor()
                    }
                    if (finger.strokeWidth != prevFinger.strokeWidth) {
                        outline.width = current.state.strokeWidth.value
                    }
                }
            }
            return@withContext graphic
        }
}
