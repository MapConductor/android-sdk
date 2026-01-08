package com.mapconductor.core.geocell

import androidx.compose.ui.geometry.Offset
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.projection.ProjectionInterface
import com.mapconductor.core.projection.WebMercator
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class HexCoord(
    val q: Int,
    val r: Int,
    val depth: Int = 0,
) {
    override fun toString(): String = "H${q}_${r}_$depth"

    // Cube coordinates (for easier calculations)
    val s: Int get() = -q - r

    // Get neighboring coordinates
    fun neighbors(): List<HexCoord> =
        Direction6.values().map {
            HexCoord(q + it.deltaQ, r + it.deltaR, depth)
        }
}

enum class Direction6(
    val deltaQ: Int,
    val deltaR: Int,
) {
    Right(1, 0),
    RightUp(1, -1),
    LeftUp(0, -1),
    Left(-1, 0),
    LeftDown(-1, 1),
    RightDown(0, 1),
}

data class HexCell(
    val coord: HexCoord,
    val centerLatLng: GeoPointInterface,
    val centerXY: Offset,
    val id: String,
) {
    fun idPrefix(levels: Int): String = id.split("_").take(levels + 1).joinToString("_")
}

data class HexCellWithDistance(
    val cell: HexCell,
    val distanceMeters: Double,
)

/**
 * Hexagonal geocell system for spatial indexing
 *
 * @param projection The map projection to use for coordinate conversion
 * @param baseHexSideLength The side length of hexagons in meters at zoom level 0
 *                          This is the actual edge length of the hexagon, not the radius
 */
