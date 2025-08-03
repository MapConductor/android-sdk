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
import com.mapconductor.core.BitmapIconCache
import com.mapconductor.core.ResourceProvider
import com.mapconductor.settings.Settings
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface

class DefaultIcon(
    private val properties: IconProperties,
) : AbstractMarkerIcon() {
    data class IconProperties(
        val fillColor: Color,
        val strokeColor: Color,
        val strokeWidth: Dp,
        val scale: Float,
        val label: String?,
        val labelTextColor: Color?,
        val labelTextSize: TextUnit,
        val labelTypeFace: Typeface,
        val labelStrokeColor: Color,
        val iconSize: Dp,
        val debug: Boolean,
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
        debug: Boolean = false,
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
            debug,
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
    override val infoAnchor: Offset = Offset(0.5f, 0f)
    override val debug: Boolean by properties::debug

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
        debug: Boolean = this.debug,
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
                debug = debug,
            ),
        )

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
            AdaptiveScaleInfo(
                displayScale = 1f,
                fontScale = 1f,
                effectiveFontScale = 1f,
                finalIconScale = scale,
                finalIconSize = iconSize,
                finalTextSize = labelTextSize,
            )

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
        val offsetY = canvasSize - scaledHeight + ResourceProvider.dpToPx(strokeWidth.value - 1f).toFloat()

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

        // Draw rectangle frame for debugging
        if (this.debug) {
            Paint()
                .apply {
                    isAntiAlias = true
                    strokeWidth = 1f
                    this.color = Color.Black.toArgb()
                    style = Paint.Style.STROKE
                }.also {
                    canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), it)
                }
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
