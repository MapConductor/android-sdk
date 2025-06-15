package com.mapconductor.core

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.core.graphics.withScale
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.geocell.HexCell
import com.mapconductor.core.geocell.HexCellRegistry
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.marker.BitmapIcon
import com.mapconductor.core.marker.MarkerIcon
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.spherical.haversineDistance
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.LruCache

class MarkerManager<ActualMarker>(
    geocell: HexGeocell,
) {
    val bitmapCache: LruCache<Int, BitmapIcon> by lazy {
        // Get max memory size by bytes
        val maxMemory = Runtime.getRuntime().maxMemory()
        val cacheSize = maxMemory / 8

        // Cache bytes
        object : LruCache<Int, BitmapIcon>(cacheSize.toInt()) {
            override fun sizeOf(
                key: Int,
                iconRes: BitmapIcon,
            ): Int = iconRes.bitmap.byteCount / 1024
        }
    }

    private val markers: ConcurrentHashMap<String, ActualMarker> = ConcurrentHashMap()
    private val entries: ConcurrentMap<String, MarkerState> = ConcurrentHashMap()
    private val hashCodes: ConcurrentMap<String, Int> = ConcurrentHashMap()
    private val cellRegistry =
        HexCellRegistry<MarkerState>(
            geocell = geocell,
            // Maximum zoom level
            zoom = 20.0,
        )

    fun containsKey(id: String): Boolean = markers.containsKey(id)

    fun equalsValue(entry: MarkerState): Boolean = entries.get(entry.id)?.equals(entry) == true

    fun getValueSet(): Set<MarkerState> = entries.values.toSet()

    fun getMarker(id: String): ActualMarker? = markers.get(id)

    fun getState(id: String): MarkerState? = entries.get(id)

    fun getStateHashCode(id: String): Int? = hashCodes.get(id)

    fun removeStateAndMarker(id: String) {
        markers.remove(id)
        hashCodes.remove(id)
        entries.remove(id)?.let {
            cellRegistry.removePoint(it)
        }
    }

    fun metersPerPixel(
        position: IGeoPoint,
        zoom: Double,
        pixels: Double,
        tileSize: Int = 256,
    ): Double = cellRegistry.metersPerPixel(position, zoom, pixels, tileSize)

    fun findNearest(position: IGeoPoint): MarkerState? {
        val cell = cellRegistry.findNearest(position) ?: return null
        val entryIDs =
            cellRegistry.getEntryIDsByHexCell(cell)?.let { entryIDs ->
                entryIDs.sortedBy { entryId ->
                    entries[entryId]?.let { state ->
                        haversineDistance(position, state.position)
                    }
                }
            } ?: return null

        val entryId = entryIDs[0]
        return entries[entryId]
    }

    fun findByIdPrefix(prefix: String): List<HexCell> = cellRegistry.findByIdPrefix(prefix)

    fun registerState(
        state: MarkerState,
        marker: ActualMarker,
    ) {
        markers[state.id] = marker
        entries[state.id] = state
        hashCodes[state.id] = state.hashCode()
        cellRegistry.setPoint(state)
    }

    fun updateState(entry: MarkerState) {
        entries[entry.id] = entry
        hashCodes[entry.id] = entry.hashCode()
        cellRegistry.setPoint(entry)
    }

    fun allKeys(): List<String> = markers.keys.toList()

    fun clear() {
        markers.clear()
        entries.clear()
        cellRegistry.clear()
    }

    fun getBitmapIcon(icon: MarkerIcon): BitmapIcon {
        val key = icon.hashCode()
        val cache = bitmapCache.get(key)
        if (cache != null) return cache

        val iconBitmap =
            createDefaultMarkerShape(
                fillColor = icon.fillColor,
                strokeColor = icon.strokeColor,
                strokeWidth = icon.strokeWidth,
                scale = icon.scale,
                label = icon.label,
                labelTextColor = icon.labelTextColor,
                labelTextSizeLogical = icon.labelTextSizeLogical,
                fillDrawable = icon.fillDrawable,
                iconDrawable = icon.iconDrawable,
            )
        bitmapCache.put(key, iconBitmap)
        return iconBitmap
    }

    fun drawIcon(
        canvasSize: Size,
        iconRect: RectF,
        bitmap: Bitmap,
        fillColor: Int? = null,
    ): Bitmap {
        val canvasBitmap = createBitmap(canvasSize.width.toInt(), canvasSize.height.toInt())

        Canvas(canvasBitmap).apply {
            if (fillColor != null) {
                drawRect(
                    Rect(0, 0, canvasSize.width.toInt(), canvasSize.height.toInt()),
                    Paint().also {
                        it.color = fillColor
                        it.style = Paint.Style.FILL
                    },
                )
            }
            drawBitmap(
                bitmap,
                null,
                iconRect,
                Paint().also {
                    it.isAntiAlias = true
                    it.flags = Paint.FILTER_BITMAP_FLAG
                },
            )
        }

        return canvasBitmap
    }

    fun createDefaultMarkerShape(
        fillColor: Int? = null,
        strokeColor: Int? = null,
        strokeWidth: Float? = 1f,
        scale: Float? = 3f,
        label: String? = null,
        labelTextColor: Int? = Color.BLACK,
        labelTextSizeLogical: Float? = 10f, // 32x32論理座標系での文字サイズ
        fillDrawable: Drawable? = null, // ★塗りつぶし用ビットマップ
        iconDrawable: Drawable? = null,
    ): BitmapIcon {
        val svgOriginalWidth = 24f // SVGの元のviewBox幅
        val svgOriginalHeight = 24f // SVGの元のviewBox高さ
        val width = svgOriginalWidth * (scale ?: 2f) * ResourceProvider.density
        val height = svgOriginalHeight * (scale ?: 2f) * ResourceProvider.density

        val bitmap = createBitmap(width.toInt(), height.toInt())
        val canvas = Canvas(bitmap)

        val pathCoordinateSystemWidth = 32f
        val pathCoordinateSystemHeight = 32f

        val scaleX = width / pathCoordinateSystemWidth
        val scaleY = height / pathCoordinateSystemHeight

        val scaleFactor = 28.0f / 24.0f
        val offsetX = 2.0f
        val offsetY = 2.0f

        val strokePath =
            Path().apply {
                // --- 最初のサブパス (外側の形状) ---
                // "m12 0" -> moveTo(12*s + offsetX, 0*s + offsetY)
                moveTo(12f * scaleFactor + offsetX, 0f * scaleFactor + offsetY) // (16f, 2f)

                // "c-4.4183 2.3685e-15 -8 3.5817-8 8" - relative cubicTo (all params scaled)
                rCubicTo(
                    -4.4183f * scaleFactor,
                    2.3685e-15f * scaleFactor,
                    -8f * scaleFactor,
                    3.5817f * scaleFactor,
                    -8f * scaleFactor,
                    8f * scaleFactor,
                )
                // (-5.1546833f, 0f, -9.333333f, 4.17865f, -9.333333f, 9.333333f)

                // "0 1.421 0.3816 2.75 1.0312 3.906" - implicit relative cubicTo
                rCubicTo(
                    0f * scaleFactor,
                    1.421f * scaleFactor,
                    0.3816f * scaleFactor,
                    2.75f * scaleFactor,
                    1.0312f * scaleFactor,
                    3.906f * scaleFactor,
                )
                // (0f, 1.6578333f, 0.4452f, 3.2083333f, 1.2030667f, 4.557f)

                // "0.1079 0.192 0.221 0.381 0.3438 0.563" - implicit relative cubicTo
                rCubicTo(
                    0.1079f * scaleFactor,
                    0.192f * scaleFactor,
                    0.221f * scaleFactor,
                    0.381f * scaleFactor,
                    0.3438f * scaleFactor,
                    0.563f * scaleFactor,
                )
                // (0.12588333f, 0.224f, 0.25783333f, 0.4445f, 0.4011f, 0.6568333f)

                // "l6.625 11.531" - relative lineTo
                rLineTo(6.625f * scaleFactor, 11.531f * scaleFactor)
                // (7.7291665f, 13.452833f)

                // "6.625-11.531" - implicit relative lineTo
                rLineTo(6.625f * scaleFactor, -11.531f * scaleFactor)
                // (7.7291665f, -13.452833f)

                // "c0.102-0.151 0.19-0.311 0.281-0.469" - relative cubicTo
                rCubicTo(
                    0.102f * scaleFactor,
                    -0.151f * scaleFactor,
                    0.19f * scaleFactor,
                    -0.311f * scaleFactor,
                    0.281f * scaleFactor,
                    -0.469f * scaleFactor,
                )
                // (0.119f, -0.17616667f, 0.22166666f, -0.36283332f, 0.32783332f, -0.54716665f)

                // "l0.063-0.094" - relative lineTo
                rLineTo(0.063f * scaleFactor, -0.094f * scaleFactor)
                // (0.0735f, -0.10966667f)

                // "c0.649-1.156 1.031-2.485 1.031-3.906" - relative cubicTo
                rCubicTo(
                    0.649f * scaleFactor,
                    -1.156f * scaleFactor,
                    1.031f * scaleFactor,
                    -2.485f * scaleFactor,
                    1.031f * scaleFactor,
                    -3.906f * scaleFactor,
                )
                // (0.7571667f, -1.3486667f, 1.2028333f, -2.8991666f, 1.2028333f, -4.557f)

                // "0-4.4183-3.582-8-8-8" - implicit relative cubicTo
                rCubicTo(
                    0f * scaleFactor,
                    -4.4183f * scaleFactor,
                    -3.582f * scaleFactor,
                    -8f * scaleFactor,
                    -8f * scaleFactor,
                    -8f * scaleFactor,
                )
                // (0f, -5.1546833f, -4.179f, -9.333333f, -9.333333f, -9.333333f)

                // "z" - closePath
                close() // 最初のサブパスを閉じる
            }

        val fillPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).also {
                if (fillDrawable != null) {
                    val iconCanvasSize =
                        Size(
                            (svgOriginalWidth * 8).toFloat(),
                            (svgOriginalHeight * 8).toFloat(),
                        )
                    val iconBitmap =
                        this.drawIcon(
                            canvasSize = iconCanvasSize,
                            iconRect =
                                RectF(
                                    iconCanvasSize.width * 0f,
                                    0f,
                                    iconCanvasSize.width * 1f,
                                    iconCanvasSize.height * 1f,
                                ),
                            bitmap =
                                toBitmap(
                                    fillDrawable,
                                    iconCanvasSize.width.toInt(),
                                    iconCanvasSize.height.toInt(),
                                ),
                            fillColor = fillColor ?: Color.RED,
                        )
                    val iconBitmapTileMode = Shader.TileMode.CLAMP
                    val bitmapShader = BitmapShader(iconBitmap, iconBitmapTileMode, iconBitmapTileMode)

                    // BitmapShaderのローカルマトリックスを設定して、
                    // ビットマップがパスの32x32論理領域を適切にカバーするようにスケーリングする
                    val shaderMatrix = Matrix()
                    val shaderScaleX = svgOriginalWidth.toFloat() / iconBitmap.width.toFloat()
                    val shaderScaleY = svgOriginalHeight.toFloat() / iconBitmap.height.toFloat()
                    shaderMatrix.setScale(shaderScaleX, shaderScaleY)

                    // 丸い部分の中心の論理座標 (24x24系)
                    val centerXLogical = (pathCoordinateSystemWidth - svgOriginalWidth) * 0.5
                    val centerYLogical = (pathCoordinateSystemHeight - svgOriginalHeight) * 0.5
                    shaderMatrix.postTranslate(centerXLogical.toFloat(), centerYLogical.toFloat())
                    bitmapShader.setLocalMatrix(shaderMatrix)
                    it.shader = bitmapShader
                }
                it.style = Paint.Style.FILL
                it.color = fillColor ?: Color.RED
            }

        val iconPaint =
            iconDrawable?.let {
                Paint(Paint.ANTI_ALIAS_FLAG).also {
                    it.strokeWidth = 0f
                    val iconCanvasSize =
                        Size(
                            (svgOriginalWidth * 8).toFloat(),
                            (svgOriginalHeight * 8).toFloat(),
                        )
                    val iconBitmap2 =
                        this.drawIcon(
                            canvasSize = iconCanvasSize,
                            iconRect =
                                RectF(
                                    iconCanvasSize.width * 0.15f,
                                    0f,
                                    iconCanvasSize.width * 0.85f,
                                    iconCanvasSize.height * 0.7f,
                                ),
                            bitmap =
                                toBitmap(
                                    iconDrawable,
                                    iconCanvasSize.width.toInt(),
                                    iconCanvasSize.height.toInt(),
                                ),
                        )
                    val iconBitmapTileMode = Shader.TileMode.CLAMP
                    val bitmapShader = BitmapShader(iconBitmap2, iconBitmapTileMode, iconBitmapTileMode)

                    // BitmapShaderのローカルマトリックスを設定して、
                    // ビットマップがパスの32x32論理領域を適切にカバーするようにスケーリングする
                    val shaderMatrix = Matrix()
                    val shaderScaleX = svgOriginalWidth.toFloat() / iconBitmap2.width.toFloat()
                    val shaderScaleY = svgOriginalHeight.toFloat() / iconBitmap2.height.toFloat()
                    shaderMatrix.setScale(shaderScaleX, shaderScaleY)

                    // 丸い部分の中心の論理座標 (24x24系)
                    val centerXLogical = (pathCoordinateSystemWidth - svgOriginalWidth) * 0.5
                    val centerYLogical = (pathCoordinateSystemHeight - svgOriginalHeight) * 0.35
                    shaderMatrix.postTranslate(centerXLogical.toFloat(), centerYLogical.toFloat())
                    bitmapShader.setLocalMatrix(shaderMatrix)
                    it.shader = bitmapShader
                }
            }

        val strokePaint =
            Paint(Paint.ANTI_ALIAS_FLAG).also {
                it.style = Paint.Style.STROKE
                it.strokeWidth = strokeWidth ?: 0f // SVGでのstrokeWidthに相当
                it.color = strokeColor ?: Color.WHITE
            }

        val shadowPaint =
            Paint().apply {
                this.color = Color.argb(0.5f, 0.0f, 0.0f, 0.0f)
                this.isAntiAlias = true
                // BlurMaskFilterの半径はピクセル単位。論理半径をピクセルに変換。
                // scaleXとscaleYが異なる場合を考慮し、平均または主要な軸のスケールを使う。ここではscaleYを例に。
                val pixelBlurRadius = 0.5
                if (pixelBlurRadius > 0f) { // 半径0だとエラーになるため
                    this.maskFilter = BlurMaskFilter(pixelBlurRadius.toFloat(), BlurMaskFilter.Blur.OUTER)
                } else {
                    // 半径が非常に小さい場合は、単純な色描画にフォールバック（または何もしない）
                    // ここでは何もしない例（maskFilterがnullのまま）
                }
            }

        canvas.withScale(scaleX.toFloat(), scaleY.toFloat()) {
            drawPath(strokePath, shadowPaint)
            drawPath(strokePath, fillPaint)
            iconPaint?.also {
                drawPath(strokePath, it)
            }

            // --- 3. ラベルの描画 (labelが指定されている場合) ---
//            if (label != null) {
//                val textPaint = Paint().apply {
//                    this.color = labelTextColor ?: Color.BLACK
//                    this.textSize = labelTextSizeLogical ?: 10f // 論理サイズ。Canvasスケールで実際の大きさが決まる
//                    this.textAlign = Paint.Align.CENTER
//                    this.typeface = Typeface.DEFAULT_BOLD
//                    this.isAntiAlias = true
//                    this.isSubpixelText = true // より滑らかなテキスト描画のため
//                }
//
//                // 丸い部分の中心の論理座標 (32x32系)
//                val centerXLogical = 16f
//                val centerYLogical = 34f / 3f // 約 11.333f
//
//                // テキストの垂直位置を調整して中央揃えにする
//                val fm = textPaint.fontMetrics
//                val yForDrawTextLogical = centerYLogical - (fm.ascent + fm.descent) / 2f
//
//                canvas.drawText(label.substring(0, 1).toString(), centerXLogical, yForDrawTextLogical, textPaint)
//            }

//            // -- ストローク --
            drawPath(strokePath, strokePaint)
        }

        val visualNormalizedTipY = 0.9375f
        val anchor =
            Offset(
                x = 0.5f,
                y = (visualNormalizedTipY + (0.5 / 64.0)).toFloat(),
            )

        val size =
            Size(
                width = width.toFloat(),
                height = height.toFloat(),
            )

        return BitmapIcon(
            bitmap = bitmap,
            anchor = anchor,
            size = size,
        )
    }

    private fun toBitmap(
        drawable: Drawable,
        width: Int,
        height: Int,
    ): Bitmap {
        return when (drawable) {
            is BitmapDrawable -> {
//                drawable.bitmap.asImageBitmap().asAndroidBitmap()
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
}
