package com.mapconductor.googlemaps.polygon

import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.polygon.AbstractPolygonRenderer
import com.mapconductor.core.polygon.PolygonEntity
import com.mapconductor.core.polygon.PolygonOverlayManager
import com.mapconductor.core.polygon.PolygonOverlayManagerImpl
import com.mapconductor.core.polygon.PolygonRenderer.UpdateParams
import com.mapconductor.core.polygon.PolygonRendererFactory
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.googlemaps.GoogleMapViewHolder
import com.mapconductor.googlemaps.toLatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DefaultGoogleMapPolygonRenderer : PolygonRendererFactory<Polygon> {
    override fun create(
        onAdd: suspend (List<PolygonState>) -> List<Polygon?>,
        onChange: suspend (List<UpdateParams<Polygon>>) -> List<Polygon?>,
        onRemove: suspend (List<PolygonEntity<Polygon>>) -> Unit,
        onPostProcess: (suspend () -> Unit)?,
    ): PolygonOverlayManager<Polygon> =
        PolygonOverlayManagerImpl(
            onRemove = onRemove,
            onAdd = onAdd,
            onChange = onChange,
            onPostProcess = onPostProcess,
        )
}

class GoogleMapPolygonRenderer(
    override val holder: GoogleMapViewHolder,
    override val coroutine: CoroutineScope,
) : AbstractPolygonRenderer<Polygon>() {
    override suspend fun addPolygons(newPolygons: List<PolygonState>): List<Polygon?> {
        return withContext(coroutine.coroutineContext) {
            return@withContext newPolygons.map { state ->
                val points = state.points.map { GeoPoint.from(it).toLatLng() }
                val options =
                    PolygonOptions()
                        .addAll(points)
                        .strokeColor(state.strokeColor.toArgb())
                        .strokeWidth(ResourceProvider.dpToPx(state.strokeWidth).toFloat())
                        .fillColor(state.fillColor.toArgb())
                        .clickable(true)
                holder.map.addPolygon(options).also {
                    it.tag = state.id
                }
            }
        }
    }

    override suspend fun removePolygons(removeEntities: List<PolygonEntity<Polygon>>) {
        coroutine.launch {
            removeEntities.forEach { params -> params.polygon.remove() }
        }
    }

    override suspend fun changePolygon(changes: List<UpdateParams<Polygon>>): List<Polygon> {
        return withContext(coroutine.coroutineContext) {
            return@withContext changes.map { params ->
                val polygon = params.entity.polygon
                val finger = params.entity.state.fingerPrint()
                val prevFinger = params.prevEntity.state.fingerPrint()
                if (finger.points != prevFinger.points) {
                    val points =
                        params.entity.state.points
                            .map { GeoPoint.from(it).toLatLng() }
                    polygon.points = points
                }
                polygon.strokeWidth = ResourceProvider.dpToPx(params.entity.state.strokeWidth).toFloat()
                polygon.strokeColor = params.entity.state.strokeColor.toArgb()
                polygon.fillColor = params.entity.state.fillColor.toArgb()
                return@map polygon
            }
        }
    }
}