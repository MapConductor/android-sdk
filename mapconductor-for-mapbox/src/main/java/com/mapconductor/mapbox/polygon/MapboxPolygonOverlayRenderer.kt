package com.mapconductor.mapbox.polygon

import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Polygon
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.polygon.AbstractPolygonOverlayRenderer
import com.mapconductor.core.polygon.PolygonEntity
import com.mapconductor.core.polygon.PolygonManager
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.core.spherical.Spherical
import com.mapconductor.mapbox.MapboxActualPolygon
import com.mapconductor.mapbox.MapboxMapViewHolder
import com.mapconductor.mapbox.toMapboxColorString
import com.mapconductor.mapbox.toPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapboxPolygonOverlayRenderer(
    val layer: MapboxPolygonLayer,
    val polygonManager: PolygonManager<MapboxActualPolygon>,
    override val holder: MapboxMapViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractPolygonOverlayRenderer<MapboxActualPolygon>() {
    override suspend fun onRemove(data: List<PolygonEntity<MapboxActualPolygon>>) {
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

    override suspend fun removePolygon(entity: PolygonEntity<MapboxActualPolygon>) {
//        val featureIds =
//            listOf(entity.polygon.getStringProperty("id"))
//        layer.source.removeGeoJSONSourceFeatures(featureIds)
    }

    override suspend fun createPolygon(state: PolygonState): MapboxActualPolygon? {
        val geoPoints: List<GeoPoint> =
            when (state.geodesic) {
                true -> createGeodesicPolygonPoints(state.points)
                false -> state.points
            }
        val points = geoPoints.map { GeoPointImpl.from(it).toPoint() }
        // Close the polygon by adding the first point at the end if not already closed
        val closedPoints =
            if (points.first() != points.last()) {
                points + points.first()
            } else {
                points
            }
        return listOf(
            Feature.fromGeometry(
                Polygon.fromLngLats(listOf(closedPoints)),
                JsonObject().apply {
                    addProperty(MapboxPolygonLayer.Prop.FILL_COLOR, state.fillColor.toMapboxColorString())
                },
                "polygon-${state.id}",
            ),
        )
    }

    override suspend fun updatePolygonProperties(
        polygon: MapboxActualPolygon,
        current: PolygonEntity<MapboxActualPolygon>,
        prev: PolygonEntity<MapboxActualPolygon>,
    ): MapboxActualPolygon? {
        val finger = current.fingerPrint
        val prevFinger = prev.fingerPrint

        if (finger.points != prevFinger.points || finger.geodesic != prevFinger.geodesic) {
            // If points or geodesic changed, recreate the polygon
            return createPolygon(current.state)
        }

        // For other property changes, return the existing polygon
        // The layer will handle style updates
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
        points: List<GeoPoint>,
        maxSegmentLength: Double = 1000.0,
    ): List<GeoPoint> {
        if (points.size < 3) return points

        val results = mutableListOf<GeoPoint>()

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

    private fun getAllPolygonEntities(): List<PolygonEntity<MapboxActualPolygon>> {
        // This would need access to the polygon manager
        // For now, we'll implement a simple workaround
        return polygonManager.allEntities()
    }
}
