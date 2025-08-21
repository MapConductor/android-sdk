package com.mapconductor.here.polyline

import androidx.compose.ui.graphics.toArgb
import com.here.sdk.core.Color
import com.here.sdk.core.GeoPolyline
import com.here.sdk.mapview.LineCap
import com.here.sdk.mapview.MapMeasureDependentRenderSize
import com.here.sdk.mapview.MapPolyline
import com.here.sdk.mapview.RenderSize
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.createInterpolatePoints
import com.mapconductor.core.createLinearInterpolatePoints
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.polyline.AbstractPolylineOverlayRenderer
import com.mapconductor.core.polyline.PolylineController
import com.mapconductor.core.polyline.PolylineEntity
import com.mapconductor.core.polyline.PolylineManager
import com.mapconductor.core.polyline.PolylineManagerImpl
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.here.HereMapActualPolyline
import com.mapconductor.here.HereMapViewHolder
import com.mapconductor.here.toGeoCoordinates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HerePolylineController(
    polylineManager: PolylineManager<HereMapActualPolyline> = PolylineManagerImpl(),
    renderer: HerePolylineOverlayRenderer,
) : PolylineController<HereMapActualPolyline>(polylineManager, renderer)

class HerePolylineOverlayRenderer(
    override val holder: HereMapViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : AbstractPolylineOverlayRenderer<HereMapActualPolyline>() {
    override suspend fun createPolyline(state: PolylineState): HereMapActualPolyline? {
        val geoPolyline = createGeoPolyline(state)
        val representation = createRepresentation(state)
        val mapPolyline = MapPolyline(geoPolyline, representation)

        coroutine.launch {
            holder.map.addMapPolylines(listOf(mapPolyline))
        }

        return mapPolyline
    }

    override suspend fun updatePolylineProperties(
        polyline: HereMapActualPolyline,
        current: PolylineEntity<HereMapActualPolyline>,
        prev: PolylineEntity<HereMapActualPolyline>,
    ): HereMapActualPolyline? {
        val finger = current.fingerPrint
        val prevFinger = prev.fingerPrint

        var needsReAdd = false

        if (finger.points != prevFinger.points || finger.geodesic != prevFinger.geodesic) {
            val geoPolyline = createGeoPolyline(current.state)
            polyline.geometry = geoPolyline
            needsReAdd = true
        }

        if (finger.strokeColor != prevFinger.strokeColor || finger.strokeWidth != prevFinger.strokeWidth) {
            val representation = createRepresentation(current.state)
            polyline.setRepresentation(representation)
            needsReAdd = true
        }

        if (needsReAdd) {
            coroutine.launch {
                holder.map.removeMapPolylines(listOf(polyline))
                holder.map.addMapPolylines(listOf(polyline))
            }
        }

        return polyline
    }

    override suspend fun removePolyline(entity: PolylineEntity<HereMapActualPolyline>) {
        coroutine.launch {
            holder.map.removeMapPolylines(listOf(entity.polyline))
        }
    }

    private fun createGeoPolyline(state: PolylineState): GeoPolyline {
        val geoPoints: List<IGeoPoint> =
            when (state.geodesic) {
                true -> createInterpolatePoints(state.points)
                false -> createLinearInterpolatePoints(state.points)
            }
        val points = geoPoints.map { GeoPoint.from(it).toGeoCoordinates() }
        return GeoPolyline(points)
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
