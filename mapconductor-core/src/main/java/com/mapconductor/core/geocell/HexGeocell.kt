package com.mapconductor.core.geocell

import androidx.compose.ui.geometry.Offset
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.projection.Projection
import kotlin.math.PI
import kotlin.math.abs
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
}

enum class Direction6(
    val dq: Int,
    val dr: Int,
) {
    Right(1, 0),
    RightUp(1, -1),
    LeftUp(0, -1),
    Left(-1, 0),
    LeftDown(-1, 1),
    RightDown(0, 1),
}

interface IdentifiedPoint {
    val id: String
    val point: IGeoPoint
}

data class HexCell(
    val coord: HexCoord,
    val centerLatLng: IGeoPoint,
    val centerXY: Offset,
    val id: String,
) {
    fun idPrefix(levels: Int): String = id.split("_").take(levels + 1).joinToString("_")
}

data class HexCellWithDistance(
    val cell: HexCell,
    val distanceMeters: Double,
)

class HexGeocell(
    val projection: Projection,
    // The radius in meter when zoom level is zero.
    // i.e.
    // - Use the value from 5000 to 500 for high zoom level (12 - 18)
    // - Use the value from 30000 to 10000 for medium zoom level (9 - 12)
    // - Use the value from 1000000 to 100000 for low zoom level (6 - 9)
    val baseHexSize: Int = 10000,
) {
    fun latLngToHexCoord(
        position: IGeoPoint,
        zoom: Double,
    ): HexCoord {
        val hexSize = adjustedHexSize(position.latitude, zoom)
        val offset = projection.project(position)
        return pixelToHex(offset, hexSize)
    }

    fun latLngToHexCell(
        position: IGeoPoint,
        zoom: Double,
    ): HexCell {
        val coord = latLngToHexCoord(position, zoom)
        val id = hexToCellId(coord)
        val centerLatLng = hexToLatLngCenter(coord, position.latitude, zoom)
        val centerXY = projection.project(centerLatLng)
        return HexCell(coord, centerLatLng, centerXY, id)
    }

    fun hexToLatLngCenter(
        coord: HexCoord,
        latHint: Double,
        zoom: Double,
    ): IGeoPoint {
        val hexSize = adjustedHexSize(latHint, zoom)
        val center = hexCenterXY(coord, hexSize)
        return projection.unproject(center)
    }

    fun hexToCellId(coord: HexCoord): String = "H${coord.q}_${coord.r}"

    fun hexToPolygonLatLng(
        coord: HexCoord,
        latHint: Double,
        zoom: Double,
    ): List<IGeoPoint> {
        val hexSize = adjustedHexSize(latHint, zoom)
        val center = hexCenterXY(coord, hexSize)
        return (0 until 6).map { i ->
            val angle = Math.toRadians(60.0 * i - 30.0)
            val x = center.x + hexSize * cos(angle)
            val y = center.y + hexSize * sin(angle)
            projection.unproject(Offset(x.toFloat(), y.toFloat()))
        }
    }

    fun enclosingCellOf(
        points: List<IdentifiedPoint>,
        zoom: Double,
    ): HexCell {
        val center = computeCentroid(points.map { it.point })
        val coord = latLngToHexCoord(center, zoom)
        val centerLatLng = hexToLatLngCenter(coord, center.latitude, zoom)
        val centerXY = projection.project(centerLatLng)
        val id = hexToCellId(coord)
        return HexCell(coord, centerLatLng, centerXY, id)
    }

    fun hexCellsForPointsWithId(
        points: List<IdentifiedPoint>,
        zoom: Double,
    ): Set<IdentifiedHexCell> =
        points
            .map {
                val coord = latLngToHexCoord(it.point, zoom)
                val centerLatLng = hexToLatLngCenter(coord, it.point.latitude, zoom)
                val centerXY = projection.project(centerLatLng)
                val cellId = hexToCellId(coord)
                val cell = HexCell(coord, centerLatLng, centerXY, cellId)
                IdentifiedHexCell(it.id, cell)
            }.toSet()

    private fun computeCentroid(points: List<IGeoPoint>): IGeoPoint {
        val avgLat = points.map { it.latitude }.average()
        val avgLng = points.map { it.longitude }.average()
        return object : IGeoPoint {
            override val latitude: Double = avgLat
            override val longitude: Double = avgLng
            override val altitude: Double? = null
        }
    }

    private fun adjustedHexSize(
        lat: Double,
        zoom: Double,
    ): Double {
        val scale = 1.0 / (2.0.pow(zoom))
        val latScale = cos(lat * PI / 180).coerceAtLeast(0.01)
        return baseHexSize * scale / latScale
    }

    private fun hexCenterXY(
        coord: HexCoord,
        hexSize: Double,
    ): Offset {
        val x = hexSize * (3.0 / 2.0 * coord.q)
        val y = hexSize * (sqrt(3.0) * (coord.r + coord.q / 2.0))
        return Offset(x.toFloat(), y.toFloat())
    }

    private fun pixelToHex(
        offset: Offset,
        hexSize: Double,
    ): HexCoord {
        val q = (2.0 / 3.0 * offset.x / hexSize)
        val r = (-1.0 / 3.0 * offset.x + sqrt(3.0) / 3.0 * offset.y) / hexSize
        return cubeRound(q, r)
    }

    private fun cubeRound(
        q: Double,
        r: Double,
    ): HexCoord {
        val x = q
        val y = r
        val z = -x - y

        var rx = x.roundToInt()
        var ry = y.roundToInt()
        var rz = z.roundToInt()

        val dx = abs(rx - x)
        val dy = abs(ry - y)
        val dz = abs(rz - z)

        if (dx > dy && dx > dz) {
            rx = -ry - rz
        } else if (dy > dz) {
            ry = -rx - rz
        } else {
            rz = -rx - ry
        }

        return HexCoord(rx, ry)
    }
}

data class IdentifiedHexCell(
    val id: String,
    val cell: HexCell,
)
