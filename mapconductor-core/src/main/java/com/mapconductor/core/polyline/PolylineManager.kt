package com.mapconductor.core.polyline

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.calculateMetersPerPixel
import com.mapconductor.core.createInterpolatePoints
import com.mapconductor.core.createLinearInterpolatePoints
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.isPointOnLinearLine
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.pointOnGeodesicSegmentOrNull
import com.mapconductor.core.spherical.Spherical
import com.mapconductor.core.spherical.isPointOnTheGeodesicLine
import com.mapconductor.settings.Settings
import kotlin.math.max
import android.util.Log

data class PolylineHitResult<ActualPolyline>(
    val entity: PolylineEntity<ActualPolyline>,
    val closestPoint: GeoPoint,
)

private data class DistanceResult(
    val distance: Double,
    val closestPoint: GeoPoint,
)

interface PolylineManager<ActualPolyline> {
    val debugDrawRectangle: ((GeoRectBounds, Color) -> Unit)?
    val debugDrawCircle: ((GeoPoint, Double, Color) -> Unit)?

    fun registerEntity(entity: PolylineEntity<ActualPolyline>)

    fun removeEntity(id: String): PolylineEntity<ActualPolyline>?

    fun getEntity(id: String): PolylineEntity<ActualPolyline>?

    fun hasEntity(id: String): Boolean

    fun allEntities(): List<PolylineEntity<ActualPolyline>>

    fun clear()

    fun find(
        position: GeoPoint,
        cameraPosition: MapCameraPositionImpl? = null,
    ): PolylineHitResult<ActualPolyline>?
}

