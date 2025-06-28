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
import com.mapconductor.core.icons.Default
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

        val iconBitmap = createIconBitmap(icon = icon)
        bitmapCache.put(key, iconBitmap)
        return iconBitmap
    }

    fun createIconCanvas(
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

    fun createIconBitmap(icon: MarkerIcon = MarkerIcon.Default()): BitmapIcon {
        val svgOriginalWidth = 24f // SVGの元のviewBox幅
        val svgOriginalHeight = 24f // SVGの元のviewBox高さ
        val width = svgOriginalWidth * (icon.scale ?: 2f) * ResourceProvider.density
        val height = svgOriginalHeight * (icon.scale ?: 2f) * ResourceProvider.density

        val bitmap = createBitmap(width.toInt(), height.toInt())
        val canvas = Canvas(bitmap)

        val pathCoordinateSystemWidth = 32f
        val pathCoordinateSystemHeight = 32f

        val scaleX = width / pathCoordinateSystemWidth
        val scaleY = height / pathCoordinateSystemHeight

        val insidePath = icon.insidePath
        val outsidePath = icon.outsidePath

        val shadowPath =
            Path(icon.outsidePath).apply {
                offset(1f, 1f)
            }

        val outsidePaint =
            Paint(Paint.ANTI_ALIAS_FLAG).also {
                if (icon.fillDrawable != null) {
                    val iconCanvasSize =
                        Size(
                            (svgOriginalWidth * 8).toFloat(),
                            (svgOriginalHeight * 8).toFloat(),
                        )
                    val iconBitmap =
                        this.createIconCanvas(
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
                                    icon.fillDrawable,
                                    iconCanvasSize.width.toInt(),
                                    iconCanvasSize.height.toInt(),
                                ),
                            fillColor = icon.outsideColor ?: Color.RED,
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
                it.color = icon.outsideColor ?: Color.RED
            }

        val insidePaint =
            Paint(Paint.ANTI_ALIAS_FLAG).also {
                if (icon.fillDrawable != null) {
                    val iconCanvasSize =
                        Size(
                            (svgOriginalWidth * 8).toFloat(),
                            (svgOriginalHeight * 8).toFloat(),
                        )
                    val iconBitmap =
                        this.createIconCanvas(
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
                                    icon.fillDrawable,
                                    iconCanvasSize.width.toInt(),
                                    iconCanvasSize.height.toInt(),
                                ),
                            fillColor = icon.insideColor ?: Color.RED,
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
                it.color = icon.insideColor ?: Color.RED
            }

        val iconPaint =
            icon.iconDrawable?.let {
                Paint(Paint.ANTI_ALIAS_FLAG).also {
                    it.strokeWidth = 0f
                    val iconCanvasSize =
                        Size(
                            (svgOriginalWidth * 8).toFloat(),
                            (svgOriginalHeight * 8).toFloat(),
                        )
                    val iconBitmap2 =
                        this.createIconCanvas(
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
                                    icon.iconDrawable,
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
                it.strokeWidth = icon.outsideWidth ?: 0f // SVGでのstrokeWidthに相当
                it.color = icon.outsideStrokeColor ?: Color.WHITE
            }

        val shadowPaint =
            Paint().apply {
                this.color = Color.argb(0.5f, 0.0f, 0.0f, 0.0f)
                this.isAntiAlias = true
                // BlurMaskFilterの半径はピクセル単位。論理半径をピクセルに変換。
                // scaleXとscaleYが異なる場合を考慮し、平均または主要な軸のスケールを使う。ここではscaleYを例に。
                val pixelBlurRadius = 2
                if (pixelBlurRadius > 0f) { // 半径0だとエラーになるため
                    this.maskFilter = BlurMaskFilter(pixelBlurRadius.toFloat(), BlurMaskFilter.Blur.OUTER)
                } else {
                    // 半径が非常に小さい場合は、単純な色描画にフォールバック（または何もしない）
                    // ここでは何もしない例（maskFilterがnullのまま）
                }
            }

        canvas.withScale(scaleX.toFloat(), scaleY.toFloat()) {
            drawPath(shadowPath, shadowPaint)
            drawPath(outsidePath, outsidePaint)
            drawPath(outsidePath, strokePaint)
            if (insidePath != null) {
                drawPath(insidePath, insidePaint)
                drawPath(insidePath, strokePaint)
            }

            // --- 3. ラベルの描画 (labelが指定されている場合) ---
//            if (icon.label != null) {
//                val textPaint = Paint().apply {
//                    this.color = icon.labelTextColor ?: Color.BLACK
//                    this.textSize = icon.labelTextSizeLogical ?: 10f // 論理サイズ。Canvasスケールで実際の大きさが決まる
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
//                canvas.drawText(icon.label, centerXLogical, yForDrawTextLogical, textPaint)
//            }
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
