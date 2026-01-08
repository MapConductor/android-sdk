package com.mapconductor.core.spherical

import com.mapconductor.core.features.GeoPointInterface

fun createLinearInterpolatePoints(
    points: List<GeoPointInterface>,
    fractionStep: Double = 0.01,
): List<GeoPointInterface> {
    val results = mutableListOf<GeoPointInterface>()
    results.add(points[0])
    for (i in 1 until points.size) {
        var fraction = fractionStep
        while (fraction <= 1.0) {
            val point =
                Spherical.linearInterpolate(
                    from = points[i - 1],
                    to = points[i],
                    fraction = fraction,
                )
            results.add(point)
            fraction += fractionStep
        }
        results.add(points[i])
    }
    return results
}
