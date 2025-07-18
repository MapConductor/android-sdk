package com.mapconductor.core

import androidx.annotation.Keep
import com.mapconductor.core.marker.BitmapIcon
import android.util.LruCache

object BitmapIconCache {
    private val counts: HashMap<Int, Int> = HashMap()

    private val bitmapCache: LruCache<Int, BitmapIcon> by lazy {
        // Get max memory size by bytes
        val maxMemory = Runtime.getRuntime().maxMemory()
        val cacheSize = maxMemory / 8

        // Cache bytes
        object : LruCache<Int, BitmapIcon>(cacheSize.toInt()) {
            override fun sizeOf(
                key: Int,
                iconRes: BitmapIcon,
            ): Int = iconRes.bitmap.byteCount / 1024
        }
    }

    fun put(
        id: Int,
        bitmapIcon: BitmapIcon,
    ) {
        val refCount = counts.getOrDefault(id, 0)
        counts.put(id, refCount + 1)
        if (refCount == 0) {
            bitmapCache.put(id, bitmapIcon)
        }
    }

    fun refCountUp(id: Int) {
        if (!counts.contains(id)) return
        val refCount = counts.getOrDefault(id, 0)
        counts.put(id, refCount + 1)
    }

    fun get(id: Int): BitmapIcon? {
        if (!counts.contains(id)) return null
        return bitmapCache.get(id)
    }

    fun refCountDown(id: Int) {
        if (!counts.contains(id)) return
        val refCount = counts.getOrDefault(id, 1) - 1
        if (refCount == 0) {
            counts.remove(id)
            bitmapCache.remove(id)
            return
        }
        counts.put(id, refCount)
    }

    @Keep
    fun clear() {
        counts.clear()
        bitmapCache.evictAll()
    }
}
