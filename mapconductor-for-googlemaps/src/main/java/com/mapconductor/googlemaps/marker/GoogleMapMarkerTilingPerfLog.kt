package com.mapconductor.googlemaps.marker

import kotlin.math.absoluteValue
import android.os.SystemClock
import android.util.Log

internal object GoogleMapMarkerTilingPerfLog {
    private const val TAG = "MapConductorTiling"

    @Volatile
    var enabled: Boolean = false

    @Volatile
    var slowOpThresholdMs: Long = 50L

    @Volatile
    var logSampleRate: Int = 100

    @Volatile
    var tileSummaryEvery: Long = 0L

    inline fun <T> measure(
        name: String,
        meta: () -> String = { "" },
        block: () -> T,
    ): T {
        if (!enabled) return block()
        val start = SystemClock.elapsedRealtime()
        val result = block()
        val elapsed = SystemClock.elapsedRealtime() - start
        if (elapsed >= slowOpThresholdMs) {
            val suffix = meta().trim().let { if (it.isEmpty()) "" else " | $it" }
            // Use INFO to survive typical `adb logcat *:I` captures.
            Log.i(TAG, "$name took ${elapsed}ms$suffix")
        }
        return result
    }

    fun shouldSample(seed: Int): Boolean {
        val rate = logSampleRate.coerceAtLeast(1)
        return (seed.absoluteValue % rate) == 0
    }

    fun logSlow(
        name: String,
        elapsedMs: Long,
        meta: String = "",
        sampleSeed: Int? = null,
    ) {
        if (!enabled) return
        if (elapsedMs < slowOpThresholdMs) return
        if (sampleSeed != null && !shouldSample(sampleSeed)) return
        val suffix = meta.trim().let { if (it.isEmpty()) "" else " | $it" }
        // Use INFO to survive typical `adb logcat *:I` captures.
        Log.i(TAG, "$name took ${elapsedMs}ms$suffix")
    }

    fun logInfo(message: String) {
        if (!enabled) return
        Log.i(TAG, message)
    }
}
