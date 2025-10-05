package com.mapconductor.core.spherical

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointImpl
import net.sf.geographiclib.Geodesic

object GeographicLibCalculator {
    private val wgs84 = Geodesic.WGS84

    fun computeDistanceBetween(
        from: GeoPoint,
        to: GeoPoint,
    ): Double {
        val result =
            wgs84.Inverse(
                from.latitude, from.longitude,
                to.latitude, to.longitude,
            )
        return result.s12 // 距離（メートル）
    }

    fun interpolate(
        from: GeoPoint,
        to: GeoPoint,
        fraction: Double,
    ): GeoPointImpl {
        val line =
            wgs84.InverseLine(
                from.latitude, from.longitude,
                to.latitude, to.longitude,
            )
        val result = line.Position(line.Distance() * fraction)

        val altitude =
            when {
                from.altitude != null && to.altitude != null ->
                    from.altitude!! + fraction * (to.altitude!! - from.altitude!!)
                from.altitude != null -> from.altitude!!
                to.altitude != null -> to.altitude!!
                else -> 0.0
            }

        return GeoPointImpl(result.lat2, result.lon2, altitude)
    }
}
