package com.mapconductor.core

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.features.normalize
import com.mapconductor.core.projection.Earth
import com.mapconductor.core.spherical.GeographicLibCalculator
import com.mapconductor.core.spherical.Spherical
import net.sf.geographiclib.Geodesic
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

fun calculateZIndex(geoPointBase: GeoPointInterface): Int {
    // 南→北で奥行きを出す
    // 同じ緯度内では西が上（前）に来る
    return (-geoPointBase.latitude * 1_000_000 - geoPointBase.longitude).roundToInt()
}

fun calculateMetersPerPixel(
    latitude: Double,
    zoom: Double,
    tileSize: Double = 256.0,
): Double {
    // Web Mercator projection formula for meters per pixel
    // Based on the standard: 1 pixel = 78271.484 meters at zoom 0 at the equator

    // At zoom level 0, the entire world (earthCircumferenceMeters meters) fits in tileSize pixels
    val metersPerPixelAtEquator = Earth.CIRCUMFERENCE_METERS / tileSize

    // Adjust for zoom level (each zoom level halves the meters per pixel)
    val metersPerPixelAtZoom = metersPerPixelAtEquator / 2.0.pow(zoom)

    // Adjust for latitude (Mercator projection stretches at higher latitudes)
    val latitudeRadians = Math.toRadians(abs(latitude))
    val latitudeAdjustment = cos(latitudeRadians)

    return (metersPerPixelAtZoom * latitudeAdjustment)
}

fun meterToPixel(
    meter: Double,
    latitude: Double,
    zoom: Double,
    tileSize: Double = 256.0, // Google Mapsはデフォルト256pxだが、Mapbox v10+はデフォルト512px
): Double {
    val earthCircumference = 2 * Math.PI * Earth.RADIUS_METERS
    val metersPerPixel = cos(Math.toRadians(latitude)) * earthCircumference / (tileSize * 2.0.pow(zoom))
    return meter / metersPerPixel
}

fun normalize(points: List<GeoPointInterface>): List<GeoPointInterface> = points.map { it.normalize() }

fun pointOnGeodesicSegmentOrNull(
    from: GeoPointInterface,
    to: GeoPointInterface,
    position: GeoPointInterface,
    thresholdMeters: Double,
): Pair<GeoPointInterface, Double>? {
    val line =
        Geodesic.WGS84.InverseLine(
            from.latitude, from.longitude,
            to.latitude, to.longitude,
        )
    val totalDistance = line.Distance()

    if (totalDistance == 0.0) {
        val distPosFrom =
            Geodesic.WGS84
                .Inverse(
                    from.latitude, from.longitude,
                    position.latitude, position.longitude,
                ).s12
        return if (distPosFrom <= thresholdMeters) {
            Pair(GeoPoint(from.latitude, from.longitude, from.altitude ?: 0.0), distPosFrom)
        } else {
            null
        }
    }

    // 三分探索で最近点を見つける
    var left = 0.0
    var right = 1.0
    val epsilon = 1e-6 // 十分な精度

    while (right - left > epsilon) {
        val m1 = left + (right - left) / 3.0
        val m2 = right - (right - left) / 3.0

        val point1 = line.Position(totalDistance * m1)
        val dist1 =
            Geodesic.WGS84
                .Inverse(
                    point1.lat2, point1.lon2,
                    position.latitude, position.longitude,
                ).s12

        val point2 = line.Position(totalDistance * m2)
        val dist2 =
            Geodesic.WGS84
                .Inverse(
                    point2.lat2, point2.lon2,
                    position.latitude, position.longitude,
                ).s12

        if (dist1 > dist2) {
            left = m1
        } else {
            right = m2
        }
    }

    val bestFraction = (left + right) / 2.0

    // 線分外の判定
    if (bestFraction <= 0.0 || bestFraction >= 1.0) {
        val distFrom =
            Geodesic.WGS84
                .Inverse(
                    from.latitude, from.longitude,
                    position.latitude, position.longitude,
                ).s12
        val distTo =
            Geodesic.WGS84
                .Inverse(
                    to.latitude, to.longitude,
                    position.latitude, position.longitude,
                ).s12

        val actualMin = min(distFrom, distTo)
        if (actualMin > thresholdMeters) return null

        return Pair(
            if (distFrom <= distTo) {
                GeoPoint(from.latitude, from.longitude, from.altitude ?: to.altitude ?: 0.0)
            } else {
                GeoPoint(to.latitude, to.longitude, to.altitude ?: from.altitude ?: 0.0)
            },
            actualMin,
        )
    }

    val closestPoint = line.Position(totalDistance * bestFraction)

    val minDistance =
        Geodesic.WGS84
            .Inverse(
                closestPoint.lat2, closestPoint.lon2,
                position.latitude, position.longitude,
            ).s12

    if (minDistance > thresholdMeters) return null

    val altitude =
        when {
            from.altitude != null && to.altitude != null ->
                from.altitude!! + bestFraction * (to.altitude!! - from.altitude!!)
            from.altitude != null -> from.altitude!!
            to.altitude != null -> to.altitude!!
            else -> 0.0
        }

    val result = GeoPoint(closestPoint.lat2, closestPoint.lon2, altitude)
    return Pair(result, minDistance)
}

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

