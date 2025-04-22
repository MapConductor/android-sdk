package com.mapconductor.core

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.util.LruCache
import androidx.annotation.Keep
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale

data class IconResource(
    val name: String,
    val width: Int,
    val height: Int,
    val anchorX: Int,
    val anchorY: Int,
    internal val resourceId: Int,
)
data class IconResourceWithBitmap(
    val name: String,
    val width: Int,
    val height: Int,
    val anchorX: Int,
    val anchorY: Int,
    val bitmap: Bitmap,
)

object ResourceProvider {
    private lateinit var appContext: Context
    private val density = Resources.getSystem().displayMetrics.density.toDouble()

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val bitmapCache: LruCache<Int, Bitmap> by lazy {
        // Get max memory size by bytes
        val maxMemory = Runtime.getRuntime().maxMemory()
        val cacheSize = maxMemory / 8

        // Cache bytes
        object : LruCache<Int, Bitmap>(cacheSize.toInt()) {
            override fun sizeOf(key: Int, bitmap: Bitmap): Int {
                return bitmap.byteCount / 1024
            }
        }
    }
    val DEFAULT_MARKER = IconResource(
        name = "DEFAULT_MARKER",
        width = 42,
        height = 42,
        anchorX = 24,
        anchorY = 42,
        resourceId = R.drawable.default_marker,
    )

    private val resourceIDs = hashMapOf<String, IconResource>(
        DEFAULT_MARKER.name to DEFAULT_MARKER,
    )

    @Keep
    fun getIconResourceWithBitmap(name: String): IconResourceWithBitmap? {
        synchronized(bitmapCache) {
            val icon = this.resourceIDs.get(name) ?: return null
            val scaledWidth = (icon.width.toDouble() * density).toInt()
            val scaledHeight = (icon.height.toDouble() * density).toInt()
            val scaledAnchorX = (icon.anchorX.toDouble() * density).toInt()
            val scaledAnchorY = (icon.anchorY.toDouble() * density).toInt()

            // If we have the bitmap in cache, return it
            bitmapCache.get(icon.resourceId)?.let {
                return IconResourceWithBitmap(
                    name = icon.name,
                    width = scaledWidth,
                    height = scaledHeight,
                    anchorX = scaledAnchorX,
                    anchorY = scaledAnchorY,
                    bitmap = it,
                )
            }

            val bitmap = getBitmapFromDrawableRes(
                resId = icon.resourceId,
                width = scaledWidth,
                height = scaledHeight,
            ) ?: return null
            // Save into the cache
            bitmapCache.put(icon.resourceId, bitmap)

            return IconResourceWithBitmap(
                name = icon.name,
                width = scaledWidth,
                height = scaledHeight,
                anchorX = scaledAnchorX,
                anchorY = scaledAnchorY,
                bitmap = bitmap,
            )
        }
    }

    private fun getBitmapFromDrawableRes(resId: Int, width: Int, height: Int): Bitmap? {
        val drawable = AppCompatResources.getDrawable(appContext, resId) ?: return null

        return when (drawable) {
            is BitmapDrawable -> {
                drawable.bitmap.scale(width, height)
            }
            else -> {
                val bitmap = createBitmap(width, height)
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                return bitmap
            }
        }
    }

    @Keep
    fun clearCache() {
        bitmapCache.evictAll()
    }
}