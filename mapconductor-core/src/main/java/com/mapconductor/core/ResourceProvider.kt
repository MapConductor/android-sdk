package com.mapconductor.core

import android.content.Context
import android.content.res.Resources
import android.util.LruCache
import androidx.annotation.Keep
import com.mapconductor.core.marker.BitmapIcon
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class IconResource(
    val name: String,
    val width: Double,
    val height: Double,
    val anchorX: Double,
    val anchorY: Double,
    internal val resourceId: Int,
)

object ResourceProvider {
    private val _initialized: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val initialized = _initialized.asStateFlow()

    private lateinit var appContext: Context
    val density = Resources.getSystem().displayMetrics.density.toDouble()

    fun init(context: Context) {
        appContext = context.applicationContext
        _initialized.value = true
    }

    fun toDp(value: Double): Double = value * density

    private val bitmapCache: LruCache<Int, BitmapIcon> by lazy {
        // Get max memory size by bytes
        val maxMemory = Runtime.getRuntime().maxMemory()
        val cacheSize = maxMemory / 8

        // Cache bytes
        object : LruCache<Int, BitmapIcon>(cacheSize.toInt()) {
            override fun sizeOf(key: Int, iconRes: BitmapIcon): Int {
                return iconRes.bitmap.byteCount / 1024
            }
        }
    }
    val DEFAULT_MARKER = IconResource(
        name = "DEFAULT_MARKER",
        width = 42.0,
        height = 42.0,
        anchorX = 24.0,
        anchorY = 42.0,
        resourceId = R.drawable.default_marker,
    )

    private val resourceIDs = hashMapOf<String, IconResource>(
        DEFAULT_MARKER.name to DEFAULT_MARKER,
    )

//    fun getIconResourceWithBitmap(name: String): BitmapIcon? {
//        synchronized(bitmapCache) {
//            val icon = this.resourceIDs.get(name) ?: return null
//            val scaledWidth = toDp(icon.width.toDouble())
//            val scaledHeight = toDp(icon.height.toDouble())
//
//            // If we have the bitmap in cache, return it
//            bitmapCache.get(icon.resourceId)?.let {
//                return it
//            }
//
//            val bitmap = getBitmapFromDrawableRes(
//                resId = icon.resourceId,
//                width = scaledWidth,
//                height = scaledHeight,
//            ) ?: return null
//
//            // Save into the cache
//            val iconRes = BitmapIcon(
//                name = icon.name,
//                width = scaledWidth,
//                height = scaledHeight,
//                anchorX = 0.5,
//                anchorY = 1.0,
//                bitmap = bitmap,
//            )
//            bitmapCache.put(icon.resourceId, iconRes)
//
//            return iconRes
//        }
//    }

    @Keep
    fun clearCache() {
        bitmapCache.evictAll()
    }
}