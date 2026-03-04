package com.mapconductor.here.polyline

import androidx.compose.ui.graphics.toArgb
import com.here.sdk.core.Color
import com.here.sdk.core.GeoPolyline
import com.here.sdk.mapview.LineCap
import com.here.sdk.mapview.MapMeasureDependentRenderSize
import com.here.sdk.mapview.MapPolyline
import com.here.sdk.mapview.RenderSize
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.polyline.AbstractPolylineOverlayRenderer
import com.mapconductor.core.polyline.PolylineEntityInterface
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.core.spherical.createInterpolatePoints
import com.mapconductor.core.spherical.createLinearInterpolatePoints
import com.mapconductor.here.HereActualPolyline
import com.mapconductor.here.HereViewHolder
import com.mapconductor.here.toGeoCoordinates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HerePolylineOverlayRenderer(
    override val holder: HereViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : AbstractPolylineOverlayRenderer<HereActualPolyline>() {
    override suspend fun createPolyline(state: PolylineState): HereActualPolyline? {
        val geoPolyline = createGeoPolyline(state)
        val representation = createRepresentation(state)
        val mapPolyline =
            MapPolyline(geoPolyline, representation).apply {
                drawOrder = state.zIndex
            }

        coroutine.launch {
            holder.map.addMapPolylines(listOf(mapPolyline))
        }

        return mapPolyline
    }

    override suspend fun updatePolylineProperties(
        polyline: HereActualPolyline,
        current: PolylineEntityInterface<HereActualPolyline>,
        prev: PolylineEntityInterface<HereActualPolyline>,
    ): HereActualPolyline? =
        withContext(coroutine.coroutineContext) {
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

            if (finger.zIndex != prevFinger.zIndex) {
                polyline.drawOrder = current.state.zIndex
                needsReAdd = true
            }

            if (needsReAdd) {
                coroutine.launch {
                    holder.map.removeMapPolylines(listOf(polyline))
                    holder.map.addMapPolylines(listOf(polyline))
                }
            }

            polyline
        }

    override suspend fun removePolyline(entity: PolylineEntityInterface<HereActualPolyline>) {
        coroutine.launch {
            holder.map.removeMapPolylines(listOf(entity.polyline))
        }
    }

    private fun createGeoPolyline(state: PolylineState): GeoPolyline {
        val geoPoints: List<GeoPointInterface> =
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
