package com.mapconductor.core.spherical

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.projection.Earth
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

data class ClosestHit(
    // P中心の円が初めて線分ABに触れる半径
    val radiusMeters: Double,
    // その交点
    val hit: GeoPointInterface,
    // "planar" か "spherical"
    val mode: String,
)

object GeoNearest {
    // 平均地球半径（WGS84準拠の近似）
    private const val DEG = PI / 180.0
    private const val EPS = 1e-12

    fun closestIntersection(
        P: GeoPointInterface,
        A: GeoPointInterface,
        B: GeoPointInterface,
    ): ClosestHit {
        // スケール判定のために概算距離をいくつか見る
        val dPA = Spherical.computeDistanceBetween(P, A)
        val dPB = Spherical.computeDistanceBetween(P, B)
        val dAB = Spherical.computeDistanceBetween(A, B)
        val maxSpan = max(dAB, max(dPA, dPB))

        // ≲50km を局所平面、≳50km を球面に
        return if (maxSpan <= 50_000.0) {
            planarNearest(P, A, B)
        } else {
            sphericalNearest(P, A, B)
        }
    }

    // --- 1) 局所平面（equirectangular, 中心P基準） ---
    private fun planarNearest(
        P: GeoPointInterface,
        A: GeoPointInterface,
        B: GeoPointInterface,
    ): ClosestHit {
        // 中心Pの緯度に合わせてlonスケールをcos(phi)で補正
        val phi0 = P.latitude * DEG
        val kx = Earth.RADIUS_METERS * cos(phi0) * DEG
        val ky = Earth.RADIUS_METERS * DEG

        fun toLocalXY(X: GeoPointInterface): Pair<Double, Double> {
            val x = (normalizelongitude(X.longitude - P.longitude)) * kx
            val y = (X.latitude - P.latitude) * ky
            return Pair(x, y)
        }

        fun toGeoPoint(
            x: Double,
            y: Double,
        ): GeoPointInterface {
            val lat = P.latitude + (y / ky)
            val lon = P.longitude + (x / kx)
            return GeoPoint(lat, normalizeLon180(lon))
        }

        val (ax, ay) = toLocalXY(A)
        val (bx, by) = toLocalXY(B)
        val testPointX = 0.0
        val testPointY = 0.0

        val segmentVectorX = bx - ax
        val segmentVectorY = by - ay
        val pointVectorX = testPointX - ax
        val pointVectorY = testPointY - ay
        val segmentLengthSquared = segmentVectorX * segmentVectorX + segmentVectorY * segmentVectorY

        val t =
            if (segmentLengthSquared <
                EPS
            ) {
                0.0
            } else {
                ((pointVectorX * segmentVectorX + pointVectorY * segmentVectorY) / segmentLengthSquared)
                    .coerceIn(0.0, 1.0)
            }
        val projectionX = ax + t * segmentVectorX
        val projectionY = ay + t * segmentVectorY

        val deltaX = projectionX - testPointX
        val deltaY = projectionY - testPointY
        val d = hypot(deltaX, deltaY) // meters

        val hitLL = toGeoPoint(projectionX, projectionY)
        return ClosestHit(radiusMeters = d, hit = hitLL, mode = "planar")
    }

    // --- 2) 球面（大円） ---
    private fun sphericalNearest(
        P: GeoPointInterface,
        A: GeoPointInterface,
        B: GeoPointInterface,
    ): ClosestHit {
        // 角度をラジアン
        val p = toUnitVec(P)
        val a = toUnitVec(A)
        val b = toUnitVec(B)

        // AB大円の法線
        val n = cross(a, b)
        val nNorm = norm(n)
        if (nNorm < 1e-15) {
            // AとBがほぼ同一点：端点勝負
            return endpointChoice(p, A, B, "spherical")
        }
        val nHat = scale(n, 1.0 / nNorm)

        // pを大円に正射影（最短距離の点）
        // q = normalize( (n × (p × n)) )
        val q = normalize(cross(nHat, cross(p, nHat)))

        // qが弧ABの内側か確認（角距離の加法性で判定）
        val dAB = acos(clamp(dot(a, b), -1.0, 1.0))
        val dAQ = acos(clamp(dot(a, q), -1.0, 1.0))
        val dQB = acos(clamp(dot(q, b), -1.0, 1.0))
        val onArc = abs((dAQ + dQB) - dAB) <= 1e-12

        val chosenQ =
            if (onArc) {
                q
            } else {
                val dPA = acos(clamp(dot(p, a), -1.0, 1.0))
                val dPB = acos(clamp(dot(p, b), -1.0, 1.0))
                if (dPA <= dPB) a else b
            }

        val delta = acos(clamp(dot(p, chosenQ), -1.0, 1.0)) // radians
        val meters = delta * Earth.RADIUS_METERS
        val hitLL = toGeoPoint(chosenQ)

        return ClosestHit(radiusMeters = meters, hit = hitLL, mode = "spherical")
    }

    // --- ユーティリティ ---
    private fun toUnitVec(ll: GeoPointInterface): DoubleArray {
        val phi = ll.latitude * DEG
        val lam = ll.longitude * DEG
        val c = cos(phi)
        return doubleArrayOf(c * cos(lam), c * sin(lam), sin(phi))
    }

    private fun toGeoPoint(v: DoubleArray): GeoPointInterface {
        val x = v[0]
        val y = v[1]
        val z = v[2]
        val r = max(EPS, sqrt(x * x + y * y + z * z))
        val zn = (z / r).coerceIn(-1.0, 1.0)
        val lat = asin(zn) / DEG
        val lon = atan2(y, x) / DEG
        return GeoPoint(lat, normalizeLon180(lon))
    }

    private fun cross(
        u: DoubleArray,
        v: DoubleArray,
    ) = doubleArrayOf(u[1] * v[2] - u[2] * v[1], u[2] * v[0] - u[0] * v[2], u[0] * v[1] - u[1] * v[0])

    private fun dot(
        u: DoubleArray,
        v: DoubleArray,
    ) = u[0] * v[0] + u[1] * v[1] + u[2] * v[2]

    private fun norm(u: DoubleArray) = sqrt(dot(u, u))

    private fun scale(
        u: DoubleArray,
        s: Double,
    ) = doubleArrayOf(u[0] * s, u[1] * s, u[2] * s)

    private fun normalize(u: DoubleArray): DoubleArray {
        val n = norm(u)
        return if (n < EPS) doubleArrayOf(1.0, 0.0, 0.0) else scale(u, 1.0 / n)
    }

    private fun clamp(
        x: Double,
        lo: Double,
        hi: Double,
    ) = max(lo, min(hi, x))

    private fun normalizelongitude(dlon: Double): Double {
        var x = dlon
        while (x > 180.0) x -= 360.0
        while (x < -180.0) x += 360.0
        return x
    }

    private fun normalizeLon180(lon: Double): Double {
        var x = lon
        while (x > 180.0) x -= 360.0
        while (x < -180.0) x += 360.0
        return x
    }

    private fun endpointChoice(
        p: DoubleArray,
        A: GeoPointInterface,
        B: GeoPointInterface,
        mode: String,
    ): ClosestHit {
        val a = toUnitVec(A)
        val b = toUnitVec(B)
        val dPA = acos(clamp(dot(p, a), -1.0, 1.0))
        val dPB = acos(clamp(dot(p, b), -1.0, 1.0))
        val chosen = if (dPA <= dPB) A else B
        val meters = min(dPA, dPB) * Earth.RADIUS_METERS
        return ClosestHit(meters, chosen, mode)
    }
}
