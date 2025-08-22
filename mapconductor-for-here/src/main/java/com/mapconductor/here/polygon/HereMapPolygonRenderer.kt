package com.mapconductor.here.polygon

import androidx.compose.ui.graphics.toArgb
import com.here.sdk.core.Color
import com.here.sdk.core.GeoPolygon
import com.here.sdk.mapview.MapPolygon
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.controller.OverlayRenderer
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.polygon.PolygonEntity
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.here.HereMapViewHolder
import com.mapconductor.here.toGeoCoordinates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class HereMapPolygonRenderer(
    val holder: HereMapViewHolder,
    val coroutine: CoroutineScope,
) : OverlayRenderer<MapPolygon, PolygonState, PolygonEntity<MapPolygon>> {
    override suspend fun onAdd(data: List<PolygonState>): List<MapPolygon?> {
        val polygons =
            data.map { state ->
                val geoPolygon = createGeoPolygon(state)
                val lineWidth = ResourceProvider.dpToPx(state.strokeWidth.value.toDouble())
                MapPolygon(
                    geoPolygon,
                    Color.valueOf(state.fillColor.toArgb()),
                    Color.valueOf(state.strokeColor.toArgb()),
                    lineWidth,
                )
            }
        coroutine.launch {
            polygons.forEach { holder.map.addMapPolygon(it) }
        }
        return polygons
    }

    override suspend fun onRemove(data: List<PolygonEntity<MapPolygon>>) {
        coroutine.launch {
            data.forEach { holder.map.removeMapPolygon(it.polygon) }
        }
    }

    override suspend fun onPostProcess() {
        // Do nothing here
    }

    override suspend fun onChange(
        data: List<OverlayRenderer.ChangeParams<PolygonEntity<MapPolygon>>>,
    ): List<MapPolygon?> {
        return data.map { params ->
            val finger = params.current.fingerPrint
            val prevFinger = params.prev.fingerPrint
            if (finger.points != prevFinger.points) {
                val geoPolygon = createGeoPolygon(params.current.state)
                params.current.polygon.geometry = geoPolygon
            }
            if (finger.strokeColor != prevFinger.strokeColor) {
                params.current.polygon.outlineColor =
                    Color.valueOf(
                        params.current.state.strokeColor
                            .toArgb(),
                    )
            }
            if (finger.strokeWidth != prevFinger.strokeWidth) {
                val lineWidth =
                    ResourceProvider.dpToPx(
                        params.current.state.strokeWidth.value
                            .toDouble(),
                    )
                params.current.polygon.outlineWidth = lineWidth
            }
            if (finger.fillColor != prevFinger.fillColor) {
                params.current.polygon.fillColor =
                    Color.valueOf(
                        params.current.state.fillColor
                            .toArgb(),
                    )
            }
            return@map params.current.polygon
        }
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
