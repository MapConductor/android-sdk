package com.mapconductor.core.marker

import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.geocell.HexCell
import com.mapconductor.core.geocell.HexCellRegistry
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.geocell.HexGeocellInterface
import com.mapconductor.core.projection.Earth
import com.mapconductor.core.spherical.Spherical
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Memory usage statistics for MarkerManager optimization
 */
data class MarkerManagerStats(
    val entityCount: Int,
    val hasSpatialIndex: Boolean,
    val spatialIndexInitialized: Boolean,
    val estimatedMemoryKB: Long,
)

open class MarkerManager<ActualMarker>(
    protected val geocell: HexGeocellInterface,
    val minMarkerCount: Int,
) {
    // Primary storage - single source of truth
    private val entities = mutableMapOf<String, MarkerEntityInterface<ActualMarker>>()

    // Lazy-initialized spatial index only when needed
    private var cellRegistry: HexCellRegistry<ActualMarker>? = null

    private val semaphore = ReentrantReadWriteLock()
    private var writeLock: ReentrantReadWriteLock.WriteLock? = null

    @Volatile
    private var isDestroyed = false

    fun lock() {
        if (writeLock != null) return
        writeLock =
            semaphore.writeLock().also {
                it.tryLock()
            }
    }

    fun unlock() {
        writeLock?.unlock()
        writeLock = null
    }

    open fun getEntity(id: String): MarkerEntityInterface<ActualMarker>? {
        checkNotDestroyed()
        return entities.get(id)
    }

    open fun hasEntity(id: String): Boolean {
        checkNotDestroyed()
        return entities.containsKey(id)
    }

    open fun removeEntity(id: String): MarkerEntityInterface<ActualMarker>? {
        checkNotDestroyed()
        val removed = entities.remove(id)
        if (removed != null) {
            // Only update spatial index if it exists
            cellRegistry?.removePoint(removed)
        }
        return removed
    }

    open fun metersPerPixel(
        position: GeoPointInterface,
        zoom: Double,
        pixels: Double,
        tileSize: Int = 256,
    ): Double {
        checkNotDestroyed()
        // Optimized calculation without native reflection calls
        val pixelsAtZoom = tileSize * Math.pow(2.0, zoom)
        return Earth.CIRCUMFERENCE_METERS / pixelsAtZoom * Math.cos(Math.toRadians(position.latitude)) * pixels
    }

    open fun findNearest(position: GeoPointInterface): MarkerEntityInterface<ActualMarker>? {
        checkNotDestroyed()

        if (entities.size > minMarkerCount) { // Use spatial index for larger datasets
            semaphore.read {
                cellRegistry = ensureCellRegistry()
                cellRegistry?.let { registry ->
                    val nearestCell = registry.findNearest(position)
                    nearestCell?.let { cell ->
                        // Find the nearest entity within the nearest cell
                        return registry
                            .getEntryIDsByHexCell(cell)
                            ?.mapNotNull { id -> entities[id] }
                            ?.minByOrNull { entity ->
                                val deltaLatitude = entity.state.position.latitude - position.latitude
                                val deltaLongitude = entity.state.position.longitude - position.longitude
                                deltaLatitude * deltaLatitude + deltaLongitude * deltaLongitude
                            }
                    }
                }
            }
        }
        // Brute force search for small datasets
        return bruteForceNearest(position)
    }

    private fun bruteForceNearest(position: GeoPointInterface): MarkerEntityInterface<ActualMarker>? {
        semaphore.read {
            return entities.values.minByOrNull { entity ->
                val dx = entity.state.position.latitude - position.latitude
                val dy = entity.state.position.longitude - position.longitude
                dx * dx + dy * dy
            }
        }
    }

    open fun findByIdPrefix(prefix: String): List<HexCell> {
        checkNotDestroyed()
        semaphore.read {
            return cellRegistry?.findByIdPrefix(prefix) ?: emptyList()
        }
    }

    open fun registerEntity(entity: MarkerEntityInterface<ActualMarker>) {
        checkNotDestroyed()
        semaphore.write {
            entities[entity.state.id] = entity
            // Only update spatial index if it exists
            cellRegistry?.setPoint(entity)
        }
    }

    /**
     * Lazy-initialize the spatial index only when spatial operations are needed.
     * This saves memory for simple use cases that don't require spatial queries.
     */
    private fun ensureCellRegistry(): HexCellRegistry<ActualMarker> {
        if (cellRegistry == null) {
            cellRegistry = HexCellRegistry(geocell = geocell, zoom = 20.0)
            semaphore.write {
                // Re-index all existing entities
                entities.values.forEach { entity ->
                    cellRegistry!!.setPoint(entity)
                }
            }
        }
        return cellRegistry!!
    }

    open fun updateEntity(entity: MarkerEntityInterface<ActualMarker>) {
        checkNotDestroyed()
        semaphore.write {
            entities[entity.state.id] = entity
            // Only update spatial index if it exists
            cellRegistry?.setPoint(entity)
        }
    }

    open fun allEntities(): List<MarkerEntityInterface<ActualMarker>> {
        checkNotDestroyed()
        semaphore.read {
            return entities.values.toList()
        }
    }

    /**
     * Get memory usage statistics for debugging and optimization
     */
    fun getMemoryStats(): MarkerManagerStats {
        checkNotDestroyed()
        return MarkerManagerStats(
            entityCount = entities.size,
            hasSpatialIndex = cellRegistry != null,
            spatialIndexInitialized = cellRegistry != null,
            estimatedMemoryKB = estimateMemoryUsage() / 1024,
        )
    }

    private fun estimateMemoryUsage(): Long {
        // Rough estimation in bytes
        val entityMapOverhead = entities.size * 64L // Map entry overhead + string key
        val entityObjects = entities.size * 200L // Rough entity size
        val spatialIndexSize = if (cellRegistry != null) entities.size * 100L else 0L // Cell registry overhead
        return entityMapOverhead + entityObjects + spatialIndexSize
    }

    open fun clear() {
        checkNotDestroyed()
        entities.clear()
        cellRegistry?.clear()
    }

    fun findMarkersInBounds(
        bounds: com.mapconductor.core.features.GeoRectBounds,
    ): List<MarkerEntityInterface<ActualMarker>> {
        checkNotDestroyed()
        if (bounds.isEmpty) return emptyList()

        // For spatial queries, ensure the cell registry is initialized
        if (entities.size > minMarkerCount) { // Only use spatial index for larger datasets
            val registry = ensureCellRegistry()
            semaphore.read {
                val distance = Spherical.computeDistanceBetween(bounds.center!!, bounds.northEast!!)
                val hexCells = registry.findWithinRadiusWithDistance(bounds.center!!, distance)
                val entryIDs: List<String> =
                    hexCells
                        .map { registry.getEntryIDsByHexCell(it.cell) }
                        .mapNotNull { it }
                        .flatMap { it.toList() }
                return entryIDs.map { getEntity(it)!! }
            }
        }

        // Brute force filtering - simple and efficient for small to medium datasets
        return entities.values.filter { entity ->
            bounds.contains(entity.state.position)
        }
    }

    /**
     * Properly destroy resources when switching map providers
     * IMPORTANT: Call this when disposing of the MarkerManager
     */
    open fun destroy() {
        if (!isDestroyed) {
            semaphore.write {
                isDestroyed = true
                entities.clear()
                cellRegistry?.clear()
                cellRegistry = null
            }
        }
    }

    private fun checkNotDestroyed() {
        if (isDestroyed) {
            throw IllegalStateException("MarkerManager has been destroyed")
        }
    }

    protected open fun finalize() {
        destroy()
    }

    companion object {
        fun <ActualMarker> defaultManager(
            geocell: HexGeocellInterface? = null,
            minMarkerCount: Int = 2000,
        ): MarkerManager<ActualMarker> =
            MarkerManager<ActualMarker>(
                geocell = geocell ?: HexGeocell.defaultGeocell(),
                minMarkerCount = minMarkerCount,
            )
    }
}
