package com.mapconductor.core.groundimage

import androidx.core.graphics.drawable.toBitmap
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.tileserver.TileProviderInterface
import com.mapconductor.core.tileserver.TileRequest
import java.io.ByteArrayOutputStream
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.LruCache

class GroundImageTileProvider(
    val tileSize: Int = DEFAULT_TILE_SIZE,
    cacheSizeKb: Int = DEFAULT_CACHE_SIZE_KB,
) : TileProviderInterface {
    private val cacheLock = Any()
    private val emptyTileMarker = ByteArray(1)
    private val cache =
        object : LruCache<String, ByteArray>(cacheSizeKb) {
            override fun sizeOf(
                key: String,
                value: ByteArray,
            ): Int = (value.size / 1024).coerceAtLeast(1)
        }

    @Volatile
    private var overlay: Overlay? = null

    @Volatile
    private var cacheEpoch: Long = 0L

    fun update(
        state: GroundImageState,
        opacity: Float = state.opacity,
    ) {
        val snapshotBounds = GeoRectBounds(state.bounds.southWest, state.bounds.northEast)
        val bitmap = state.image.toBitmap()
        overlay =
            Overlay(
                bounds = snapshotBounds,
                bitmap = bitmap,
                opacity = opacity.coerceIn(0.0f, 1.0f),
            )
        synchronized(cacheLock) {
            cacheEpoch += 1
            cache.evictAll()
        }
    }

    override fun renderTile(request: TileRequest): ByteArray? {
        val epoch = cacheEpoch
        val key = "$epoch:${request.z}/${request.x}/${request.y}"
        synchronized(cacheLock) {
            cache.get(key)?.let { cached ->
                return if (cached === emptyTileMarker) null else cached
            }
        }

        val overlaySnapshot = overlay
        if (overlaySnapshot == null || overlaySnapshot.bounds.isEmpty) {
            synchronized(cacheLock) { cache.put(key, emptyTileMarker) }
            return null
        }

        val bytes = renderTileInternal(overlaySnapshot, request)
        synchronized(cacheLock) { cache.put(key, bytes ?: emptyTileMarker) }
        return bytes
    }

    private fun renderTileInternal(
        overlay: Overlay,
        request: TileRequest,
    ): ByteArray? {
        val sw = overlay.bounds.southWest ?: return null
        val ne = overlay.bounds.northEast ?: return null

        val z = request.z
        val worldSize = tileSize.toDouble() * 2.0.pow(z)

        val north = ne.latitude
        val south = sw.latitude
        var west = sw.longitude
        var east = ne.longitude
        if (west > east) {
            east += 360.0
        }

        val overlayLeft = WebMercator.lonToPixelX(west, worldSize)
        val overlayRight = WebMercator.lonToPixelX(east, worldSize)
        val overlayTop = WebMercator.latToPixelY(north, worldSize)
        val overlayBottom = WebMercator.latToPixelY(south, worldSize)

        val overlayWidth = overlayRight - overlayLeft
        val overlayHeight = overlayBottom - overlayTop
        if (overlayWidth <= 0.0 || overlayHeight <= 0.0) return null

        val tileLeftBase = request.x.toDouble() * tileSize
        val tileRightBase = tileLeftBase + tileSize
        val tileTop = request.y.toDouble() * tileSize
        val tileBottom = tileTop + tileSize

        val tileLeft =
            if (rectsIntersect(overlayLeft, overlayRight, tileLeftBase, tileRightBase)) {
                tileLeftBase
            } else if (rectsIntersect(overlayLeft, overlayRight, tileLeftBase + worldSize, tileRightBase + worldSize)) {
                tileLeftBase + worldSize
            } else {
                return null
            }

        val tileRight = tileLeft + tileSize

        val interLeft = max(overlayLeft, tileLeft)
        val interRight = min(overlayRight, tileRight)
        val interTop = max(overlayTop, tileTop)
        val interBottom = min(overlayBottom, tileBottom)
        if (interLeft >= interRight || interTop >= interBottom) return null

        val bitmap = overlay.bitmap
        val srcLeft =
            floor(((interLeft - overlayLeft) / overlayWidth) * bitmap.width)
                .toInt()
                .coerceIn(0, bitmap.width)
        val srcRight =
            ceil(((interRight - overlayLeft) / overlayWidth) * bitmap.width)
                .toInt()
                .coerceIn(0, bitmap.width)
        val srcTop =
            floor(((interTop - overlayTop) / overlayHeight) * bitmap.height)
                .toInt()
                .coerceIn(0, bitmap.height)
        val srcBottom =
            ceil(((interBottom - overlayTop) / overlayHeight) * bitmap.height)
                .toInt()
                .coerceIn(0, bitmap.height)

        if (srcLeft >= srcRight || srcTop >= srcBottom) return null

        val destLeft = (interLeft - tileLeft).toFloat()
        val destRight = (interRight - tileLeft).toFloat()
        val destTop = (interTop - tileTop).toFloat()
        val destBottom = (interBottom - tileTop).toFloat()

        val out = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val paint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                isFilterBitmap = true
                alpha = (overlay.opacity * 255.0f).toInt().coerceIn(0, 255)
            }
        canvas.drawBitmap(
            bitmap,
            Rect(srcLeft, srcTop, srcRight, srcBottom),
            RectF(destLeft, destTop, destRight, destBottom),
            paint,
        )

        val stream = ByteArrayOutputStream()
        out.compress(Bitmap.CompressFormat.PNG, 100, stream)
        out.recycle()
        return stream.toByteArray()
    }

    private fun rectsIntersect(
        aLeft: Double,
        aRight: Double,
        bLeft: Double,
        bRight: Double,
    ): Boolean = aLeft < bRight && aRight > bLeft

    private data class Overlay(
        val bounds: GeoRectBounds,
        val bitmap: Bitmap,
        val opacity: Float,
    )

    private object WebMercator {
        private const val MAX_LAT = 85.05112878

        fun lonToPixelX(
            lon: Double,
            worldSize: Double,
        ): Double = ((lon + 180.0) / 360.0) * worldSize

        fun latToPixelY(
            lat: Double,
            worldSize: Double,
        ): Double {
            val clamped = lat.coerceIn(-MAX_LAT, MAX_LAT)
            val sinLat = sin(Math.toRadians(clamped))
            val y = 0.5 - ln((1.0 + sinLat) / (1.0 - sinLat)) / (4.0 * Math.PI)
            return y * worldSize
        }
    }

    companion object {
        const val DEFAULT_TILE_SIZE = 512
        private const val DEFAULT_CACHE_SIZE_KB = 8 * 1024
    }
}
