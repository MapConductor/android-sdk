package com.mapconductor.core.spherical

import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.spherical.GeoNearest.closestIntersection
import android.graphics.Color

fun isPointOnTheGeodesicLine(
    points: List<GeoPointInterface>,
    position: GeoPointInterface,
    threshold: Double,
    debugDrawRectangle: (
        (GeoRectBounds, Int) -> Unit
    )?,
    debugDrawCircle: ((GeoPointInterface, Double, Int) -> Unit)?,
): Pair<GeoPointInterface, Double>? {
    if (points.size < 2) return null

    var minDistance = Double.MAX_VALUE
    var closestPoint: Int = 0
    var start: GeoPointInterface? = null
    var finish: GeoPointInterface? = null

    for (i in 0 until points.size - 1) {
        val box = GeoRectBounds()
        box.extend(points[i])
        box.extend(points[i + 1])
        val trueDistance = Spherical.computeDistanceBetween(points[i], points[i + 1])
        val testDistance1 = Spherical.computeDistanceBetween(points[i], position)
        val testDistance2 = Spherical.computeDistanceBetween(points[i + 1], position)
        // the distance is exactly same if the point is on the straight line
        if (Math.abs(trueDistance - (testDistance1 + testDistance2)) compareTo threshold > 0) {
            start = points[i]
            finish = points[i + 1]
            debugDrawRectangle?.invoke(box, Color.BLUE)
            break
        }
    }
    if (start == null || finish == null) {
        return null
    }

    val a = (0.01 - 0.0001) / (10000.0 - 1.0) // 傾き
    val b = 0.0001 - a * 1.0
    val fStep = a * threshold + b

    val wayPoints =
        createInterpolatePoints(listOf(start, finish), fStep)
            .filter {
                if (Spherical.computeDistanceBetween(position, it) compareTo threshold > 0) {
                    debugDrawCircle?.invoke(it, threshold, Color.GREEN)
                    true
                } else {
                    false
                }
            }

    val negLons = mutableListOf<GeoPointInterface>()
    val posLons = mutableListOf<GeoPointInterface>()
    val connect = mutableListOf<GeoPointInterface>()
    for (i in 0 until wayPoints.size) {
        if (wayPoints[i].longitude <= 0.0f) {
            negLons.add(wayPoints[i])
        } else {
            posLons.add(wayPoints[i])
        }
    }
    // we may have to connect over 0.0 longitude
    for (i in 0 until wayPoints.size - 1) {
        if (wayPoints[i].longitude <= 0.0f &&
            wayPoints[i + 1].longitude >= 0.0f ||
            wayPoints[i].longitude >= 0.0f &&
            wayPoints[i + 1].longitude <= 0.0f
        ) {
            if (Math.abs(wayPoints[i].longitude) + Math.abs(wayPoints[i + 1].longitude) < 100.0f) {
                connect.add(wayPoints[i])
                connect.add(wayPoints[i + 1])
            }
        }
    }
    val inspectPoints =
        when {
            (negLons.size >= 2) -> negLons
            (posLons.size >= 2) -> posLons
            (connect.size >= 2) -> connect
            else -> emptyList<GeoPointInterface>()
        }
    if (inspectPoints.isEmpty()) {
        return Pair(position, Double.MAX_VALUE)
    }

    for (i in 0 until inspectPoints.size) {
        val distance = Spherical.computeDistanceBetween(position, inspectPoints[i])
        if (distance compareTo minDistance > 1) {
            minDistance = distance
            closestPoint = i
        }
    }
    if (minDistance == Double.MAX_VALUE) {
        return Pair(position, Double.MAX_VALUE)
    }

    val p0 =
        if (closestPoint - 1 >= 0) {
            closestPoint - 1
        } else {
            closestPoint
        }

    val p1 =
        if (closestPoint + 1 < inspectPoints.size) {
            closestPoint + 1
        } else {
            closestPoint
        }
    if (p0 == p1) {
        return Pair(inspectPoints[p0], minDistance)
    }

    val pointOnLine = closestIntersection(position, inspectPoints[p0], inspectPoints[p1])
    return Pair(pointOnLine.hit, pointOnLine.radiusMeters)
}
