package com.mapconductor.core.marker

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withClip
import com.mapconductor.core.BitmapIconCache
import com.mapconductor.core.ResourceProvider
import com.mapconductor.settings.Settings
import kotlin.math.max
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable

/**
 * DefaultIconの基底クラス
 * マーカーの共通機能を提供し、フィル描画のみサブクラスで実装する
 */
abstract class AbstractDefaultIcon(
    protected val baseProperties: BaseIconProperties,
) : AbstractMarkerIcon() {
    data class BaseIconProperties(
        val strokeColor: Color,
        val strokeWidth: Dp,
        val scale: Float,
        val label: String?,
        val labelTextColor: Color?,
        val labelTextSize: TextUnit,
        val labelTypeFace: Typeface,
        val labelStrokeColor: Color,
        val infoAnchor: Offset,
        val iconSize: Dp,
        val debug: Boolean,
    )

    // 共通プロパティの委譲
    val strokeColor: Color by baseProperties::strokeColor
    val strokeWidth: Dp by baseProperties::strokeWidth
    final override val scale: Float by baseProperties::scale
    val label: String? by baseProperties::label
    val labelTextColor: Color? by baseProperties::labelTextColor
    val labelTextSize: TextUnit by baseProperties::labelTextSize
    val labelTypeFace: Typeface by baseProperties::labelTypeFace
    val labelStrokeColor: Color by baseProperties::labelStrokeColor
    final override val iconSize: Dp by baseProperties::iconSize
    final override val anchor: Offset = Offset(0.5f, 1f)
    final override val infoAnchor: Offset by baseProperties::infoAnchor
    final override val debug: Boolean by baseProperties::debug

    /**
     * サブクラスでマーカーのフィル描画を実装する
     */
    protected abstract fun drawMarkerFill(
        canvas: Canvas,
        path: Path,
        canvasSize: Float,
        iconScale: Float,
    )

    /**
     * サブクラスで等価性比較のための固有プロパティを返す
     */
    protected abstract fun getUniqueProperties(): Any

    override fun toBitmapIcon(): BitmapIcon {
        val id = "default_icon_${hashCode()}".hashCode()
        BitmapIconCache.get(id)?.let {
            return it
        }

        // Calculate canvas size with scale applied
        val baseCanvasSize = ResourceProvider.dpToPxForBitmap(iconSize.value)
        val markerSize = (baseCanvasSize * scale).toInt()

        // ラベルの幅を測定
        var labelWidth = 0f
        var labelHeight = 0f
        label?.let { labelText ->
            val baseTextSize = convertTextUnitToPx(labelTextSize, 1f)
            val textPaint =
                Paint().apply {
                    textSize = baseTextSize
                    textAlign = Paint.Align.CENTER
                    typeface = labelTypeFace
                    isAntiAlias = true
                }
            labelWidth = textPaint.measureText(labelText)
            val metrics = textPaint.fontMetrics
            labelHeight = metrics.descent - metrics.ascent
        }

        // ビットマップのサイズを決定（マーカーとラベルの両方が収まるように）
        // 横幅: マーカーまたはラベルの大きい方 + 余白
        val padding = markerSize * 0.1f // 左右に5%ずつの余白
        val bitmapWidth = max(markerSize.toFloat(), labelWidth + padding).toInt()
        // 高さ: マーカーの高さを基準（ラベルはマーカー内に収まる想定）
        val bitmapHeight = markerSize

        val bitmap = createBitmap(bitmapWidth, bitmapHeight)
        // Set bitmap density based on override (e.g., 1.0 for MapLibre to prevent auto-scaling)
        ResourceProvider.getBitmapDensity().let { density ->
            bitmap.density = (density * android.util.DisplayMetrics.DENSITY_DEFAULT).toInt()
        }
        val canvas = Canvas(bitmap)

        // マーカーを中央に配置するためのオフセット
        val markerOffsetX = (bitmapWidth - markerSize) / 2f

        // Draw marker (scale is already applied in markerSize)
        drawMarker(
            canvas = canvas,
            canvasSize = markerSize.toFloat(),
            iconScale = scale,
            offsetX = markerOffsetX,
        )

        // Draw label
        drawLabel(
            canvas = canvas,
            canvasSize = markerSize.toFloat(),
            iconScale = scale,
            textSize = labelTextSize,
            offsetX = markerOffsetX,
        )

        // アンカーを調整（マーカーの底部中央）
        val adjustedAnchor = Offset(0.5f, 1f)

        val result =
            BitmapIcon(
                bitmap = bitmap,
                anchor = adjustedAnchor,
                size = Size(bitmapWidth.toFloat(), bitmapHeight.toFloat()),
            )
        BitmapIconCache.put(id, result)
        return result
    }

    /**
     * マーカー本体の描画（共通処理）
     */
    protected fun drawMarker(
        canvas: Canvas,
        canvasSize: Float,
        iconScale: Float,
        offsetX: Float = 0f,
    ) {
        val strokePath = createMarkerPath(canvasSize, iconScale, offsetX)

        // デバッグ用の枠描画
        if (debug) {
            drawDebugFrame(canvas)
        }

        // フィル描画（サブクラス実装）
        drawMarkerFill(canvas, strokePath, canvasSize, iconScale)

        // ストローク描画（共通）
        val strokePaint = createStrokePaint(iconScale)
        canvas.drawPath(strokePath, strokePaint)
    }

    /**
     * マーカーのパスを生成
     */
    protected fun createMarkerPath(
        canvasSize: Float,
        iconScale: Float,
        horizontalOffset: Float = 0f,
    ): Path {
        val originalSize = Size(23.5f, 25.6f)

        // Since canvasSize is already scaled (baseCanvasSize * scale),
        // we don't need to apply iconScale again to the markerScale calculation
        val scaledStrokeWidth =
            ResourceProvider
                .dpToPxForBitmap(strokeWidth.value * iconScale)
                .toFloat()

        // Reserve space for stroke on sides and top, but not bottom (point should touch edge)
        val epsilon = 0.75f
        val padding = (scaledStrokeWidth / 2f - epsilon).coerceAtLeast(0f)
        val availableWidth = canvasSize - (padding * 2f)
        val availableHeight = canvasSize - padding // Only top padding, bottom point touches edge

        // Calculate scale to fit marker within available space
        // DO NOT multiply by iconScale here as canvasSize already includes it
        val markerScale =
            minOf(
                availableWidth / originalSize.width,
                availableHeight / originalSize.height,
            )

        val scaledWidth = originalSize.width * markerScale
        val scaledHeight = originalSize.height * markerScale

        // Center horizontally, align bottom point to canvas edge
        // The path's bottom point should touch the canvas bottom, the stroke will extend beyond
        val offsetX = (canvasSize - scaledWidth) / 2f + horizontalOffset
        val offsetY = (canvasSize - scaledHeight + (strokeWidth.value * markerScale)) / 2f

        return Path().apply {
            moveTo(12f * markerScale + offsetX, 0f * markerScale + offsetY)

            rCubicTo(
                -4.4183f * markerScale,
                2.3685e-15f * markerScale,
                -8f * markerScale,
                3.5817f * markerScale,
                -8f * markerScale,
                8f * markerScale,
            )

            rCubicTo(
                0f * markerScale,
                1.421f * markerScale,
                0.3816f * markerScale,
                2.75f * markerScale,
                1.0312f * markerScale,
                3.906f * markerScale,
            )

            rCubicTo(
                0.1079f * markerScale,
                0.192f * markerScale,
                0.221f * markerScale,
                0.381f * markerScale,
                0.3438f * markerScale,
                0.563f * markerScale,
            )

            rLineTo(6.625f * markerScale, 11.531f * markerScale)
            rLineTo(6.625f * markerScale, -11.531f * markerScale)

            rCubicTo(
                0.102f * markerScale,
                -0.151f * markerScale,
                0.19f * markerScale,
                -0.311f * markerScale,
                0.281f * markerScale,
                -0.469f * markerScale,
            )

            rLineTo(0.063f * markerScale, -0.094f * markerScale)

            rCubicTo(
                0.649f * markerScale,
                -1.156f * markerScale,
                1.031f * markerScale,
                -2.485f * markerScale,
                1.031f * markerScale,
                -3.906f * markerScale,
            )

            rCubicTo(
                0f * markerScale,
                -4.4183f * markerScale,
                -3.582f * markerScale,
                -8f * markerScale,
                -8f * markerScale,
                -8f * markerScale,
            )

            close()
        }
    }

    /**
     * ストローク用のPaintを作成
     */
    protected fun createStrokePaint(iconScale: Float): Paint =
        Paint().apply {
            color = strokeColor.toArgb()
            style = Paint.Style.STROKE
            strokeWidth =
                ResourceProvider
                    .dpToPxForBitmap(
                        this@AbstractDefaultIcon.strokeWidth.value * iconScale,
                    ).toFloat()
            isAntiAlias = true
            strokeJoin = Paint.Join.ROUND // 追加
            strokeCap = Paint.Cap.ROUND // 追加（尖り部のにじみ軽減）
        }

    /**
     * ラベルテキストの描画
     */
    protected fun drawLabel(
        canvas: Canvas,
        canvasSize: Float,
        iconScale: Float,
        textSize: TextUnit,
        offsetX: Float = 0f,
    ) {
        label?.let { labelText ->
            // 基本テキストサイズを計算（スケーリング適用）
            val baseTextSize = convertTextUnitToPx(textSize, 1f)

            val textPaint =
                Paint().apply {
                    color = labelTextColor?.toArgb() ?: Color.Black.toArgb()
                    this.textSize = baseTextSize
                    textAlign = Paint.Align.CENTER
                    typeface = labelTypeFace
                    isAntiAlias = true
                    isSubpixelText = true
                }

            // マーカーの円形部分の中心に配置
            val markerCenterX = canvasSize / 2f + offsetX
            val markerCenterY = canvasSize * 0.35f // 円形部分の中心

            val metrics = textPaint.fontMetrics
            val textHeight = metrics.descent - metrics.ascent
            val baselineOffset = textHeight / 2f - metrics.descent

            // アウトライン描画（アイコンスケールを考慮したストローク幅）
            val outlineStrokeWidth =
                max(
                    ResourceProvider.dpToPxForBitmap(1f * iconScale).toFloat(),
                    2f, // 最小2px
                )

            val outlinePaint =
                Paint(textPaint).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = outlineStrokeWidth
                    color = labelStrokeColor.toArgb()
                    strokeJoin = Paint.Join.ROUND
                    strokeCap = Paint.Cap.ROUND
                }

            canvas.drawText(labelText, markerCenterX, markerCenterY + baselineOffset, outlinePaint)
            canvas.drawText(labelText, markerCenterX, markerCenterY + baselineOffset, textPaint)
        }
    }

    /**
     * TextUnitをピクセルサイズに変換
     */
    protected fun convertTextUnitToPx(
        textUnit: TextUnit,
        scale: Float,
    ): Float =
        when (textUnit.type) {
            TextUnitType.Sp -> {
                val spValue = textUnit.value * scale
                ResourceProvider.spToPx(spValue.toDouble()).toFloat()
            }
            TextUnitType.Em -> {
                val baseFontSize = 16f
                val emValue = baseFontSize * textUnit.value * scale
                ResourceProvider.spToPx(emValue.toDouble()).toFloat()
            }
            else -> {
                val dpValue = textUnit.value * scale
                ResourceProvider.dpToPx(dpValue.toDouble()).toFloat()
            }
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false
        other as AbstractDefaultIcon
        return baseProperties == other.baseProperties &&
            getUniqueProperties() == other.getUniqueProperties()
    }

    override fun hashCode(): Int = baseProperties.hashCode() * 31 + getUniqueProperties().hashCode()

    override fun toString(): String =
        "${this::class.simpleName}(baseProperties=$baseProperties, uniqueProperties=${getUniqueProperties()})"
}

