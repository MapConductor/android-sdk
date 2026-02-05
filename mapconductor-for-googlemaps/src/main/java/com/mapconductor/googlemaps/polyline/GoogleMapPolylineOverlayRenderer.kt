package com.mapconductor.googlemaps.polyline

import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.polyline.AbstractPolylineOverlayRenderer
import com.mapconductor.core.polyline.PolylineEntityInterface
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.core.spherical.createInterpolatePoints
import com.mapconductor.core.spherical.createLinearInterpolatePoints
import com.mapconductor.googlemaps.AdaptiveInterpolation
import com.mapconductor.googlemaps.GoogleMapActualPolyline
import com.mapconductor.googlemaps.GoogleMapViewHolder
import com.mapconductor.googlemaps.LatLngInterpolationCache
import com.mapconductor.googlemaps.toLatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GoogleMapPolylineOverlayRenderer(
    override val holder: GoogleMapViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractPolylineOverlayRenderer<GoogleMapActualPolyline>() {
    private val interpolationCache = LatLngInterpolationCache(maxEntries = 64)

    private fun geodesicPoints(statePoints: List<GeoPointInterface>): List<LatLng> {
        val camera = holder.map.cameraPosition
        val maxSegmentLength =
            AdaptiveInterpolation.maxSegmentLengthMeters(
                zoom = camera.zoom,
                latitude = camera.target.latitude,
            )
        val key = AdaptiveInterpolation.cacheKey(AdaptiveInterpolation.pointsHash(statePoints), maxSegmentLength)
        interpolationCache.get(key)?.let { return it }

        val geoPoints = createInterpolatePoints(statePoints, maxSegmentLength = maxSegmentLength)
        val points = geoPoints.map { GeoPoint.from(it).toLatLng() }
        interpolationCache.put(key, points)
        return points
    }

    override suspend fun createPolyline(state: PolylineState): GoogleMapActualPolyline? =
        withContext(coroutine.coroutineContext) {
            val points: List<LatLng> =
                when (state.geodesic) {
                    true -> geodesicPoints(state.points)
                    false -> createLinearInterpolatePoints(state.points).map { GeoPoint.from(it).toLatLng() }
                }
            val options =
                PolylineOptions()
                    .addAll(points)
                    .color(state.strokeColor.toArgb())
                    .width(ResourceProvider.dpToPx(state.strokeWidth).toFloat())
                    .geodesic(state.geodesic)
                    .zIndex(state.zIndex.toFloat())
                    .clickable(false)

            holder.map.addPolyline(options).also {
                it.tag = state.id
            }
        }

    override suspend fun updatePolylineProperties(
        polyline: GoogleMapActualPolyline,
        current: PolylineEntityInterface<GoogleMapActualPolyline>,
        prev: PolylineEntityInterface<GoogleMapActualPolyline>,
    ): Polyline? =
        withContext(coroutine.coroutineContext) {
            val finger = current.fingerPrint
            val prevFinger = prev.fingerPrint

            if (finger.points != prevFinger.points || finger.geodesic != prevFinger.geodesic) {
                val points: List<LatLng> =
                    when (current.state.geodesic) {
                        true -> geodesicPoints(current.state.points)
                        false ->
                            createLinearInterpolatePoints(current.state.points).map {
                                GeoPoint.from(it).toLatLng()
                            }
                    }
                polyline.points = points
            }

            if (finger.strokeWidth != prevFinger.strokeWidth) {
                polyline.width = ResourceProvider.dpToPx(current.state.strokeWidth).toFloat()
            }

            if (finger.strokeColor != prevFinger.strokeColor) {
                polyline.color = current.state.strokeColor.toArgb()
            }

            if (finger.zIndex != prevFinger.zIndex) {
                polyline.zIndex = current.state.zIndex.toFloat()
            }

            polyline
        }

    override suspend fun removePolyline(entity: PolylineEntityInterface<GoogleMapActualPolyline>) {
        coroutine.launch {
            entity.polyline.remove()
        }
    }
}
