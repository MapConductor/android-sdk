package com.mapconductor.googlemaps.polygon

import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.maps.model.PolygonOptions
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.controller.OverlayRenderer
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.polygon.PolygonEntity
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.googlemaps.GoogleMapActualPolygon
import com.mapconductor.googlemaps.GoogleMapViewHolder
import com.mapconductor.googlemaps.toLatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GoogleMapPolygonRenderer(
    val holder: GoogleMapViewHolder,
    val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : OverlayRenderer<GoogleMapActualPolygon, PolygonState, PolygonEntity<GoogleMapActualPolygon>> {
    override suspend fun onAdd(data: List<PolygonState>): List<GoogleMapActualPolygon?> {
        return withContext(coroutine.coroutineContext) {
            return@withContext data.map { state ->
                val points = state.points.map { GeoPoint.from(it).toLatLng() }
                val options =
                    PolygonOptions()
                        .addAll(points)
                        .strokeColor(state.strokeColor.toArgb())
                        .strokeWidth(ResourceProvider.dpToPx(state.strokeWidth).toFloat())
                        .fillColor(state.fillColor.toArgb())
                        .clickable(true)
                holder.map.addPolygon(options)?.also {
                    it.tag = state.id
                }
            }
        }
    }

    override suspend fun onRemove(data: List<PolygonEntity<GoogleMapActualPolygon>>) {
        coroutine.launch {
            data.forEach { entity -> entity.polygon.remove() }
        }
    }

    override suspend fun onPostProcess() {
        // Do nothing here
    }

    override suspend fun onChange(
        data: List<OverlayRenderer.ChangeParams<PolygonEntity<GoogleMapActualPolygon>>>,
    ): List<GoogleMapActualPolygon?> {
        return withContext(coroutine.coroutineContext) {
            return@withContext data.map { params ->
                val polygon = params.current.polygon
                val finger = params.current.fingerPrint
                val prevFinger = params.prev.fingerPrint
                if (finger.points != prevFinger.points) {
                    val points =
                        params.current.state.points
                            .map { GeoPoint.from(it).toLatLng() }
                    polygon.points = points
                }
                polygon.strokeWidth = ResourceProvider.dpToPx(params.current.state.strokeWidth).toFloat()
                polygon.strokeColor =
                    params.current.state.strokeColor
                        .toArgb()
                polygon.fillColor =
                    params.current.state.fillColor
                        .toArgb()
                return@map polygon
            }
        }
    }
}