fun normalizeLng(lng: Double): Double {
    // [-180, 180] に収める
    return (((lng + 180.0) % 360.0 + 360.0) % 360.0) - 180.0
}

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

/**
 * Splits a list of points by the 180°/-180° meridian line and adds interpolated points
 * at the meridian crossings to eliminate gaps.
 *
 * @param points List of IGeoPoint to split
 * @param geodesic If true, uses geodesic (great circle) interpolation; if false, uses linear interpolation
 * @return List of point groups, each representing a continuous segment without meridian crossings
 */
fun splitByMeridian(
    points: List<GeoPointInterface>,
    geodesic: Boolean,
): List<List<GeoPointInterface>> {
    if (points.isEmpty()) return emptyList()

    val results = mutableListOf<List<GeoPointInterface>>()
    var fragment = mutableListOf<GeoPointInterface>()

    for (i in points.indices) {
        val currentPoint = points[i]

        if (fragment.isEmpty()) {
            fragment.add(currentPoint)
            continue
        }

        val previousPoint = fragment.last()
        val prevLng = previousPoint.longitude
        val currLng = currentPoint.longitude

        // Check if meridian crossing occurs
//        val crossesMeridian = (prevLng >= 0 && currLng < 0) || (prevLng < 0 && currLng >= 0)
        // 180°線交差のみを検出（0°線は除外）
        val lngDiff = currLng - prevLng
        val crossesMeridian = abs(lngDiff) > 180.0

        if (!crossesMeridian) {
            // No meridian crossing, add point to current fragment
            fragment.add(currentPoint)
        } else {
            // Meridian crossing detected, add interpolated point at meridian
            val meridianPoint = interpolateAtMeridian(previousPoint, currentPoint, geodesic)
            fragment.add(meridianPoint)

            // Close current fragment and start new one
            results.add(fragment.toList())
            fragment = mutableListOf<GeoPointInterface>()

            // Add the opposite meridian point to start the new fragment
            val oppositeMeridianPoint = createOppositeMeridianPoint(meridianPoint)
            fragment.add(oppositeMeridianPoint)
            fragment.add(currentPoint)
        }
    }

    if (fragment.isNotEmpty()) {
        results.add(fragment.toList())
    }

    return results
}

/**
 * Interpolates a point at the 180°/-180° meridian line between two points.
 *
 * @param from Starting point
 * @param to Ending point
 * @param geodesic If true, uses geodesic (great circle) interpolation; if false, uses linear interpolation
 * @return Point at the meridian crossing
 */
