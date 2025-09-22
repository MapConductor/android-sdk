package com.mapconductor.core

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.features.normalize
import com.mapconductor.core.spherical.Spherical
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.time.Duration
import android.util.Log
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

fun calculateZIndex(geoPointBase: GeoPoint): Int {
    // 南→北で奥行きを出す
    // 同じ緯度内では西が上（前）に来る
    return (-geoPointBase.latitude * 1_000_000 - geoPointBase.longitude).roundToInt()
}

fun meterToPixel(
    meter: Double,
    latitude: Double,
    zoom: Double,
    tileSize: Double = 256.0, // Google Mapsはデフォルト256pxだが、Mapbox v10+はデフォルト512px
): Double {
    val earthCircumference = 2 * Math.PI * 6378137
    val metersPerPixel = cos(Math.toRadians(latitude)) * earthCircumference / (tileSize * 2.0.pow(zoom))
    return meter / metersPerPixel
}

fun printPoints(
    tag: String,
    points: List<GeoPoint>,
) {
    Log.d(tag, "-----------")
    points.forEach { point ->
        Log.d(tag, GeoPointImpl.from(point).toUrlValue())
    }
}

fun normalize(points: List<GeoPoint>): List<GeoPoint> = points.map { it.normalize() }

fun createInterpolatePoints(points: List<GeoPoint>): List<GeoPoint> {
    val results = mutableListOf<GeoPoint>()
    val fractionStep = 0.01
    results.add(points[0])
    for (i in 1 until points.size) {
        var fraction = fractionStep
        while (fraction <= 1.0) {
            val point =
                Spherical.interpolate(
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

fun createLinearInterpolatePoints(points: List<GeoPoint>): List<GeoPoint> {
    val results = mutableListOf<GeoPoint>()
    val fractionStep = 0.01
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
    points: List<GeoPoint>,
    geodesic: Boolean,
): List<List<GeoPoint>> {
    if (points.isEmpty()) return emptyList()

    val results = mutableListOf<List<GeoPoint>>()
    var fragment = mutableListOf<GeoPoint>()

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
            fragment = mutableListOf<GeoPoint>()

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
    from: GeoPoint,
    to: GeoPoint,
    geodesic: Boolean,
): GeoPointImpl {
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
    from: GeoPoint,
    to: GeoPoint,
): GeoPointImpl {
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

    return GeoPointImpl(
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
    from: GeoPoint,
    to: GeoPoint,
): GeoPointImpl {
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
        val interpolatedPoint = Spherical.interpolate(from, to, mid)
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
    val crossingPoint = Spherical.interpolate(from, to, finalFraction)

    // Ensure the longitude is exactly at the target meridian
    return GeoPointImpl(
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
private fun createOppositeMeridianPoint(point: GeoPoint): GeoPointImpl {
    val oppositeLongitude = if (point.longitude >= 0) -180.0 else 180.0

    return GeoPointImpl(
        latitude = point.latitude,
        longitude = oppositeLongitude,
        altitude = point.altitude ?: 0.0,
    )
}

/**
 * 指定時間の無入力でバーストを確定し、まとめて List で流す。
 * 例: window=300ms の間に来た値を 1 バッチとして emit。
 */
@OptIn(FlowPreview::class)
internal fun <T> Flow<T>.debounceBatch(
    window: Duration,
    maxSize: Int,
): Flow<List<T>> =
    channelFlow {
        require(maxSize > 0) { "maxSize must be > 0" }

        val acc = ArrayList<T>(maxSize)
        val lock = Mutex()

        // イベント発生通知用（タイマ用）ホットストリーム
        val activity = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1)

        suspend fun flushIfNotEmpty() {
            val batch: List<T>? =
                lock.withLock {
                    if (acc.isEmpty()) null else acc.toList().also { acc.clear() }
                }
            if (batch != null) send(batch)
        }

        // 上流の値を取り込み、maxSize 到達なら即フラッシュ
        val collectorJob =
            launch {
                try {
                    this@debounceBatch.collect { v ->
                        var shouldFlushNow = false
                        lock.withLock {
                            acc.add(v)
                            if (acc.size >= maxSize) {
                                shouldFlushNow = true
                            }
                        }
                        if (shouldFlushNow) {
                            // 最大件数に達したので即フラッシュ
                            flushIfNotEmpty()
                        } else {
                            // タイマ更新用の「活動通知」
                            activity.tryEmit(Unit)
                        }
                    }
                } finally {
                    // 上流が完了したら残りを流して終了
                    flushIfNotEmpty()
                }
            }

        // 「静寂境界」：window の間新規通知が来なければフラッシュ
        val timerJob =
            launch {
                activity
                    .debounce(window)
                    .collect { flushIfNotEmpty() }
            }

        // channelFlow は子 Job の完了まで開いている
        collectorJob.join()
        timerJob.cancel()
    }
