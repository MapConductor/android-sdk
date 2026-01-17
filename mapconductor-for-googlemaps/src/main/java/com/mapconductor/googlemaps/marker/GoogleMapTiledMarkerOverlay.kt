package com.mapconductor.googlemaps.marker

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Tile
import com.google.android.gms.maps.model.TileOverlay
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.android.gms.maps.model.TileProvider
import com.mapconductor.core.ResourceProvider
import java.io.ByteArrayOutputStream
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.floor
import kotlin.math.roundToInt
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.SystemClock
import android.util.DisplayMetrics
import android.util.LruCache

internal class GoogleMapTiledMarkerOverlay(
    map: GoogleMap,
    private val tileSize: Int = TILE_SIZE,
    private val finalTileDownscaleFilter: Boolean = true,
    private val debugTileOverlay: Boolean = false,
    private val renderScaleOverride: Int? = null,
    private val declutterEnabled: Boolean = false,
    private val declutterMaxZoomInt: Int = 7,
    private val declutterMaxMarkersPerTile: Int = 800,
    private val declutterIconPx: Int = 28,
    private val declutterCellPx: Int = 8,
    private val fixedMarkerPixelSize: Boolean = true,
    private val fixedMarkerPixelSizeReferenceZoom: Int = 10,
    zIndex: Float = DEFAULT_Z_INDEX,
) : TileProvider {
    // Google Maps TileOverlay uses the Tile's width/height as the tile coordinate basis.
    // If we return 512px tiles, x/y/z addressing effectively corresponds to a 512px world-tile basis.
    private val renderScale: Int =
        (renderScaleOverride ?: (ResourceProvider.getOptimalTileSize() / tileSize))
            .coerceAtLeast(1)
    private val renderTileSize: Int = tileSize * renderScale

    private val tileOverlay: TileOverlay =
        requireNotNull(
            map.addTileOverlay(
                TileOverlayOptions()
                    .tileProvider(this)
                    .zIndex(zIndex),
            ),
        )

    @Volatile
    private var markersById: Map<String, RenderMarker> = emptyMap()

    @Volatile
    private var indexedZoom: Int = -1

    @Volatile
    private var tileToMarkerIds: Map<Long, List<String>> = emptyMap()

    @Volatile
    private var tileIndexByZoom: Map<Int, Map<Long, List<String>>> = emptyMap()

    @Volatile
    private var bitmapPxToWorldPx: Double = 1.0

    @Volatile
    private var cacheVersion: Int = 0

    // Cache rendered tiles to avoid repeated bitmap+PNG work. Also caches empty tiles.
    // (Inspired by iOS HeatmapTileRenderer's NSCache strategy.)
    private val tileCache: LruCache<Long, ByteArray> =
        object : LruCache<Long, ByteArray>(DEFAULT_TILE_CACHE_BYTES) {
            override fun sizeOf(
                key: Long,
                value: ByteArray,
            ): Int = value.size
        }

    @Volatile
    private var emptyTileBytes: ByteArray = makeEmptyTilePng(tileSize)

    private val tilesRendered = AtomicLong(0L)
    private val tilesCacheHits = AtomicLong(0L)
    private val tilesTotalMs = AtomicLong(0L)
    private val tilesCompressMs = AtomicLong(0L)
    private val tilesCandidates = AtomicLong(0L)
    private val tilesDrawn = AtomicLong(0L)
    private val maxTileMs = AtomicLong(0L)

    fun setMarkers(
        markers: Map<String, RenderMarker>,
        zoom: Int,
        tileIndex: Map<Long, List<String>>,
    ) {
        markersById = markers
        indexedZoom = zoom
        tileToMarkerIds = tileIndex
        tileIndexByZoom = mapOf(zoom to tileIndex)
        cacheVersion = (cacheVersion + 1) and 0x7fffffff
        tileCache.evictAll()
        emptyTileBytes = makeEmptyTilePng(tileSize)
        tileOverlay.clearTileCache()
    }

    fun setMarkersAndTileIndexes(
        markers: Map<String, RenderMarker>,
        indexes: Map<Int, Map<Long, List<String>>>,
        indexedZoom: Int,
    ) {
        markersById = markers
        this.indexedZoom = indexedZoom
        tileIndexByZoom = indexes
        tileToMarkerIds = indexes[indexedZoom].orEmpty()
        cacheVersion = (cacheVersion + 1) and 0x7fffffff
        tileCache.evictAll()
        emptyTileBytes = makeEmptyTilePng(tileSize)
        tileOverlay.clearTileCache()
    }

    fun setTileIndexes(
        indexes: Map<Int, Map<Long, List<String>>>,
        indexedZoom: Int,
    ) {
        this.indexedZoom = indexedZoom
        this.tileIndexByZoom = indexes
        this.tileToMarkerIds = indexes[indexedZoom].orEmpty()
        cacheVersion = (cacheVersion + 1) and 0x7fffffff
        tileCache.evictAll()
        tileOverlay.clearTileCache()
    }

    fun setMarkerScale(bitmapPxToWorldPx: Double) {
        val next = bitmapPxToWorldPx.coerceAtLeast(1e-6)
        if (kotlin.math.abs(this.bitmapPxToWorldPx - next) < 1e-4) return
        this.bitmapPxToWorldPx = next
        cacheVersion = (cacheVersion + 1) and 0x7fffffff
        tileCache.evictAll()
        tileOverlay.clearTileCache()
    }

    fun setZoom(zoom: Int) {
        // Deprecated: use setZoom(zoom, tileIndex) so the heavy index build can be done off-main.
        if (zoom == indexedZoom) return
        indexedZoom = zoom
        tileToMarkerIds = emptyMap()
        tileIndexByZoom = emptyMap()
        cacheVersion = (cacheVersion + 1) and 0x7fffffff
        tileCache.evictAll()
        tileOverlay.clearTileCache()
    }

    fun setZoom(
        zoom: Int,
        tileIndex: Map<Long, List<String>>,
    ) {
        if (zoom == indexedZoom && tileIndex === tileToMarkerIds) return
        indexedZoom = zoom
        tileToMarkerIds = tileIndex
        tileIndexByZoom = mapOf(zoom to tileIndex)
        cacheVersion = (cacheVersion + 1) and 0x7fffffff
        tileCache.evictAll()
        tileOverlay.clearTileCache()
    }

    fun remove() {
        tileOverlay.remove()
    }

    override fun getTile(
        x: Int,
        y: Int,
        zoom: Int,
    ): Tile {
        val tileStart = if (GoogleMapMarkerTilingPerfLog.enabled) SystemClock.elapsedRealtime() else 0L
        val zoomTileIndex = tileIndexByZoom[zoom] ?: if (zoom == indexedZoom) tileToMarkerIds else emptyMap()
        val worldTileCount = 1 shl zoom
        if (y !in 0 until worldTileCount) return NO_TILE
        val normalizedX = normalizeTileX(x, worldTileCount)
        val debugBit = if (debugTileOverlay) (1L shl 47) else 0L
        val versionBits = ((cacheVersion and 0x7f).toLong() shl 40)
        val cacheKey = tileKey(normalizedX, y) xor (zoom.toLong() shl 48) xor debugBit xor versionBits
        tileCache.get(cacheKey)?.let { cached ->
            if (GoogleMapMarkerTilingPerfLog.enabled) {
                tilesCacheHits.incrementAndGet()
            }
            return Tile(tileSize, tileSize, cached)
        }
        var hasAny = false
        for (dy in -1..1) {
            val ny = y + dy
            if (ny !in 0 until worldTileCount) continue
            for (dx in -1..1) {
                val nx = normalizeTileX(normalizedX + dx, worldTileCount)
                val key = tileKey(nx, ny)
                if (!zoomTileIndex[key].isNullOrEmpty()) {
                    hasAny = true
                    break
                }
            }
            if (hasAny) break
        }
        if (!hasAny) {
            // Return a real (transparent) tile to maximize caching and reduce re-requests.
            val bytes =
                if (debugTileOverlay) {
                    renderDebugOnlyTile(
                        tileSize = tileSize,
                        zoom = zoom,
                        x = normalizedX,
                        y = y,
                        candidates = 0,
                        drawn = 0,
                        compressMs = 0,
                        cacheHit = false,
                    )
                } else {
                    emptyTileBytes
                }
            tileCache.put(cacheKey, bytes)
            return Tile(tileSize, tileSize, bytes)
        }

        var candidateCount = 0
        var drawnCount = 0
        val renderBitmap = Bitmap.createBitmap(renderTileSize, renderTileSize, Bitmap.Config.ARGB_8888)
        renderBitmap.density = DisplayMetrics.DENSITY_DEFAULT
        val canvas = Canvas(renderBitmap)
        val paint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                isFilterBitmap = true
                isDither = true
            }
        val worldPixelSize = worldTileCount.toDouble() * tileSize.toDouble()
        // Zoom-dependent scaling (business-logic specific).
        // autoScalable=true: apply zoom-based scaling relative to fixedMarkerPixelSizeReferenceZoom.
        // autoScalable=false: no zoom scaling (keeps the bitmap's screen size constant).
        val pxToWorldFixed = Math.pow(2.0, (zoom - fixedMarkerPixelSizeReferenceZoom).toDouble())
        val pxToWorldScalable = 1.0
        // For debug overlay display
        val zoomScale = pxToWorldFixed
        val tileOriginX = normalizedX.toDouble() * tileSize.toDouble()
        val tileOriginY = y.toDouble() * tileSize.toDouble()
        val renderScaleDouble = renderScale.toDouble()
        val renderBound = renderTileSize.toFloat()

        val scaledBitmaps = HashMap<Int, Bitmap>()

        fun getScaledBitmap(
            bitmap: Bitmap,
            width: Int,
            height: Int,
        ): Bitmap {
            if (bitmap.isRecycled) return bitmap
            if (bitmap.width == width && bitmap.height == height) {
                // No-op scaling. Never cache/recycle the shared source bitmap.
                return bitmap
            }
            val key =
                31 * (31 * System.identityHashCode(bitmap) + width) + height
            scaledBitmaps[key]?.let { cached ->
                if (!cached.isRecycled) return cached
                scaledBitmaps.remove(key)
            }
            val scaled = Bitmap.createScaledBitmap(bitmap, width, height, true)
            // createScaledBitmap may return the source bitmap when no scaling is needed (or on some implementations).
            if (scaled === bitmap) return bitmap
            scaled.density = Bitmap.DENSITY_NONE
            scaledBitmaps[key] = scaled
            return scaled
        }

        val shouldDeclutter =
            declutterEnabled &&
                zoom <= declutterMaxZoomInt &&
                declutterMaxMarkersPerTile > 0 &&
                declutterCellPx > 0 &&
                declutterIconPx > 0
        val occupancy =
            if (shouldDeclutter) {
                TileOccupancy(
                    width = renderTileSize,
                    height = renderTileSize,
                    cellPx = declutterCellPx * renderScale,
                )
            } else {
                null
            }

        for (dy in -1..1) {
            val ny = y + dy
            if (ny !in 0 until worldTileCount) continue
            for (dx in -1..1) {
                val nx = normalizeTileX(normalizedX + dx, worldTileCount)
                val key = tileKey(nx, ny)
                val idsAll = zoomTileIndex[key].orEmpty()
                val ids =
                    if (shouldDeclutter) {
                        // Deterministic ordering to avoid "random disappearance" complaints.
                        // Keeping first N by stable hash gives stable visuals across runs.
                        idsAll
                            .asSequence()
                            .sortedBy { stableHash(it) }
                            .take(declutterMaxMarkersPerTile)
                            .toList()
                    } else {
                        idsAll
                    }
                if (ids.isEmpty()) continue

                for (id in ids) {
                    candidateCount++
                    val marker = markersById[id] ?: continue
                    if (!marker.visible) continue
                    if (marker.bitmap.isRecycled) continue
                    val pixelX = marker.mercatorX * worldPixelSize
                    val pixelY = (marker.mercatorY * worldPixelSize).coerceIn(0.0, worldPixelSize - 1.0)
                    // Handle world-wrap: choose the delta that keeps markers near the current tile.
                    val deltaX0 = pixelX - tileOriginX
                    val deltaX =
                        when {
                            deltaX0 > worldPixelSize / 2.0 -> deltaX0 - worldPixelSize
                            deltaX0 < -worldPixelSize / 2.0 -> deltaX0 + worldPixelSize
                            else -> deltaX0
                        }
                    val localX = deltaX * renderScaleDouble
                    val localY = (pixelY - tileOriginY) * renderScaleDouble

                    // Choose scale based on marker's autoScalable flag:
                    // autoScalable=true: apply zoom-based scaling
                    // autoScalable=false: no zoom scaling (fixed screen size)
                    val markerScale = if (marker.autoScalable) pxToWorldFixed else pxToWorldScalable
                    val drawWidth = (marker.bitmap.width.toDouble() * markerScale).coerceAtLeast(1.0)
                    val drawHeight = (marker.bitmap.height.toDouble() * markerScale).coerceAtLeast(1.0)
                    val drawWidthPx = (drawWidth * renderScaleDouble).roundToInt().coerceAtLeast(1)
                    val drawHeightPx = (drawHeight * renderScaleDouble).roundToInt().coerceAtLeast(1)

                    val left = (localX - marker.anchorX * drawWidthPx.toDouble()).toFloat()
                    val top = (localY - marker.anchorY * drawHeightPx.toDouble()).toFloat()
                    val right = left + drawWidthPx.toFloat()
                    val bottom = top + drawHeightPx.toFloat()
                    if (right <= 0f || bottom <= 0f || left >= renderBound || top >= renderBound) continue

                    if (occupancy != null) {
                        // Approximate overlap culling in tile pixel space.
                        // Use a smaller "virtual occupancy" than the real drawn bitmap size so we don't
                        // over-prune at low zoom where icons are dense.
                        val occW = minOf(drawWidthPx, declutterIconPx * renderScale)
                        val occH = minOf(drawHeightPx, declutterIconPx * renderScale)
                        val occLeft = (localX - marker.anchorX * occW.toDouble()).toFloat()
                        val occTop = (localY - marker.anchorY * occH.toDouble()).toFloat()
                        val occRight = occLeft + occW.toFloat()
                        val occBottom = occTop + occH.toFloat()
                        if (!occupancy.tryOccupy(occLeft, occTop, occRight, occBottom)) continue
                    }

                    val scaledBitmap = getScaledBitmap(marker.bitmap, drawWidthPx, drawHeightPx)
                    canvas.drawBitmap(scaledBitmap, left, top, paint)
                    drawnCount++
                }
            }
        }

        val compressStart = if (GoogleMapMarkerTilingPerfLog.enabled) SystemClock.elapsedRealtime() else 0L
        val bitmap =
            if (renderScale == 1) {
                renderBitmap
            } else {
                Bitmap
                    .createScaledBitmap(renderBitmap, tileSize, tileSize, finalTileDownscaleFilter)
                    .also {
                        it.density = DisplayMetrics.DENSITY_DEFAULT
                    }.also {
                        renderBitmap.recycle()
                    }
            }

        if (debugTileOverlay) {
            drawDebugOverlay(
                bitmap = bitmap,
                tileSize = tileSize,
                zoom = zoom,
                x = normalizedX,
                y = y,
                candidates = candidateCount,
                drawn = drawnCount,
                compressMs = 0,
                cacheHit = false,
                zoomScale = zoomScale,
            )
        }

        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        val compressMs =
            if (GoogleMapMarkerTilingPerfLog.enabled) SystemClock.elapsedRealtime() - compressStart else 0L
        val bytes = output.toByteArray()
        if (bitmap !== renderBitmap && !bitmap.isRecycled) bitmap.recycle()
        if (!renderBitmap.isRecycled) renderBitmap.recycle()
        scaledBitmaps.values.forEach { it.recycle() }
        if (GoogleMapMarkerTilingPerfLog.enabled) {
            val totalMs = SystemClock.elapsedRealtime() - tileStart
            val seed = (normalizedX * 73856093) xor (y * 19349663) xor (zoom * 83492791)
            GoogleMapMarkerTilingPerfLog.logSlow(
                name = "TiledOverlay.getTile",
                elapsedMs = totalMs,
                meta = "z=$zoom x=$normalizedX y=$y candidates=$candidateCount drawn=$drawnCount compressMs=$compressMs tiles=${zoomTileIndex.size}",
                sampleSeed = seed,
            )

            val n = tilesRendered.incrementAndGet()
            tilesTotalMs.addAndGet(totalMs)
            tilesCompressMs.addAndGet(compressMs)
            tilesCandidates.addAndGet(candidateCount.toLong())
            tilesDrawn.addAndGet(drawnCount.toLong())
            while (true) {
                val current = maxTileMs.get()
                if (totalMs <= current) break
                if (maxTileMs.compareAndSet(current, totalMs)) break
            }
            val every = GoogleMapMarkerTilingPerfLog.tileSummaryEvery
            if (every > 0 && (n % every) == 0L) {
                val avgMs = tilesTotalMs.get().toDouble() / n.toDouble()
                val avgCompress = tilesCompressMs.get().toDouble() / n.toDouble()
                val avgCandidates = tilesCandidates.get().toDouble() / n.toDouble()
                val avgDrawn = tilesDrawn.get().toDouble() / n.toDouble()
                val hits = tilesCacheHits.get()
                GoogleMapMarkerTilingPerfLog.logInfo(
                    "TiledOverlay.getTile summary | rendered=$n cacheHits=$hits avgMs=${"%.1f".format(avgMs)} avgCompressMs=${"%.1f".format(avgCompress)} avgCandidates=${"%.1f".format(avgCandidates)} avgDrawn=${"%.1f".format(avgDrawn)} maxMs=${maxTileMs.get()} cacheBytes=${tileCache.size()}",
                )
            }
        }
        tileCache.put(cacheKey, bytes)
        return Tile(tileSize, tileSize, bytes)
    }

    companion object {
        const val DEFAULT_TILE_CACHE_BYTES: Int = 8 * 1024 * 1024

        internal fun buildTileIndex(
            markers: Map<String, RenderMarker>,
            zoom: Int,
            tileSize: Int,
        ): Map<Long, List<String>> {
            if (markers.isEmpty()) return emptyMap()
            val start = if (GoogleMapMarkerTilingPerfLog.enabled) SystemClock.elapsedRealtime() else 0L
            val worldTileCount = 1 shl zoom
            val worldPixelSize = worldTileCount.toDouble() * tileSize.toDouble()
            val tiles = mutableMapOf<Long, MutableList<String>>()

            var visibleCount = 0
            markers.values.forEach { marker ->
                if (!marker.visible) return@forEach
                visibleCount++
                val pixelX = marker.mercatorX * worldPixelSize
                val pixelY = (marker.mercatorY * worldPixelSize).coerceIn(0.0, worldPixelSize - 1.0)
                val tileX = floor(pixelX / tileSize.toDouble()).toInt()
                val tileY = floor(pixelY / tileSize.toDouble()).toInt()
                if (tileY !in 0 until worldTileCount) return@forEach
                val normalizedX = ((tileX % worldTileCount) + worldTileCount) % worldTileCount
                val key = (normalizedX.toLong() shl 32) xor (tileY.toLong() and 0xffffffffL)
                tiles.getOrPut(key) { mutableListOf() }.add(marker.id)
            }
            if (GoogleMapMarkerTilingPerfLog.enabled) {
                val elapsed = SystemClock.elapsedRealtime() - start
                GoogleMapMarkerTilingPerfLog.logSlow(
                    name = "TiledOverlay.buildTileIndex",
                    elapsedMs = elapsed,
                    meta = "markers=${markers.size} visible=$visibleCount tiles=${tiles.size} z=$zoom tileSize=$tileSize",
                )
            }
            return tiles
        }

        private const val TILE_SIZE = 256
        private const val DEFAULT_Z_INDEX = 0f
        private val NO_TILE: Tile = TileProvider.NO_TILE
    }

    internal data class RenderMarker(
        val id: String,
        /**
         * Normalized WebMercator coordinates. Zoom-independent.
         * X is in [0,1) and wraps around the world, Y is in [0,1].
         */
        val mercatorX: Double,
        val mercatorY: Double,
        val visible: Boolean,
        val bitmap: Bitmap,
        val anchorX: Float,
        val anchorY: Float,
        /**
         * When true, the marker is zoom-scaled.
         * When false (default), the marker keeps a consistent screen size.
         */
        val autoScalable: Boolean = false,
    )

    private fun normalizeTileX(
        x: Int,
        worldTileCount: Int,
    ): Int {
        val wrapped = x % worldTileCount
        return if (wrapped < 0) wrapped + worldTileCount else wrapped
    }

    private fun tileKey(
        x: Int,
        y: Int,
    ): Long = (x.toLong() shl 32) xor (y.toLong() and 0xffffffffL)

    private fun makeEmptyTilePng(tileSize: Int): ByteArray {
        val bitmap = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.TRANSPARENT)
        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, output)
        bitmap.recycle()
        return output.toByteArray()
    }

    private fun renderDebugOnlyTile(
        tileSize: Int,
        zoom: Int,
        x: Int,
        y: Int,
        candidates: Int,
        drawn: Int,
        compressMs: Long,
        cacheHit: Boolean,
    ): ByteArray {
        val bitmap = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.TRANSPARENT)
        drawDebugOverlay(
            bitmap = bitmap,
            tileSize = tileSize,
            zoom = zoom,
            x = x,
            y = y,
            candidates = candidates,
            drawn = drawn,
            compressMs = compressMs,
            cacheHit = cacheHit,
            zoomScale = 1.0,
        )
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        bitmap.recycle()
        return out.toByteArray()
    }

    private fun drawDebugOverlay(
        bitmap: Bitmap,
        tileSize: Int,
        zoom: Int,
        x: Int,
        y: Int,
        candidates: Int,
        drawn: Int,
        compressMs: Long,
        cacheHit: Boolean,
        zoomScale: Double,
    ) {
        val canvas = Canvas(bitmap)
        val strokePaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                color = Color.MAGENTA
                strokeWidth = 2f
            }
        canvas.drawRect(0f, 0f, (tileSize - 1).toFloat(), (tileSize - 1).toFloat(), strokePaint)
        canvas.drawLine(0f, 0f, (tileSize - 1).toFloat(), 0f, strokePaint)
        canvas.drawLine(0f, 0f, 0f, (tileSize - 1).toFloat(), strokePaint)

        val textPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.MAGENTA
                textSize = 20f
                typeface = Typeface.MONOSPACE
                style = Paint.Style.FILL
            }
        val bgPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(140, 0, 0, 0)
                style = Paint.Style.FILL
            }

        val line1 = "z=$zoom x=$x y=$y"
        val line2 = "cand=$candidates drawn=$drawn"
        val declutter =
            declutterEnabled && zoom <= declutterMaxZoomInt && declutterMaxMarkersPerTile > 0
        val line3 =
            if (fixedMarkerPixelSize) {
                "cacheHit=$cacheHit fixedPx=true v=${cacheVersion and 0x7f} declutter=$declutter"
            } else {
                "cacheHit=$cacheHit scale=${"%.3f".format(bitmapPxToWorldPx)} zScale=${"%.2f".format(zoomScale)} v=${cacheVersion and 0x7f} declutter=$declutter"
            }
        val line4 = if (compressMs > 0) "compressMs=$compressMs" else "renderScale=$renderScale out=$tileSize"
        val padding = 6f
        val lineHeight = textPaint.fontMetrics.run { (descent - ascent) }
        val boxW =
            maxOf(
                textPaint.measureText(line1),
                textPaint.measureText(line2),
                textPaint.measureText(line3),
                textPaint.measureText(line4),
            ) + padding * 2
        val boxH = lineHeight * 4 + padding * 5
        canvas.drawRect(0f, 0f, boxW, boxH, bgPaint)
        val baseY = padding - textPaint.fontMetrics.ascent
        canvas.drawText(line1, padding, baseY, textPaint)
        canvas.drawText(line2, padding, baseY + (lineHeight + padding) * 1, textPaint)
        canvas.drawText(line3, padding, baseY + (lineHeight + padding) * 2, textPaint)
        canvas.drawText(line4, padding, baseY + (lineHeight + padding) * 3, textPaint)
    }

    private fun sampleIds(
        ids: List<String>,
        maxCount: Int,
        seed: Int,
    ): List<String> {
        if (ids.size <= maxCount) return ids
        val safeMax = maxCount.coerceAtLeast(1)
        val stride = ((ids.size + safeMax - 1) / safeMax).coerceAtLeast(1)
        val offset = kotlin.math.abs(seed) % stride
        val out = ArrayList<String>(safeMax)
        var i = offset
        while (i < ids.size && out.size < safeMax) {
            out.add(ids[i])
            i += stride
        }
        return out
    }

    private fun stableHash(s: String): Int {
        var h = 1125899907
        for (c in s) {
            h = 31 * h + c.code
        }
        return h
    }

    private class TileOccupancy(
        width: Int,
        height: Int,
        cellPx: Int,
    ) {
        private val cell = cellPx.coerceAtLeast(1)
        private val cols = ((width + cell - 1) / cell).coerceAtLeast(1)
        private val rows = ((height + cell - 1) / cell).coerceAtLeast(1)
        private val used = BooleanArray(cols * rows)

        fun tryOccupy(
            left: Float,
            top: Float,
            right: Float,
            bottom: Float,
        ): Boolean {
            val x0 = (left / cell).toInt().coerceIn(0, cols - 1)
            val y0 = (top / cell).toInt().coerceIn(0, rows - 1)
            val x1 = ((right - 1f) / cell).toInt().coerceIn(0, cols - 1)
            val y1 = ((bottom - 1f) / cell).toInt().coerceIn(0, rows - 1)

            for (y in y0..y1) {
                val rowBase = y * cols
                for (x in x0..x1) {
                    if (used[rowBase + x]) return false
                }
            }
            for (y in y0..y1) {
                val rowBase = y * cols
                for (x in x0..x1) {
                    used[rowBase + x] = true
                }
            }
            return true
        }
    }
}
