package com.mapconductor.googlemaps.polygon

import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.maps.model.PolygonOptions
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.polygon.AbstractPolygonOverlayRenderer
import com.mapconductor.core.polygon.PolygonEntity
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.googlemaps.GoogleMapActualPolygon
import com.mapconductor.googlemaps.GoogleMapViewHolder
import com.mapconductor.googlemaps.toLatLng
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GoogleMapPolygonOverlayRenderer(
    override val holder: GoogleMapViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractPolygonOverlayRenderer<GoogleMapActualPolygon>() {
    override suspend fun removePolygon(entity: PolygonEntity<GoogleMapActualPolygon>) {
        coroutine.launch {
            entity.polygon.remove()
        }
    }

    override suspend fun createPolygon(state: PolygonState) =
        withContext(coroutine.coroutineContext) {
            val points = state.points.map { GeoPointImpl.from(it).toLatLng() }
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

    override suspend fun updatePolygonProperties(
        polygon: GoogleMapActualPolygon,
        current: PolygonEntity<GoogleMapActualPolygon>,
        prev: PolygonEntity<GoogleMapActualPolygon>,
    ): GoogleMapActualPolygon? =
        withContext(coroutine.coroutineContext) {
            val polygon = current.polygon
            val finger = current.fingerPrint
            val prevFinger = prev.fingerPrint
            Log.d("GoogleMaps", "----->$finger, $prevFinger")
            if (finger.points != prevFinger.points) {
                val points =
                    current.state.points
                        .map { GeoPointImpl.from(it).toLatLng() }
                polygon.points = points
            }
            polygon.strokeWidth = ResourceProvider.dpToPx(current.state.strokeWidth).toFloat()
            polygon.strokeColor =
                current.state.strokeColor.toArgb()
            polygon.fillColor =
                current.state.fillColor.toArgb()
            polygon
        }
}
