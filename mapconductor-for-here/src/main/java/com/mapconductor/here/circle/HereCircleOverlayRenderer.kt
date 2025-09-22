package com.mapconductor.here.circle

import androidx.compose.ui.graphics.toArgb
import com.here.sdk.core.Color
import com.here.sdk.core.GeoCircle
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.GeoPolygon
import com.here.sdk.mapview.MapPolygon
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.circle.AbstractCircleOverlayRenderer
import com.mapconductor.core.circle.CircleEntity
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.here.HereActualCircle
import com.mapconductor.here.HereViewHolder
import com.mapconductor.here.toGeoCoordinates
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HereCircleOverlayRenderer(
    override val holder: HereViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : AbstractCircleOverlayRenderer<HereActualCircle>() {
    override suspend fun createCircle(state: CircleState): HereActualCircle? {
        val geoCircle = createCirclePolygon(state)
        val lineWidth = ResourceProvider.dpToPx(state.strokeWidth.value.toDouble())
        val mapCircle =
            MapPolygon(
                geoCircle,
                Color.valueOf(state.fillColor.toArgb()),
                Color.valueOf(state.strokeColor.toArgb()),
                lineWidth,
            )
        coroutine.launch {
            holder.map.addMapPolygon(mapCircle)
        }
        return mapCircle
    }

    override suspend fun removeCircle(entity: CircleEntity<HereActualCircle>) {
        coroutine.launch {
            holder.map.removeMapPolygon(entity.circle)
        }
    }

    override suspend fun updateCircleProperties(
        circle: HereActualCircle,
        current: CircleEntity<HereActualCircle>,
        prev: CircleEntity<HereActualCircle>,
    ): HereActualCircle? =
        withContext(coroutine.coroutineContext) {
            val finger = current.fingerPrint
            val prevFinger = prev.fingerPrint

            // Update geometry if center or radius changed
            if (finger.center != prevFinger.center || finger.radiusMeters != prevFinger.radiusMeters) {
                val geoCircle = createCirclePolygon(current.state)
                current.circle.geometry = geoCircle
            }

            // Update stroke color
            if (finger.strokeColor != prevFinger.strokeColor) {
                current.circle.outlineColor =
                    Color.valueOf(
                        current.state.strokeColor
                            .toArgb(),
                    )
            }

            // Update stroke width
            if (finger.strokeWidth != prevFinger.strokeWidth) {
                val lineWidth =
                    ResourceProvider.dpToPx(
                        current.state.strokeWidth.value
                            .toDouble(),
                    )
                current.circle.outlineWidth = lineWidth
            }

            // Update fill color
            if (finger.fillColor != prevFinger.fillColor) {
                current.circle.fillColor =
                    Color.valueOf(
                        current.state.fillColor
                            .toArgb(),
                    )
            }
            current.circle.outlineWidth =
                current.state.strokeWidth.value
                    .toDouble()

            circle
        }

    /**
     * Creates a circle that approximates a circle by generating points around the circumference
     */
    private fun createCirclePolygon(state: CircleState): GeoPolygon {
        val center = GeoPointImpl.from(state.center).toGeoCoordinates()
        // val radiusMeters = state.radiusMeters

//        val points = mutableListOf<GeoCoordinates>()
//
//        // Generate points around the circle
//        for (i in 0 until CIRCLE_POINT_COUNT) {
//            val angle = 2.0 * PI * i / CIRCLE_POINT_COUNT
//            val point = calculateCirclePoint(center, radiusMeters, angle)
//            points.add(point)
//        }
//
//        // Close the circle by adding the first point at the end
//        if (points.isNotEmpty()) {
//            points.add(points.first())
//        }
        val geoCircle = GeoCircle(center, state.radiusMeters)
        val geoPolygon = GeoPolygon(geoCircle)

        return geoPolygon
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
