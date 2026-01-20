package com.mapconductor.core.marker

import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.homography.CalcHomographyMatrixOptions
import com.mapconductor.core.homography.PointD
import com.mapconductor.core.homography.applyMatrix
import com.mapconductor.core.homography.calcHomographyMatrix
import com.mapconductor.core.homography.calcInverseMatrix
import com.mapconductor.core.tileserver.TileProviderInterface
import com.mapconductor.core.tileserver.TileRequest
import java.io.ByteArrayOutputStream
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sinh
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.util.DisplayMetrics
import android.util.Log
import android.util.LruCache

/**
 * A tile renderer for markers that implements [TileProviderInterface].
 *
 * This renderer generates PNG tiles containing marker icons at the appropriate
 * positions and sizes for the requested zoom level. It is designed to be used
 * with a local tile server and RasterLayer for SDK-agnostic marker rendering.
 *
 * Features:
 * - Fixed pixel size markers (markers keep consistent screen size across zoom levels)
 * - Auto-scalable markers (markers scale with zoom level)
 * - Decluttering support (limits markers per tile at low zoom levels)
 * - Internal tile caching
 *
 * Thread Safety:
 * - [renderTile] may be called from multiple threads concurrently
 * - [setMarkers] and [updateCameraZoom] should be called from a single thread (typically Main)
 */
