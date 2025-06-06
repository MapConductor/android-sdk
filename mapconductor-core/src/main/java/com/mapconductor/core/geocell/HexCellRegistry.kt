package com.mapconductor.core.geocell

import com.mapconductor.core.features.IGeoPoint
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.pow
import kotlin.math.sqrt

// HexCellRegistry（KDTree + markDirty制御）

class HexCellRegistry<T : IdentifiedPoint>(
    private val geocell: HexGeocell,
    private val zoom: Double,
) {
    private var kdTree: KDTree? = null
    private val allCells = ConcurrentHashMap<String, HexCell>()
    private val entryIDsByCell = ConcurrentHashMap<String, List<String>>()
    private val allEntries = ConcurrentHashMap<String, String>()
    private var needsRebuild = false

    //    fun registerPoints(points: List<T>) {
//        allCells.clear()
//        points.forEach {
//            val coord = geocell.latLngToHex(it.point, zoom)
//            val centerLatLng = geocell.hexToLatLngCenter(coord, it.point.latitude, zoom)
//            val centerXY = geocell.projection.project(centerLatLng)
//            val id = geocell.hexToCellId(coord)
//            val cell = HexCell(coord, centerLatLng, centerXY, id)
//            allCells[it.id] = cell
//        }
//        kdTree = KDTree(allCells.values.toList())
//        needsRebuild = false
//    }
    fun getCell(entry: T): HexCell {
        val coord = geocell.latLngToHexCoord(entry.position, zoom)
        val centerLatLng = geocell.hexToLatLngCenter(coord, entry.position.latitude, zoom)
        val centerXY = geocell.projection.project(centerLatLng)
        val cellId = geocell.hexToCellId(coord)
        return HexCell(coord, centerLatLng, centerXY, cellId)
    }

    fun setPoint(entry: T): HexCell {
        allEntries[entry.id]?.let { cellId ->
            entryIDsByCell[cellId]?.let { entryIDs ->
                val removed = entryIDs.filter { it != entry.id }
                if (removed.isEmpty()) {
                    allCells.remove(cellId)
                    entryIDsByCell.remove(cellId)
                } else {
                    entryIDsByCell[cellId] = removed
                }
            }
        }

        val cell = getCell(entry)
        val cellId = cell.id

        allCells[cellId] = cell
        allEntries[entry.id] = cell.id
        entryIDsByCell[cellId] = (entryIDsByCell[cellId] ?: emptyList<String>()) + entry.id
        markDirty()
        return cell
    }

    fun contains(hexId: String): Boolean = allCells.containsKey(hexId)

    fun removePoint(entry: T): Boolean {
        val cell = getCell(entry)
        val cellId = cell.id
        val entryIDs = entryIDsByCell[cellId] ?: return false
        markDirty()
        if (entryIDs.isEmpty()) {
            allCells.remove(cellId)
            entryIDsByCell.remove(cellId)
            return true
        }
        val removed = entryIDs.filter { it != entry.id }
        if (removed.isEmpty()) {
            allCells.remove(cellId)
            entryIDsByCell.remove(cellId)
            return true
        }
        entryIDsByCell[cellId] = removed
        return true
    }

    fun clear() {
        allCells.clear()
        needsRebuild = false
        kdTree = null
    }

    fun markDirty() {
        needsRebuild = true
    }

    private fun rebuildIfNeeded() {
        if (needsRebuild) {
            kdTree = KDTree(allCells.values.toList())
            needsRebuild = false
        }
    }

    fun findNearest(point: IGeoPoint): HexCell? {
        rebuildIfNeeded()
        return kdTree?.nearest(geocell.projection.project(point))
    }

    fun findNearestWithDistance(point: IGeoPoint): HexCellWithDistance? {
        rebuildIfNeeded()
        return kdTree?.nearestWithDistance(geocell.projection.project(point))
    }

    fun findNearestKWithDistance(
        point: IGeoPoint,
        k: Int,
    ): List<HexCellWithDistance> {
        rebuildIfNeeded()
        return kdTree?.nearestKWithDistance(geocell.projection.project(point), k).orEmpty()
    }

    fun findWithinRadiusWithDistance(
        point: IGeoPoint,
        radius: Double,
    ): List<HexCellWithDistance> {
        rebuildIfNeeded()
        return kdTree?.withinRadiusWithDistance(geocell.projection.project(point), radius).orEmpty()
    }

    fun all(): List<HexCell> = allCells.values.toList()

    fun getEntryIDsByHexCell(hexCell: HexCell): List<String>? = entryIDsByCell[hexCell.id]

    fun metersPerPixel(
        position: IGeoPoint,
        zoom: Double,
        pixels: Double,
        tileSize: Int = 256,
    ): Double {
        // val scale = 1.0 / (2.0.pow(zoom)) // ピクセルの地図上のスケール
        val deltaLng = 360.0 * pixels / (tileSize * 2.0.pow(zoom)) // 経度方向にずらす

        val p1 = geocell.projection.project(position)
        val p2 =
            geocell.projection.project(
                object : IGeoPoint {
                    override val latitude
                        get() = position.latitude
                    override val longitude: Double
                        get() = position.longitude + deltaLng
                    override val altitude: Double?
                        get() = position.altitude
                },
            )

        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        return sqrt(dx * dx + dy * dy).toDouble()
    }

    fun findWithinPixelRadius(
        position: IGeoPoint,
        zoom: Double,
        pixels: Double,
        tileSize: Int = 256,
    ): List<HexCellWithDistance> {
        val meters = metersPerPixel(position, zoom, pixels, tileSize)
        return findWithinRadiusWithDistance(position, meters)
    }

    fun findByIdPrefix(prefix: String): List<HexCell> = all().filter { it.id.startsWith(prefix) }
}