private fun interpolateAtMeridian(
    from: GeoPointInterface,
    to: GeoPointInterface,
    geodesic: Boolean,
): GeoPoint {
    if (geodesic) {
        // Use geodesic interpolation (great circle path)
        return interpolateAtMeridianGeodesic(from, to)
    } else {
        // Use linear interpolation
        return interpolateAtMeridianLinear(from, to)
    }
}

/**
 * Performs linear interpolation to find the meridian crossing point.
 */
private fun interpolateAtMeridianLinear(
    from: GeoPointInterface,
    to: GeoPointInterface,
): GeoPoint {
    val fromLng = from.longitude
    val toLng = to.longitude

    // Determine which meridian to interpolate to (180 or -180)
    val targetMeridian = if (fromLng >= 0) 180.0 else -180.0

    // Calculate the fraction where meridian crossing occurs
    val totalLngDiff = toLng - fromLng
    val meridianDiff = targetMeridian - fromLng
    val fraction = meridianDiff / totalLngDiff

    // Interpolate latitude and altitude at the meridian
    val interpolatedLatitude = from.latitude + fraction * (to.latitude - from.latitude)
    val interpolatedAltitude =
        when {
            from.altitude != null && to.altitude != null ->
                from.altitude!! + fraction * (to.altitude!! - from.altitude!!)
            from.altitude != null -> from.altitude
            to.altitude != null -> to.altitude
            else -> 0.0
        }

    return GeoPoint(
        latitude = interpolatedLatitude,
        longitude = targetMeridian,
        altitude = interpolatedAltitude!!,
    )
}

/**
 * Performs geodesic interpolation to find the meridian crossing point.
 * Uses iterative method to find where the great circle path crosses the meridian.
 */
private fun interpolateAtMeridianGeodesic(
    from: GeoPointInterface,
    to: GeoPointInterface,
): GeoPoint {
    val fromLng = from.longitude

    // Determine target meridian
    val targetMeridian = if (fromLng >= 0) 180.0 else -180.0

    // Use binary search to find the crossing point on the great circle
    var low = 0.0
    var high = 1.0
    val tolerance = 1e-10
    val maxIterations = 50

    var iteration = 0
    while (iteration < maxIterations && (high - low) > tolerance) {
        val mid = (low + high) / 2.0
        val interpolatedPoint = Spherical.sphericalInterpolate(from, to, mid)
        val interpolatedLng = interpolatedPoint.longitude

        // Normalize longitude to handle crossing
        val normalizedLng =
            when {
                interpolatedLng > 180 -> interpolatedLng - 360
                interpolatedLng <= -180 -> interpolatedLng + 360
                else -> interpolatedLng
            }

        // Check which side of the target meridian we're on
        val onTargetSide =
            if (targetMeridian > 0) {
                // Looking for 180°
                normalizedLng >= 0
            } else {
                // Looking for -180°
                normalizedLng < 0
            }

        val fromOnTargetSide =
            if (targetMeridian > 0) {
                fromLng >= 0
            } else {
                fromLng < 0
            }

        if (onTargetSide == fromOnTargetSide) {
            low = mid
        } else {
            high = mid
        }

        iteration++
    }

    // Final interpolation at the crossing point
    val finalFraction = (low + high) / 2.0
    val crossingPoint = Spherical.sphericalInterpolate(from, to, finalFraction)

    // Ensure the longitude is exactly at the target meridian
    return GeoPoint(
        latitude = crossingPoint.latitude,
        longitude = targetMeridian,
        altitude = crossingPoint.altitude,
    )
}

/**
 * Creates a point at the opposite meridian (180° ↔ -180°) with the same latitude and altitude.
 *
 * @param point Point at one meridian
 * @return Point at the opposite meridian
 */
private fun createOppositeMeridianPoint(point: GeoPointInterface): GeoPoint {
    val oppositeLongitude = if (point.longitude >= 0) -180.0 else 180.0

    return GeoPoint(
        latitude = point.latitude,
        longitude = oppositeLongitude,
        altitude = point.altitude ?: 0.0,
    )
}
