package com.mapconductor.core.polyline

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.spherical.Spherical
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class PolylineHitResult<ActualPolyline>(
    val entity: PolylineEntity<ActualPolyline>,
    val closestPoint: GeoPoint,
)

private data class DistanceResult(
    val distance: Double,
    val closestPoint: GeoPoint,
)

interface PolylineManager<ActualPolyline> {
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

class PolylineManagerImpl<ActualPolyline> : PolylineManager<ActualPolyline> {
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
        val toleranceMeters = calculateToleranceInMeters(position, cameraPosition)

        entities.values.forEach { entity ->
            val points = entity.state.points
            if (points.size < 2) return@forEach

            var closestResult: DistanceResult? = null
            var minDistance = Double.MAX_VALUE

            // Check if the position is within tolerance of any line segment
            for (i in 0 until points.size - 1) {
                val segmentStart = points[i]
                val segmentEnd = points[i + 1]
                val result =
                    if (entity.state.geodesic) {
                        distanceFromPointToGeodesicSegmentWithPoint(position, segmentStart, segmentEnd)
                    } else {
                        distanceFromPointToLineSegmentWithPoint(position, segmentStart, segmentEnd)
                    }

                if (result.distance < minDistance) {
                    minDistance = result.distance
                    closestResult = result
                }
            }

            // If any segment is within tolerance, return this entity with the closest point
            if (minDistance <= toleranceMeters && closestResult != null) {
                return PolylineHitResult(
                    entity = entity,
                    closestPoint = closestResult.closestPoint.wrap(),
                )
            }
        }

        return null
    }

    private fun distanceFromPointToLineSegmentWithPoint(
        point: GeoPoint,
        lineStart: GeoPoint,
        lineEnd: GeoPoint,
    ): DistanceResult {
        // Check if line segment is actually a point
        if (lineStart.latitude == lineEnd.latitude && lineStart.longitude == lineEnd.longitude) {
            return DistanceResult(
                distance = Spherical.computeDistanceBetween(point, lineStart),
                closestPoint = GeoPointImpl.from(lineStart),
            )
        }

        // For non-geodesic lines, we'll use a more accurate approach
        // Sample points along the line segment and find the closest one
        var minDistance = Double.MAX_VALUE
        var bestFraction = 0.0

        // Sample points along the line segment
        val samples = 20 // Number of sample points
        for (i in 0..samples) {
            val fraction = i.toDouble() / samples
            val samplePoint = Spherical.linearInterpolate(lineStart, lineEnd, fraction)
            val distance = Spherical.computeDistanceBetween(point, samplePoint)

            if (distance < minDistance) {
                minDistance = distance
                bestFraction = fraction
            }
        }

        // Refine the result using binary search in the vicinity of the best fraction
        val searchRadius = 1.0 / samples
        val refinedFraction =
            refineLinearFraction(
                point, lineStart, lineEnd, bestFraction, searchRadius, 5,
            )

        val closestPoint = Spherical.linearInterpolate(lineStart, lineEnd, refinedFraction)
        return DistanceResult(
            distance = Spherical.computeDistanceBetween(point, closestPoint),
            closestPoint = closestPoint,
        )
    }

    private fun haversineDistance(
        point1: GeoPoint,
        point2: GeoPoint,
    ): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(point2.latitude - point1.latitude)
        val dLon = Math.toRadians(point2.longitude - point1.longitude)
        val lat1 = Math.toRadians(point1.latitude)
        val lat2 = Math.toRadians(point2.latitude)

        val a = sin(dLat / 2).pow(2) + sin(dLon / 2).pow(2) * cos(lat1) * cos(lat2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c * 1000 // Convert to meters
    }

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

        // Sample points along the geodesic segment to find the approximate closest point
        val samples = 20 // Number of sample points
        for (i in 0..samples) {
            val fraction = i.toDouble() / samples
            val samplePoint = Spherical.interpolate(lineStart, lineEnd, fraction)
            val distance = Spherical.computeDistanceBetween(point, samplePoint)

            if (distance < minDistance) {
                minDistance = distance
                bestFraction = fraction
            }
        }

        // Refine the result using binary search in the vicinity of the best fraction
        val searchRadius = 1.0 / samples
        val refinedFraction =
            refineGeodesicFraction(
                point, lineStart, lineEnd, bestFraction, searchRadius, 5,
            )

        val closestPoint = Spherical.interpolate(lineStart, lineEnd, refinedFraction)
        return DistanceResult(
            distance = Spherical.computeDistanceBetween(point, closestPoint),
            closestPoint = closestPoint,
        )
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
        val tolerancePixels = 20.0

        // Fallback to fixed tolerance if no camera position available
        if (cameraPosition == null) {
            return 50.0 // meters
        }

        // Calculate meters per pixel at the current zoom level and latitude
        val metersPerPixel = calculateMetersPerPixel(position.latitude, cameraPosition.zoom)

        // Convert pixel tolerance to meters
        return tolerancePixels * metersPerPixel
    }

    private fun calculateMetersPerPixel(
        latitude: Double,
        zoom: Double,
    ): Double {
        // Web Mercator projection formula for meters per pixel
        // Based on the standard: 1 pixel = 78271.484 meters at zoom 0 at the equator

        val earthCircumference = 40075016.686 // meters at equator
        val tileSize = 256.0 // standard tile size in pixels

        // At zoom level 0, the entire world (40M meters) fits in 256 pixels
        val metersPerPixelAtEquator = earthCircumference / tileSize

        // Adjust for zoom level (each zoom level halves the meters per pixel)
        val metersPerPixelAtZoom = metersPerPixelAtEquator / 2.0.pow(zoom)

        // Adjust for latitude (Mercator projection stretches at higher latitudes)
        val latitudeRadians = Math.toRadians(abs(latitude))
        val latitudeAdjustment = cos(latitudeRadians)

        return metersPerPixelAtZoom / latitudeAdjustment
    }
}
