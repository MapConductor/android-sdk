package com.mapconductor.core.spherical

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.projection.Earth
import java.lang.Math.toRadians
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

object WGS84Geodesic {
    // WGS84 楕円体パラメータ
    private const val FLATTENING = 1.0 / 298.257223563 // 扁平率
    private const val SEMI_MINOR_AXIS = Earth.RADIUS_METERS * (1.0 - FLATTENING) // 極半径

    /**
     * Vincenty の公式を使用した WGS84 楕円体上の距離計算
     * Google Maps の測地線計算と互換性があります
     */
    fun computeDistanceBetween(
        from: GeoPointInterface,
        to: GeoPointInterface,
    ): Double {
        val lat1 = toRadians(from.latitude)
        val lat2 = toRadians(to.latitude)
        val lon1 = toRadians(from.longitude)
        val lon2 = toRadians(to.longitude)

        val longitudeDifference = lon2 - lon1
        val reducedLatitude1 = atan((1 - FLATTENING) * tan(lat1))
        val reducedLatitude2 = atan((1 - FLATTENING) * tan(lat2))
        val sinU1 = sin(reducedLatitude1)
        val cosU1 = cos(reducedLatitude1)
        val sinU2 = sin(reducedLatitude2)
        val cosU2 = cos(reducedLatitude2)

        var lambda = longitudeDifference
        var lambdaP: Double
        var iterLimit = 100
        var cosSqAlpha: Double
        var sinSigma: Double
        var cos2SigmaM: Double
        var cosSigma: Double
        var sigma: Double

        do {
            val sinLambda = sin(lambda)
            val cosLambda = cos(lambda)
            sinSigma =
                sqrt(
                    (cosU2 * sinLambda) * (cosU2 * sinLambda) +
                        (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda) *
                        (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda),
                )

            if (sinSigma == 0.0) return 0.0

            cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda
            sigma = atan2(sinSigma, cosSigma)
            val sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma
            cosSqAlpha = 1 - sinAlpha * sinAlpha
            cos2SigmaM = cosSigma - 2 * sinU1 * sinU2 / cosSqAlpha

            if (cos2SigmaM.isNaN()) cos2SigmaM = 0.0

            val correctionFactor = FLATTENING / 16 * cosSqAlpha * (4 + FLATTENING * (4 - 3 * cosSqAlpha))
            lambdaP = lambda
            lambda = longitudeDifference + (1 - correctionFactor) * FLATTENING * sinAlpha *
                (
                    sigma +
                        correctionFactor * sinSigma *
                        (cos2SigmaM + correctionFactor * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM))
                )
        } while (abs(lambda - lambdaP) > 1e-12 && --iterLimit > 0)

        if (iterLimit == 0) return 0.0

