package com.mapconductor.here.polygon

import androidx.compose.ui.graphics.toArgb
import com.here.sdk.core.Color
import com.here.sdk.core.GeoPolygon
import com.here.sdk.mapview.LineCap
import com.here.sdk.mapview.MapMeasureDependentRenderSize
import com.here.sdk.mapview.MapPolygon
import com.here.sdk.mapview.MapPolyline
import com.here.sdk.mapview.RenderSize
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.controller.OverlayRenderer
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.polygon.AbstractPolygonOverlayRenderer
import com.mapconductor.core.polygon.PolygonController
import com.mapconductor.core.polygon.PolygonEntity
import com.mapconductor.core.polygon.PolygonManager
import com.mapconductor.core.polygon.PolygonManagerImpl
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.here.HereActualPolygon
import com.mapconductor.here.HereViewHolder
import com.mapconductor.here.toGeoCoordinates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HerePolygonController(
    polygonManager: PolygonManager<HereActualPolygon> = PolygonManagerImpl(),
    renderer: HerePolygonOverlayRenderer,
) : PolygonController<HereActualPolygon>(polygonManager, renderer)

class HerePolygonOverlayRenderer(
    override val holder: HereViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : AbstractPolygonOverlayRenderer<HereActualPolygon>() {
    override suspend fun removePolygon(entity: PolygonEntity<HereActualPolygon>) {
        coroutine.launch {
            holder.map.removeMapPolygon(entity.polygon)
        }
    }

    override suspend fun createPolygon(state: PolygonState): HereActualPolygon? {
        val geoPolygon = createGeoPolygon(state)
        val lineWidth = ResourceProvider.dpToPx(state.strokeWidth.value.toDouble())
        val mapPolygon = MapPolygon(
            geoPolygon,
            Color.valueOf(state.fillColor.toArgb()),
            Color.valueOf(state.strokeColor.toArgb()),
            lineWidth,
        )
        coroutine.launch {
            holder.map.addMapPolygon(mapPolygon)
        }
        return mapPolygon
    }

    override suspend fun updatePolygonProperties(
        polygon: HereActualPolygon,
        current: PolygonEntity<HereActualPolygon>,
        prev: PolygonEntity<HereActualPolygon>
    ): HereActualPolygon? =
        withContext(coroutine.coroutineContext) {
            val finger = current.fingerPrint
            val prevFinger = prev.fingerPrint

            if (finger.points != prevFinger.points) {
                val geoPolygon = createGeoPolygon(current.state)
                current.polygon.geometry = geoPolygon
            }
            if (finger.strokeColor != prevFinger.strokeColor) {
                current.polygon.outlineColor =
                    Color.valueOf(
                        current.state.strokeColor.toArgb(),
                    )
            }
            if (finger.strokeWidth != prevFinger.strokeWidth) {
                val lineWidth =
                    ResourceProvider.dpToPx(
                        current.state.strokeWidth.value.toDouble(),
                    )
                current.polygon.outlineWidth = lineWidth
            }
            if (finger.fillColor != prevFinger.fillColor) {
                current.polygon.fillColor =
                    Color.valueOf(
                        current.state.fillColor.toArgb(),
                    )
            }
            polygon
        }

    private fun createGeoPolygon(state: PolygonState): GeoPolygon {
        val points = state.points.map { GeoPoint.from(it).toGeoCoordinates() }
        // Ensure the polygon is closed by adding the first point at the end if not already closed
        val closedPoints =
            if (points.first() != points.last()) {
                points + points.first()
            } else {
                points
            }
        return GeoPolygon(closedPoints)
    }
}
