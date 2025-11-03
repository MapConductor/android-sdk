package com.mapconductor.mapbox.polyline

import com.mapbox.maps.extension.style.sources.removeGeoJSONSourceFeatures
import com.mapconductor.core.polyline.AbstractPolylineOverlayRenderer
import com.mapconductor.core.polyline.PolylineEntity
import com.mapconductor.core.polyline.PolylineManager
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.mapbox.MapboxActualPolyline
import com.mapconductor.mapbox.MapboxMapViewHolder
import com.mapconductor.mapbox.createMapboxLines
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapboxPolylineOverlayRenderer(
    val layer: MapboxPolylineLayer,
    val polylineManager: PolylineManager<MapboxActualPolyline>,
    override val holder: MapboxMapViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractPolylineOverlayRenderer<MapboxActualPolyline>() {
    override suspend fun createPolyline(state: PolylineState): MapboxActualPolyline? =
        createMapboxLines(
            id = state.id,
            points = state.points,
            geodesic = state.geodesic,
            strokeColor = state.strokeColor,
            strokeWidth = state.strokeWidth,
            zIndex = (state.extra as? Int) ?: 0,
        )

    override suspend fun updatePolylineProperties(
        polyline: MapboxActualPolyline,
        current: PolylineEntity<MapboxActualPolyline>,
        prev: PolylineEntity<MapboxActualPolyline>,
    ): MapboxActualPolyline? {
        // For Mapbox, we need to recreate the features when properties change
        return createMapboxLines(
            id = current.state.id,
            points = current.state.points,
            geodesic = current.state.geodesic,
            strokeColor = current.state.strokeColor,
            strokeWidth = current.state.strokeWidth,
            zIndex = (current.state.extra as? Int) ?: 0,
        )
    }

    override suspend fun removePolyline(entity: PolylineEntity<MapboxActualPolyline>) {
        val featureIds =
            entity.polyline.map { feature ->
                feature.getStringProperty("id")
            }
        layer.source.removeGeoJSONSourceFeatures(featureIds)
    }

    override suspend fun onPostProcess() {
        // Redraw all polylines on the layer
        val polylines = getAllPolylineEntities()
        coroutine.launch {
            layer.draw(polylines)
        }
    }

    private fun getAllPolylineEntities(): List<PolylineEntity<MapboxActualPolyline>> {
        // This would need access to the polyline manager
        // For now, we'll implement a simple workaround
        return polylineManager.allEntities()
    }
}
