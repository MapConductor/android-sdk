package com.mapconductor.core.geocell

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.marker.MarkerEntityInterface
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * HexCellRegistry - Thread-safe hexagonal cell management with KDTree spatial indexing
 *
 * @param T The actual marker type
 * @param geocell The hexagonal geocell system
 * @param zoom The zoom level for this registry
 */
class HexCellRegistry<ActualMarker>(
    private val geocell: HexGeocellInterface,
    private val zoom: Double,
) {
    private var kdTree: KDTree? = null
    private val allCells = ConcurrentHashMap<String, HexCell>()
    private val entryIDsByCell = ConcurrentHashMap<String, MutableSet<String>>()
    private val allEntries = ConcurrentHashMap<String, String>()

    // entityId -> cellId
    @Volatile
    private var needsRebuild = false

    // Thread safety for complex operations

    private val lock = ReentrantReadWriteLock()

    /**
     * Get the hex cell for a given entity without registering it
     */
    fun getCell(entity: MarkerEntityInterface<ActualMarker>): HexCell {
        val coord = geocell.latLngToHexCoord(entity.state.position, zoom)
        val centerLatLng = geocell.hexToLatLngCenter(coord, entity.state.position.latitude, zoom)
        val centerXY = geocell.projection.project(centerLatLng)
        val cellId = geocell.hexToCellId(coord, zoom) // Include zoom in ID
        return HexCell(coord, centerLatLng, centerXY, cellId)
    }

    /**
     * Register or update a point in the registry
     * @return The hex cell containing the point
     */
    fun setPoint(entity: MarkerEntityInterface<ActualMarker>): HexCell =
        lock.write {
            val entityId = entity.state.id

            // Remove from old cell if exists
            allEntries[entityId]?.let { oldCellId ->
                removeFromCell(oldCellId, entityId)
            }

            // Add to new cell
            val cell = getCell(entity)
            val cellId = cell.id

            allCells[cellId] = cell
            allEntries[entityId] = cellId

            // Add entity to cell's entry list
            entryIDsByCell.compute(cellId) { _, existingSet ->
                (existingSet ?: mutableSetOf()).apply { add(entityId) }
            }

            markDirty()
            return cell
        }

    /**
     * Check if a hex cell exists in the registry
     */
    fun contains(hexId: String): Boolean = allCells.containsKey(hexId)

    /**
     * Remove a point from the registry
     * @return true if the point was removed, false if it wasn't found
     */
    fun removePoint(entity: MarkerEntityInterface<ActualMarker>): Boolean =
        lock.write {
            val entityId = entity.state.id
            val cellId = allEntries[entityId] ?: return false

            val removed = removeFromCell(cellId, entityId)
            if (removed) {
                allEntries.remove(entityId)
                markDirty()
            }

            return removed
        }

    /**
     * Remove an entity from a specific cell
     * @return true if removed, false if not found
     */
    private fun removeFromCell(
        cellId: String,
        entityId: String,
    ): Boolean {
        val entryIds = entryIDsByCell[cellId] ?: return false
        val removed = entryIds.remove(entityId)

        if (removed && entryIds.isEmpty()) {
            // Remove empty cell
            allCells.remove(cellId)
            entryIDsByCell.remove(cellId)
        }
        return removed
    }

    /**
     * Clear all points and rebuild the spatial index
     */
    fun clear() =
        lock.write {
            allCells.clear()
            entryIDsByCell.clear()
            allEntries.clear()
            kdTree = null
            needsRebuild = false
        }

    /**
     * Mark the spatial index as needing rebuild
     */
    private fun markDirty() {
        needsRebuild = true
    }

    /**
     * Rebuild the spatial index if needed
     */
    private fun rebuildIfNeeded() {
        // まず read で「必要か」を見る（短時間）
        val dirty = lock.read { needsRebuild }
        if (!dirty) return

        // read を解放してから write で再確認＆再構築
        lock.write {
            if (!needsRebuild) return // ここに来るまでに別スレッドが rebuild 済みの場合
            kdTree =
                if (allCells.isNotEmpty()) {
                    KDTree(allCells.values.toList())
                } else {
                    null
                }
            needsRebuild = false
        }
    }

    /**
     * Find the nearest hex cell to a point
     */
    fun findNearest(point: GeoPointInterface): HexCell? {
        rebuildIfNeeded()
        return lock.read {
            kdTree?.nearest(geocell.projection.project(point))
        }
    }

    /**
     * Find the nearest hex cell with distance
     */
    fun findNearestWithDistance(point: GeoPointInterface): HexCellWithDistance? {
        rebuildIfNeeded()
        return lock.read {
            kdTree?.nearestWithDistance(geocell.projection.project(point))
        }
    }

    /**
     * Find k nearest hex cells with distances
     */
    fun findNearestKWithDistance(
        point: GeoPointInterface,
        k: Int,
    ): List<HexCellWithDistance> {
        rebuildIfNeeded()
        return lock.read {
            kdTree?.nearestKWithDistance(geocell.projection.project(point), k).orEmpty()
        }
    }

    /**
     * Find all hex cells within a radius with distances
     */
    fun findWithinRadiusWithDistance(
        point: GeoPointInterface,
        radius: Double,
    ): List<HexCellWithDistance> {
        rebuildIfNeeded()
        return lock.read {
            kdTree?.withinRadiusWithDistance(geocell.projection.project(point), radius).orEmpty()
        }
    }

    /**
     * Get all hex cells
     */
    fun all(): List<HexCell> = allCells.values.toList()

    /**
     * Get entity IDs for a specific hex cell
     */
    fun getEntryIDsByHexCell(hexCell: HexCell): Set<String>? =
        entryIDsByCell[hexCell.id]?.toSet() // Return immutable copy

    /**
     * Calculate meters per pixel at a given position and zoom level
     *
     * Note: This assumes the projection returns coordinates in meters.
     * Verify that your projection implementation meets this requirement.
     */
    fun metersPerPixel(
        position: GeoPointInterface,
        zoom: Double,
        pixels: Double,
        tileSize: Int = 256,
    ): Double {
        require(pixels > 0) { "Pixels must be positive" }
        require(tileSize > 0) { "Tile size must be positive" }

        val deltaLng = 360.0 * pixels / (tileSize * 2.0.pow(zoom))

        // Handle potential longitude overflow
        val newLng =
            (position.longitude + deltaLng).let { lng ->
                when {
                    lng > 180.0 -> lng - 360.0
                    lng < -180.0 -> lng + 360.0
                    else -> lng
                }
            }

        val p1 = geocell.projection.project(position)
        val p2 =
            geocell.projection.project(
                object : GeoPointInterface {
                    override val latitude = position.latitude
                    override val longitude = newLng
                    override val altitude = position.altitude

                    override fun wrap(): GeoPointInterface = GeoPoint(latitude, longitude, altitude ?: 0.0).wrap()
                },
            )

        val deltaX = p2.x - p1.x
        val deltaY = p2.y - p1.y
        return sqrt(deltaX * deltaX + deltaY * deltaY).toDouble()
    }

    /**
     * Find hex cells within a pixel radius
     */
    fun findWithinPixelRadius(
        position: GeoPointInterface,
        zoom: Double,
        pixels: Double,
        tileSize: Int = 256,
    ): List<HexCellWithDistance> {
        val meters = metersPerPixel(position, zoom, pixels, tileSize)
        return findWithinRadiusWithDistance(position, meters)
    }

    /**
     * Find hex cells by ID prefix (optimized for common prefixes)
     */
    fun findByIdPrefix(prefix: String): List<HexCell> {
        require(prefix.isNotEmpty()) { "Prefix cannot be empty" }

        return allCells.entries
            .asSequence()
            .filter { it.key.startsWith(prefix) }
            .map { it.value }
            .toList()
    }

    /**
     * Get statistics about the registry
     */
    fun getStats(): RegistryStats =
        RegistryStats(
            totalCells = allCells.size,
            totalEntries = allEntries.size,
            kdTreeBuilt = kdTree != null,
            needsRebuild = needsRebuild,
        )
}

/**
 * Statistics about the registry state
 */
data class RegistryStats(
    val totalCells: Int,
    val totalEntries: Int,
    val kdTreeBuilt: Boolean,
    val needsRebuild: Boolean,
)
