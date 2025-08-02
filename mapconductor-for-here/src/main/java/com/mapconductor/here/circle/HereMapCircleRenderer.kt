package com.mapconductor.here.circle

import androidx.compose.ui.graphics.toArgb
import com.here.sdk.core.Color
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.GeoPolygon
import com.here.sdk.mapview.MapPolygon
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.circle.AbstractCircleRenderer
import com.mapconductor.core.circle.CircleEntity
import com.mapconductor.core.circle.CircleOverlayManager
import com.mapconductor.core.circle.CircleOverlayManagerImpl
import com.mapconductor.core.circle.CircleRenderer.UpdateParams
import com.mapconductor.core.circle.CircleRendererFactory
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.here.HereMapViewHolder
import com.mapconductor.here.toGeoCoordinates
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class DefaultHereMapCircleRenderer : CircleRendererFactory<MapPolygon> {
    override fun create(
        onAdd: suspend (List<CircleState>) -> List<MapPolygon?>,
        onChange: suspend (List<UpdateParams<MapPolygon>>) -> List<MapPolygon?>,
        onRemove: suspend (List<CircleEntity<MapPolygon>>) -> Unit,
        onPostProcess: (suspend () -> Unit)?,
    ): CircleOverlayManager<MapPolygon> =
        CircleOverlayManagerImpl(
            onRemove = onRemove,
            onAdd = onAdd,
            onChange = onChange,
            onPostProcess = onPostProcess,
        )
}

class HereMapCircleRenderer(
    override val holder: HereMapViewHolder,
    override val coroutine: CoroutineScope,
) : AbstractCircleRenderer<MapPolygon>() {
    companion object {
        // Number of points to approximate a circle - more points = smoother circle
        private const val CIRCLE_POINT_COUNT = 64
    }

    override suspend fun addCircles(newCircles: List<CircleState>): List<MapPolygon?> {
        val polygons =
            newCircles.map { state ->
                val geoPolygon = createCirclePolygon(state)
                val lineWidth = ResourceProvider.dpToPx(state.strokeWidth.value.toDouble())
                MapPolygon(
                    geoPolygon,
                    Color.valueOf(state.fillColor.toArgb()),
                    Color.valueOf(state.strokeColor.toArgb()),
                    lineWidth,
                )
            }
        coroutine.launch {
            polygons.forEach { holder.map.addMapPolygon(it) }
        }
        return polygons
    }

    override suspend fun removeCircles(removeEntities: List<CircleEntity<MapPolygon>>) {
        coroutine.launch {
            removeEntities.forEach { holder.map.removeMapPolygon(it.circle) }
        }
    }

    override suspend fun changeCircle(changes: List<UpdateParams<MapPolygon>>): List<MapPolygon> {
        return changes.map { params ->
            val finger = params.entity.fingerPrint
            val prevFinger = params.prevEntity.fingerPrint

            // Update geometry if center or radius changed
            if (finger.center != prevFinger.center || finger.radiusMeters != prevFinger.radiusMeters) {
                val geoPolygon = createCirclePolygon(params.entity.state)
                params.entity.circle.geometry = geoPolygon
            }

            // Update stroke color
            if (finger.strokeColor != prevFinger.strokeColor) {
                params.entity.circle.outlineColor =
                    Color.valueOf(
                        params.entity.state.strokeColor
                            .toArgb(),
                    )
            }

            // Update stroke width
            if (finger.strokeWidth != prevFinger.strokeWidth) {
                val lineWidth =
                    ResourceProvider.dpToPx(
                        params.entity.state.strokeWidth.value
                            .toDouble(),
                    )
                params.entity.circle.outlineWidth = lineWidth
            }

            // Update fill color
            if (finger.fillColor != prevFinger.fillColor) {
                params.entity.circle.fillColor =
                    Color.valueOf(
                        params.entity.state.fillColor
                            .toArgb(),
                    )
            }

            return@map params.entity.circle
        }
    }

    /**
     * Creates a polygon that approximates a circle by generating points around the circumference
     */
    private fun createCirclePolygon(state: CircleState): GeoPolygon {
        val center = GeoPoint.from(state.center).toGeoCoordinates()
        val radiusMeters = state.radiusMeters

        val points = mutableListOf<GeoCoordinates>()

        // Generate points around the circle
        for (i in 0 until CIRCLE_POINT_COUNT) {
            val angle = 2.0 * PI * i / CIRCLE_POINT_COUNT
            val point = calculateCirclePoint(center, radiusMeters, angle)
            points.add(point)
        }

        // Close the polygon by adding the first point at the end
        if (points.isNotEmpty()) {
            points.add(points.first())
        }

        return GeoPolygon(points)
    }

    /**
     * Calculate a point on a circle given center, radius and angle
     * Uses approximate conversion from meters to degrees for small circles
     */
    private fun calculateCirclePoint(
        center: GeoCoordinates,
        radiusMeters: Double,
        angleRadians: Double,
    ): GeoCoordinates {
        // Approximate conversion: 1 degree latitude ≈ 111,320 meters
        // Longitude conversion varies by latitude, use cosine correction
        val latDegrees = radiusMeters / 111320.0
        val lonDegrees = radiusMeters / (111320.0 * cos(Math.toRadians(center.latitude)))

        val deltaLat = latDegrees * cos(angleRadians)
        val deltaLon = lonDegrees * sin(angleRadians)

        return GeoCoordinates(
            center.latitude + deltaLat,
            center.longitude + deltaLon,
        )
    }
}
