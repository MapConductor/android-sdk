package com.mapconductor.maplibre.polyline

import com.mapconductor.core.polyline.AbstractPolylineOverlayRenderer
import com.mapconductor.core.polyline.PolylineEntity
import com.mapconductor.core.polyline.PolylineManager
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.maplibre.MapLibreActualPolyline
import com.mapconductor.maplibre.MapLibreMapViewHolder
import com.mapconductor.maplibre.createMapLibreLines
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapLibrePolylineOverlayRenderer(
    val layer: MapLibrePolylineLayer,
    val polylineManager: PolylineManager<MapLibreActualPolyline>,
    override val holder: MapLibreMapViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractPolylineOverlayRenderer<MapLibreActualPolyline>() {
    override suspend fun createPolyline(state: PolylineState): MapLibreActualPolyline? =
        createMapLibreLines(
            id = state.id,
            points = state.points,
            geodesic = state.geodesic,
            strokeColor = state.strokeColor,
            strokeWidth = state.strokeWidth,
            zIndex = (state.extra as? Int) ?: 0,
        )

    override suspend fun updatePolylineProperties(
        polyline: MapLibreActualPolyline,
        current: PolylineEntity<MapLibreActualPolyline>,
        prev: PolylineEntity<MapLibreActualPolyline>,
    ): MapLibreActualPolyline? {
        // Recreate features to apply updated properties
        return createMapLibreLines(
            id = current.state.id,
            points = current.state.points,
            geodesic = current.state.geodesic,
            strokeColor = current.state.strokeColor,
            strokeWidth = current.state.strokeWidth,
            zIndex = (current.state.extra as? Int) ?: 0,
        )
    }

    override suspend fun removePolyline(entity: PolylineEntity<MapLibreActualPolyline>) {
        // Remove features by rewriting source without this entity
        // Actual removal is handled in onPostProcess by redrawing all remaining polylines
    }

    override suspend fun onPostProcess() {
        val polylines = getAllPolylineEntities()
        // Use the same style instance from the controller when available
        val style = holder.getController()?.getStyleInstance() ?: holder.map.style
        style?.let {
            coroutine.launch {
                layer.draw(polylines, it)
            }
        }
    }

    private fun getAllPolylineEntities(): List<PolylineEntity<MapLibreActualPolyline>> {
        // This would need access to the polyline manager
        // For now, we'll implement a simple workaround
        return polylineManager.allEntities()
    }

    fun redraw() {
        val entities = polylineManager.allEntities()
        // Get style from controller to use the same instance
        val style = holder.getController()?.getStyleInstance() ?: holder.map.style
        style?.let {
            coroutine.launch {
                layer.draw(entities, it)
            }
        }
    }
}
