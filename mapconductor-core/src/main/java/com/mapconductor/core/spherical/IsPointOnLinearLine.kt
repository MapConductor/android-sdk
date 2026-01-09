package com.mapconductor.core.spherical

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.normalizeLng
import kotlin.math.cos
import kotlin.math.sqrt

/**
 * position が from–to の「直線（平面）線分」から threshold[m] 以内か判定。
 * 地球の丸みは無視し、経度は短い差分を用いて unwrap します（±180°跨ぎ対応）。
 */
fun isPointOnLinearLine(
    from: GeoPointInterface,
    to: GeoPointInterface,
    position: GeoPointInterface,
    thresholdMeters: Double,
): Pair<GeoPointInterface, Double>? {
    // --- 経度の unwrap（短い経路を採用） ---
    val fromLng = from.longitude
    val toLng = to.longitude
    val directDiff = toLng - fromLng
    val crossMeridianDiff =
        when {
            directDiff > 180.0 -> directDiff - 360.0
            directDiff < -180.0 -> directDiff + 360.0
            else -> directDiff
        }
    val toLngUnwrapped = fromLng + crossMeridianDiff

    // position も from を基準に unwrap（±180 内に収める）
    fun unwrapLngRelative(
        baseLng: Double,
        targetLng: Double,
    ): Double {
        var diff = targetLng - baseLng
        while (diff > 180.0) diff -= 360.0
        while (diff < -180.0) diff += 360.0
        return baseLng + diff
    }
    val posLngUnwrapped = unwrapLngRelative(fromLng, position.longitude)

    // --- 緯度経度 → 平面(メートル)近似 ---
    val lat0Rad = Math.toRadians((from.latitude + to.latitude) / 2.0)
    val metersPerDegLat = 111_132.954
    val metersPerDegLng = metersPerDegLat * cos(lat0Rad)

    data class P(
        val x: Double,
        val y: Double,
    )

    fun toMetersPoint(
        lat: Double,
        lng: Double,
    ) = P(x = lng * metersPerDegLng, y = lat * metersPerDegLat)

    val a = toMetersPoint(from.latitude, fromLng)
    val b = toMetersPoint(to.latitude, toLngUnwrapped)
    val pp = toMetersPoint(position.latitude, posLngUnwrapped)

    val segmentVectorX = b.x - a.x
    val segmentVectorY = b.y - a.y
    val pointVectorX = pp.x - a.x
    val pointVectorY = pp.y - a.y
    val segmentLengthSquared = segmentVectorX * segmentVectorX + segmentVectorY * segmentVectorY

    // --- 退化: from==to は点距離で判定 ---
    if (segmentLengthSquared == 0.0) {
        val deltaX = pp.x - a.x
        val deltaY = pp.y - a.y
        val d = sqrt(deltaX * deltaX + deltaY * deltaY)
        if (d > thresholdMeters) return null

        // 最近点は from 自身
        val alt =
            when {
                from.altitude != null -> from.altitude!!
                to.altitude != null -> to.altitude!!
                else -> 0.0
            }
        return Pair<GeoPointInterface, Double>(
            GeoPoint(
                latitude = from.latitude,
                longitude = normalizeLng(fromLng),
                altitude = alt,
            ),
            d,
        )
    }

    // --- 線分への射影（最近点） ---
    val t = ((pointVectorX * segmentVectorX + pointVectorY * segmentVectorY) / segmentLengthSquared).coerceIn(0.0, 1.0)
    val projectionX = a.x + t * segmentVectorX
    val projectionY = a.y + t * segmentVectorY
    val deltaX = pp.x - projectionX
    val deltaY = pp.y - projectionY
    val distanceMeters = sqrt(deltaX * deltaX + deltaY * deltaY)

    // --- t を地理座標に戻す（linearInterpolate と同じルール） ---
    val latitude = from.latitude + t * (to.latitude - from.latitude)
    val longitude = fromLng + t * crossMeridianDiff

    if (distanceMeters > thresholdMeters) return null

    val alt =
        when {
            from.altitude != null && to.altitude != null ->
                from.altitude!! + t * (to.altitude!! - from.altitude!!)
            from.altitude != null -> from.altitude!!
            to.altitude != null -> to.altitude!!
            else -> 0.0
        }

    return Pair<GeoPointInterface, Double>(
        GeoPoint(
            latitude = latitude,
            longitude = normalizeLng(longitude),
            altitude = alt,
        ),
        distanceMeters,
    )
}
