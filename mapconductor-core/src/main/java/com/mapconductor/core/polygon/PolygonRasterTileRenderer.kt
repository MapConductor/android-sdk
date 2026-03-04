package com.mapconductor.core.polygon

import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.spherical.createInterpolatePoints
import com.mapconductor.core.spherical.splitByMeridian
import com.mapconductor.core.tileserver.TileProviderInterface
import com.mapconductor.core.tileserver.TileRequest
import java.io.ByteArrayOutputStream
import kotlin.math.PI
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.Log

private const val TAG = "PolygonRasterTile"

class PolygonRasterTileRenderer(
    private val tileSizePx: Int = 256,
) : TileProviderInterface {
    @Volatile
    var points: List<GeoPointInterface> = emptyList()

    @Volatile
    var holes: List<List<GeoPointInterface>> = emptyList()

    @Volatile
    var fillColor: Int = android.graphics.Color.TRANSPARENT

    @Volatile
    var strokeColor: Int = android.graphics.Color.TRANSPARENT

    @Volatile
    var strokeWidthPx: Float = 0f

    @Volatile
    var geodesic: Boolean = false

    @Volatile
    var outerBounds: GeoRectBounds? = null

    private val transparentTileBytes: ByteArray by lazy {
        val bitmap = Bitmap.createBitmap(tileSizePx, tileSizePx, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(android.graphics.Color.TRANSPARENT)
        bitmapToPng(bitmap).also { bitmap.recycle() }
    }

    private var tileRequestCount = 0

    override fun renderTile(request: TileRequest): ByteArray? {
        tileRequestCount++
        if (points.isEmpty()) {
            if (tileRequestCount <= 5) Log.d(TAG, "  -> points empty, returning transparent")
            return transparentTileBytes
        }
        val z = request.z
        val worldTileCount = 1 shl z
        val x = normalizeTileX(request.x, worldTileCount)
        val y = request.y
        if (y !in 0 until worldTileCount) return null

        val tileBounds = getTileBounds(x = x, y = y, z = z)
        val bounds = outerBounds
        if (bounds != null) {
            if (!intersectsBounds(bounds, tileBounds)) {
                if (tileRequestCount <= 5) Log.d(TAG, "  -> tile outside outerBounds, returning transparent")
                return transparentTileBytes
            }
        }

        val bitmap = Bitmap.createBitmap(tileSizePx, tileSizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val path = Path().apply { fillType = Path.FillType.EVEN_ODD }

        val tileOriginX = (x.toDouble() * tileSizePx)
        val tileOriginY = (y.toDouble() * tileSizePx)

        fun addRingToPath(ring: List<GeoPointInterface>) {
            if (ring.size < 3) return
            val fragments = splitByMeridian(ring, geodesic)
            fragments.forEach { fragment ->
                val densified =
                    if (geodesic) {
                        try {
                            createInterpolatePoints(fragment)
                        } catch (_: Exception) {
                            fragment
                        }
                    } else {
                        fragment
                    }
                if (densified.size < 2) return@forEach
                var moved = false
                densified.forEach { p ->
                    val px = (lonToPixelX(p.longitude, z) - tileOriginX).toFloat()
                    val py = (latToPixelY(p.latitude, z) - tileOriginY).toFloat()
                    if (!moved) {
                        path.moveTo(px, py)
                        moved = true
                    } else {
                        path.lineTo(px, py)
                    }
                }
                path.close()
            }
        }

        addRingToPath(points)
        holes.forEach { addRingToPath(it) }

        val fillPaint =
            Paint().apply {
                style = Paint.Style.FILL
                color = fillColor
                isAntiAlias = true
            }
        canvas.drawPath(path, fillPaint)

        if (strokeWidthPx > 0f && android.graphics.Color.alpha(strokeColor) > 0) {
            val strokePaint =
                Paint().apply {
                    style = Paint.Style.STROKE
                    color = strokeColor
                    strokeWidth = strokeWidthPx
                    isAntiAlias = true
                }
            canvas.drawPath(path, strokePaint)
        }

        if (tileRequestCount <= 5) {
            Log.d(TAG, "  -> rendered tile with fillColor=${Integer.toHexString(fillColor)}, pathEmpty=${path.isEmpty}")
        }

        return bitmapToPng(bitmap).also { bitmap.recycle() }
    }

    private fun normalizeTileX(
        x: Int,
        worldTileCount: Int,
    ): Int {
        val wrapped = x % worldTileCount
        return if (wrapped < 0) wrapped + worldTileCount else wrapped
    }

    private fun getTileBounds(
        x: Int,
        y: Int,
        z: Int,
    ): GeoRectBounds {
        val nw = tileToGeoPoint(x.toDouble(), y.toDouble(), z.toDouble())
        val se = tileToGeoPoint(x + 1.0, y + 1.0, z.toDouble())
        return GeoRectBounds().apply {
            extend(nw)
            extend(se)
        }
    }

    private fun intersectsBounds(
        outer: GeoRectBounds,
        tile: GeoRectBounds,
    ): Boolean {
        val outerSw = outer.southWest ?: return true
        val outerNe = outer.northEast ?: return true
        val tileSw = tile.southWest ?: return true
        val tileNe = tile.northEast ?: return true

        // GeoRectBounds uses "minimal arc" for longitudes, which can shrink world-like bounds.
        // For raster polygons, only use latitude culling to avoid dropping tiles.
        return tileNe.latitude >= outerSw.latitude &&
            outerNe.latitude >= tileSw.latitude
    }

    private fun tileToGeoPoint(
        x: Double,
        y: Double,
        z: Double,
    ): com.mapconductor.core.features.GeoPoint {
        val n = 2.0.pow(z)
        val lon = x / n * 360.0 - 180.0
        val latRad = kotlin.math.atan(kotlin.math.sinh(PI * (1 - 2 * y / n)))
        val lat = latRad * 180.0 / PI
        return com.mapconductor.core.features
            .GeoPoint(lat, lon)
    }

    private fun lonToPixelX(
        lonDeg: Double,
        z: Int,
    ): Double {
        val worldSize = tileSizePx.toDouble() * (1 shl z).toDouble()
        return ((lonDeg + 180.0) / 360.0) * worldSize
    }

    private fun latToPixelY(
        latDeg: Double,
        z: Int,
    ): Double {
        val worldSize = tileSizePx.toDouble() * (1 shl z).toDouble()
        val siny = sin(latDeg * PI / 180.0).coerceIn(-0.9999, 0.9999)
        val y = 0.5 - (ln((1.0 + siny) / (1.0 - siny)) / (4.0 * PI))
        return y * worldSize
    }

    private fun bitmapToPng(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }
}
