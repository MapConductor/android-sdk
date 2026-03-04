package com.mapconductor.core.spherical

import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.features.GeoRectBounds

object LineSegmentUtils {
    fun createSegmentBounds(
        point1: GeoPointInterface,
        point2: GeoPointInterface,
        geodesic: Boolean = false,
    ): GeoRectBounds {
        val bounds = GeoRectBounds()
        if (!geodesic) {
            bounds.extend(point1)
            bounds.extend(point2)
            return bounds
        }
        // sample along the geodesic to approximate bounds
        val samples = 32
        bounds.extend(point1)
        for (s in 1..samples) {
            val f = s.toDouble() / samples
            val sp = Spherical.sphericalInterpolate(point1, point2, f)
            bounds.extend(sp)
        }
        return bounds
    }

    fun segmentIntersectsRegion(
        start: GeoPointInterface,
        end: GeoPointInterface,
        region: GeoRectBounds,
        geodesic: Boolean = false,
    ): Boolean {
        if (region.isEmpty) return false

        val segmentBounds = createSegmentBounds(start, end, geodesic)
        val result = segmentBounds.intersects(region)
        return result
    }
}
