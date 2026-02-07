package com.mapconductor.googlemaps

import com.google.android.gms.maps.model.LatLng
import com.mapconductor.core.features.GeoPointInterface
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToLong
import android.util.LruCache

internal object AdaptiveInterpolation {
    private const val EARTH_RADIUS_METERS = 6_378_137.0
    private const val EARTH_CIRCUMFERENCE_METERS = 2.0 * Math.PI * EARTH_RADIUS_METERS
    private const val TILE_SIZE_PIXELS = 256.0

    /**
     * Target segment length on screen (pixels).
     *
     * 400px roughly preserves the historical "1000m at zoom16 near equator" behavior:
     * - zoom16 meters/px ≈ 2.39 → 2.39 * 400 ≈ 956m
     *
     * Lower values increase accuracy but cost more; higher values reduce CPU.
     */
    private const val TARGET_SEGMENT_PIXELS = 400.0

    private const val MIN_SEGMENT_LENGTH_METERS = 50.0
    private const val MAX_SEGMENT_LENGTH_METERS = 100_000.0

    fun maxSegmentLengthMeters(
        zoom: Float,
        latitude: Double,
    ): Double {
        val metersPerPixel =
            (EARTH_CIRCUMFERENCE_METERS * cos(Math.toRadians(abs(latitude)))) /
                (TILE_SIZE_PIXELS * 2.0.pow(zoom.toDouble()))
        return (metersPerPixel * TARGET_SEGMENT_PIXELS)
            .coerceIn(MIN_SEGMENT_LENGTH_METERS, MAX_SEGMENT_LENGTH_METERS)
    }

    fun pointsHash(points: List<GeoPointInterface>): Long {
        // 64-bit FNV-1a over quantized lat/lng to avoid floating instability.
        var hash = -0x340d631b7bdddcdbL // FNV offset basis
        for (point in points) {
            val lat = (point.latitude * 1e6).roundToLong()
            val lng = (point.longitude * 1e6).roundToLong()
            hash = (hash xor lat) * 0x100000001b3L
            hash = (hash xor lng) * 0x100000001b3L
        }
        hash = (hash xor points.size.toLong()) * 0x100000001b3L
        return hash
    }

    fun cacheKey(
        pointsHash: Long,
        maxSegmentLengthMeters: Double,
    ): String = "${pointsHash}_${maxSegmentLengthMeters.roundToLong()}"
}

internal class LatLngInterpolationCache(
    maxEntries: Int,
) {
    private val cache = LruCache<String, List<LatLng>>(maxEntries)

    fun get(key: String): List<LatLng>? = cache.get(key)

    fun put(
        key: String,
        value: List<LatLng>,
    ) {
        cache.put(key, value)
    }
}