        val uSq =
            cosSqAlpha * (Earth.RADIUS_METERS * Earth.RADIUS_METERS - SEMI_MINOR_AXIS * SEMI_MINOR_AXIS) /
                (SEMI_MINOR_AXIS * SEMI_MINOR_AXIS)
        val ellipsoidFactor = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)))
        val correctionTerm = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)))
        val deltaSigma =
            correctionTerm * sinSigma * (
                cos2SigmaM + correctionTerm / 4 * (
                    cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM) -
                        correctionTerm / 6 * cos2SigmaM * (-3 + 4 * sinSigma * sinSigma) *
                        (-3 + 4 * cos2SigmaM * cos2SigmaM)
                )
            )

        return SEMI_MINOR_AXIS * ellipsoidFactor * (sigma - deltaSigma)
    }

    /**
     * WGS84 楕円体上の方位角計算
     */
    fun computeHeading(
        from: GeoPointInterface,
        to: GeoPointInterface,
    ): Double {
        val lat1 = Math.toRadians(from.latitude)
        val lat2 = Math.toRadians(to.latitude)
        val dLon = Math.toRadians(to.longitude - from.longitude)

        val yComponent = sin(dLon) * cos(lat2)
        val xComponent = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)

        var heading = Math.toDegrees(atan2(yComponent, xComponent))
        while (heading > 180) heading -= 360
        while (heading <= -180) heading += 360

        return heading
    }

    /**
     * WGS84 楕円体上での補間（簡易版）
     * 正確な実装には Vincenty の直接解が必要ですが、
     * 短距離では球面補間で十分な精度が得られます
     */
    fun interpolate(
        from: GeoPointInterface,
        to: GeoPointInterface,
        fraction: Double,
    ): GeoPoint {
        // 球面線形補間（Slerp）を使用
        // WGS84楕円体での正確な補間は複雑なので、まず球面補間で試す

        val lat1 = Math.toRadians(from.latitude)
        val lng1 = Math.toRadians(from.longitude)
        val lat2 = Math.toRadians(to.latitude)
        val lng2 = Math.toRadians(to.longitude)

        // 3D単位ベクトルに変換
        val x1 = cos(lat1) * cos(lng1)
        val y1 = cos(lat1) * sin(lng1)
        val z1 = sin(lat1)

        val x2 = cos(lat2) * cos(lng2)
        val y2 = cos(lat2) * sin(lng2)
        val z2 = sin(lat2)

        // 内積から角度を求める
        val dot = x1 * x2 + y1 * y2 + z1 * z2
        val angle = acos(dot.coerceIn(-1.0, 1.0))

        // 球面線形補間（Slerp）
        val sinAngle = sin(angle)
        val firstWeight = sin((1 - fraction) * angle) / sinAngle
        val secondWeight = sin(fraction * angle) / sinAngle

        val xInterpolated = firstWeight * x1 + secondWeight * x2
        val yInterpolated = firstWeight * y1 + secondWeight * y2
        val zInterpolated = firstWeight * z1 + secondWeight * z2

        // 3Dベクトルから緯度経度に変換
        val lat = asin(zInterpolated)
        val lng = atan2(yInterpolated, xInterpolated)

        val interpolatedAltitude =
            when {
                from.altitude != null && to.altitude != null ->
                    from.altitude!! + fraction * (to.altitude!! - from.altitude!!)
                from.altitude != null -> from.altitude
                to.altitude != null -> to.altitude
                else -> 0.0
            }

        return GeoPoint(
            latitude = Math.toDegrees(lat),
            longitude = Math.toDegrees(lng),
            altitude = interpolatedAltitude!!,
        )
    }

    private fun computeOffset(
        origin: GeoPointInterface,
        distance: Double,
        heading: Double,
    ): GeoPoint {
        // Vincenty の直接解の簡易実装
        // 完全な実装は複雑なので、ここでは近似を使用
        val lat1 = Math.toRadians(origin.latitude)
        val lon1 = Math.toRadians(origin.longitude)
        val alpha1 = Math.toRadians(heading)
        val distanceValue = distance

        val sinAlpha1 = sin(alpha1)
        val cosAlpha1 = cos(alpha1)

        val tanU1 = (1 - FLATTENING) * tan(lat1)
        val cosU1 = 1 / sqrt(1 + tanU1 * tanU1)
        val sinU1 = tanU1 * cosU1
        val sigma1 = atan2(tanU1, cosAlpha1)
        val sinAlpha = cosU1 * sinAlpha1
        val cosSqAlpha = 1 - sinAlpha * sinAlpha
        val uSq =
            cosSqAlpha * (Earth.RADIUS_METERS * Earth.RADIUS_METERS - SEMI_MINOR_AXIS * SEMI_MINOR_AXIS) /
                (SEMI_MINOR_AXIS * SEMI_MINOR_AXIS)
        val ellipsoidFactorOffset = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)))
        val correctionTermOffset = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)))

        var sigma = distanceValue / (SEMI_MINOR_AXIS * ellipsoidFactorOffset)
        var sigmaP: Double
        var cos2SigmaM: Double
        var sinSigma: Double
        var cosSigma: Double

        do {
            cos2SigmaM = cos(2 * sigma1 + sigma)
            sinSigma = sin(sigma)
            cosSigma = cos(sigma)
            val deltaSigma =
                correctionTermOffset * sinSigma * (
                    cos2SigmaM + correctionTermOffset / 4 * (
                        cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM) -
                            correctionTermOffset / 6 * cos2SigmaM * (-3 + 4 * sinSigma * sinSigma) *
                            (-3 + 4 * cos2SigmaM * cos2SigmaM)
                    )
                )
            sigmaP = sigma
            sigma = distanceValue / (SEMI_MINOR_AXIS * ellipsoidFactorOffset) + deltaSigma
        } while (abs(sigma - sigmaP) > 1e-12)

        val tmp = sinU1 * sinSigma - cosU1 * cosSigma * cosAlpha1
        val lat2 =
            atan2(
                sinU1 * cosSigma + cosU1 * sinSigma * cosAlpha1,
                (1 - FLATTENING) * sqrt(sinAlpha * sinAlpha + tmp * tmp),
            )
        val lambda = atan2(sinSigma * sinAlpha1, cosU1 * cosSigma - sinU1 * sinSigma * cosAlpha1)
        val correctionFactorOffset = FLATTENING / 16 * cosSqAlpha * (4 + FLATTENING * (4 - 3 * cosSqAlpha))
        val longitudeDifferenceOffset =
            lambda - (1 - correctionFactorOffset) * FLATTENING * sinAlpha *
                (
                    sigma +
                        correctionFactorOffset * sinSigma *
                        (cos2SigmaM + correctionFactorOffset * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM))
                )
        val lon2 = lon1 + longitudeDifferenceOffset

        return GeoPoint(
            latitude = Math.toDegrees(lat2),
            longitude = Math.toDegrees(lon2),
            altitude = origin.altitude ?: 0.0,
        )
    }
}
