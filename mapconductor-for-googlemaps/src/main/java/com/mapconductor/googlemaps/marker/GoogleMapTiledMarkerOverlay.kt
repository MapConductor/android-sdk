package com.mapconductor.googlemaps.marker

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
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

    fun setMarkers(
        markers: Map<String, RenderMarker>,
        zoom: Int,
    ) {
        markersById = markers
        rebuildIndex(zoom)
        tileOverlay.clearTileCache()
    }

    fun setZoom(zoom: Int) {
        if (zoom == indexedZoom) return
        rebuildIndex(zoom)
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
        val key = tileKey(normalizedX, y)
        val ids = zoomTileIndex[key].orEmpty()
        if (ids.isEmpty()) return NO_TILE

        val renderBitmap = Bitmap.createBitmap(renderTileSize, renderTileSize, Bitmap.Config.ARGB_8888)
        renderBitmap.density = DisplayMetrics.DENSITY_DEFAULT
        val canvas = Canvas(renderBitmap)
        if (renderScale != 1) {
            // Render at higher resolution and downsample to 256px tiles to reduce aliasing,
            // while keeping the final on-map size identical.
            canvas.scale(renderScale.toFloat(), renderScale.toFloat())
        }
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = true
            isDither = true
        }
        val worldPixelSize = worldTileCount.toDouble() * tileSize.toDouble()

        ids.forEach { id ->
            val marker = markersById[id] ?: return@forEach
            if (!marker.visible) return@forEach
            val pixel = mercatorPixel(marker.latitude, marker.longitude, zoom, worldPixelSize)
            val localX = pixel.x - normalizedX.toDouble() * tileSize.toDouble()
            val localY = pixel.y - y.toDouble() * tileSize.toDouble()

            val left = (localX - marker.anchorX * marker.drawWidth).toFloat()
            val top = (localY - marker.anchorY * marker.drawHeight).toFloat()
            val dst =
                RectF(
                    left,
                    top,
                    (left + marker.drawWidth).toFloat(),
                    (top + marker.drawHeight).toFloat(),
                )
            canvas.drawBitmap(marker.bitmap, null, dst, paint)
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
        if (bitmap != renderBitmap) {
            bitmap.recycle()
        }
        return Tile(tileSize, tileSize, output.toByteArray())
    }

    private fun rebuildIndex(zoom: Int) {
        val markers = markersById
        if (markers.isEmpty()) {
            indexedZoom = zoom
            tileToMarkerIds = emptyMap()
            return
        }

        val worldTileCount = 1 shl zoom
        val worldPixelSize = worldTileCount.toDouble() * tileSize.toDouble()
        val tiles = mutableMapOf<Long, MutableList<String>>()

        markers.values.forEach { marker ->
            if (!marker.visible) return@forEach
            val pixel = mercatorPixel(marker.latitude, marker.longitude, zoom, worldPixelSize)

            val left = pixel.x - marker.anchorX * marker.drawWidth
            val top = pixel.y - marker.anchorY * marker.drawHeight
            val right = left + marker.drawWidth
            val bottom = top + marker.drawHeight

            val minTileX = floor(left / tileSize.toDouble()).toInt()
            val maxTileX = floor((right - 1.0) / tileSize.toDouble()).toInt()
            val minTileY = floor(top / tileSize.toDouble()).toInt()
            val maxTileY = floor((bottom - 1.0) / tileSize.toDouble()).toInt()

            for (tileY in minTileY..maxTileY) {
                if (tileY !in 0 until worldTileCount) continue
                for (tileX in minTileX..maxTileX) {
                    val normalizedX = normalizeTileX(tileX, worldTileCount)
                    val key = tileKey(normalizedX, tileY)
                    tiles.getOrPut(key) { mutableListOf() }.add(marker.id)
                }
            }
        }

        indexedZoom = zoom
        tileToMarkerIds = tiles
    }

    internal data class RenderMarker(
        val id: String,
        val latitude: Double,
        val longitude: Double,
        val visible: Boolean,
        val bitmap: Bitmap,
        val anchorX: Float,
        val anchorY: Float,
        val drawWidth: Double,
        val drawHeight: Double,
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

    companion object {
        private const val TILE_SIZE = 256
        private const val DEFAULT_Z_INDEX = 0f
        private val NO_TILE: Tile = TileProvider.NO_TILE
    }
}
