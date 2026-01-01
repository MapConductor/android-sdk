package com.mapconductor.googlemaps.circle

import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.maps.model.PolygonOptions
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
import kotlinx.coroutines.withContext

class GoogleMapCircleOverlayRenderer(
    override val holder: GoogleMapViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractCircleOverlayRenderer<GoogleMapActualCircle>() {
    override suspend fun createCircle(state: CircleState): GoogleMapActualCircle? =
        withContext(coroutine.coroutineContext) {
            val center = GeoPointImpl.from(state.center).toLatLng()
            val circlePoints =
                CirclePolygonHelper.generateCirclePoints(
                    center = center,
                    radiusMeters = state.radiusMeters,
                    geodesic = state.geodesic,
                )

            val options =
                PolygonOptions()
                    .addAll(circlePoints)
                    .strokeColor(state.strokeColor.toArgb())
                    .strokeWidth(ResourceProvider.dpToPx(state.strokeWidth).toFloat())
                    .fillColor(state.fillColor.toArgb())
                    .clickable(false)
                    .geodesic(state.geodesic)
            holder.map.addPolygon(options).also {
                it.tag = state.id
            }
        }

    override suspend fun removeCircle(entity: CircleEntity<GoogleMapActualCircle>) {
        withContext(coroutine.coroutineContext) {
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

            // If center, radius, or geodesic changed, we need to regenerate the polygon points
            val needsRegeneration =
                finger.center != prevFinger.center ||
                    finger.radiusMeters != prevFinger.radiusMeters ||
                    finger.geodesic != prevFinger.geodesic

            if (needsRegeneration) {
                val center = GeoPointImpl.from(current.state.center).toLatLng()
                val circlePoints =
                    CirclePolygonHelper.generateCirclePoints(
                        center = center,
                        radiusMeters = current.state.radiusMeters,
                        geodesic = current.state.geodesic,
                    )
                circle.points = circlePoints
                circle.isGeodesic = current.state.geodesic
            }

            if (finger.strokeColor != prevFinger.strokeColor) {
                circle.strokeColor = current.state.strokeColor.toArgb()
            }
            if (finger.strokeWidth != prevFinger.strokeWidth) {
                circle.strokeWidth = ResourceProvider.dpToPx(current.state.strokeWidth).toFloat()
            }
            if (finger.fillColor != prevFinger.fillColor) {
                circle.fillColor = current.state.fillColor.toArgb()
            }
            circle
        }
}
