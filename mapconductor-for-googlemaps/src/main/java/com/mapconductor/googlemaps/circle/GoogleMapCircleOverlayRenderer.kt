package com.mapconductor.googlemaps.circle

import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.maps.model.CircleOptions
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.circle.AbstractCircleOverlayRenderer
import com.mapconductor.core.circle.CircleEntity
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.googlemaps.GoogleMapActualCircle
import com.mapconductor.googlemaps.GoogleMapViewHolder
import com.mapconductor.googlemaps.toLatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GoogleMapCircleOverlayRenderer(
    override val holder: GoogleMapViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractCircleOverlayRenderer<GoogleMapActualCircle>() {
    override suspend fun createCircle(state: CircleState): GoogleMapActualCircle? =
        withContext(coroutine.coroutineContext) {
            val center = GeoPointImpl.from(state.center).toLatLng()
            val options =
                CircleOptions()
                    .center(center)
                    .radius(state.radiusMeters)
                    .strokeColor(state.strokeColor.toArgb())
                    .strokeWidth(ResourceProvider.dpToPx(state.strokeWidth).toFloat())
                    .fillColor(state.fillColor.toArgb())
                    .clickable(false)
            holder.map.addCircle(options).also {
                it.tag = state.id
            }
        }

    override suspend fun removeCircle(entity: CircleEntity<GoogleMapActualCircle>) {
        coroutine.launch {
            entity.circle.remove()
        }
    }

    override suspend fun updateCircleProperties(
        circle: GoogleMapActualCircle,
        current: CircleEntity<GoogleMapActualCircle>,
        prev: CircleEntity<GoogleMapActualCircle>,
    ): GoogleMapActualCircle? =
        withContext(coroutine.coroutineContext) {
            val finger = current.fingerPrint
            val prevFinger = prev.fingerPrint

            if (finger.center != prevFinger.center) {
                circle.center = GeoPointImpl.from(current.state.center).toLatLng()
            }
            if (finger.radiusMeters != prevFinger.radiusMeters) {
                circle.radius = current.state.radiusMeters
            }
            if (finger.strokeColor != prevFinger.strokeColor) {
                circle.strokeColor =
                    current.state.strokeColor.toArgb()
            }
            if (finger.strokeWidth != prevFinger.strokeWidth) {
                circle.strokeWidth = ResourceProvider.dpToPx(current.state.strokeWidth).toFloat()
            }
            if (finger.fillColor != prevFinger.fillColor) {
                circle.fillColor =
                    current.state.fillColor.toArgb()
            }
            circle
        }
}
