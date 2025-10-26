package com.mapconductor.maplibre.polygon

import com.google.gson.JsonObject
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.polygon.AbstractPolygonOverlayRenderer
import com.mapconductor.core.polygon.PolygonEntity
import com.mapconductor.core.polygon.PolygonManager
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.core.spherical.Spherical
import com.mapconductor.maplibre.MapLibreActualPolygon
import com.mapconductor.maplibre.MapLibreMapViewHolder
import com.mapconductor.maplibre.toMapLibreColorString
import com.mapconductor.maplibre.toPoint
import com.mapconductor.maplibre.createMapLibrePolygons
import org.maplibre.geojson.Feature
import org.maplibre.geojson.Polygon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapLibrePolygonOverlayRenderer(
    val layer: MapLibrePolygonLayer,
    val polygonManager: PolygonManager<MapLibreActualPolygon>,
    override val holder: MapLibreMapViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractPolygonOverlayRenderer<MapLibreActualPolygon>() {
    override suspend fun onRemove(data: List<PolygonEntity<MapLibreActualPolygon>>) {
        // Actual removal handled by redrawing remaining polygons in onPostProcess
    }

    override suspend fun onPostProcess() {
        val polygons = getAllPolygonEntities()
        val style = holder.getController()?.getStyleInstance() ?: holder.map.style
        style?.let {
            coroutine.launch {
                this@MapLibrePolygonOverlayRenderer.layer.draw(polygons, it)
            }
        }
    }

    override suspend fun removePolygon(entity: PolygonEntity<MapLibreActualPolygon>) {
        // No-op; we redraw full collection
    }

    override suspend fun createPolygon(state: PolygonState): MapLibreActualPolygon? =
        createMapLibrePolygons(
            id = state.id,
            points = state.points,
            geodesic = state.geodesic,
            fillColor = state.fillColor,
        )

    override suspend fun updatePolygonProperties(
        polygon: MapLibreActualPolygon,
        current: PolygonEntity<MapLibreActualPolygon>,
        prev: PolygonEntity<MapLibreActualPolygon>,
    ): MapLibreActualPolygon? {
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
            if (distance <= maxSegmentLength) continue

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

    private fun getAllPolygonEntities(): List<PolygonEntity<MapLibreActualPolygon>> = polygonManager.allEntities()
}
