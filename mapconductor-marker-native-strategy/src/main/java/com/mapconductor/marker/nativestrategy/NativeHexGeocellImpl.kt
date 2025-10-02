package com.mapconductor.marker.nativestrategy

import androidx.compose.ui.geometry.Offset
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.geocell.HexCell
import com.mapconductor.core.geocell.HexCoord
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.geocell.IdentifiedHexCell
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.projection.Projection
import com.mapconductor.core.projection.WebMercator
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Native-compatible HexGeocell implementation that mirrors the API of
 * com.mapconductor.core.geocell.HexGeocell while living in the native strategy module.
 */
class NativeHexGeocellImpl(
    override val projection: Projection,
    override val baseHexSideLength: Int = 1000,
) : HexGeocell {
    override fun latLngToHexCoord(
        position: GeoPoint,
        zoom: Double,
    ): HexCoord {
        val hexSideLength = adjustedHexSideLength(position.latitude, zoom)
        val offset = projection.project(position)
        return pixelToHex(offset, hexSideLength)
    }

    override fun latLngToHexCell(
        position: GeoPoint,
        zoom: Double,
    ): HexCell {
        val coord = latLngToHexCoord(position, zoom)
        val id = hexToCellId(coord, zoom)
        val centerLatLng = hexToLatLngCenter(coord, position.latitude, zoom)
        val centerXY = projection.project(centerLatLng)
        return HexCell(coord, centerLatLng, centerXY, id)
    }

    override fun hexToLatLngCenter(
        coord: HexCoord,
        latHint: Double,
        zoom: Double,
    ): GeoPoint {
        val hexSideLength = adjustedHexSideLength(latHint, zoom)
        val center = hexCenterXY(coord, hexSideLength)
        return projection.unproject(center)
    }

    override fun hexToCellId(
        coord: HexCoord,
        zoom: Double,
    ): String = "H${coord.q}_${coord.r}_Z${zoom.toInt()}"

    override fun hexToPolygonLatLng(
        coord: HexCoord,
        latHint: Double,
        zoom: Double,
    ): List<GeoPoint> {
        val hexSideLength = adjustedHexSideLength(latHint, zoom)
        val center = hexCenterXY(coord, hexSideLength)
        val circumRadius = hexSideLength * 2.0 / sqrt(3.0)
        return (0 until 6).map { i ->
            val angle = Math.toRadians(60.0 * i - 30.0)
            val x = center.x + circumRadius * cos(angle)
            val y = center.y + circumRadius * sin(angle)
            projection.unproject(Offset(x.toFloat(), y.toFloat()))
        }
    }

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

    private fun computeGeographicCentroid(points: List<GeoPoint>): GeoPoint {
        if (points.size == 1) return points[0]
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
        return object : GeoPoint {
            override val latitude: Double = centralLat
            override val longitude: Double = centralLng
            override val altitude: Double? = null

            override fun wrap(): GeoPoint = GeoPointImpl(latitude, longitude, altitude ?: 0.0).wrap()
        }
    }

    private fun adjustedHexSideLength(
        lat: Double,
        zoom: Double,
    ): Double {
        val scale = 1.0 / (2.0.pow(zoom))
        val latScale = cos(lat * PI / 180).coerceAtLeast(0.01)
        return baseHexSideLength * scale / latScale
    }

    private fun hexCenterXY(
        coord: HexCoord,
        hexSideLength: Double,
    ): Offset {
        val x = hexSideLength * (3.0 / 2.0 * coord.q)
        val y = hexSideLength * (sqrt(3.0) * (coord.r + coord.q / 2.0))
        return Offset(x.toFloat(), y.toFloat())
    }

    private fun pixelToHex(
        offset: Offset,
        hexSideLength: Double,
    ): HexCoord {
        val q = (2.0 / 3.0 * offset.x / hexSideLength)
        val r = (-1.0 / 3.0 * offset.x + sqrt(3.0) / 3.0 * offset.y) / hexSideLength
        return cubeRound(q, r)
    }

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

    override fun hexDistance(
        a: HexCoord,
        b: HexCoord,
    ): Int = (abs(a.q - b.q) + abs(a.q + a.r - b.q - b.r) + abs(a.r - b.r)) / 2

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
        fun defaultGeocell(): HexGeocell =
            NativeHexGeocellImpl(
                projection = WebMercator,
                baseHexSideLength = 100000, // 100km - 中ズームレベルに適した値
            )
    }
}