class HexGeocell(
    override val projection: ProjectionInterface,
    // IMPORTANT: This is now the side length, not radius!
    // Use values like:
    // - 100-1000m for high zoom levels (15-18)
    // - 1000-10000m for medium zoom levels (10-15)
    // - 10000-100000m for low zoom levels (5-10)
    override val baseHexSideLength: Int = 1000,
) : HexGeocellInterface {
    /**
     * Convert lat/lng to hexagonal coordinate
     */
    override fun latLngToHexCoord(
        position: GeoPointInterface,
        zoom: Double,
    ): HexCoord {
        val hexSideLength = adjustedHexSideLength(position.latitude, zoom)
        val offset = projection.project(position)
        return pixelToHex(offset, hexSideLength)
    }

    /**
     * Convert lat/lng to hex cell with all computed properties
     */
    override fun latLngToHexCell(
        position: GeoPointInterface,
        zoom: Double,
    ): HexCell {
        val coord = latLngToHexCoord(position, zoom)
        val id = hexToCellId(coord, zoom)
        val centerLatLng = hexToLatLngCenter(coord, position.latitude, zoom)
        val centerXY = projection.project(centerLatLng)
        return HexCell(coord, centerLatLng, centerXY, id)
    }

    /**
     * Convert hex coordinate to lat/lng center
     */
    override fun hexToLatLngCenter(
        coord: HexCoord,
        latHint: Double,
        zoom: Double,
    ): GeoPointInterface {
        val hexSideLength = adjustedHexSideLength(latHint, zoom)
        val center = hexCenterXY(coord, hexSideLength)
        return projection.unproject(center)
    }

    /**
     * Generate unique cell ID including zoom level to prevent collisions
     */
    override fun hexToCellId(
        coord: HexCoord,
        zoom: Double,
    ): String = "H${coord.q}_${coord.r}_Z${zoom.toInt()}"

    /**
     * Get hexagon polygon vertices in lat/lng coordinates
     */
    override fun hexToPolygonLatLng(
        coord: HexCoord,
        latHint: Double,
        zoom: Double,
    ): List<GeoPointInterface> {
        val hexSideLength = adjustedHexSideLength(latHint, zoom)
        val center = hexCenterXY(coord, hexSideLength)

        // Calculate circumradius from side length
        val circumRadius = hexSideLength * 2.0 / sqrt(3.0)

        return (0 until 6).map { i ->
            val angle = Math.toRadians(60.0 * i - 30.0) // Start at -30° for flat-top
            val x = center.x + circumRadius * cos(angle)
            val y = center.y + circumRadius * sin(angle)
            projection.unproject(Offset(x.toFloat(), y.toFloat()))
        }
    }

    /**
     * Find the hex cell that encloses the centroid of multiple points
     */
    override fun enclosingCellOf(
        points: List<MarkerState>,
        zoom: Double,
    ): HexCell {
        require(points.isNotEmpty()) { "Points list cannot be empty" }

        val center = computeGeographicCentroid(points.map { it.position })
        val coord = latLngToHexCoord(center, zoom)
        val centerLatLng = hexToLatLngCenter(coord, center.latitude, zoom)
        val centerXY = projection.project(centerLatLng)
        val id = hexToCellId(coord, zoom)
        return HexCell(coord, centerLatLng, centerXY, id)
    }

    /**
     * Get hex cells for multiple points with their IDs
     */
    override fun hexCellsForPointsWithId(
        points: List<MarkerState>,
        zoom: Double,
    ): Set<IdentifiedHexCell> =
        points
            .map {
                val coord = latLngToHexCoord(it.position, zoom)
                val centerLatLng = hexToLatLngCenter(coord, it.position.latitude, zoom)
                val centerXY = projection.project(centerLatLng)
                val cellId = hexToCellId(coord, zoom)
                val cell = HexCell(coord, centerLatLng, centerXY, cellId)
                IdentifiedHexCell(it.id, cell)
            }.toSet()

    /**
     * Compute geographic centroid considering Earth's curvature (improved version)
     */
    private fun computeGeographicCentroid(points: List<GeoPointInterface>): GeoPointInterface {
        if (points.size == 1) return points[0]

        // Use spherical coordinates for better accuracy
        var x = 0.0
        var y = 0.0
        var z = 0.0

        points.forEach { point ->
            val latRad = point.latitude * PI / 180
            val lngRad = point.longitude * PI / 180

            x += cos(latRad) * cos(lngRad)
            y += cos(latRad) * sin(lngRad)
            z += sin(latRad)
        }

        x /= points.size
        y /= points.size
        z /= points.size

        val centralLng = atan2(y, x) * 180 / PI
        val centralSquareRoot = sqrt(x * x + y * y)
        val centralLat = atan2(z, centralSquareRoot) * 180 / PI

        return object : GeoPointInterface {
            override val latitude: Double = centralLat
            override val longitude: Double = centralLng
            override val altitude: Double? = null

            override fun wrap(): GeoPointInterface = GeoPoint(latitude, longitude, altitude ?: 0.0).wrap()
        }
    }

    /**
     * Calculate adjusted hex side length based on latitude and zoom
     */
    private fun adjustedHexSideLength(
        lat: Double,
        zoom: Double,
    ): Double {
        val scale = 1.0 / (2.0.pow(zoom))
        val latScale = cos(lat * PI / 180).coerceAtLeast(0.01) // Prevent division by zero
        return baseHexSideLength * scale / latScale
    }

    /**
     * Calculate hex center in XY coordinates from hex coordinate and side length
     * FIXED: Now correctly uses side length instead of radius
     */
    private fun hexCenterXY(
        coord: HexCoord,
        hexSideLength: Double,
    ): Offset {
        // For flat-top hexagons with side length s:
        // - Distance between adjacent hex centers in q direction = s * 3/2
        // - Distance between adjacent hex centers in r direction = s * √3
        val x = hexSideLength * (3.0 / 2.0 * coord.q)
        val y = hexSideLength * (sqrt(3.0) * (coord.r + coord.q / 2.0))
        return Offset(x.toFloat(), y.toFloat())
    }

    /**
     * Convert pixel coordinates to hex coordinate
     */
    private fun pixelToHex(
        offset: Offset,
        hexSideLength: Double,
    ): HexCoord {
        val q = (2.0 / 3.0 * offset.x / hexSideLength)
        val r = (-1.0 / 3.0 * offset.x + sqrt(3.0) / 3.0 * offset.y) / hexSideLength
        return cubeRound(q, r)
    }

    /**
     * Round fractional cube coordinates to the nearest hex coordinate
     */
    private fun cubeRound(
        q: Double,
        r: Double,
    ): HexCoord {
        val s = -q - r

        var rq = q.roundToInt()
        var rr = r.roundToInt()
        var rs = s.roundToInt()

        val qDiff = abs(rq - q)
        val rDiff = abs(rr - r)
        val sDiff = abs(rs - s)

        when {
            qDiff > rDiff && qDiff > sDiff -> rq = -rr - rs
            rDiff > sDiff -> rr = -rq - rs
            else -> rs = -rq - rr
        }

        return HexCoord(rq, rr)
    }

    /**
     * Calculate distance between two hex coordinates
     */
    override fun hexDistance(
        a: HexCoord,
        b: HexCoord,
    ): Int = (abs(a.q - b.q) + abs(a.q + a.r - b.q - b.r) + abs(a.r - b.r)) / 2

    /**
     * Get all hex coordinates within a certain distance
     */
    override fun hexRange(
        center: HexCoord,
        radius: Int,
    ): List<HexCoord> {
        val results = mutableListOf<HexCoord>()
        for (dq in -radius..radius) {
            val minR = maxOf(-radius, -dq - radius)
            val maxR = minOf(radius, -dq + radius)
            for (dr in minR..maxR) {
                results.add(HexCoord(center.q + dq, center.r + dr, center.depth))
            }
        }
        return results
    }

    companion object {
        fun defaultGeocell(): HexGeocellInterface =
            HexGeocell(
                projection = WebMercator,
                baseHexSideLength = 100000, // 100km - 中ズームレベルに適した値
            )
    }
}

data class IdentifiedHexCell(
    val id: String,
    val cell: HexCell,
)
