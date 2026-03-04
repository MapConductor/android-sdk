package com.mapconductor.core.polygon

import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.normalizeLng
import com.mapconductor.core.spherical.createInterpolatePoints
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

interface PolygonManagerInterface<ActualPolygon> {
    fun registerEntity(entity: PolygonEntityInterface<ActualPolygon>)

    fun removeEntity(id: String): PolygonEntityInterface<ActualPolygon>?

    fun getEntity(id: String): PolygonEntityInterface<ActualPolygon>?

    fun hasEntity(id: String): Boolean

    fun allEntities(): List<PolygonEntityInterface<ActualPolygon>>

    fun clear()

    fun find(position: GeoPointInterface): PolygonEntityInterface<ActualPolygon>?
}

class PolygonManager<ActualPolygon> : PolygonManagerInterface<ActualPolygon> {
    private val entities = mutableMapOf<String, PolygonEntityInterface<ActualPolygon>>()

    override fun registerEntity(entity: PolygonEntityInterface<ActualPolygon>) {
        entities[entity.state.id] = entity
    }

    override fun removeEntity(id: String): PolygonEntityInterface<ActualPolygon>? = entities.remove(id)

    override fun getEntity(id: String): PolygonEntityInterface<ActualPolygon>? = entities[id]

    override fun hasEntity(id: String): Boolean = entities.containsKey(id)

    override fun allEntities(): List<PolygonEntityInterface<ActualPolygon>> = entities.values.toList()

    override fun clear() {
        entities.clear()
    }

    override fun find(position: GeoPointInterface): PolygonEntityInterface<ActualPolygon>? {
        val testX = normalizeLng(position.longitude)
        val testY = position.latitude

        // Iterate from top-most to bottom-most by zIndex
        for (entity in entities.values.sortedByDescending { it.state.zIndex }) {
            val state = entity.state
            val basePoints = state.points
            if (basePoints.size < 3) continue

            // Densify edges to better approximate geodesic/linear edges
            val ring =
                try {
                    if (state.geodesic) createInterpolatePoints(basePoints) else basePoints
                } catch (_: Exception) {
                    basePoints
                }

            // Ensure closed ring
            val closedRing =
                if (ring.first() != ring.last()) ring + ring.first() else ring

            if (pointInPolygonWindingNumber(testX, testY, closedRing)) {
                // Exclude holes: if the point is inside any hole, treat it as outside.
                val holes = state.holes
                var inHole = false
                for (hole in holes) {
                    if (hole.size < 3) continue
                    val holeRing =
                        try {
                            if (state.geodesic) createInterpolatePoints(hole) else hole
                        } catch (_: Exception) {
                            hole
                        }
                    val closedHole =
                        if (holeRing.first() != holeRing.last()) holeRing + holeRing.first() else holeRing
                    if (pointInPolygonWindingNumber(testX, testY, closedHole)) {
                        inHole = true
                        break
                    }
                }
                if (!inHole) return entity
            }
        }
        return null
    }

    private fun pointInPolygonWindingNumber(
        testX: Double,
        testY: Double,
        ring: List<GeoPointInterface>,
    ): Boolean {
        if (ring.size < 3) return false

        // Unwrap longitudes around the test longitude to handle antimeridian
        val unwrapped = unwrapLongitudesAround(ring, testX)

        // Quick bounding box check
        var minY = Double.POSITIVE_INFINITY
        var maxY = Double.NEGATIVE_INFINITY
        var minX = Double.POSITIVE_INFINITY
        var maxX = Double.NEGATIVE_INFINITY
        for (p in unwrapped) {
            minY = min(minY, p.second)
            maxY = max(maxY, p.second)
            minX = min(minX, p.first)
            maxX = max(maxX, p.first)
        }
        if (testY < minY || testY > maxY || testX < minX - 1.0 || testX > maxX + 1.0) return false

        val eps = 1e-6
        var wn = 0 // winding number

        var i = 0
        while (i < unwrapped.size - 1) {
            val ax = unwrapped[i].first
            val ay = unwrapped[i].second
            val bx = unwrapped[i + 1].first
            val by = unwrapped[i + 1].second

            // On-edge check
            if (pointOnSegment(testX, testY, ax, ay, bx, by, eps)) return true

            // Upward crossing
            if (ay <= testY) {
                if (by > testY && isLeft(ax, ay, bx, by, testX, testY) > 0) {
                    wn++
                }
            } else {
                // Downward crossing
                if (by <= testY && isLeft(ax, ay, bx, by, testX, testY) < 0) {
                    wn--
                }
            }
            i++
        }
        return wn != 0
    }

    private fun isLeft(
        ax: Double,
        ay: Double,
        bx: Double,
        by: Double,
        px: Double,
        py: Double,
    ): Double = (bx - ax) * (py - ay) - (by - ay) * (px - ax)

    private fun pointOnSegment(
        px: Double,
        py: Double,
        ax: Double,
        ay: Double,
        bx: Double,
        by: Double,
        eps: Double,
    ): Boolean {
        val dx = bx - ax
        val dy = by - ay
        val cross = dx * (py - ay) - dy * (px - ax)
        val segLen = sqrt(dx * dx + dy * dy)
        if (abs(cross) > eps * max(1.0, segLen)) return false
        val dot = (px - ax) * (px - bx) + (py - ay) * (py - by)
        return dot <= eps * max(1.0, segLen)
    }

    private fun unwrapLongitudesAround(
        points: List<GeoPointInterface>,
        refLng: Double,
    ): List<Pair<Double, Double>> {
        if (points.isEmpty()) return emptyList()
        val result = ArrayList<Pair<Double, Double>>(points.size)

        var prevX = Double.NaN
        for (p in points) {
            var x = normalizeLng(p.longitude)
            val y = p.latitude
            if (prevX.isNaN()) {
                // Shift first near reference
                val k = Math.round((refLng - x) / 360.0).toInt()
                x += 360.0 * k
            } else {
                // Keep continuity with previous
                var delta = x - prevX
                if (delta > 180.0) {
                    val k = Math.floor((delta + 180.0) / 360.0).toInt()
                    x -= 360.0 * k
                } else if (delta < -180.0) {
                    val k = Math.floor((-delta + 180.0) / 360.0).toInt()
                    x += 360.0 * k
                }
            }
            result.add(Pair(x, y))
            prevX = x
        }
        return result
    }
}
