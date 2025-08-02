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
import com.mapconductor.arcgis.ArcGISMapViewHolder
import com.mapconductor.arcgis.toArcGISColor
import com.mapconductor.arcgis.toPoint
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
            val spec =
                holder.mapView.sceneView.scene
                    ?.spatialReference
            return@withContext changes.map { params ->
                val finger = params.entity.fingerPrint
                val prevFinger = params.prevEntity.fingerPrint
                val graphic = params.entity.circle

                if (finger.center != prevFinger.center || finger.radiusMeters != prevFinger.radiusMeters) {
                    val centerPoint = GeoPoint.from(params.entity.state.center).toPoint(spec)

                    val newGeometry =
                        GeometryEngine.bufferGeodeticOrNull(
                            geometry = centerPoint,
                            distance = params.entity.state.radiusMeters,
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
                            params.entity.state.fillColor
                                .toArcGISColor()
                    }
                    symbol.outline?.let { outline ->
                        if (finger.strokeColor != prevFinger.strokeColor) {
                            outline.color =
                                params.entity.state.strokeColor
                                    .toArcGISColor()
                        }
                        if (finger.strokeWidth != prevFinger.strokeWidth) {
                            outline.width = params.entity.state.strokeWidth.value
                        }
                    }
                }
                graphic
            }
        }
    }
}
