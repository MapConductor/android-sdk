package com.mapconductor.core.spherical

import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.features.GeoRectBounds
import android.util.Log

object LineSegmentUtils {
    private const val DEBUG_SEGMENT = true
    private const val TAG = "LineSegmentUtils"

    private fun d(msg: String) {
        if (DEBUG_SEGMENT) Log.d(TAG, msg)
    }

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
        d(
            "segmentIntersectsRegion: seg=(${start.latitude}," +
                "${start.longitude})-(${end.latitude},${end.longitude}) bounds=$segmentBounds vis=$region -> $result",
        )
        return result
    }
}
