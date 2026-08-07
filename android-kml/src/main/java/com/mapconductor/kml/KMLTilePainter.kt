package com.mapconductor.kml

import java.nio.ByteBuffer
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Path

/**
 * フィーチャーを `Canvas` へ描き、PNG にする部分。
 *
 * `Bitmap` / `Path` / ピクセルバッファは**スレッドごとに使い回す**。タイル 1 枚ごとに
 * 確保すると、1 画面数十枚では GC を強く叩く。
 *
 * android-geojson-layer の `GeoJSONTilePainter.kt` と同じ描き方
 * （あちらは pixelRatio に対応しているが、KML は等倍のみ。これは元からの差）。
 */
internal class KMLTilePainter(
    private val tileSize: Int,
) {
    /** タイル用の透明なビットマップを用意する。 */
    fun beginTile(): Bitmap {
        val bitmap = getBitmap()
        bitmap.eraseColor(android.graphics.Color.TRANSPARENT)
        return bitmap
    }

    fun drawFeature(
        canvas: Canvas,
        feature: RenderFeature,
        zoom: Int,
        worldSize: Double,
        originX: Double,
        originY: Double,
        tileMinX: Double,
        tileMinY: Double,
        tileMaxX: Double,
        tileMaxY: Double,
    ): Boolean =
        drawGeometry(
            canvas,
            feature,
            feature.worldGeometry,
            zoom,
            worldSize,
            originX,
            originY,
            tileMinX,
            tileMinY,
            tileMaxX,
            tileMaxY,
        )

    private fun drawGeometry(
        canvas: Canvas,
        feature: RenderFeature,
        geometry: WorldGeometry,
        zoom: Int,
        worldSize: Double,
        originX: Double,
        originY: Double,
        tileMinX: Double,
        tileMinY: Double,
        tileMaxX: Double,
        tileMaxY: Double,
    ): Boolean =
        when (geometry) {
            is WorldGeometry.Point -> {
                val px = KMLWorld.toPixel(geometry.wx, worldSize, originX)
                val py = KMLWorld.toPixel(geometry.wy, worldSize, originY)
                canvas.drawCircle(px, py, feature.pointRadius, feature.fillPaint)
                feature.strokePaint?.let { canvas.drawCircle(px, py, feature.pointRadius, it) }
                true
            }

            is WorldGeometry.Points -> {
                val points = geometry.points
                var i = 0
                while (i < points.size) {
                    val px = KMLWorld.toPixel(points[i], worldSize, originX)
                    val py = KMLWorld.toPixel(points[i + 1], worldSize, originY)
                    canvas.drawCircle(px, py, feature.pointRadius, feature.fillPaint)
                    feature.strokePaint?.let { canvas.drawCircle(px, py, feature.pointRadius, it) }
                    i += 2
                }
                points.isNotEmpty()
            }

            is WorldGeometry.Line -> {
                val path =
                    buildLinePath(
                        geometry.rings,
                        zoom,
                        worldSize,
                        originX,
                        originY,
                        tileMinX,
                        tileMinY,
                        tileMaxX,
                        tileMaxY,
                        feature.strokePaint?.strokeWidth ?: feature.fillPaint.strokeWidth,
                    )
                if (!path.isEmpty) {
                    canvas.drawPath(path, feature.strokePaint ?: feature.fillPaint)
                    true
                } else {
                    false
                }
            }

            is WorldGeometry.Polygon -> {
                val path = buildPolygonPath(geometry.rings, zoom, worldSize, originX, originY)
                if (!path.isEmpty) {
                    canvas.drawPath(path, feature.fillPaint)
                    feature.strokePaint?.let { canvas.drawPath(path, it) }
                    true
                } else {
                    false
                }
            }

            is WorldGeometry.Collection -> {
                var drew = false
                for (part in geometry.parts) {
                    if (
                        drawGeometry(
                            canvas,
                            feature,
                            part,
                            zoom,
                            worldSize,
                            originX,
                            originY,
                            tileMinX,
                            tileMinY,
                            tileMaxX,
                            tileMaxY,
                        )
                    ) {
                        drew = true
                    }
                }
                drew
            }

            WorldGeometry.Empty -> false
        }

    private fun buildLinePath(
        rings: List<WorldRing>,
        zoom: Int,
        worldSize: Double,
        originX: Double,
        originY: Double,
        tileMinX: Double,
        tileMinY: Double,
        tileMaxX: Double,
        tileMaxY: Double,
        strokeWidth: Float,
    ): Path {
        val path = getPath()
        path.rewind()
        val margin = ((tileMaxX - tileMinX) * 0.25) + (strokeWidth.toDouble() / worldSize)
        val minX = tileMinX - margin
        val minY = tileMinY - margin
        val maxX = tileMaxX + margin
        val maxY = tileMaxY + margin
        for (ring in rings) {
            val coords = ring.coordsForZoom(zoom, tileSize)
            if (coords.size < 4) continue
            var needsMove = true
            var i = 2
            while (i < coords.size) {
                val ax = coords[i - 2]
                val ay = coords[i - 1]
                val bx = coords[i]
                val by = coords[i + 1]
                if (!KMLWorld.segmentOutside(ax, ay, bx, by, minX, minY, maxX, maxY)) {
                    if (needsMove) {
                        path.moveTo(KMLWorld.toPixel(ax, worldSize, originX), KMLWorld.toPixel(ay, worldSize, originY))
                        needsMove = false
                    }
                    path.lineTo(KMLWorld.toPixel(bx, worldSize, originX), KMLWorld.toPixel(by, worldSize, originY))
                } else {
                    needsMove = true
                }
                i += 2
            }
        }
        return path
    }

    private fun buildPolygonPath(
        rings: List<WorldRing>,
        zoom: Int,
        worldSize: Double,
        originX: Double,
        originY: Double,
    ): Path {
        val path = getPath()
        path.rewind()
        path.fillType = Path.FillType.EVEN_ODD
        for (ring in rings) {
            val coords = ring.coordsForZoom(zoom, tileSize)
            if (coords.size < 6) continue
            path.moveTo(
                KMLWorld.toPixel(coords[0], worldSize, originX),
                KMLWorld.toPixel(coords[1], worldSize, originY),
            )
            var i = 2
            while (i < coords.size) {
                path.lineTo(
                    KMLWorld.toPixel(coords[i], worldSize, originX),
                    KMLWorld.toPixel(coords[i + 1], worldSize, originY),
                )
                i += 2
            }
            path.close()
        }
        return path
    }

    fun toPng(bitmap: Bitmap): ByteArray {
        val byteCount = bitmap.byteCount
        val buffer = getPixelBuffer(byteCount)
        buffer.clear()
        bitmap.copyPixelsToBuffer(buffer)
        // copyPixelsToBuffer yields the bitmap's native ARGB_8888 layout, which is
        // R,G,B,A byte order with premultiplied alpha; PNG requires straight alpha,
        // so un-premultiply translucent pixels.
        val source = buffer.array()
        val rgba = getRgbaBuffer(byteCount)
        var i = 0
        while (i < byteCount) {
            when (val a = source[i + 3].toInt() and 0xff) {
                255 -> {
                    rgba[i] = source[i]
                    rgba[i + 1] = source[i + 1]
                    rgba[i + 2] = source[i + 2]
                    rgba[i + 3] = source[i + 3]
                }
                0 -> {
                    rgba[i] = 0
                    rgba[i + 1] = 0
                    rgba[i + 2] = 0
                    rgba[i + 3] = 0
                }
                else -> {
                    val half = a / 2
                    rgba[i] = ((((source[i].toInt() and 0xff) * 255 + half) / a).coerceAtMost(255)).toByte()
                    rgba[i + 1] = ((((source[i + 1].toInt() and 0xff) * 255 + half) / a).coerceAtMost(255)).toByte()
                    rgba[i + 2] = ((((source[i + 2].toInt() and 0xff) * 255 + half) / a).coerceAtMost(255)).toByte()
                    rgba[i + 3] = source[i + 3]
                }
            }
            i += 4
        }
        return FastPngEncoder.encode(bitmap.width, bitmap.height, rgba)
    }

    private val threadLocalBitmap = ThreadLocal<Bitmap>()
    private val threadLocalPath = ThreadLocal<Path>()
    private val threadLocalPixelBuffer = ThreadLocal<ByteBuffer>()
    private val threadLocalRgba = ThreadLocal<ByteArray>()

    private fun getBitmap(): Bitmap {
        val existing = threadLocalBitmap.get()
        if (existing != null &&
            !existing.isRecycled &&
            existing.width == tileSize &&
            existing.height == tileSize
        ) {
            return existing
        }
        val bm = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888)
        threadLocalBitmap.set(bm)
        return bm
    }

    private fun getPath(): Path {
        val existing = threadLocalPath.get()
        if (existing != null) return existing
        val path = Path()
        threadLocalPath.set(path)
        return path
    }

    private fun getPixelBuffer(byteCount: Int): ByteBuffer {
        val existing = threadLocalPixelBuffer.get()
        if (existing != null && existing.capacity() >= byteCount && existing.hasArray()) {
            return existing
        }
        val buffer = ByteBuffer.allocate(byteCount)
        threadLocalPixelBuffer.set(buffer)
        return buffer
    }

    private fun getRgbaBuffer(byteCount: Int): ByteArray {
        val existing = threadLocalRgba.get()
        if (existing != null && existing.size >= byteCount) return existing
        val buffer = ByteArray(byteCount)
        threadLocalRgba.set(buffer)
        return buffer
    }
}