/**
 * カラーフィル版のDefaultIcon
 */
class ColorDefaultIcon(
    private val fillColor: Color,
    baseProperties: BaseIconProperties,
) : AbstractDefaultIcon(baseProperties) {
    // 便利なコンストラクタ
    constructor(
        fillColor: Color = Color.Red,
        strokeColor: Color = Color.White,
        strokeWidth: Dp = Settings.Default.iconStroke,
        scale: Float = 1f,
        label: String? = null,
        labelTextColor: Color? = Color.Black,
        labelTextSize: TextUnit = 18.sp,
        labelTypeFace: Typeface = Typeface.DEFAULT,
        labelStrokeColor: Color = Color.White,
        infoAnchor: Offset = Offset(0.5f, 0f),
        iconSize: Dp = Settings.Default.iconSize,
        debug: Boolean = false,
    ) : this(
        fillColor = fillColor,
        baseProperties =
            BaseIconProperties(
                strokeColor = strokeColor,
                strokeWidth = strokeWidth,
                scale = scale,
                label = label,
                labelTextColor = labelTextColor,
                labelTextSize = labelTextSize,
                labelTypeFace = labelTypeFace,
                labelStrokeColor = labelStrokeColor,
                infoAnchor = infoAnchor,
                iconSize = iconSize,
                debug = debug,
            ),
    )

    override fun drawMarkerFill(
        canvas: Canvas,
        path: Path,
        canvasSize: Float,
        iconScale: Float,
    ) {
        val fillPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = fillColor.toArgb()
            }
        canvas.drawPath(path, fillPaint)
    }

    override fun getUniqueProperties(): Any = fillColor

    fun copy(
        fillColor: Color = this.fillColor,
        strokeColor: Color = this.strokeColor,
        strokeWidth: Dp = this.strokeWidth,
        scale: Float = this.scale,
        label: String? = this.label,
        labelTextColor: Color? = this.labelTextColor,
        labelTextSize: TextUnit = this.labelTextSize,
        labelTypeFace: Typeface = this.labelTypeFace,
        labelStrokeColor: Color = this.labelStrokeColor,
        iconSize: Dp = this.iconSize,
        debug: Boolean = this.debug,
    ): ColorDefaultIcon =
        ColorDefaultIcon(
            fillColor = fillColor,
            baseProperties =
                baseProperties.copy(
                    strokeColor = strokeColor,
                    strokeWidth = strokeWidth,
                    scale = scale,
                    label = label,
                    labelTextColor = labelTextColor,
                    labelTextSize = labelTextSize,
                    labelTypeFace = labelTypeFace,
                    labelStrokeColor = labelStrokeColor,
                    iconSize = iconSize,
                    debug = debug,
                ),
        )

    fun copy(
        scale: Float,
        iconSize: Dp,
    ): ColorDefaultIcon = copy(scale = scale, iconSize = iconSize)
}

