package com.mapconductor.here.polyline

import androidx.compose.ui.graphics.toArgb
import com.here.sdk.core.Color
import com.here.sdk.core.GeoPolyline
import com.here.sdk.mapview.LineCap
import com.here.sdk.mapview.MapMeasureDependentRenderSize
import com.here.sdk.mapview.MapPolyline
import com.here.sdk.mapview.RenderSize
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.createGeodesicPoints
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.polyline.AbstractPolylineRenderer
import com.mapconductor.core.polyline.PolylineEntity
import com.mapconductor.core.polyline.PolylineOverlayManager
import com.mapconductor.core.polyline.PolylineOverlayManagerImpl
import com.mapconductor.core.polyline.PolylineRenderer.UpdateParams
import com.mapconductor.core.polyline.PolylineRendererFactory
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.core.toDegree
import com.mapconductor.core.toRadius
import com.mapconductor.here.HereMapViewHolder
import com.mapconductor.here.toGeoCoordinates
import java.lang.Math.pow
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class DefaultHereMapPolylineRenderer : PolylineRendererFactory<MapPolyline> {
    override fun create(
        onAdd: suspend (List<PolylineState>) -> List<MapPolyline?>,
        onChange: suspend (List<UpdateParams<MapPolyline>>) -> List<MapPolyline?>,
        onRemove: suspend (List<PolylineEntity<MapPolyline>>) -> Unit,
        onPostProcess: (suspend () -> Unit)?,
    ): PolylineOverlayManager<MapPolyline> =
        PolylineOverlayManagerImpl(
            onRemove = onRemove,
            onAdd = onAdd,
            onChange = onChange,
            onPostProcess = onPostProcess,
        )
}

class HereMapPolylineRenderer(
    override val holder: HereMapViewHolder,
    override val coroutine: CoroutineScope,
) : AbstractPolylineRenderer<MapPolyline>() {
    override suspend fun addPolylines(newLines: List<PolylineState>): List<MapPolyline?> {
        val polylines =
            newLines.map { state ->
                val geoPolyline = createGeoPolyline(state)
                val representation = createRepresentation(state)
                MapPolyline(geoPolyline, representation)
            }
        coroutine.launch {
            holder.map.addMapPolylines(polylines)
        }
        return polylines
    }

    override suspend fun removePolylines(removeEntities: List<PolylineEntity<MapPolyline>>) {
        val polylines = removeEntities.map { it.polyline }
        coroutine.launch {
            holder.map.removeMapPolylines(polylines)
        }
    }

    override suspend fun changePolylines(changes: List<UpdateParams<MapPolyline>>): List<MapPolyline> {
        return changes.map { params ->
            val finger = params.entity.fingerPrint
            val prevFinger = params.prevEntity.fingerPrint
            if (finger.points != prevFinger.points) {
                val geoPolyline = createGeoPolyline(params.entity.state)
                params.entity.polyline.geometry = geoPolyline
            }
            if (finger.strokeColor != prevFinger.strokeColor || finger.strokeWidth != prevFinger.strokeColor) {
                val representation = createRepresentation(params.entity.state)
                params.entity.polyline.setRepresentation(representation)
            }
            return@map params.entity.polyline
        }
    }

    private fun createGeoPolyline(state: PolylineState): GeoPolyline {
        val points = when (state.geodesic) {
            false -> state.points.map { GeoPoint.from(it).toGeoCoordinates() }
            true -> {
                val results = createGeodesicPoints(state.points)
                results.map { GeoPoint.from(it).toGeoCoordinates() }
            }
        }
        val geoPolyline = GeoPolyline(points)
        return geoPolyline
    }


    private fun createRepresentation(state: PolylineState): MapPolyline.Representation {
        val lineWidth =
            MapMeasureDependentRenderSize(
                RenderSize.Unit.PIXELS,
                ResourceProvider.dpToPx(state.strokeWidth.value.toDouble()),
            )
        val lineColor = Color.valueOf(state.strokeColor.toArgb())
        val lineCap = LineCap.SQUARE
        return MapPolyline.SolidRepresentation(lineWidth, lineColor, lineCap)
    }
}