class PolylineManagerImpl<ActualPolyline>(
    override val debugDrawRectangle: ((GeoRectBounds, Color) -> Unit)? = null,
    override val debugDrawCircle: ((GeoPoint, Double, Color) -> Unit)? = null,
) : PolylineManager<ActualPolyline> {
    companion object {
        private const val DEBUG_FIND = true
        private const val TAG = "PolylineManager"

        private fun d(msg: String) {
            if (DEBUG_FIND) Log.d(TAG, msg)
        }
    }

    private val entities = mutableMapOf<String, PolylineEntity<ActualPolyline>>()

    override fun registerEntity(entity: PolylineEntity<ActualPolyline>) {
        entities[entity.state.id] = entity
    }

    override fun removeEntity(id: String): PolylineEntity<ActualPolyline>? = entities.remove(id)

    override fun getEntity(id: String): PolylineEntity<ActualPolyline>? = entities[id]

    override fun hasEntity(id: String): Boolean = entities.containsKey(id)

    override fun allEntities(): List<PolylineEntity<ActualPolyline>> = entities.values.toList()

    override fun clear() {
        entities.clear()
    }

    override fun find(
        position: GeoPoint,
        cameraPosition: MapCameraPositionImpl?,
    ): PolylineHitResult<ActualPolyline>? {
        // Calculate pixel-based tolerance that adapts to zoom level
//        val toleranceMeters = calculateToleranceInMeters(position, cameraPosition)

        // Get visible region for viewport filtering
        val visibleRegion = cameraPosition?.visibleRegion?.bounds

//        d(
//            "find: pos=${GeoPointImpl.from(position).toUrlValue()} tol=${"%.2f".format(toleranceMeters)} " +
//                "visibleRegion=${visibleRegion} camZoom=${cameraPosition?.zoom}"
//        )
//        // Expand visible region by tolerance (converted to degrees) to avoid false negatives
//        // especially for geodesic bulges and near-screen edges.
//        val latRef = cameraPosition?.position?.latitude ?: position.latitude
//        val metersPerDegLat = 111_320.0
//        val metersPerDegLon = (111_320.0 * cos(Math.toRadians(kotlin.math.abs(latRef)))).coerceAtLeast(1e-3)
//        val padLatDeg = toleranceMeters / metersPerDegLat
//        val padLonDeg = toleranceMeters / metersPerDegLon
//        val paddedRegion = visibleRegion?.expandedByDegrees(padLatDeg, padLonDeg)
//        val metersPerPixelAtTap = cameraPosition?.let { calculateMetersPerPixel(latRef, it.zoom) }

        // Collect all candidates with their closest distances
        val candidates = mutableListOf<Triple<PolylineEntity<ActualPolyline>, GeoPoint, Double>>()
        val fingerSize = ResourceProvider.dpToPx(Settings.Default.tapTolerance)
        val zoom = cameraPosition?.zoom ?: 0.0
        val threshold = calculateMetersPerPixel(position.latitude, zoom) * fingerSize
        debugDrawCircle?.invoke(
            position,
            threshold,
            Color.Green
        )

        entities.values.forEach { entity ->
            val points: List<GeoPoint> =
                when (entity.state.geodesic) {
                    true -> createInterpolatePoints(entity.state.points)
                    false -> createLinearInterpolatePoints(entity.state.points)
                }

//
            for (i in 0 until points.size - 1) {
                val box = GeoRectBounds()
                box.extend(points[i])
                box.extend(points[i + 1])
                if (visibleRegion == null || visibleRegion.intersects(box)) {
//                    if (entity.state.geodesic) {
                        pointOnGeodesicSegmentOrNull(
                            points[i],
                            points[i + 1],
                            position,
        threshold)?.let {
                            candidates.add(
                                Triple(
                                    entity,
                                    it.first,
                                    it.second,
                                ),
                            )
                        }
//                    } else {
//                    isPointOnLinearLine(
//                        points[i],
//                        points[i + 1],
//                        position,
//        threshold
//                    )?.let {
//                        candidates.add(
//                            Triple(
//                                entity,
//                                it.first,
//                                it.second,
//                            ),
//                        )
//                    }
//                        }
                }
            }
        }

        // Return the closest candidate among all qualifying polylines
        val closest = candidates.minByOrNull { it.third }
        return closest?.let { (entity, closestPoint, distance) ->
            PolylineHitResult(
                entity = entity,
                closestPoint = position,
            ).also {
                d(
                    "winner id=${entity.state.id} point=${GeoPointImpl.from(closestPoint).toUrlValue()}" +
                        " dist=${"%.2f".format(distance)}",
                )
            }
        }
    }

//    private fun distanceFromPointToLineSegmentWithPoint(
//        point: GeoPoint,
//        lineStart: GeoPoint,
//        lineEnd: GeoPoint,
//    ): DistanceResult {
//        // Check if line segment is actually a point
//        if (lineStart.latitude == lineEnd.latitude && lineStart.longitude == lineEnd.longitude) {
//            return DistanceResult(
//                distance = Spherical.computeDistanceBetween(point, lineStart),
//                closestPoint = GeoPointImpl.from(lineStart),
//            )
//        }
//
//        // For non-geodesic lines, we'll use a more accurate approach
//        // Sample points along the line segment and find the closest one
//        var minDistance = Double.MAX_VALUE
//        var bestFraction = 0.0
//
//        // Sample points along the line segment
//        val samples = 20 // Number of sample points
//        for (i in 0..samples) {
//            val fraction = i.toDouble() / samples
//            val samplePoint = Spherical.linearInterpolate(lineStart, lineEnd, fraction)
//            val distance = Spherical.computeDistanceBetween(point, samplePoint)
//
//            if (distance < minDistance) {
//                minDistance = distance
//                bestFraction = fraction
//            }
//        }
//
//        // Refine the result using binary search in the vicinity of the best fraction
//        val searchRadius = 1.0 / samples
//        val refinedFraction =
//            refineLinearFraction(
//                point, lineStart, lineEnd, bestFraction, searchRadius, 5,
//            )
//
//        val closestPoint = Spherical.linearInterpolate(lineStart, lineEnd, refinedFraction)
//        return DistanceResult(
//            distance = Spherical.computeDistanceBetween(point, closestPoint),
//            closestPoint = closestPoint,
//        )
//    }

//    private fun haversineDistance(
//        point1: GeoPoint,
//        point2: GeoPoint,
//    ): Double {
//        val earthRadiusKm = 6371.0
//        val dLat = Math.toRadians(point2.latitude - point1.latitude)
//        val dLon = Math.toRadians(point2.longitude - point1.longitude)
//        val lat1 = Math.toRadians(point1.latitude)
//        val lat2 = Math.toRadians(point2.latitude)
//
//        val a = sin(dLat / 2).pow(2) + sin(dLon / 2).pow(2) * cos(lat1) * cos(lat2)
//        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
//        return earthRadiusKm * c * 1000 // Convert to meters
//    }

    private fun distanceFromPointToGeodesicSegmentWithPoint(
        point: GeoPoint,
        lineStart: GeoPoint,
        lineEnd: GeoPoint,
    ): DistanceResult {
        // If the line segment is actually a point, return distance from point to point
        if (lineStart.latitude == lineEnd.latitude && lineStart.longitude == lineEnd.longitude) {
            return DistanceResult(
                distance = Spherical.computeDistanceBetween(point, lineStart),
                closestPoint = GeoPointImpl.from(lineStart),
            )
        }

        // For geodesic lines, we need to find the closest point on the great circle arc
        val segmentDistance = Spherical.computeDistanceBetween(lineStart, lineEnd)

        // If the segment is very short, treat it as a point
        if (segmentDistance < 1.0) { // Less than 1 meter
            return DistanceResult(
                distance = Spherical.computeDistanceBetween(point, lineStart),
                closestPoint = GeoPointImpl.from(lineStart),
            )
        }

        // Use iterative approach to find the closest point on the geodesic segment
        var minDistance = Double.MAX_VALUE
        var bestFraction = 0.0
        var closestPoint = lineStart

        // Sample points along the geodesic segment to find the approximate closest point
        val samples = 20 // Number of sample points
        for (i in 0..samples) {
            val fraction = i.toDouble() / samples
            val samplePoint = Spherical.interpolate(lineStart, lineEnd, fraction)
            val distance = Spherical.computeDistanceBetween(point, samplePoint)

            if (distance < minDistance) {
                minDistance = distance
                bestFraction = fraction
                closestPoint = samplePoint
            }
        }
        return DistanceResult(
            distance = minDistance,
            closestPoint = closestPoint,
        )

        // Refine the result using binary search in the vicinity of the best fraction
//        val searchRadius = 1.0 / samples
//        val refinedFraction =
//            refineGeodesicFraction(
//                point, lineStart, lineEnd, bestFraction, searchRadius, 5,
//            )
//
//        val closestPoint = Spherical.interpolate(lineStart, lineEnd, refinedFraction)
//        return DistanceResult(
//            distance = Spherical.computeDistanceBetween(point, closestPoint),
//            closestPoint = closestPoint,
//        )
    }

    private fun refineGeodesicFraction(
        point: GeoPoint,
        lineStart: GeoPoint,
        lineEnd: GeoPoint,
        initialFraction: Double,
        searchRadius: Double,
        iterations: Int,
    ): Double {
        var bestFraction = initialFraction
        var bestDistance = Double.MAX_VALUE
        var currentRadius = searchRadius

        repeat(iterations) {
            val startFraction = (bestFraction - currentRadius).coerceAtLeast(0.0)
            val endFraction = (bestFraction + currentRadius).coerceAtMost(1.0)

            // Test several points in the current search range
            for (i in 0..10) {
                val fraction = startFraction + i * (endFraction - startFraction) / 10
                val testPoint = Spherical.interpolate(lineStart, lineEnd, fraction)
                val distance = Spherical.computeDistanceBetween(point, testPoint)

                if (distance < bestDistance) {
                    bestDistance = distance
                    bestFraction = fraction
                }
            }

            // Narrow the search radius for next iteration
            currentRadius *= 0.5
        }

        return bestFraction
    }

    private fun refineLinearFraction(
        point: GeoPoint,
        lineStart: GeoPoint,
        lineEnd: GeoPoint,
        initialFraction: Double,
        searchRadius: Double,
        iterations: Int,
    ): Double {
        var bestFraction = initialFraction
        var bestDistance = Double.MAX_VALUE
        var currentRadius = searchRadius

        repeat(iterations) {
            val startFraction = (bestFraction - currentRadius).coerceAtLeast(0.0)
            val endFraction = (bestFraction + currentRadius).coerceAtMost(1.0)

            // Test several points in the current search range
            for (i in 0..10) {
                val fraction = startFraction + i * (endFraction - startFraction) / 10
                val testPoint = Spherical.linearInterpolate(lineStart, lineEnd, fraction)
                val distance = Spherical.computeDistanceBetween(point, testPoint)

                if (distance < bestDistance) {
                    bestDistance = distance
                    bestFraction = fraction
                }
            }

            // Narrow the search radius for next iteration
            currentRadius *= 0.5
        }

        return bestFraction
    }

    private fun calculateToleranceInMeters(
        position: GeoPoint,
        cameraPosition: MapCameraPositionImpl?,
    ): Double {
        // Default pixel tolerance for touch targets (20 pixels is good for mobile touch)
        val baseTolerancePx = 20.0
        val minTolerancePx = 16.0

        // Fallback to fixed tolerance if no camera position available
        if (cameraPosition == null) {
            return 50.0 // meters
        }

        // Calculate meters per pixel at the current zoom level and latitude
        val metersPerPixel = calculateMetersPerPixel(position.latitude, cameraPosition.zoom)

        // Convert pixel tolerance to meters
        val tolerancePixels = max(baseTolerancePx, minTolerancePx)
        val minMeters = 20.0
        return max(tolerancePixels * metersPerPixel, minMeters)
    }
}