/**
 * 画像フィル版のDefaultIcon
 */
class ImageDefaultIcon(
    private val backgroundImage: Bitmap,
    baseProperties: BaseIconProperties,
) : AbstractDefaultIcon(baseProperties) {
    // 便利なコンストラクタ
    constructor(
        backgroundImage: Bitmap,
        strokeColor: Color = Color.White,
        strokeWidth: Dp = Settings.Default.iconStroke,
        scale: Float = 1f,
        label: String? = null,
        labelTextColor: Color? = Color.Black,
        labelTextSize: TextUnit = 18.sp,
        labelTypeFace: Typeface = Typeface.DEFAULT,
        labelStrokeColor: Color = Color.White,
        infoAnchor: Offset = Offset(0.5f, 0f),
        iconSize: Dp = Settings.Default.iconSize,
        debug: Boolean = false,
    ) : this(
        backgroundImage = backgroundImage,
        baseProperties =
            BaseIconProperties(
                strokeColor = strokeColor,
                strokeWidth = strokeWidth,
                scale = scale,
                label = label,
                labelTextColor = labelTextColor,
                labelTextSize = labelTextSize,
                labelTypeFace = labelTypeFace,
                labelStrokeColor = labelStrokeColor,
                infoAnchor = infoAnchor,
                iconSize = iconSize,
                debug = debug,
            ),
    )

    override fun drawMarkerFill(
        canvas: Canvas,
        path: Path,
        canvasSize: Float,
        iconScale: Float,
    ) {
        canvas.withClip(path) {
            // マーカー形状でクリッピング

            // 背景画像をマーカーサイズにスケーリングして描画
            // アスペクト比を保持してセンタークロップ
            val bitmapWidth = backgroundImage.width.toFloat()
            val bitmapHeight = backgroundImage.height.toFloat()
            val bitmapRatio = bitmapWidth / bitmapHeight
            val canvasRatio = 1f // 正方形のキャンバス

            val matrix = Matrix()

            if (bitmapRatio > canvasRatio) {
                // ビットマップが横長の場合：高さを合わせてセンタリング
                val scale = canvasSize / bitmapHeight
                val scaledWidth = bitmapWidth * scale
                val offsetX = (canvasSize - scaledWidth) / 2f
                matrix.setScale(scale, scale)
                matrix.postTranslate(offsetX, 0f)
            } else {
                // ビットマップが縦長または正方形の場合：幅を合わせてセンタリング
                val scale = canvasSize / bitmapWidth
                val scaledHeight = bitmapHeight * scale
                val offsetY = (canvasSize - scaledHeight) / 2f
                matrix.setScale(scale, scale)
                matrix.postTranslate(0f, offsetY)
            }

            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            drawBitmap(backgroundImage, matrix, paint)
        }
    }

    override fun getUniqueProperties(): Any {
        // Bitmapの内容をハッシュ化して比較用に使用
        return backgroundImage.let { bitmap ->
            val buffer = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(buffer, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            buffer.contentHashCode()
        }
    }

    fun copy(
        backgroundImage: Bitmap = this.backgroundImage,
        strokeColor: Color = this.strokeColor,
        strokeWidth: Dp = this.strokeWidth,
        scale: Float = this.scale,
        label: String? = this.label,
        labelTextColor: Color? = this.labelTextColor,
        labelTextSize: TextUnit = this.labelTextSize,
        labelTypeFace: Typeface = this.labelTypeFace,
        labelStrokeColor: Color = this.labelStrokeColor,
        iconSize: Dp = this.iconSize,
        debug: Boolean = this.debug,
    ): ImageDefaultIcon =
        ImageDefaultIcon(
            backgroundImage = backgroundImage,
            baseProperties =
                baseProperties.copy(
                    strokeColor = strokeColor,
                    strokeWidth = strokeWidth,
                    scale = scale,
                    label = label,
                    labelTextColor = labelTextColor,
                    labelTextSize = labelTextSize,
                    labelTypeFace = labelTypeFace,
                    labelStrokeColor = labelStrokeColor,
                    iconSize = iconSize,
                    debug = debug,
                ),
        )

    fun copy(
        scale: Float,
        iconSize: Dp,
    ): ImageDefaultIcon = copy(scale = scale, iconSize = iconSize)
}

/**
 * Drawable フィル版のDefaultIcon
 */
class DrawableDefaultIcon(
    private val backgroundDrawable: Drawable,
    baseProperties: BaseIconProperties,
) : AbstractDefaultIcon(baseProperties) {
    // 便利なコンストラクタ
    constructor(
        backgroundDrawable: Drawable,
        strokeColor: Color = Color.White,
        strokeWidth: Dp = Settings.Default.iconStroke,
        scale: Float = 1f,
        label: String? = null,
        labelTextColor: Color? = Color.Black,
        labelTextSize: TextUnit = 18.sp,
        labelTypeFace: Typeface = Typeface.DEFAULT,
        labelStrokeColor: Color = Color.White,
        infoAnchor: Offset = Offset(0.5f, 0f),
        iconSize: Dp = Settings.Default.iconSize,
        debug: Boolean = false,
    ) : this(
        backgroundDrawable = backgroundDrawable,
        baseProperties =
            BaseIconProperties(
                strokeColor = strokeColor,
                strokeWidth = strokeWidth,
                scale = scale,
                label = label,
                labelTextColor = labelTextColor,
                labelTextSize = labelTextSize,
                labelTypeFace = labelTypeFace,
                labelStrokeColor = labelStrokeColor,
                infoAnchor = infoAnchor,
                iconSize = iconSize,
                debug = debug,
            ),
    )

    override fun drawMarkerFill(
        canvas: Canvas,
        path: Path,
        canvasSize: Float,
        iconScale: Float,
    ) {
        canvas.withClip(path) {
            // マーカー形状でクリッピング

            // Drawableのサイズを設定
            val canvasInt = canvasSize.toInt()

            // Drawableの固有サイズを取得
            val intrinsicWidth = backgroundDrawable.intrinsicWidth
            val intrinsicHeight = backgroundDrawable.intrinsicHeight

            if (intrinsicWidth > 0 && intrinsicHeight > 0) {
                // 固有サイズがある場合：アスペクト比を保持してセンタークロップ
                val drawableRatio = intrinsicWidth.toFloat() / intrinsicHeight.toFloat()
                val canvasRatio = 1f // 正方形のキャンバス

                val bounds =
                    if (drawableRatio > canvasRatio) {
                        // Drawableが横長の場合：高さを合わせてセンタリング
                        val scaledWidth = (canvasInt * drawableRatio).toInt()
                        val offsetX = (canvasInt - scaledWidth) / 2
                        Rect(offsetX, 0, offsetX + scaledWidth, canvasInt)
                    } else {
                        // Drawableが縦長または正方形の場合：幅を合わせてセンタリング
                        val scaledHeight = (canvasInt / drawableRatio).toInt()
                        val offsetY = (canvasInt - scaledHeight) / 2
                        Rect(0, offsetY, canvasInt, offsetY + scaledHeight)
                    }

                backgroundDrawable.bounds = bounds
            } else {
                // 固有サイズがない場合：キャンバス全体に描画
                backgroundDrawable.setBounds(0, 0, canvasInt, canvasInt)
            }

            backgroundDrawable.draw(this)
        }
    }

    override fun getUniqueProperties(): Any {
        // Drawableの識別用にクラス名とパラメータのハッシュを使用
        return when (backgroundDrawable) {
            is BitmapDrawable -> {
                // BitmapDrawableの場合はビットマップの内容をハッシュ化
                backgroundDrawable.bitmap?.let { bitmap ->
                    if (bitmap.isRecycled) {
                        // リサイクルされたビットマップの場合はDrawableのハッシュを使用
                        backgroundDrawable.hashCode()
                    } else {
                        try {
                            // サンプリングサイズを計算（メモリ効率のため）
                            val sampleWidth = minOf(bitmap.width, 32)
                            val sampleHeight = minOf(bitmap.height, 32)
                            val bufferSize = sampleWidth * sampleHeight
                            val buffer = IntArray(bufferSize)

                            // 正しい引数でgetPixelsを呼び出し
                            bitmap.getPixels(
                                buffer, // pixels配列
                                0, // offset
                                sampleWidth, // stride（一行のピクセル数）
                                0, // x開始位置
                                0, // y開始位置
                                sampleWidth, // 取得する幅
                                sampleHeight, // 取得する高さ
                            )
                            buffer.contentHashCode()
                        } catch (e: Exception) {
                            // getPixelsでエラーが発生した場合はDrawableのハッシュを使用
                            backgroundDrawable.hashCode()
                        }
                    }
                } ?: backgroundDrawable.hashCode()
            }
            is ColorDrawable -> {
                // ColorDrawableの場合は色の値を使用
                backgroundDrawable.color
            }
            is GradientDrawable -> {
                // GradientDrawableの場合はDrawable自体のハッシュを使用
                backgroundDrawable.hashCode()
            }
            else -> {
                // その他のDrawableの場合はクラス名とハッシュコードを組み合わせ
                "${backgroundDrawable::class.java.name}_${backgroundDrawable.hashCode()}"
            }
        }
    }

    fun copy(
        backgroundDrawable: Drawable = this.backgroundDrawable,
        strokeColor: Color = this.strokeColor,
        strokeWidth: Dp = this.strokeWidth,
        scale: Float = this.scale,
        label: String? = this.label,
        labelTextColor: Color? = this.labelTextColor,
        labelTextSize: TextUnit = this.labelTextSize,
        labelTypeFace: Typeface = this.labelTypeFace,
        labelStrokeColor: Color = this.labelStrokeColor,
        iconSize: Dp = this.iconSize,
        debug: Boolean = this.debug,
    ): DrawableDefaultIcon =
        DrawableDefaultIcon(
            backgroundDrawable = backgroundDrawable,
            baseProperties =
                baseProperties.copy(
                    strokeColor = strokeColor,
                    strokeWidth = strokeWidth,
                    scale = scale,
                    label = label,
                    labelTextColor = labelTextColor,
                    labelTextSize = labelTextSize,
                    labelTypeFace = labelTypeFace,
                    labelStrokeColor = labelStrokeColor,
                    iconSize = iconSize,
                    debug = debug,
                ),
        )

    fun copy(
        scale: Float,
        iconSize: Dp,
    ): DrawableDefaultIcon = copy(scale = scale, iconSize = iconSize)
}

// 後方互換性のため、元のDefaultIconをColorDefaultIconのエイリアスとして定義
typealias DefaultMarkerIcon = ColorDefaultIcon
