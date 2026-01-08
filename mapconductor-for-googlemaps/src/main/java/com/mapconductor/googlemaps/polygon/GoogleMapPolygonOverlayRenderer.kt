package com.mapconductor.googlemaps.polygon

import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.maps.model.PolygonOptions
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.polygon.AbstractPolygonOverlayRenderer
import com.mapconductor.core.polygon.PolygonEntityInterface
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.core.spherical.createInterpolatePoints
import com.mapconductor.core.spherical.createLinearInterpolatePoints
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
    override suspend fun removePolygon(entity: PolygonEntityInterface<GoogleMapActualPolygon>) {
        coroutine.launch {
            entity.polygon.remove()
        }
    }

    override suspend fun createPolygon(state: PolygonState) =
        withContext(coroutine.coroutineContext) {
            val geoPoints =
                when (state.geodesic) {
                    true -> createInterpolatePoints(state.points)
                    false -> createLinearInterpolatePoints(state.points)
                }
            val points = geoPoints.map { GeoPoint.from(it).toLatLng() }
            val options =
                PolygonOptions()
                    .addAll(points)
                    .strokeColor(state.strokeColor.toArgb())
                    .strokeWidth(ResourceProvider.dpToPx(state.strokeWidth).toFloat())
                    .fillColor(state.fillColor.toArgb())
                    .zIndex(state.zIndex.toFloat())
                    .clickable(false)
            holder.map.addPolygon(options)?.also {
                it.tag = state.id
            }
        }

    override suspend fun updatePolygonProperties(
        polygon: GoogleMapActualPolygon,
        current: PolygonEntityInterface<GoogleMapActualPolygon>,
        prev: PolygonEntityInterface<GoogleMapActualPolygon>,
    ): GoogleMapActualPolygon? =
        withContext(coroutine.coroutineContext) {
            val polygon = current.polygon
            val finger = current.fingerPrint
            val prevFinger = prev.fingerPrint
            Log.d("GoogleMaps", "----->$finger, $prevFinger")
            if (finger.points != prevFinger.points || finger.geodesic != prevFinger.geodesic) {
                val geoPoints =
                    when (current.state.geodesic) {
                        true -> createInterpolatePoints(current.state.points)
                        false -> createLinearInterpolatePoints(current.state.points)
                    }
                val points = geoPoints.map { GeoPoint.from(it).toLatLng() }
                polygon.points = points
            }
            polygon.strokeWidth = ResourceProvider.dpToPx(current.state.strokeWidth).toFloat()
            polygon.strokeColor =
                current.state.strokeColor.toArgb()
            polygon.fillColor =
                current.state.fillColor.toArgb()
            polygon.zIndex = current.state.zIndex.toFloat()
            polygon
        }
}
