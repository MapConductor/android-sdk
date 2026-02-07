package com.mapconductor.core.marker

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.tileserver.TileProviderInterface
import com.mapconductor.core.tileserver.TileRequest
import java.io.ByteArrayOutputStream
import java.util.ArrayDeque
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sinh
import kotlin.math.tan
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.LruCache

data class PointD(
    val x: Double,
    val y: Double,
)

/**
 * A tile renderer for markers that implements [TileProviderInterface].
 *
 * This renderer generates PNG tiles containing marker icons at the appropriate
 * positions and sizes for the requested zoom level. It is designed to be used
 * with a local tile server and RasterLayer for SDK-agnostic marker rendering.
 *
 * Features:
 * - Fixed pixel size markers (markers keep consistent screen size across zoom levels)
 * - Decluttering support (limits markers per tile at low zoom levels)
 * - Internal tile caching
 *
 * Thread Safety:
 * - [renderTile] may be called from multiple threads concurrently
 */
class MarkerTileRenderer<ActualMarker>(
    val markerManager: MarkerManager<ActualMarker>,
    val tileSize: Int,
    cacheSizeBytes: Int,
    private val debugTileOverlay: Boolean = false,
    private val iconScaleCallback: ((MarkerState, Int) -> Double)? = null,
) : TileProviderInterface {
    @Volatile
    private var cacheVersion: Int = 0

    private val tileCache: LruCache<Long, ByteArray> =
        object : LruCache<Long, ByteArray>(cacheSizeBytes) {
            override fun sizeOf(
                key: Long,
                value: ByteArray,
            ): Int = value.size
        }

    private val tileCacheLock = Any()

    private val tilesRendered = AtomicLong(0L)
    private val tilesCacheHits = AtomicLong(0L)
    private val stateEpoch = AtomicLong(0L)

    private fun bumpStateEpoch() {
        stateEpoch.incrementAndGet()
    }

    /**
     * Invalidates the internal tile cache.
     */
    fun invalidate() {
        cacheVersion = (cacheVersion + 1) and 0x7fffffff
        synchronized(tileCacheLock) { tileCache.evictAll() }
        bumpStateEpoch()
    }

    /**
     * Clears all cached tiles.
     */
    fun clear() {
        cacheVersion = (cacheVersion + 1) and 0x7fffffff
        synchronized(tileCacheLock) { tileCache.evictAll() }
        bumpStateEpoch()
    }

    private fun tileCacheKey(
        normalizedX: Int,
        y: Int,
        zoom: Int,
        debug: Boolean,
        cacheVersion: Int,
        tileSize: Int,
    ): Long {
        val version7 = (cacheVersion and 0x7f).toLong()
        val debug1 = if (debug) 1L else 0L
        val tileSize11 = (tileSize and 0x7ff).toLong()
        if (zoom in 0..24 && normalizedX in 0 until (1 shl 24) && y in 0 until (1 shl 24)) {
            val base =
                (y.toLong() and 0xFFFFFFL) or
                    ((normalizedX.toLong() and 0xFFFFFFL) shl 24) or
                    ((zoom.toLong() and 0x3fL) shl 48) or
                    (debug1 shl 54) or
                    (tileSize11 shl 55) or
                    (version7 shl 58)
            return base
        }
        var k = (normalizedX.toLong() shl 32) xor (y.toLong() and 0xffffffffL)
        k = k xor (zoom.toLong() shl 16)
        k = k xor (debug1 shl 1)
        k = k xor (tileSize11 shl 2)
        k = k xor (version7 shl 13)
        k *= -0x3d4d51cb3a1b5a75L
        k = java.lang.Long.rotateLeft(k, 27)
        k *= -0x52dce729L
        return mixKey(k)
    }

    private fun mixKey(key: Long): Long {
        var k = key
        k = k xor (k ushr 33)
        k *= -0xae502812aa7333L
        k = k xor (k ushr 33)
        k *= -0x3b314601e57a13adL
        k = k xor (k ushr 33)
        return k
    }

    private val bitmapPoolLock = Any()
    private val bitmapPool = LinkedHashMap<Int, ArrayDeque<Bitmap>>()
    private var bitmapPoolCount = 0
    private val maxPoolBitmaps = 6
    private val maxPoolPerSize = 2

    private val scaledTileSize = ResourceProvider.dpToPx(tileSize.dp)
    private val debugPaint =
        Paint().apply {
            setTextSize(ResourceProvider.dpToPxForBitmap(10f).toFloat())
            setColor(Color.RED)
            setStrokeWidth(ResourceProvider.dpToPxForBitmap(1f).toFloat())
            setFlags(Paint.ANTI_ALIAS_FLAG)
        }

    private val bmpPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = true
            isDither = true
        }

    private val defaultIcon = DefaultMarkerIcon()

    override fun renderTile(request: TileRequest): ByteArray? {
        val zoomInt = request.z
        val worldTileCount = 1 shl zoomInt
        if (request.y !in 0 until worldTileCount) return null
        val normalizedX = normalizeTileX(request.x, worldTileCount)
        val tileYInt = request.y

        val cacheVersionSnapshot = cacheVersion
        val cacheKey =
            tileCacheKey(
                normalizedX = normalizedX,
                y = tileYInt,
                zoom = zoomInt,
                debug = debugTileOverlay,
                cacheVersion = cacheVersionSnapshot,
                tileSize = tileSize,
            )
        synchronized(tileCacheLock) {
            tileCache.get(cacheKey)?.let { cached ->
                tilesCacheHits.incrementAndGet()
                return cached
            }
        }

        val tileX = normalizedX.toDouble()
        val tileY = tileYInt.toDouble()
        val zoom = zoomInt.toDouble()
        val tilePx = scaledTileSize.toDouble().coerceAtLeast(1.0)
        val tilePxInt = tilePx.toInt().coerceAtLeast(1)

        // Tile geographic bounds (NW and SE corners).
        val leftTop = tileToGeoPoint(tileX, tileY, zoom)
        val rightBottom = tileToGeoPoint(tileX + 1.0, tileY + 1.0, zoom)
        val bounds =
            GeoRectBounds().apply {
                extend(leftTop)
                extend(rightBottom)
            }

        data class PreparedMarker(
            val bitmap: Bitmap,
            val centerNorm: PointD,
            val drawW: Float,
            val drawH: Float,
            val anchor: Offset,
        )

        fun prepareMarkers(entities: List<MarkerEntityInterface<ActualMarker>>): Pair<List<PreparedMarker>, Double> {
            var maxHalfExtentPx = 0.0
            val prepared = ArrayList<PreparedMarker>(entities.size)
            for (entity in entities) {
                val stateIcon = entity.state.icon
                val icon = stateIcon?.toBitmapIcon() ?: defaultIcon.toBitmapIcon()
                val pos = entity.state.position
                val tilePoint = geoToTilePoint(pos.longitude, pos.latitude, zoomInt)
                val centerNorm =
                    PointD(
                        x = tilePoint.x - tileX,
                        y = tilePoint.y - tileY,
                    )
                val callbackScale =
                    (iconScaleCallback?.invoke(entity.state, zoomInt) ?: 1.0)
                        .coerceAtLeast(0.0)
                val scale = ((stateIcon?.scale?.toDouble() ?: 1.0) * callbackScale).coerceAtLeast(0.0)
                val drawW = (icon.size.width.toDouble() * scale).coerceAtLeast(1.0)
                val drawH = (icon.size.height.toDouble() * scale).coerceAtLeast(1.0)
                val anchorX = icon.anchor.x.toDouble()
                val anchorY = icon.anchor.y.toDouble()
                val halfExtentX =
                    max(
                        kotlin.math.abs(drawW * anchorX),
                        kotlin.math.abs(drawW * (1.0 - anchorX)),
                    )
                val halfExtentY =
                    max(
                        kotlin.math.abs(drawH * anchorY),
                        kotlin.math.abs(drawH * (1.0 - anchorY)),
                    )
                maxHalfExtentPx = max(maxHalfExtentPx, max(halfExtentX, halfExtentY))
                prepared.add(
                    PreparedMarker(
                        bitmap = icon.bitmap,
                        centerNorm = centerNorm,
                        drawW = drawW.toFloat(),
                        drawH = drawH.toFloat(),
                        anchor = icon.anchor,
                    ),
                )
            }
            return prepared to maxHalfExtentPx
        }

        fun queryByHalfExtentPx(halfExtentPx: Double): List<MarkerEntityInterface<ActualMarker>> {
            val span = bounds.toSpan() ?: GeoPoint(0.0, 0.0)
            val padNorm = (halfExtentPx / tilePx).coerceAtLeast(0.0)
            val latPad = span.latitude * padNorm
            val lonPad = span.longitude * padNorm
            val extended = bounds.expandedByDegrees(latPad, lonPad)
            return markerManager.findMarkersInBounds(extended)
        }

        // First query uses a conservative padding (in dp) so we capture markers slightly outside
        // the tile that can overlap its edges.
        val assumedHalfExtentPx = ResourceProvider.dpToPxForBitmap(32.0) // assume up to 32dp icons
        var entities = queryByHalfExtentPx(assumedHalfExtentPx)

        if (entities.isEmpty()) {
            if (!debugTileOverlay) return null
            val debugBitmap = createBitmap(tilePxInt, tilePxInt)
            Canvas(debugBitmap).also { c ->
                c.drawLine(0f, 0f, tilePxInt.toFloat(), 0f, debugPaint)
                c.drawLine(0f, 0f, 0f, tilePxInt.toFloat(), debugPaint)
                c.drawText("x/y/z=$tileX/$tileY/$zoom, entries=0", 20f, 20f, debugPaint)
            }
            val bytes = bitmapToByteArray(debugBitmap).also { if (!debugBitmap.isRecycled) debugBitmap.recycle() }
            tilesRendered.incrementAndGet()
            if (cacheVersionSnapshot == cacheVersion) {
                synchronized(tileCacheLock) { tileCache.put(cacheKey, bytes) }
            }
            return bytes
        }

        var (prepared, maxHalfExtentPx) = prepareMarkers(entities)
        // If our assumed padding was too small, re-query once with the measured maximum extent.
        if (maxHalfExtentPx > assumedHalfExtentPx + 1.0) {
            entities = queryByHalfExtentPx(maxHalfExtentPx)
            val prepared2 = prepareMarkers(entities)
            prepared = prepared2.first
            maxHalfExtentPx = prepared2.second
        }

        // Instead of allocating a fixed 3x tile, allocate (tile + padding*2) based on icon size.
        val paddingPx =
            kotlin.math
                .ceil(maxHalfExtentPx + 2.0)
                .toInt()
                .coerceAtLeast(2)
        val offscreenSize = tilePxInt + paddingPx * 2
        val offscreenBitmap = acquireBitmap(offscreenSize)
        offscreenBitmap.eraseColor(Color.TRANSPARENT)

        Canvas(offscreenBitmap).also { canvas ->
            if (debugTileOverlay) {
                val o = paddingPx.toFloat()
                canvas.drawLine(o, o, o + tilePxInt.toFloat(), o, debugPaint)
                canvas.drawLine(o, o, o, o + tilePxInt.toFloat(), debugPaint)
                canvas.drawText(
                    "x/y/z=$tileX/$tileY/$zoom, entries=${prepared.size}",
                    o + 20f,
                    o + 20f,
                    debugPaint,
                )
            }

            for (m in prepared) {
                val centerX = (m.centerNorm.x * tilePx) + paddingPx.toDouble()
                val centerY = (m.centerNorm.y * tilePx) + paddingPx.toDouble()
                val anchorX = m.anchor.x.toDouble()
                val anchorY = m.anchor.y.toDouble()
                val dst =
                    RectF(
                        (centerX - m.drawW * anchorX).toFloat(),
                        (centerY - m.drawH * anchorY).toFloat(),
                        (centerX + m.drawW * (1.0 - anchorX)).toFloat(),
                        (centerY + m.drawH * (1.0 - anchorY)).toFloat(),
                    )
                canvas.drawBitmap(m.bitmap, null, dst, bmpPaint)
            }
        }

        val finalBitmap = acquireBitmap(tilePxInt)
        finalBitmap.eraseColor(Color.TRANSPARENT)
        Canvas(finalBitmap).also { canvas ->
            val src = Rect(paddingPx, paddingPx, paddingPx + tilePxInt, paddingPx + tilePxInt)
            val dst = Rect(0, 0, tilePxInt, tilePxInt)
            canvas.drawBitmap(offscreenBitmap, src, dst, bmpPaint)
        }

        val output = bitmapToByteArray(finalBitmap)
        releaseBitmap(offscreenBitmap)
        releaseBitmap(finalBitmap)
        tilesRendered.incrementAndGet()

        // Avoid caching if inputs changed mid-render (best-effort; renderTile can be concurrent).
        if (cacheVersionSnapshot == cacheVersion) {
            synchronized(tileCacheLock) { tileCache.put(cacheKey, output) }
        }
        return output
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    private fun acquireBitmap(size: Int): Bitmap {
        val safeSize = size.coerceAtLeast(1)
        synchronized(bitmapPoolLock) {
            val bucket = bitmapPool[safeSize]
            while (bucket != null && bucket.isNotEmpty()) {
                val candidate = bucket.removeFirst()
                bitmapPoolCount = (bitmapPoolCount - 1).coerceAtLeast(0)
                if (!candidate.isRecycled && candidate.width == safeSize && candidate.height == safeSize) {
                    return candidate
                }
                if (!candidate.isRecycled) {
                    candidate.recycle()
                }
            }
        }
        return createBitmap(safeSize, safeSize)
    }

    private fun releaseBitmap(bitmap: Bitmap) {
        if (bitmap.isRecycled) return
        val size = bitmap.width
        if (bitmap.height != size || size <= 0) {
            bitmap.recycle()
            return
        }
        synchronized(bitmapPoolLock) {
            if (bitmapPoolCount >= maxPoolBitmaps) {
                bitmap.recycle()
                return
            }
            val bucket = bitmapPool.getOrPut(size) { ArrayDeque() }
            if (bucket.size >= maxPoolPerSize) {
                bitmap.recycle()
                return
            }
            bucket.addLast(bitmap)
            bitmapPoolCount += 1
        }
    }

    private fun tileToGeoPoint(
        x: Double,
        y: Double,
        z: Double,
    ): GeoPoint {
        // Slippy map tile (XYZ) -> WGS84 (lat/lng).
        //
        // This returns the NW (top-left) corner of the tile.
        // If you need the center, use (x + 0.5, y + 0.5) instead.
        val n = 2.0.pow(z.toDouble())
        val lonDeg = (x.toDouble() / n) * 360.0 - 180.0
        val latRad = atan(sinh(PI * (1.0 - 2.0 * (y.toDouble() / n))))
        val latDeg = latRad * 180.0 / PI
        return GeoPoint.fromLatLong(latitude = latDeg, longitude = lonDeg)
    }

    private fun geoToTilePoint(
        longitude: Double,
        latitude: Double,
        zoom: Int,
    ): PointD {
        // WGS84 (lat/lng) -> slippy map tile coordinates (XYZ), using Web Mercator.
        //
        // Returns the fractional tile coordinate (x,y) at the given zoom.
        val n = 2.0.pow(zoom.toDouble())

        // Wrap longitude into [-180, 180) then map to [0, n).
        val lonWrapped = ((longitude + 180.0) % 360.0 + 360.0) % 360.0 - 180.0
        val x0 = ((lonWrapped + 180.0) / 360.0) * n
        val x = ((x0 % n) + n) % n

        // Clamp latitude to Web Mercator limits to avoid infinity.
        val latClamped = latitude.coerceIn(-MAX_MERCATOR_LAT, MAX_MERCATOR_LAT)
        val latRad = latClamped * PI / 180.0
        val y =
            (1.0 - ln(tan(latRad) + (1.0 / cos(latRad))) / PI) / 2.0 * n

        // y can be slightly outside due to floating errors; clamp to valid range.
        val yClamped = y.coerceIn(0.0, n - 1e-9)

        return PointD(x = x, y = yClamped)
    }

    private fun normalizeTileX(
        x: Int,
        worldTileCount: Int,
    ): Int {
        val wrapped = x % worldTileCount
        return if (wrapped < 0) wrapped + worldTileCount else wrapped
    }

    private companion object {
        private const val MAX_MERCATOR_LAT = 85.05112878
    }
}
