package com.mapconductor.core.spherical

import com.mapconductor.core.features.IGeoPoint
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

fun haversineDistance(
    p1: IGeoPoint,
    p2: IGeoPoint,
): Double {
    val earthR = 6371000.0 // 地球の半径（m）
    val lat1 = Math.toRadians(p1.latitude)
    val lat2 = Math.toRadians(p2.latitude)
    val dLat = lat2 - lat1
    val dLon = Math.toRadians(p2.longitude - p1.longitude)

    val a =
        sin(dLat / 2).pow(2.0) +
            cos(lat1) * cos(lat2) *
            sin(dLon / 2).pow(2.0)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earthR * c
}
