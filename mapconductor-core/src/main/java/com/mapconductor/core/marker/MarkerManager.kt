package com.mapconductor.core.marker

import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.geocell.HexCell
import com.mapconductor.core.geocell.HexCellRegistry
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.spherical.haversineDistance
import java.util.concurrent.ConcurrentHashMap

class MarkerManager<ActualMarker>(
    geocell: HexGeocell,
) {
    private val entities: ConcurrentHashMap<String, MarkerEntity<ActualMarker>> = ConcurrentHashMap()
    private val cellRegistry =
        HexCellRegistry<ActualMarker>(
            geocell = geocell,
            // Maximum zoom level
            zoom = 20.0,
        )

    fun getEntity(id: String): MarkerEntity<ActualMarker>? = entities.get(id)

    fun removeEntity(id: String): MarkerEntity<ActualMarker>? {
        val removed =
            entities.remove(id)?.also {
                cellRegistry.removePoint(it)
            }
        return removed
    }

    fun metersPerPixel(
        position: IGeoPoint,
        zoom: Double,
        pixels: Double,
        tileSize: Int = 256,
    ): Double = cellRegistry.metersPerPixel(position, zoom, pixels, tileSize)

    fun findNearest(position: IGeoPoint): MarkerEntity<ActualMarker>? {
        val cell = cellRegistry.findNearest(position) ?: return null
        val entryIDs =
            cellRegistry.getEntryIDsByHexCell(cell)?.let { entryIDs ->
                entryIDs
                    .filter { entryId ->
                        entities[entryId]?.state?.clickable == true
                    }.filterNotNull()
                    .sortedBy { entryId ->
                        entities[entryId]?.let { entity ->
                            haversineDistance(position, entity.state.position)
                        }
                    }
            } ?: return null

        val entryId = entryIDs[0]
        return entities[entryId]
    }

    fun findByIdPrefix(prefix: String): List<HexCell> = cellRegistry.findByIdPrefix(prefix)

    fun registerEntity(entity: MarkerEntity<ActualMarker>) {
        entities[entity.state.id] = entity
        cellRegistry.setPoint(entity)
    }

    fun updateEntity(entity: MarkerEntity<ActualMarker>) {
        entities[entity.state.id] = entity
        cellRegistry.setPoint(entity)
    }

    fun allEntities(): List<MarkerEntity<ActualMarker>> = entities.values.toList()

    fun clear() {
        entities.clear()
        cellRegistry.clear()
    }

    fun findMarkersInBounds(bounds: com.mapconductor.core.features.GeoRectBounds): List<MarkerEntity<ActualMarker>> {
        if (bounds.isEmpty) return emptyList()

        val center = bounds.center ?: return emptyList()
        val span = bounds.toSpan() ?: return emptyList()
        
        // Calculate search radius based on bounding box diagonal
        val latRadius = span.latitude / 2.0
        val lngRadius = span.longitude / 2.0
        val searchRadius = kotlin.math.sqrt(latRadius * latRadius + lngRadius * lngRadius) * 111000 // rough meters per degree
        
        // Find all cells within the search radius
        val cellsWithDistance = cellRegistry.findWithinRadiusWithDistance(center, searchRadius)
        
        // Collect all entities from those cells and filter by actual bounds
        val markersInBounds = mutableListOf<MarkerEntity<ActualMarker>>()
        
        cellsWithDistance.forEach { cellWithDistance ->
            val entryIDs = cellRegistry.getEntryIDsByHexCell(cellWithDistance.cell)
            entryIDs?.forEach { entryId ->
                entities[entryId]?.let { entity ->
                    if (bounds.contains(entity.state.position)) {
                        markersInBounds.add(entity)
                    }
                }
            }
        }
        
        return markersInBounds
    }
}
