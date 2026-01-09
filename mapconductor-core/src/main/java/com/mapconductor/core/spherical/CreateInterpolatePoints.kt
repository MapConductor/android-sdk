package com.mapconductor.core.spherical

import com.mapconductor.core.features.GeoPointInterface
import net.sf.geographiclib.Geodesic

fun createInterpolatePoints(
    points: List<GeoPointInterface>,
    // 最大セグメント長（メートル）
    maxSegmentLength: Double = 10000.0,
): List<GeoPointInterface> {
    val results = mutableListOf<GeoPointInterface>()
    results.add(points[0])

    for (i in 1 until points.size) {
        val distance =
            Geodesic.WGS84
                .Inverse(
                    points[i - 1].latitude, points[i - 1].longitude,
                    points[i].latitude, points[i].longitude,
                ).s12

        val numSegments = (distance / maxSegmentLength).toInt().coerceAtLeast(1)
        val step = 1.0 / numSegments

        var fraction = step
        while (fraction < 1.0) {
            val point =
                GeographicLibCalculator.interpolate(
                    points[i - 1], points[i], fraction,
                )
            results.add(point)
            fraction += step
        }
        results.add(points[i])
    }
    return results
}
