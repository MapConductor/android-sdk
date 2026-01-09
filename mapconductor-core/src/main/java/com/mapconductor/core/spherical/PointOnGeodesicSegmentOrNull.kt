package com.mapconductor.core.spherical

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import net.sf.geographiclib.Geodesic
import kotlin.math.min

fun pointOnGeodesicSegmentOrNull(
    from: GeoPointInterface,
    to: GeoPointInterface,
    position: GeoPointInterface,
    thresholdMeters: Double,
): Pair<GeoPointInterface, Double>? {
    val line =
        Geodesic.WGS84.InverseLine(
            from.latitude, from.longitude,
            to.latitude, to.longitude,
        )
    val totalDistance = line.Distance()

    if (totalDistance == 0.0) {
        val distPosFrom =
            Geodesic.WGS84
                .Inverse(
                    from.latitude, from.longitude,
                    position.latitude, position.longitude,
                ).s12
        return if (distPosFrom <= thresholdMeters) {
            Pair(GeoPoint(from.latitude, from.longitude, from.altitude ?: 0.0), distPosFrom)
        } else {
            null
        }
    }

    // 三分探索で最近点を見つける
    var left = 0.0
    var right = 1.0
    val epsilon = 1e-6 // 十分な精度

    while (right - left > epsilon) {
        val m1 = left + (right - left) / 3.0
        val m2 = right - (right - left) / 3.0

        val point1 = line.Position(totalDistance * m1)
        val dist1 =
            Geodesic.WGS84
                .Inverse(
                    point1.lat2, point1.lon2,
                    position.latitude, position.longitude,
                ).s12

        val point2 = line.Position(totalDistance * m2)
        val dist2 =
            Geodesic.WGS84
                .Inverse(
                    point2.lat2, point2.lon2,
                    position.latitude, position.longitude,
                ).s12

        if (dist1 > dist2) {
            left = m1
        } else {
            right = m2
        }
    }

    val bestFraction = (left + right) / 2.0

    // 線分外の判定
    if (bestFraction <= 0.0 || bestFraction >= 1.0) {
        val distFrom =
            Geodesic.WGS84
                .Inverse(
                    from.latitude, from.longitude,
                    position.latitude, position.longitude,
                ).s12
        val distTo =
            Geodesic.WGS84
                .Inverse(
                    to.latitude, to.longitude,
                    position.latitude, position.longitude,
                ).s12

        val actualMin = min(distFrom, distTo)
        if (actualMin > thresholdMeters) return null

        return Pair(
            if (distFrom <= distTo) {
                GeoPoint(from.latitude, from.longitude, from.altitude ?: to.altitude ?: 0.0)
            } else {
                GeoPoint(to.latitude, to.longitude, to.altitude ?: from.altitude ?: 0.0)
            },
            actualMin,
        )
    }

    val closestPoint = line.Position(totalDistance * bestFraction)

    val minDistance =
        Geodesic.WGS84
            .Inverse(
                closestPoint.lat2, closestPoint.lon2,
                position.latitude, position.longitude,
            ).s12

    if (minDistance > thresholdMeters) return null

    val altitude =
        when {
            from.altitude != null && to.altitude != null ->
                from.altitude!! + bestFraction * (to.altitude!! - from.altitude!!)
            from.altitude != null -> from.altitude!!
            to.altitude != null -> to.altitude!!
            else -> 0.0
        }

    val result = GeoPoint(closestPoint.lat2, closestPoint.lon2, altitude)
    return Pair(result, minDistance)
}
