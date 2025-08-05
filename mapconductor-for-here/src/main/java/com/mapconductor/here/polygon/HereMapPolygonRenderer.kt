package com.mapconductor.here.polygon

import androidx.compose.ui.graphics.toArgb
import com.here.sdk.core.Color
import com.here.sdk.core.GeoPolygon
import com.here.sdk.mapview.MapPolygon
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.polygon.AbstractPolygonRenderer
import com.mapconductor.core.polygon.PolygonEntity
import com.mapconductor.core.polygon.PolygonOverlayManager
import com.mapconductor.core.polygon.PolygonOverlayManagerImpl
import com.mapconductor.core.polygon.PolygonRenderer.UpdateParams
import com.mapconductor.core.polygon.PolygonRendererFactory
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.here.HereMapViewHolder
import com.mapconductor.here.toGeoCoordinates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class DefaultHereMapPolygonRenderer : PolygonRendererFactory<MapPolygon> {
    override fun create(
        onAdd: suspend (List<PolygonState>) -> List<MapPolygon?>,
        onChange: suspend (List<UpdateParams<MapPolygon>>) -> List<MapPolygon?>,
        onRemove: suspend (List<PolygonEntity<MapPolygon>>) -> Unit,
        onPostProcess: (suspend () -> Unit)?,
    ): PolygonOverlayManager<MapPolygon> =
        PolygonOverlayManagerImpl(
            onRemove = onRemove,
            onAdd = onAdd,
            onChange = onChange,
            onPostProcess = onPostProcess,
        )
}

class HereMapPolygonRenderer(
    override val holder: HereMapViewHolder,
    override val coroutine: CoroutineScope,
) : AbstractPolygonRenderer<MapPolygon>() {
    override suspend fun addPolygons(newPolygons: List<PolygonState>): List<MapPolygon?> {
        val polygons =
            newPolygons.map { state ->
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

    override suspend fun removePolygons(removeEntities: List<PolygonEntity<MapPolygon>>) {
        coroutine.launch {
            removeEntities.forEach { holder.map.removeMapPolygon(it.polygon) }
        }
    }

    override suspend fun changePolygon(changes: List<UpdateParams<MapPolygon>>): List<MapPolygon> {
        return changes.map { params ->
            val finger = params.entity.state.fingerPrint()
            val prevFinger = params.prevEntity.state.fingerPrint()
            if (finger.points != prevFinger.points) {
                val geoPolygon = createGeoPolygon(params.entity.state)
                params.entity.polygon.geometry = geoPolygon
            }
            if (finger.strokeColor != prevFinger.strokeColor) {
                params.entity.polygon.outlineColor =
                    Color.valueOf(
                        params.entity.state.strokeColor
                            .toArgb(),
                    )
            }
            if (finger.strokeWidth != prevFinger.strokeWidth) {
                val lineWidth =
                    ResourceProvider.dpToPx(
                        params.entity.state.strokeWidth.value
                            .toDouble(),
                    )
                params.entity.polygon.outlineWidth = lineWidth
            }
            if (finger.fillColor != prevFinger.fillColor) {
                params.entity.polygon.fillColor =
                    Color.valueOf(
                        params.entity.state.fillColor
                            .toArgb(),
                    )
            }
            return@map params.entity.polygon
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
