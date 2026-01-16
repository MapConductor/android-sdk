package com.mapconductor.googlemaps.marker

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.DisplayMetrics
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Tile
import com.google.android.gms.maps.model.TileOverlay
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.android.gms.maps.model.TileProvider
import com.mapconductor.core.ResourceProvider
import java.io.ByteArrayOutputStream
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.roundToInt
import kotlin.math.sin

internal class GoogleMapTiledMarkerOverlay(
    map: GoogleMap,
    private val tileSize: Int = TILE_SIZE,
    zIndex: Float = DEFAULT_Z_INDEX,
) : TileProvider {
    private val renderScale: Int = (ResourceProvider.getOptimalTileSize() / tileSize).coerceAtLeast(1)
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
    private var bitmapPxToWorldPx: Double = 1.0

    fun setMarkers(
        markers: Map<String, RenderMarker>,
        zoom: Int,
        tileIndex: Map<Long, List<String>>,
    ) {
        markersById = markers
        indexedZoom = zoom
        tileToMarkerIds = tileIndex
        tileOverlay.clearTileCache()
    }

    fun setMarkerScale(
        bitmapPxToWorldPx: Double,
    ) {
        val next = bitmapPxToWorldPx.coerceAtLeast(1e-6)
        if (kotlin.math.abs(this.bitmapPxToWorldPx - next) < 1e-4) return
        this.bitmapPxToWorldPx = next
        tileOverlay.clearTileCache()
    }

    fun setZoom(zoom: Int) {
        // Deprecated: use setZoom(zoom, tileIndex) so the heavy index build can be done off-main.
        if (zoom == indexedZoom) return
        indexedZoom = zoom
        tileToMarkerIds = emptyMap()
        tileOverlay.clearTileCache()
    }

    fun setZoom(
        zoom: Int,
        tileIndex: Map<Long, List<String>>,
    ) {
        if (zoom == indexedZoom && tileIndex === tileToMarkerIds) return
        indexedZoom = zoom
        tileToMarkerIds = tileIndex
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
        val zoomTileIndex = if (zoom == indexedZoom) tileToMarkerIds else emptyMap()
        val worldTileCount = 1 shl zoom
        if (y !in 0 until worldTileCount) return NO_TILE
        val normalizedX = normalizeTileX(x, worldTileCount)
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
        if (!hasAny) return NO_TILE

        val renderBitmap = Bitmap.createBitmap(renderTileSize, renderTileSize, Bitmap.Config.ARGB_8888)
        renderBitmap.density = DisplayMetrics.DENSITY_DEFAULT
        val canvas = Canvas(renderBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = true
            isDither = true
        }
        val worldPixelSize = worldTileCount.toDouble() * tileSize.toDouble()
        val pxToWorld = bitmapPxToWorldPx
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

        for (dy in -1..1) {
            val ny = y + dy
            if (ny !in 0 until worldTileCount) continue
            for (dx in -1..1) {
                val nx = normalizeTileX(normalizedX + dx, worldTileCount)
                val key = tileKey(nx, ny)
                val ids = zoomTileIndex[key].orEmpty()
                if (ids.isEmpty()) continue

                for (id in ids) {
                    val marker = markersById[id] ?: continue
                    if (!marker.visible) continue
                    if (marker.bitmap.isRecycled) continue
                    val pixel = mercatorPixel(marker.latitude, marker.longitude, zoom, worldPixelSize)
                    val localX = (pixel.x - tileOriginX) * renderScaleDouble
                    val localY = (pixel.y - tileOriginY) * renderScaleDouble

                    val drawWidth = (marker.bitmap.width.toDouble() * pxToWorld).coerceAtLeast(1.0)
                    val drawHeight = (marker.bitmap.height.toDouble() * pxToWorld).coerceAtLeast(1.0)
                    val drawWidthPx = (drawWidth * renderScaleDouble).roundToInt().coerceAtLeast(1)
                    val drawHeightPx = (drawHeight * renderScaleDouble).roundToInt().coerceAtLeast(1)

                    val left = (localX - marker.anchorX * drawWidthPx.toDouble()).toFloat()
                    val top = (localY - marker.anchorY * drawHeightPx.toDouble()).toFloat()
                    val right = left + drawWidthPx.toFloat()
                    val bottom = top + drawHeightPx.toFloat()
                    if (right <= 0f || bottom <= 0f || left >= renderBound || top >= renderBound) continue

                    val scaledBitmap = getScaledBitmap(marker.bitmap, drawWidthPx, drawHeightPx)
                    canvas.drawBitmap(scaledBitmap, left, top, paint)
                }
            }
        }

        val bitmap =
            if (renderScale == 1) {
                renderBitmap
            } else {
                Bitmap.createScaledBitmap(renderBitmap, tileSize, tileSize, true).also {
                    it.density = DisplayMetrics.DENSITY_DEFAULT
                }.also {
                    renderBitmap.recycle()
                }
            }

        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        if (bitmap !== renderBitmap && !bitmap.isRecycled) bitmap.recycle()
        if (!renderBitmap.isRecycled) renderBitmap.recycle()
        scaledBitmaps.values.forEach { it.recycle() }
        return Tile(tileSize, tileSize, output.toByteArray())
    }

    companion object {
        internal fun buildTileIndex(
            markers: Map<String, RenderMarker>,
            zoom: Int,
            tileSize: Int,
        ): Map<Long, List<String>> {
            if (markers.isEmpty()) return emptyMap()
            val worldTileCount = 1 shl zoom
            val worldPixelSize = worldTileCount.toDouble() * tileSize.toDouble()
            val tiles = mutableMapOf<Long, MutableList<String>>()

            markers.values.forEach { marker ->
                if (!marker.visible) return@forEach
                val clampedLatitude = marker.latitude.coerceIn(-85.05112878, 85.05112878)
                val sinLatitude = sin(Math.toRadians(clampedLatitude)).coerceIn(-0.9999, 0.9999)
                val x = (marker.longitude + 180.0) / 360.0
                val y = 0.5 - ln((1.0 + sinLatitude) / (1.0 - sinLatitude)) / (4.0 * Math.PI)
                val pixelX = x * worldPixelSize
                val pixelY = (y * worldPixelSize).coerceIn(0.0, worldPixelSize - 1.0)
                val tileX = floor(pixelX / tileSize.toDouble()).toInt()
                val tileY = floor(pixelY / tileSize.toDouble()).toInt()
                if (tileY !in 0 until worldTileCount) return@forEach
                val normalizedX = ((tileX % worldTileCount) + worldTileCount) % worldTileCount
                val key = (normalizedX.toLong() shl 32) xor (tileY.toLong() and 0xffffffffL)
                tiles.getOrPut(key) { mutableListOf() }.add(marker.id)
            }
            return tiles
        }

        private const val TILE_SIZE = 256
        private const val DEFAULT_Z_INDEX = 0f
        private val NO_TILE: Tile = TileProvider.NO_TILE
    }

    internal data class RenderMarker(
        val id: String,
        val latitude: Double,
        val longitude: Double,
        val visible: Boolean,
        val bitmap: Bitmap,
        val anchorX: Float,
        val anchorY: Float,
    )

    private data class Pixel(val x: Double, val y: Double)

    private fun mercatorPixel(
        latitude: Double,
        longitude: Double,
        zoom: Int,
        worldPixelSize: Double,
    ): Pixel {
        val clampedLatitude = latitude.coerceIn(-85.05112878, 85.05112878)
        val sinLatitude = sin(Math.toRadians(clampedLatitude)).coerceIn(-0.9999, 0.9999)
        val x = (longitude + 180.0) / 360.0
        val y = 0.5 - ln((1.0 + sinLatitude) / (1.0 - sinLatitude)) / (4.0 * Math.PI)

        val pixelX = normalizePixel(x * worldPixelSize, worldPixelSize)
        val pixelY = (y * worldPixelSize).coerceIn(0.0, worldPixelSize - 1.0)
        return Pixel(pixelX, pixelY)
    }

    private fun normalizePixel(
        pixel: Double,
        worldPixelSize: Double,
    ): Double {
        val wrapped = pixel % worldPixelSize
        return if (wrapped < 0.0) wrapped + worldPixelSize else wrapped
    }

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
    ): Long {
        return (x.toLong() shl 32) xor (y.toLong() and 0xffffffffL)
    }

}
