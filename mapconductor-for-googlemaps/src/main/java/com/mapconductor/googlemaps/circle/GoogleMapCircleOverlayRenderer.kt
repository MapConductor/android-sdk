package com.mapconductor.googlemaps.circle

import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.maps.model.PolygonOptions
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.calculateZIndex
import com.mapconductor.core.circle.AbstractCircleOverlayRenderer
import com.mapconductor.core.circle.CircleEntityInterface
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.spherical.Spherical.computeDistanceBetween
import com.mapconductor.googlemaps.GoogleMapActualCircle
import com.mapconductor.googlemaps.GoogleMapViewHolder
import com.mapconductor.googlemaps.toLatLng
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GoogleMapCircleOverlayRenderer(
    override val holder: GoogleMapViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractCircleOverlayRenderer<GoogleMapActualCircle>() {
    override suspend fun createCircle(state: CircleState): GoogleMapActualCircle? =
        withContext(coroutine.coroutineContext) {
            val center = GeoPoint.from(state.center).toLatLng()
            val adjustedRadiusMeters = adjustedRadiusMeters(state, center)
            val circlePoints =
                CirclePolygonHelper.generateCirclePoints(
                    center = center,
                    radiusMeters = adjustedRadiusMeters,
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
                    .zIndex((state.zIndex ?: calculateZIndex(state.center)).toFloat())
            holder.map.addPolygon(options).also {
                it.tag = state.id
            }
        }

    override suspend fun removeCircle(entity: CircleEntityInterface<GoogleMapActualCircle>) {
        withContext(coroutine.coroutineContext) {
            entity.circle.remove()
        }
    }

    override suspend fun updateCircleProperties(
        circle: GoogleMapActualCircle,
        current: CircleEntityInterface<GoogleMapActualCircle>,
        prev: CircleEntityInterface<GoogleMapActualCircle>,
    ): GoogleMapActualCircle? =
        withContext(coroutine.coroutineContext) {
            val finger = current.fingerPrint
            val prevFinger = prev.fingerPrint

            // If center, radius, or geodesic changed, we need to regenerate the polygon points
            val needsRegeneration =
                finger.center != prevFinger.center ||
                    finger.radiusMeters != prevFinger.radiusMeters ||
                    finger.geodesic != prevFinger.geodesic ||
                    finger.strokeWidth != prevFinger.strokeWidth

            if (needsRegeneration) {
                val center = GeoPoint.from(current.state.center).toLatLng()
                val adjustedRadiusMeters = adjustedRadiusMeters(current.state, center)
                val circlePoints =
                    CirclePolygonHelper.generateCirclePoints(
                        center = center,
                        radiusMeters = adjustedRadiusMeters,
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
            if (finger.zIndex != prevFinger.zIndex) {
                circle.zIndex = (current.state.zIndex ?: calculateZIndex(current.state.center)).toFloat()
            }
            circle
        }

    private fun adjustedRadiusMeters(
        state: CircleState,
        center: com.google.android.gms.maps.model.LatLng,
    ): Double {
        val strokeWidthPx = ResourceProvider.dpToPx(state.strokeWidth).toDouble()
        if (strokeWidthPx <= 0.0) return state.radiusMeters

        val projection = holder.map.projection
        val centerPoint = projection.toScreenLocation(center)
        val offsetPoint =
            android.graphics.Point(
                centerPoint.x,
                (centerPoint.y - (strokeWidthPx / 2.0)).roundToInt(),
            )
        val offsetCoord = projection.fromScreenLocation(offsetPoint)

        val strokeMeters =
            computeDistanceBetween(
                GeoPoint.fromLatLong(center.latitude, center.longitude),
                GeoPoint.fromLatLong(offsetCoord.latitude, offsetCoord.longitude),
            )
        return state.radiusMeters + strokeMeters
    }
}
