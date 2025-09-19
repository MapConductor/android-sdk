package com.mapconductor.googlemaps.marker

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import java.util.concurrent.ConcurrentHashMap
import android.graphics.Bitmap

/**
 * Cache wrapper for BitmapDescriptor to avoid recreating instances for identical bitmaps.
 * Uses bitmap hashCode as the cache key for efficient lookup.
 */
object BitmapDescriptorCache {
    private val cache = ConcurrentHashMap<Int, BitmapDescriptor>()

    /**
     * Gets a cached BitmapDescriptor for the given bitmap, or creates and caches a new one.
     *
     * @param bitmap The bitmap to convert to BitmapDescriptor
     * @return Cached or newly created BitmapDescriptor
     */
    fun fromBitmap(bitmap: Bitmap): BitmapDescriptor {
        val key = bitmap.hashCode()
        return cache.getOrPut(key) {
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    /**
     * Clears the entire cache. Use this when memory pressure is detected
     * or when you want to force recreation of all descriptors.
     */
    fun clearCache() {
        cache.clear()
    }

    /**
     * Gets the current cache size for debugging/monitoring purposes.
     */
    fun getCacheSize(): Int = cache.size
}
