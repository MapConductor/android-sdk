package com.mapconductor.googlemaps.polygon

import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.polygon.AbstractPolygonOverlayRenderer
import com.mapconductor.core.polygon.PolygonEntityInterface
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.core.spherical.createInterpolatePoints
import com.mapconductor.core.spherical.createLinearInterpolatePoints
import com.mapconductor.googlemaps.AdaptiveInterpolation
import com.mapconductor.googlemaps.GoogleMapActualPolygon
import com.mapconductor.googlemaps.GoogleMapViewHolder
import com.mapconductor.googlemaps.LatLngInterpolationCache
import com.mapconductor.googlemaps.toLatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GoogleMapPolygonOverlayRenderer(
    override val holder: GoogleMapViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractPolygonOverlayRenderer<GoogleMapActualPolygon>() {
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

    override suspend fun removePolygon(entity: PolygonEntityInterface<GoogleMapActualPolygon>) {
        coroutine.launch {
            entity.polygon.remove()
        }
    }

    override suspend fun createPolygon(state: PolygonState) =
        withContext(coroutine.coroutineContext) {
            val points: List<LatLng> =
                when (state.geodesic) {
                    true -> geodesicPoints(state.points)
                    false -> createLinearInterpolatePoints(state.points).map { GeoPoint.from(it).toLatLng() }
                }
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
            if (finger.points != prevFinger.points || finger.geodesic != prevFinger.geodesic) {
                val points: List<LatLng> =
                    when (current.state.geodesic) {
                        true -> geodesicPoints(current.state.points)
                        false ->
                            createLinearInterpolatePoints(current.state.points).map {
                                GeoPoint.from(it).toLatLng()
                            }
                    }
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