class MarkerTileRenderer<ActualMarker>(
    val markerManager: MarkerManager<ActualMarker>,
    val tileSize: Int = DEFAULT_TILE_SIZE,
    private val worldTileSize: Int = tileSize,
    private val scaleZoomOffset: Double = 0.0,
    private val useCameraZoomCompensation: Boolean = false,
    private val useCameraZoomForScale: Boolean = false,
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
    private val drawRawBitmapInTilePixels: Boolean = false,
    cacheSizeBytes: Int = DEFAULT_TILE_CACHE_BYTES,
) : TileProviderInterface {
    private val worldToOutputScale: Double =
        (tileSize.toDouble() / worldTileSize.toDouble()).coerceAtLeast(1e-6)

    private val renderScale: Int =
        (renderScaleOverride ?: (ResourceProvider.getOptimalTileSize() / tileSize))
            .coerceAtLeast(1)
    private val renderTileSize: Int = tileSize * renderScale

    @Volatile
    private var markersById: Map<String, RenderMarker> = emptyMap()

    @Volatile
    private var indexedZoom: Int = -1

    @Volatile
    private var autoScaleReferenceZoom: Int = -1

    @Volatile
    private var tileToMarkerIds: Map<Long, List<String>> = emptyMap()

    @Volatile
    private var tileIndexByZoom: Map<Int, Map<Long, List<String>>> = emptyMap()

    @Volatile
    private var bitmapPxToWorldPx: Double = 1.0

    @Volatile
    private var markerScaleZoomInt: Int = -1

    @Volatile
    private var cacheVersion: Int = 0

    @Volatile
    private var cameraZoomQuantized: Double? = null

    @Volatile
    private var cameraZoomExact: Double? = null

    @Volatile
    private var cameraZoomKey: Int? = null

    private val tileCache: LruCache<Long, ByteArray> =
        object : LruCache<Long, ByteArray>(cacheSizeBytes) {
            override fun sizeOf(
                key: Long,
                value: ByteArray,
            ): Int = value.size
        }

    @Volatile
    private var emptyTileBytes: ByteArray = makeEmptyTilePng(tileSize)

    private val tilesRendered = AtomicLong(0L)
    private val tilesCacheHits = AtomicLong(0L)
    private val stateEpoch = AtomicLong(0L)

    private fun bumpStateEpoch(reason: String) {
        val epoch = stateEpoch.incrementAndGet()
        if (!debugLoggingEnabled) return
        Log.d(
            TAG,
            "stateUpdate | e=$epoch reason=$reason ixZ=$indexedZoom msZ=$markerScaleZoomInt " +
                "autoRefZ=$autoScaleReferenceZoom bmpPxToWorld=${"%.6f".format(bitmapPxToWorldPx)} " +
                "cacheV=$cacheVersion markers=${markersById.size}",
        )
    }

    private fun effectivePxToWorldForZoom(
        zoom: Int,
        bitmapPxToWorldPx: Double,
        markerScaleZoomInt: Int,
        indexedZoom: Int,
    ): Double {
        if (!fixedMarkerPixelSize) return 1.0
        val baseZoom = if (markerScaleZoomInt >= 0) markerScaleZoomInt else indexedZoom
        if (baseZoom < 0 || zoom == baseZoom) return bitmapPxToWorldPx
        return bitmapPxToWorldPx * 2.0.pow((zoom - baseZoom).toDouble())
    }

    private fun tileCacheKey(
        normalizedX: Int,
        y: Int,
        zoom: Int,
        debug: Boolean,
        cacheVersion: Int,
    ): Long {
        val version7 = (cacheVersion and 0x7f).toLong()
        val debug1 = if (debug) 1L else 0L
        if (zoom in 0..24 && normalizedX in 0 until (1 shl 24) && y in 0 until (1 shl 24)) {
            return (y.toLong() and 0xFFFFFFL) or
                ((normalizedX.toLong() and 0xFFFFFFL) shl 24) or
                ((zoom.toLong() and 0x3fL) shl 48) or
                (debug1 shl 54) or
                (version7 shl 55)
        }
        var k = (normalizedX.toLong() shl 32) xor (y.toLong() and 0xffffffffL)
        k = k xor (zoom.toLong() shl 16)
        k = k xor (debug1 shl 1)
        k = k xor (version7 shl 2)
        k *= -0x3d4d51cb3a1b5a75L
        k = java.lang.Long.rotateLeft(k, 27)
        k *= -0x52dce729L
        return k
    }

    /**
     * Updates the camera zoom for fractional zoom support.
     * Similar to HeatmapTileRenderer, this quantizes the zoom level.
     */
    fun updateCameraZoom(zoom: Double) {
        val nextKey = (zoom * CAMERA_ZOOM_KEY_SCALE).roundToInt()
        val prevKey = cameraZoomKey
        if (prevKey == nextKey && cameraZoomQuantized != null) return
        cameraZoomKey = nextKey
        cameraZoomQuantized = nextKey.toDouble() / CAMERA_ZOOM_KEY_SCALE
        cameraZoomExact = zoom
    }

    /**
     * Sets the markers and tile indexes for rendering.
     *
     * @param markers Map of marker ID to RenderMarker
     * @param indexes Map of zoom level to tile index (tile key -> list of marker IDs)
     * @param indexedZoom The primary zoom level for the indexes
     * @param bitmapPxToWorldPx Scale factor from bitmap pixels to world pixels
     */
    fun setMarkers(
        markers: Map<String, RenderMarker>,
        indexes: Map<Int, Map<Long, List<String>>>,
        indexedZoom: Int,
        bitmapPxToWorldPx: Double,
        autoScaleReferenceZoom: Int = indexedZoom,
    ) {
        val prevIndexes = tileIndexByZoom
        val prevTileToIds = tileToMarkerIds
        val prevIndexedZoom = this.indexedZoom

        markersById = markers
        this.indexedZoom = indexedZoom
        this.autoScaleReferenceZoom = autoScaleReferenceZoom
        tileIndexByZoom = indexes
        tileToMarkerIds = indexes[indexedZoom].orEmpty()
        this.bitmapPxToWorldPx = bitmapPxToWorldPx.coerceAtLeast(1e-6)
        this.markerScaleZoomInt = indexedZoom
        cacheVersion = (cacheVersion + 1) and 0x7fffffff
        tileCache.evictAll()
        emptyTileBytes = makeEmptyTilePng(tileSize)
        bumpStateEpoch("setMarkers")
    }

    /**
     * Updates only the tile indexes without changing markers.
     */
    fun setTileIndexes(
        indexes: Map<Int, Map<Long, List<String>>>,
        indexedZoom: Int,
        autoScaleReferenceZoom: Int = this.autoScaleReferenceZoom,
    ) {
        this.indexedZoom = indexedZoom
        this.autoScaleReferenceZoom = autoScaleReferenceZoom
        this.tileIndexByZoom = indexes
        this.tileToMarkerIds = indexes[indexedZoom].orEmpty()
        cacheVersion = (cacheVersion + 1) and 0x7fffffff
        tileCache.evictAll()
        bumpStateEpoch("setTileIndexes")
    }

    /**
     * Updates the marker scale without changing markers or indexes.
     */
    fun setMarkerScale(
        bitmapPxToWorldPx: Double,
        markerScaleZoomInt: Int,
    ) {
        val next = bitmapPxToWorldPx.coerceAtLeast(1e-6)
        if (abs(this.bitmapPxToWorldPx - next) < 1e-4) return
        this.bitmapPxToWorldPx = next
        this.markerScaleZoomInt = markerScaleZoomInt.coerceAtLeast(0)
        cacheVersion = (cacheVersion + 1) and 0x7fffffff
        tileCache.evictAll()
        bumpStateEpoch("setMarkerScale")
    }

    /**
     * Updates both tile indexes and marker scale.
     */
    fun setTileIndexesAndMarkerScale(
        indexes: Map<Int, Map<Long, List<String>>>,
        indexedZoom: Int,
        bitmapPxToWorldPx: Double,
        autoScaleReferenceZoom: Int = this.autoScaleReferenceZoom,
    ) {
        val prevIndexedZoom = this.indexedZoom
        val prevIndexes = this.tileIndexByZoom
        val prevTileToIds = this.tileToMarkerIds
        val prevMsZ = this.markerScaleZoomInt
        val prevBmp = this.bitmapPxToWorldPx
        val prevKeys = this.tileIndexByZoom.keys.toSet()

        this.indexedZoom = indexedZoom
        this.autoScaleReferenceZoom = autoScaleReferenceZoom
        this.tileIndexByZoom = indexes
        this.tileToMarkerIds = indexes[indexedZoom].orEmpty()
        val nextBmp = bitmapPxToWorldPx.coerceAtLeast(1e-6)
        this.bitmapPxToWorldPx = nextBmp
        this.markerScaleZoomInt = indexedZoom

        val keysToCompare = prevKeys.intersect(indexes.keys)
        val shouldInvalidateScale =
            keysToCompare.any { z ->
                val prevEff = effectivePxToWorldForZoom(z, prevBmp, prevMsZ, prevIndexedZoom)
                val nextEff = effectivePxToWorldForZoom(z, nextBmp, indexedZoom, indexedZoom)
                abs(prevEff - nextEff) > 1e-6
            }
        val shouldInvalidateIndex =
            prevIndexedZoom != indexedZoom || prevIndexes !== indexes || prevTileToIds !== tileToMarkerIds
        if (shouldInvalidateScale || shouldInvalidateIndex) {
            cacheVersion = (cacheVersion + 1) and 0x7fffffff
            tileCache.evictAll()
        }
        bumpStateEpoch("setTileIndexesAndMarkerScale")
    }

    /**
     * Clears all markers and caches.
     */
    fun clear() {
        markersById = emptyMap()
        indexedZoom = -1
        autoScaleReferenceZoom = -1
        tileToMarkerIds = emptyMap()
        tileIndexByZoom = emptyMap()
        bitmapPxToWorldPx = 1.0
        markerScaleZoomInt = -1
        cacheVersion = (cacheVersion + 1) and 0x7fffffff
        tileCache.evictAll()
        bumpStateEpoch("clear")
    }

    private val scaledTileSize = ResourceProvider.dpToPx(tileSize.dp)
    private val debugPaint = Paint().apply {
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

    override fun renderTile(request: TileRequest): ByteArray? {
        val x = request.x.toDouble()
        val y = request.y.toDouble()
        val zoom = request.z.toDouble()

        val leftTop = tileToGeoPoint(x, y, zoom)
        val rightBottom = tileToGeoPoint(x + 1, y + 1, zoom)
        val bounds = GeoRectBounds()
        bounds.extend(leftTop)
        bounds.extend(rightBottom)
        val extended = bounds.expandedByDegrees(0.1, 0.1)

        val entities = markerManager.findMarkersInBounds(extended)
        if (entities.isEmpty()) {
            val copied = createBitmap(scaledTileSize.toInt(), scaledTileSize.toInt())
            Canvas(copied).also {
                val vertex = ResourceProvider.dpToPxForBitmap(tileSize.dp).toFloat()
                it.drawLine(0f, 0f, vertex, 0f, debugPaint)
                it.drawLine(0f, 0f, 0f, vertex, debugPaint)
                it.drawText("x/y/z=${x}/${y}/${zoom}, entries=${entities.size}", 20f, 20f, debugPaint)
            }
            val output = bitmapToByteArray(copied)
            if (!copied.isRecycled) copied.recycle()
            return output
        }

        val left = min(leftTop.longitude, rightBottom.longitude)
        val right = max(leftTop.longitude, rightBottom.longitude)
        // WGS84 latitude: north is larger, south is smaller.
        val top = max(leftTop.latitude, rightBottom.latitude)
        val bottom = min(leftTop.latitude, rightBottom.latitude)

        val homographyMatrix = CalcHomographyMatrixOptions(
            farLeftPx = PointD(
                x = left,
                y = top
            ),
            farRightPx = PointD(
                x = right,
                y = top
            ),
            nearRightPx = PointD(
                x = right,
                y = bottom
            ),
            nearLeftPx = PointD(
                x = left,
                y = bottom
            ),
        )
        val matrix = calcHomographyMatrix(homographyMatrix)
        val invertMatrix = calcInverseMatrix(matrix)


        val extendTileSize = (scaledTileSize * 3).toInt()
        val renderBitmap = createBitmap(extendTileSize, extendTileSize)
        Canvas(renderBitmap).also {
            it.drawLine(scaledTileSize.toFloat(), scaledTileSize.toFloat(), (scaledTileSize * 2).toFloat(), scaledTileSize.toFloat(), debugPaint)
            it.drawLine(scaledTileSize.toFloat(), scaledTileSize.toFloat(), scaledTileSize.toFloat(), (scaledTileSize * 2).toFloat(), debugPaint)
            it.drawText("x/y/z=${x}/${y}/${zoom}, entries=${entities.size}", scaledTileSize.toFloat() + 20f, scaledTileSize.toFloat() + 20f, debugPaint)

            entities.forEach { entity ->
                entity.state.icon?.toBitmapIcon()?.let { bitmapIcon ->
                    val scaledSize = bitmapIcon.size
                    val positionPx = applyMatrix(
                        pos = PointD(
                            x = entity.state.position.longitude,
                            y = entity.state.position.latitude,
                        ),
                        invertMatrix,
                    )
//
                    val bmpLeft = ((positionPx.x * scaledTileSize.toDouble() - (scaledSize.width / 2f)) + scaledTileSize).toFloat()
                    val bmpTop = ((positionPx.y * scaledTileSize.toDouble() - (scaledSize.height / 2f)) + scaledTileSize).toFloat()
//                    println("    position=${(entity.state.position as GeoPoint).toUrlValue()}, (${centerPx.x}, ${centerPx.y})")

                    it.drawBitmap(bitmapIcon.bitmap, bmpLeft, bmpTop, bmpPaint)
                }
            }
        }
        val finalBitmap = createBitmap(scaledTileSize.toInt(), scaledTileSize.toInt())
        Canvas(finalBitmap).also {
            val src = Rect(scaledTileSize.toInt(), scaledTileSize.toInt(), (scaledTileSize * 2).toInt(), (scaledTileSize * 2).toInt())
            val dst = Rect(0, 0, scaledTileSize.toInt(), scaledTileSize.toInt())
            it.drawBitmap(renderBitmap, src, dst, bmpPaint)
        }


        val output = bitmapToByteArray(finalBitmap)
        if (!renderBitmap.isRecycled) renderBitmap.recycle()
        if (!finalBitmap.isRecycled) finalBitmap.recycle()
        return output
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    private fun tileToGeoPoint(x: Double, y: Double, z: Double): GeoPoint {
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

    /*
     * Reference (lat/lng -> tile XYZ):
     *
     * n = 2.0 ** zoom
     * x = int(n * ((lng + 180.0) / 360.0))
     * lat_rad = radians(lat)
     * y = int(n * (1.0 - (log(tan(lat_rad) + 1 / cos(lat_rad)) / PI)) / 2.0)
     */

    fun renderTile2(request: TileRequest): ByteArray? {
        val x = request.x
        val y = request.y
        val zoom = request.z

        var attempt = 0
        while (true) {
            val epoch0 = stateEpoch.get()
            val indexedZoom0 = indexedZoom
            val markerScaleZoomInt0 = markerScaleZoomInt
            val autoScaleReferenceZoom0 = autoScaleReferenceZoom
            val bitmapPxToWorldPx0 = bitmapPxToWorldPx
            val cacheVersion0 = cacheVersion
            val tileIndexByZoom0 = tileIndexByZoom
            val tileToMarkerIds0 = tileToMarkerIds
            val markersById0 = markersById
            val cameraZoomQuantized0 = cameraZoomQuantized
            val cameraZoomExact0 = cameraZoomExact

            val worldTileCount = 1 shl zoom
            if (y !in 0 until worldTileCount) return null
            val normalizedX = normalizeTileX(x, worldTileCount)

            fun maybeRetry(phase: String): Boolean {
                val epochNow = stateEpoch.get()
                if (epochNow == epoch0) return false
                val nowEff = effectivePxToWorldForZoom(zoom, bitmapPxToWorldPx, markerScaleZoomInt, indexedZoom)
                val snapshotEff = effectivePxToWorldForZoom(zoom, bitmapPxToWorldPx0, markerScaleZoomInt0, indexedZoom0)
                val scaleChanged = abs(nowEff - snapshotEff) > 1e-6
                if (!scaleChanged) return false
                if (attempt >= 1) return false
                attempt++
                return true
            }

            val zoomTileIndex =
                tileIndexByZoom0[zoom] ?: if (zoom == indexedZoom0) tileToMarkerIds0 else emptyMap()

            val cacheKey =
                tileCacheKey(
                    normalizedX = normalizedX,
                    y = y,
                    zoom = zoom,
                    debug = debugTileOverlay,
                    cacheVersion = cacheVersion0,
                )
            tileCache.get(cacheKey)?.let { cached ->
                if (maybeRetry("cacheHit")) continue
                tilesCacheHits.incrementAndGet()
                return cached
            }

            var hasAny = false
            val tileKey = tileKey(normalizedX, y)
            val tileIdsAll = zoomTileIndex[tileKey].orEmpty()
            if (tileIdsAll.isNotEmpty()) hasAny = true
            if (!hasAny) {
                val bytes =
                    if (debugTileOverlay) {
                        renderDebugOnlyTile(
                            tileSize = tileSize,
                            zoom = zoom,
                            x = normalizedX,
                            y = y,
                            candidates = 0,
                            drawn = 0,
                        )
                    } else {
                        emptyTileBytes
                    }
                if (maybeRetry("noMarkers")) continue
                tileCache.put(cacheKey, bytes)
                return bytes
            }

            var candidateCount = 0
            var drawnCount = 0
            val tileSize = ResourceProvider.dpToPxForBitmap(renderTileSize.dp).toInt()
            val renderBitmap = createBitmap(tileSize, tileSize)
            val canvas = Canvas(renderBitmap)
            val paint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    isFilterBitmap = true
                    isDither = true
                }
            val worldPixelSize = worldTileCount.toDouble() * worldTileSize.toDouble()
            val referenceZoom =
                when {
                    autoScaleReferenceZoom0 >= 0 -> autoScaleReferenceZoom0
                    indexedZoom0 >= 0 -> indexedZoom0
                    else -> fixedMarkerPixelSizeReferenceZoom
                }
            val zoomForScale =
                if (useCameraZoomForScale) {
                    (cameraZoomExact0 ?: cameraZoomQuantized0 ?: zoom.toDouble()) + scaleZoomOffset
                } else {
                    zoom.toDouble() + scaleZoomOffset
                }
            val lodToCameraScale =
                if (useCameraZoomCompensation && cameraZoomQuantized0 != null) {
                    2.0.pow(zoom.toDouble() - cameraZoomQuantized0)
                } else {
                    1.0
                }
            val pxToWorldFixed =
                minOf(
                    1.0,
                    2.0.pow(zoomForScale - referenceZoom.toDouble()),
                )
            val zoomScale = pxToWorldFixed
            val pxToWorldScalable = 1.0
            val markerScaleZ = if (markerScaleZoomInt0 >= 0) markerScaleZoomInt0 else indexedZoom0
            val zoomToMarkerScale =
                if (fixedMarkerPixelSize && markerScaleZ >= 0 && zoomForScale != markerScaleZ.toDouble()) {
                    2.0.pow(zoomForScale - markerScaleZ.toDouble())
                } else {
                    1.0
                }
            val tileOriginX = normalizedX.toDouble() * worldTileSize.toDouble()
            val tileOriginY = y.toDouble() * worldTileSize.toDouble()
            val renderScaleDouble = renderScale.toDouble()
            val coordScaleDouble = renderScaleDouble * worldToOutputScale
            val renderBound = renderTileSize.toFloat()

            val scaledBitmaps = HashMap<Int, Bitmap>()

            fun getScaledBitmap(
                bitmap: Bitmap,
                width: Int,
                height: Int,
            ): Bitmap {
                if (bitmap.isRecycled) return bitmap
                if (bitmap.width == width && bitmap.height == height) {
                    return bitmap
                }
                val key =
                    31 * (31 * System.identityHashCode(bitmap) + width) + height
                scaledBitmaps[key]?.let { cached ->
                    if (!cached.isRecycled) return cached
                    scaledBitmaps.remove(key)
                }
                val scaled = Bitmap.createScaledBitmap(bitmap, width, height, true)
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

            val ids =
                if (shouldDeclutter) {
                    tileIdsAll
                        .asSequence()
                        .sortedBy { stableHash(it) }
                        .take(declutterMaxMarkersPerTile)
                        .toList()
                } else {
                    tileIdsAll
                }
            for (id in ids) {
                candidateCount++
                val marker = markersById0[id] ?: continue
                if (!marker.visible) continue
                if (marker.bitmap.isRecycled) continue
                val pixelX = marker.mercatorX * worldPixelSize
                val pixelY = (marker.mercatorY * worldPixelSize).coerceIn(0.0, worldPixelSize - 1.0)
                val deltaX0 = pixelX - tileOriginX
                val deltaX =
                    when {
                        deltaX0 > worldPixelSize / 2.0 -> deltaX0 - worldPixelSize
                        deltaX0 < -worldPixelSize / 2.0 -> deltaX0 + worldPixelSize
                        else -> deltaX0
                    }
                val localX = deltaX * coordScaleDouble
                val localY = (pixelY - tileOriginY) * coordScaleDouble

                val pxToWorld =
                    if (drawRawBitmapInTilePixels) {
                        1.0
                    } else {
                        val pxToWorldBase =
                            if (fixedMarkerPixelSize) {
                                min(bitmapPxToWorldPx0 * zoomToMarkerScale, bitmapPxToWorldPx0)
                            } else {
                                1.0
                            }
                        val pxToWorldRaw =
                            pxToWorldBase *
                                if (marker.autoScalable) {
                                    zoomScale * lodToCameraScale
                                } else {
                                    pxToWorldScalable * lodToCameraScale
                                }
                        // Cap at maxPxToWorld to prevent markers from exceeding their intended size
                        // Adjust for compositor scaling so final screen size is capped at maxPxToWorld
                        val effectiveMaxPxToWorld = marker.maxPxToWorld.toDouble() * lodToCameraScale
                        minOf(pxToWorldRaw, effectiveMaxPxToWorld)
                    }
                val drawWidth = ResourceProvider.dpToPxForBitmap(marker.bitmap.width.toDouble() * pxToWorld).coerceAtLeast(1.0)
                val drawHeight = ResourceProvider.dpToPxForBitmap(marker.bitmap.height.toDouble() * pxToWorld).coerceAtLeast(1.0)
                val drawWidthPx = (drawWidth * coordScaleDouble).roundToInt().coerceAtLeast(1)
                val drawHeightPx = (drawHeight * coordScaleDouble).roundToInt().coerceAtLeast(1)

                val left = (localX - marker.anchorX * drawWidthPx.toDouble()).toFloat()
                val top = (localY - marker.anchorY * drawHeightPx.toDouble()).toFloat()
                val right = left + drawWidthPx.toFloat()
                val bottom = top + drawHeightPx.toFloat()
                if (right <= 0f || bottom <= 0f || left >= renderBound || top >= renderBound) continue

                if (occupancy != null) {
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

            if (!debugTileOverlay && drawnCount == 0) {
                if (!renderBitmap.isRecycled) renderBitmap.recycle()
                scaledBitmaps.values.forEach { it.recycle() }
                if (maybeRetry("drawn=0")) continue
                tileCache.put(cacheKey, emptyTileBytes)
                return emptyTileBytes
            }

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
                    zoomScale = zoomScale,
                )
            }

            val output = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            val bytes = output.toByteArray()
            if (bitmap !== renderBitmap && !bitmap.isRecycled) bitmap.recycle()
            if (!renderBitmap.isRecycled) renderBitmap.recycle()
            scaledBitmaps.values.forEach { it.recycle() }

            if (maybeRetry("beforeCachePut")) continue

            tilesRendered.incrementAndGet()
            tileCache.put(cacheKey, bytes)
            return bytes
        }
    }

    companion object {
        const val DEFAULT_TILE_SIZE = 256
        const val DEFAULT_TILE_CACHE_BYTES: Int = 8 * 1024 * 1024
        private const val CAMERA_ZOOM_KEY_SCALE = 4
        private const val TAG = "MarkerTileRenderer"

        @Volatile
        var debugLoggingEnabled: Boolean = false

        /**
         * Builds a tile index mapping tile keys to marker IDs for a given zoom level.
         *
         * @param markers Map of marker ID to RenderMarker
         * @param zoom The zoom level to build the index for
         * @param tileSize The tile size in pixels
         * @param bitmapPxToWorldPx Scale factor from bitmap pixels to world pixels
         * @param markerScaleZoomInt The zoom level at which bitmapPxToWorldPx was computed
         * @param autoScaleReferenceZoom Reference zoom for auto-scalable markers
         * @param fixedMarkerPixelSize Whether markers keep fixed pixel size
         * @param fixedMarkerPixelSizeReferenceZoom Reference zoom for fixed pixel size
         * @return Map of tile key to list of marker IDs in that tile
         */
        fun buildTileIndex(
            markers: Map<String, RenderMarker>,
            zoom: Int,
            tileSize: Int,
            bitmapPxToWorldPx: Double,
            markerScaleZoomInt: Int,
            autoScaleReferenceZoom: Int,
            fixedMarkerPixelSize: Boolean,
            fixedMarkerPixelSizeReferenceZoom: Int,
            scaleZoomOffset: Double = 0.0,
            scaleZoomOverride: Double? = null,
            drawRawBitmapInTilePixels: Boolean = false,
        ): Map<Long, List<String>> {
            if (markers.isEmpty()) return emptyMap()
            val worldTileCount = 1 shl zoom
            val worldPixelSize = worldTileCount.toDouble() * tileSize.toDouble()
            val tiles = mutableMapOf<Long, MutableList<String>>()

            markers.values.forEach { marker ->
                if (!marker.visible) return@forEach
                val pixelX = marker.mercatorX * worldPixelSize
                val pixelY = (marker.mercatorY * worldPixelSize).coerceIn(0.0, worldPixelSize - 1.0)
                val msZ = markerScaleZoomInt.coerceAtLeast(-1)
                val refZ =
                    when {
                        autoScaleReferenceZoom >= 0 -> autoScaleReferenceZoom
                        msZ >= 0 -> msZ
                        else -> fixedMarkerPixelSizeReferenceZoom
                    }
                val zoomForScale = (scaleZoomOverride ?: zoom.toDouble()) + scaleZoomOffset
                val zoomScale =
                    minOf(
                        1.0,
                        2.0.pow(zoomForScale - refZ.toDouble()),
                    )
                val baseZoom = if (msZ >= 0) msZ else zoom
                val zoomToMarkerScale =
                    if (fixedMarkerPixelSize && baseZoom >= 0 && zoomForScale != baseZoom.toDouble()) {
                        2.0.pow(zoomForScale - baseZoom.toDouble())
                    } else {
                        1.0
                    }
                val pxToWorld =
                    if (drawRawBitmapInTilePixels) {
                        1.0
                    } else {
                        val pxToWorldBase =
                            if (fixedMarkerPixelSize) {
                                min(bitmapPxToWorldPx * zoomToMarkerScale, bitmapPxToWorldPx)
                            } else {
                                1.0
                            }
                        val pxToWorldRaw = pxToWorldBase * if (marker.autoScalable) zoomScale else 1.0
                        // Cap at maxPxToWorld to prevent markers from exceeding their intended size
                        minOf(pxToWorldRaw, marker.maxPxToWorld.toDouble())
                    }

                val drawW = (marker.bitmap.width.toDouble() * pxToWorld).coerceAtLeast(1.0)
                val drawH = (marker.bitmap.height.toDouble() * pxToWorld).coerceAtLeast(1.0)
                val leftExtent = marker.anchorX.toDouble() * drawW + 1.0
                val rightExtent = (1.0 - marker.anchorX.toDouble()) * drawW + 1.0
                val topExtent = marker.anchorY.toDouble() * drawH + 1.0
                val bottomExtent = (1.0 - marker.anchorY.toDouble()) * drawH + 1.0

                val tileX0 = floor((pixelX - leftExtent) / tileSize.toDouble()).toInt()
                val tileX1 = floor((pixelX + rightExtent) / tileSize.toDouble()).toInt()
                val tileY0 = floor((pixelY - topExtent) / tileSize.toDouble()).toInt()
                val tileY1 = floor((pixelY + bottomExtent) / tileSize.toDouble()).toInt()

                val y0 = tileY0.coerceIn(0, worldTileCount - 1)
                val y1 = tileY1.coerceIn(0, worldTileCount - 1)
                if (y0 > y1) return@forEach
                for (ty in y0..y1) {
                    for (tx in tileX0..tileX1) {
                        val nx = ((tx % worldTileCount) + worldTileCount) % worldTileCount
                        val key = (nx.toLong() shl 32) xor (ty.toLong() and 0xffffffffL)
                        tiles.getOrPut(key) { mutableListOf() }.add(marker.id)
                    }
                }
            }
            return tiles
        }
    }

    /**
     * Marker data for tile rendering.
     */
    data class RenderMarker(
        val id: String,
        /**
         * Normalized WebMercator X coordinate in [0,1).
         */
        val mercatorX: Double,
        /**
         * Normalized WebMercator Y coordinate in [0,1].
         */
        val mercatorY: Double,
        val visible: Boolean,
        val bitmap: Bitmap,
        val anchorX: Float,
        val anchorY: Float,
        /**
         * When true, the marker scales with zoom level.
         * When false (default), the marker keeps a consistent screen size.
         */
        val autoScalable: Boolean = false,
        /**
         * Maximum pxToWorld scale factor. Used to cap marker size when zooming in.
         * Typically set to icon.scale to prevent markers from exceeding their intended size.
         * Default is 1.0 (no upscaling beyond original bitmap size).
         */
        val maxPxToWorld: Float = 1.0f,
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
                "fixedPx=true v=${cacheVersion and 0x7f} declutter=$declutter"
            } else {
                "scale=${"%.3f".format(bitmapPxToWorldPx)} zScale=${"%.2f".format(zoomScale)} v=${cacheVersion and 0x7f} declutter=$declutter"
            }
        val line4 = "renderScale=$renderScale out=$tileSize"
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
