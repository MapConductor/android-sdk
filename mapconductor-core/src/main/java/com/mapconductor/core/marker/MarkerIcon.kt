package com.mapconductor.core.marker

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import com.mapconductor.core.BitmapIconCache
import com.mapconductor.core.ResourceProvider
import com.mapconductor.settings.Settings
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

interface MarkerIcon {
    val scale: Float
    val anchor: Offset
    val iconSize: Dp
    val infoAnchor: Offset

//    fun getSizeInPixel(): Size
    fun toBitmapIcon(): BitmapIcon
}

abstract class AbstractMarkerIcon : MarkerIcon {
    abstract override val scale: Float
    abstract override val anchor: Offset
    abstract override val iconSize: Dp
    abstract override val infoAnchor: Offset
}

abstract class AndroidDrawableIcon(
    val drawable: Drawable,
) : AbstractMarkerIcon() {
    protected fun toBitmap(
        drawable: Drawable,
        width: Int,
        height: Int,
    ): Bitmap {
        return when (drawable) {
            is BitmapDrawable -> {
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

class DefaultIcon(
    private val properties: IconProperties = IconProperties(),
) : MarkerIcon {
    data class IconProperties(
        val fillColor: Color = Color.Red,
        val strokeColor: Color = Color.White,
        val strokeWidth: Dp = 1.dp,
        val scale: Float = 1f,
        val label: String? = null,
        val labelTextColor: Color? = Color.Black,
        val labelTextSize: TextUnit = 18.sp,
        val labelTypeFace: Typeface = Typeface.DEFAULT,
        val labelStrokeColor: Color = Color.White,
        val iconSize: Dp = Settings.Default.iconSize,
        val adaptiveScaling: Boolean = true, // 適応的スケーリングを有効にするか
        val maxFontScale: Float = 2.5f, // フォントスケールの上限
        val minIconSize: Dp = 24.dp, // アイコンの最小サイズ
        val maxIconSize: Dp = 120.dp, // アイコンの最大サイズ
    )

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
        iconSize: Dp = Settings.Default.iconSize,
        adaptiveScaling: Boolean = true,
        maxFontScale: Float = 1.0f,
        minIconSize: Dp = 24.dp,
        maxIconSize: Dp = 80.dp,
    ) : this(
        IconProperties(
            fillColor,
            strokeColor,
            strokeWidth,
            scale,
            label,
            labelTextColor,
            labelTextSize,
            labelTypeFace,
            labelStrokeColor,
            iconSize,
            adaptiveScaling,
            maxFontScale,
            minIconSize,
            maxIconSize,
        ),
    )

    // プロパティの委譲
    val fillColor: Color by properties::fillColor
    val strokeColor: Color by properties::strokeColor
    val strokeWidth: Dp by properties::strokeWidth
    override val scale: Float by properties::scale
    val label: String? by properties::label
    val labelTextColor: Color? by properties::labelTextColor
    val labelTextSize: TextUnit by properties::labelTextSize
    val labelTypeFace: Typeface by properties::labelTypeFace
    val labelStrokeColor: Color by properties::labelStrokeColor
    override val iconSize: Dp by properties::iconSize
    override val anchor: Offset = Offset(0.5f, 1f)
    override val infoAnchor: Offset = Offset(0.1f, 0f)
    val adaptiveScaling: Boolean by properties::adaptiveScaling
    val maxFontScale: Float by properties::maxFontScale
    val minIconSize: Dp by properties::minIconSize
    val maxIconSize: Dp by properties::maxIconSize

    /**
     * 適応的スケーリング情報
     */
    data class AdaptiveScaleInfo(
        val displayScale: Float, // ディスプレイ密度スケール（1.0が標準）
        val fontScale: Float, // フォントスケール（1.0が標準）
        val effectiveFontScale: Float, // 実際に適用するフォントスケール（上限制限あり）
        val finalIconScale: Float, // 最終的なアイコンスケール
        val finalIconSize: Dp, // 最終的なアイコンサイズ
        val finalTextSize: TextUnit, // 最終的なテキストサイズ
    )

    // data classのcopyを活用した独自copyメソッド
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
        adaptiveScaling: Boolean = this.adaptiveScaling,
        maxFontScale: Float = this.maxFontScale,
        minIconSize: Dp = this.minIconSize,
        maxIconSize: Dp = this.maxIconSize,
    ): DefaultIcon =
        DefaultIcon(
            properties.copy(
                fillColor = fillColor,
                strokeColor = strokeColor,
                strokeWidth = strokeWidth,
                scale = scale,
                label = label,
                labelTextColor = labelTextColor,
                labelTextSize = labelTextSize,
                labelTypeFace = labelTypeFace,
                labelStrokeColor = labelStrokeColor,
                iconSize = iconSize,
                adaptiveScaling = adaptiveScaling,
                maxFontScale = maxFontScale,
                minIconSize = minIconSize,
                maxIconSize = maxIconSize,
            ),
        )

    /**
     * 現在のシステム設定から適応的スケーリング情報を計算
     */
    private fun calculateAdaptiveScale(): AdaptiveScaleInfo {
        val displayMetrics = ResourceProvider.getDisplayMetrics()
        val configuration = ResourceProvider.getSystemConfiguration()

        // 1. ディスプレイ密度スケール（1.0 = mdpi, 1.5 = hdpi, 2.0 = xhdpi, etc.）
        val displayScale = displayMetrics.density

        // 2. フォントスケール（ユーザーのアクセシビリティ設定）
        val systemFontScale = configuration.fontScale

        // 3. 効果的なフォントスケール（上限制限）
        val effectiveFontScale = min(systemFontScale, maxFontScale)

        // 4. 最終的なアイコンスケール計算
        val baseScale = scale
        val fontScaleAdjustment = sqrt(effectiveFontScale) // 平方根で緩やかに調整
        val finalIconScale = baseScale * fontScaleAdjustment

        // 5. 最終的なアイコンサイズ（制限内に収める）
        val calculatedIconSize = (iconSize.value * finalIconScale).dp
        val finalIconSize = calculatedIconSize.coerceIn(minIconSize, maxIconSize)

        // 6. 最終的なテキストサイズ（重要：effectiveFontScaleとfinalIconScaleの両方を考慮）
        val textScaleMultiplier = effectiveFontScale * (finalIconSize.value / iconSize.value)
        val finalTextSize =
            when (labelTextSize.type) {
                TextUnitType.Sp -> (labelTextSize.value * textScaleMultiplier).sp
                else -> (labelTextSize.value * textScaleMultiplier).sp
            }

        return AdaptiveScaleInfo(
            displayScale = displayScale,
            fontScale = systemFontScale,
            effectiveFontScale = effectiveFontScale,
            finalIconScale = finalIconSize.value / iconSize.value,
            finalIconSize = finalIconSize,
            finalTextSize = finalTextSize,
        )
    }

    fun copy(
        scale: Float,
        iconSize: Dp,
    ): DefaultIcon = copy(scale = scale, iconSize = iconSize)

    // equals, hashCode, toStringも委譲
    override fun equals(other: Any?): Boolean = other is DefaultIcon && properties == other.properties

    override fun hashCode(): Int = properties.hashCode()

    override fun toString(): String = "DefaultIcon($properties)"

    override fun toBitmapIcon(): BitmapIcon {
        val scaleInfo =
            if (adaptiveScaling) {
                calculateAdaptiveScale()
            } else {
                // 適応的スケーリング無効の場合は基本設定を使用
                AdaptiveScaleInfo(
                    displayScale = 1f,
                    fontScale = 1f,
                    effectiveFontScale = 1f,
                    finalIconScale = scale,
                    finalIconSize = iconSize,
                    finalTextSize = labelTextSize,
                )
            }

        val id = "adaptive_icon_${hashCode()}_${scaleInfo.hashCode()}".hashCode()
        BitmapIconCache.get(id)?.let {
            return it
        }

        // 適応的スケーリング情報を使用してアイコンを描画
        val canvasSize = ResourceProvider.dpToPx(scaleInfo.finalIconSize.value)
        val bitmap = createBitmap(canvasSize.toInt(), canvasSize.toInt())
        val canvas = Canvas(bitmap)

        // マーカーの描画（適応的スケール適用）
        drawMarker(canvas, canvasSize.toFloat(), scaleInfo.finalIconScale)

        // ラベルの描画（適応的フォントサイズ適用）
        drawLabel(canvas, canvasSize.toFloat(), scaleInfo)

        val result =
            BitmapIcon(
                bitmap = bitmap,
                anchor = anchor,
                size = Size(canvasSize.toFloat(), canvasSize.toFloat()),
            )
        BitmapIconCache.put(id, result)
        return result
    }

    /**
     * マーカー本体の描画
     */
    private fun drawMarker(
        canvas: Canvas,
        canvasSize: Float,
        iconScale: Float,
    ) {
        val originalSize = Size(23.5f, 25.6f)
        val markerScale = minOf(canvasSize / originalSize.width, canvasSize / originalSize.height)
        val scaledWidth = originalSize.width * markerScale
        val scaledHeight = originalSize.height * markerScale
        val offsetX = (canvasSize - scaledWidth) / 2f
        val offsetY = canvasSize - scaledHeight

        val strokePath =
            Path().apply {
                // マーカーパスの生成（省略 - 既存のコードと同じ）
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

        val fillPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = fillColor.toArgb()
            }

        val strokePaint =
            Paint().apply {
                color = strokeColor.toArgb()
                style = Paint.Style.STROKE
                strokeWidth = ResourceProvider.dpToPx(this@DefaultIcon.strokeWidth.value * iconScale).toFloat()
                isAntiAlias = true
            }

        canvas.drawPath(strokePath, fillPaint)
        canvas.drawPath(strokePath, strokePaint)
    }

    /**
     * ラベルテキストの描画（適応的サイズ）
     */
    private fun drawLabel(
        canvas: Canvas,
        canvasSize: Float,
        scaleInfo: AdaptiveScaleInfo,
    ) {
        label?.let { labelText ->
            // 基本テキストサイズを計算（スケーリング適用）
            // 重要：scale=1fで呼び出す（スケールは既にfinalTextSizeに含まれている）
            val baseTextSize = convertTextUnitToPx(scaleInfo.finalTextSize, 1f)

            val textPaint =
                Paint().apply {
                    color = labelTextColor?.toArgb() ?: Color.Black.toArgb()
                    textSize = baseTextSize
                    textAlign = Paint.Align.CENTER
                    typeface = labelTypeFace
                    isAntiAlias = true
                    isSubpixelText = true
                }

            // マーカーの円形部分の中心に配置
            val markerCenterX = canvasSize / 2f
            val markerCenterY = canvasSize * 0.35f // 円形部分の中心

            val metrics = textPaint.fontMetrics
            val textHeight = metrics.descent - metrics.ascent
            val baselineOffset = textHeight / 2f - metrics.descent

            // アウトライン描画（アイコンスケールを考慮したストローク幅）
            val outlineStrokeWidth =
                max(
                    ResourceProvider.dpToPx(1f * scaleInfo.finalIconScale).toFloat(),
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
    private fun convertTextUnitToPx(
        textUnit: TextUnit,
        scale: Float,
    ): Float {
        val result =
            when (textUnit.type) {
                TextUnitType.Sp -> {
                    val spValue = textUnit.value * scale
                    val pxValue = ResourceProvider.spToPx(spValue.toDouble()).toFloat()
                    pxValue
                }
                TextUnitType.Em -> {
                    val baseFontSize = 16f
                    val emValue = baseFontSize * textUnit.value * scale
                    val pxValue = ResourceProvider.spToPx(emValue.toDouble()).toFloat()
                    pxValue
                }
                else -> {
                    val dpValue = textUnit.value * scale
                    val pxValue = ResourceProvider.dpToPx(dpValue.toDouble()).toFloat()
                    pxValue
                }
            }

        return result
    }
}
