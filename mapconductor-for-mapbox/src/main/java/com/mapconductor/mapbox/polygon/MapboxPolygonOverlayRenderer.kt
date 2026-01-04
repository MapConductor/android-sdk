package com.mapconductor.mapbox.polygon

import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.polygon.AbstractPolygonOverlayRenderer
import com.mapconductor.core.polygon.PolygonEntityInterface
import com.mapconductor.core.polygon.PolygonManagerInterface
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.core.spherical.Spherical
import com.mapconductor.mapbox.MapboxActualPolygon
import com.mapconductor.mapbox.MapboxMapViewHolder
import com.mapconductor.mapbox.createMapboxPolygons
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapboxPolygonOverlayRenderer(
    val layer: MapboxPolygonLayer,
    val polygonManager: PolygonManagerInterface<MapboxActualPolygon>,
    override val holder: MapboxMapViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractPolygonOverlayRenderer<MapboxActualPolygon>() {
    override suspend fun onRemove(data: List<PolygonEntityInterface<MapboxActualPolygon>>) {
//        val featureIds = data.map { entity ->
//            entity.polygon.getStringProperty("id")
//        }
//        layer.source.removeGeoJSONSourceFeatures(featureIds)
    }

    override suspend fun onPostProcess() {
        val polygons = getAllPolygonEntities()
        coroutine.launch {
            this@MapboxPolygonOverlayRenderer.layer.draw(polygons)
        }
    }

    override suspend fun removePolygon(entity: PolygonEntityInterface<MapboxActualPolygon>) {
//        val featureIds =
//            listOf(entity.polygon.getStringProperty("id"))
//        layer.source.removeGeoJSONSourceFeatures(featureIds)
    }

    override suspend fun createPolygon(state: PolygonState): MapboxActualPolygon? =
        createMapboxPolygons(
            id = state.id,
            points = state.points,
            geodesic = state.geodesic,
            fillColor = state.fillColor,
            zIndex = state.zIndex,
        )

    override suspend fun updatePolygonProperties(
        polygon: MapboxActualPolygon,
        current: PolygonEntityInterface<MapboxActualPolygon>,
        prev: PolygonEntityInterface<MapboxActualPolygon>,
    ): MapboxActualPolygon? {
        val finger = current.fingerPrint
        val prevFinger = prev.fingerPrint

        if (finger != prevFinger) {
            // Recreate features when any polygon property changes
            return createPolygon(current.state)
        }
        return prev.polygon
    }

    /**
     * Creates geodesic polygon points by interpolating between each consecutive pair of vertices.
     * This ensures that polygon edges follow great circle paths instead of straight lines.
     *
     * @param points Original polygon vertices
     * @param maxSegmentLength Maximum distance between interpolated points in meters
     * @return List of points with interpolated vertices along geodesic paths
     */
    private fun createGeodesicPolygonPoints(
        points: List<GeoPointInterface>,
        maxSegmentLength: Double = 1000.0,
    ): List<GeoPointInterface> {
        if (points.size < 3) return points

        val results = mutableListOf<GeoPointInterface>()

        for (i in points.indices) {
            val currentPoint = points[i]
            val nextPoint = points[(i + 1) % points.size] // Wrap around to create closed polygon

            results.add(currentPoint)

            // Calculate distance between current and next point
            val distance = Spherical.computeDistanceBetween(currentPoint, nextPoint)

            // Skip interpolation if points are very close
            if (distance <= maxSegmentLength) {
                continue
            }

            // Calculate number of interpolation segments needed
            val numSegments = (distance / maxSegmentLength).toInt().coerceAtLeast(1)
            val step = 1.0 / numSegments

            // Add interpolated points between current and next vertex
            var fraction = step
            while (fraction < 1.0) {
                val interpolatedPoint = Spherical.sphericalInterpolate(currentPoint, nextPoint, fraction)
                results.add(interpolatedPoint)
                fraction += step
            }
        }

        return results
    }

    private fun getAllPolygonEntities(): List<PolygonEntityInterface<MapboxActualPolygon>> {
        // This would need access to the polygon manager
        // For now, we'll implement a simple workaround
        return polygonManager.allEntities()
    }
}
