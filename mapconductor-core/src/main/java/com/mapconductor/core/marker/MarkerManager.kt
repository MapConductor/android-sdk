package com.mapconductor.core.marker

import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.geocell.HexCell
import com.mapconductor.core.geocell.HexCellRegistry
import com.mapconductor.core.geocell.HexGeocell

class MarkerManager<ActualMarker>(
    geocell: HexGeocell,
) {
    private val entities = mutableMapOf<String, MarkerEntity<ActualMarker>>()
    private val cellRegistry =
        HexCellRegistry<ActualMarker>(
            geocell = geocell,
            // Maximum zoom level
            zoom = 20.0,
        )

    // Native index for performance-critical operations
    private val nativeIndex =
        NativeMarkerIndex.create(
            baseHexSideLength = geocell.baseHexSideLength,
            zoom = 20.0,
        )
    
    @Volatile
    private var isDestroyed = false

    fun getEntity(id: String): MarkerEntity<ActualMarker>? {
        checkNotDestroyed()
        return entities.get(id)
    }

    fun hasEntity(id: String): Boolean {
        checkNotDestroyed()
        return nativeIndex.hasMarker(id)
    }

    fun removeEntity(id: String): MarkerEntity<ActualMarker>? {
        checkNotDestroyed()
        val removed =
            entities.remove(id)?.also {
                cellRegistry.removePoint(it)
                nativeIndex.removeMarker(id)
            }
        return removed
    }

    fun metersPerPixel(
        position: IGeoPoint,
        zoom: Double,
        pixels: Double,
        tileSize: Int = 256,
    ): Double {
        checkNotDestroyed()
        return nativeIndex.metersPerPixel(position, zoom, pixels, tileSize)
    }

    fun findNearest(position: IGeoPoint): MarkerEntity<ActualMarker>? {
        checkNotDestroyed()
        val nearestId = nativeIndex.findNearest(position) ?: return null
        return entities[nearestId]
    }

    fun findByIdPrefix(prefix: String): List<HexCell> {
        checkNotDestroyed()
        return cellRegistry.findByIdPrefix(prefix)
    }

    fun registerEntity(entity: MarkerEntity<ActualMarker>) {
        checkNotDestroyed()
        entities[entity.state.id] = entity
        cellRegistry.setPoint(entity)
        nativeIndex.registerMarker(entity.state.id, entity.state.position, entity.state.clickable)
    }

    fun updateEntity(entity: MarkerEntity<ActualMarker>) {
        checkNotDestroyed()
        entities[entity.state.id] = entity
        cellRegistry.setPoint(entity)
        nativeIndex.updateMarker(entity.state.id, entity.state.position, entity.state.clickable)
    }

    fun allEntities(): List<MarkerEntity<ActualMarker>> {
        checkNotDestroyed()
        return entities.values.toList()
    }

    fun clear() {
        checkNotDestroyed()
        entities.clear()
        cellRegistry.clear()
        nativeIndex.clear()
    }

    fun findMarkersInBounds(bounds: com.mapconductor.core.features.GeoRectBounds): List<MarkerEntity<ActualMarker>> {
        checkNotDestroyed()
        if (bounds.isEmpty) return emptyList()

        val markerIds = nativeIndex.findMarkersInBounds(bounds)
        return markerIds.mapNotNull { id -> entities[id] }
    }
    
    /**
     * Properly destroy native resources when switching map providers
     * IMPORTANT: Call this when disposing of the MarkerManager
     */
    fun destroy() {
        if (!isDestroyed) {
            isDestroyed = true
            entities.clear()
            cellRegistry.clear()
            nativeIndex.destroy()
        }
    }
    
    private fun checkNotDestroyed() {
        if (isDestroyed) {
            throw IllegalStateException("MarkerManager has been destroyed")
        }
    }
    
    protected fun finalize() {
        destroy()
    }
}
