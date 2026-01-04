package com.mapconductor.core.polyline

import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.spherical.Spherical
import android.util.Log

interface PolylineEntityInterface<ActualPolyline> {
    val polyline: ActualPolyline
    val state: PolylineState
    val fingerPrint: PolylineFingerPrint
    val bounds: GeoRectBounds
}

class PolylineEntity<ActualPolyline>(
    override val polyline: ActualPolyline,
    override val state: PolylineState,
) : PolylineEntityInterface<ActualPolyline> {
    override val fingerPrint: PolylineFingerPrint = state.fingerPrint()

    private var cachedBounds: GeoRectBounds? = null
    private var boundsFingerprint: Int? = null
    private val tag = "PolylineEntityInterface"

    override val bounds: GeoRectBounds
        get() {
            val currentFingerprint = 31 * state.points.hashCode() + state.geodesic.hashCode()
            if (cachedBounds == null || boundsFingerprint != currentFingerprint) {
                cachedBounds = calculateBounds()
                boundsFingerprint = currentFingerprint
                Log.d(tag, "calc bounds id=${state.id} -> $cachedBounds")
            }
            return cachedBounds!!
        }

    private fun calculateBounds(): GeoRectBounds {
        val bounds = GeoRectBounds()
        val pts = state.points
        if (pts.isEmpty()) return bounds

        if (!state.geodesic) {
            pts.forEach { point ->
                bounds.extend(point)
            }
            return bounds
        }

        // Geodesic: sample along each segment to capture bulges for bounds
        bounds.extend(pts.first())
        for (i in 0 until pts.size - 1) {
            val p1 = pts[i]
            val p2 = pts[i + 1]
            val samples = 32
            for (s in 1..samples) {
                val f = s.toDouble() / samples
                val sp = Spherical.sphericalInterpolate(p1, p2, f)
                bounds.extend(sp)
            }
        }
        return bounds
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PolylineEntity<*>

        if (polyline != other.polyline) return false
        if (state != other.state) return false

        return true
    }

    override fun hashCode(): Int {
        var result = polyline?.hashCode() ?: 0
        result = 31 * result + state.hashCode()
        return result
    }
}
