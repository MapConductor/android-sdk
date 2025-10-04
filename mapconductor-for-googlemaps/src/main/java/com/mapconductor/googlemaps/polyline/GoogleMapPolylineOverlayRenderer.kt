package com.mapconductor.googlemaps.polyline

import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.createInterpolatePoints
import com.mapconductor.core.createLinearInterpolatePoints
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.polyline.AbstractPolylineOverlayRenderer
import com.mapconductor.core.polyline.PolylineEntity
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.googlemaps.GoogleMapActualPolyline
import com.mapconductor.googlemaps.GoogleMapViewHolder
import com.mapconductor.googlemaps.toLatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GoogleMapPolylineOverlayRenderer(
    override val holder: GoogleMapViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractPolylineOverlayRenderer<GoogleMapActualPolyline>() {
    override suspend fun createPolyline(state: PolylineState): GoogleMapActualPolyline? =
        withContext(coroutine.coroutineContext) {
            val geoPoints: List<GeoPoint> =
                when (state.geodesic) {
                    true -> createInterpolatePoints(state.points)
                    false -> createLinearInterpolatePoints(state.points)
                }
            val points = geoPoints.map { GeoPointImpl.from(it).toLatLng() }
            val options =
                PolylineOptions()
                    .addAll(points)
                    .color(state.strokeColor.toArgb())
                    .width(ResourceProvider.dpToPx(state.strokeWidth).toFloat())
                    .geodesic(false)

            holder.map.addPolyline(options).also {
                it.tag = state.id
            }
        }

    override suspend fun updatePolylineProperties(
        polyline: GoogleMapActualPolyline,
        current: PolylineEntity<GoogleMapActualPolyline>,
        prev: PolylineEntity<GoogleMapActualPolyline>,
    ): Polyline? =
        withContext(coroutine.coroutineContext) {
            val finger = current.fingerPrint
            val prevFinger = prev.fingerPrint

            if (finger.points != prevFinger.points || finger.geodesic != prevFinger.geodesic) {
                val geoPoints: List<GeoPoint> =
                    when (current.state.geodesic) {
                        true -> createInterpolatePoints(current.state.points)
                        false -> createLinearInterpolatePoints(current.state.points)
                    }
                val points = geoPoints.map { GeoPointImpl.from(it).toLatLng() }
                polyline.points = points
            }

            if (finger.strokeWidth != prevFinger.strokeWidth) {
                polyline.width = ResourceProvider.dpToPx(current.state.strokeWidth).toFloat()
            }

            if (finger.strokeColor != prevFinger.strokeColor) {
                polyline.color = current.state.strokeColor.toArgb()
            }

            polyline
        }

    override suspend fun removePolyline(entity: PolylineEntity<GoogleMapActualPolyline>) {
        coroutine.launch {
            entity.polyline.remove()
        }
    }
}
