package com.mapconductor.core.marker

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.core.graphics.withScale
import com.mapconductor.core.BitmapIconCache
import com.mapconductor.core.ResourceProvider
import com.mapconductor.settings.Settings
import kotlin.math.max
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable


data class IconProperties(
    val fillColor: Color = Color.Red,
    val strokeColor: Color = Color.White,
    val strokeWidth: Dp = 1.dp,
    val scale: Float = 1f,
    val label: String? = null,
    val labelTextColor: Color? = Color.Black,
    val labelTextSize: Dp = 10.dp,
    val labelTypeFace: Typeface = Typeface.DEFAULT,
    val labelStrokeColor: Color = Color.White,
    val iconSize: Dp = Settings.Default.iconSize,
)

interface MarkerIcon  {
    val scale: Float
    val anchor: Offset
    val iconSize: Dp
    val infoAnchor: Offset

    fun getSizeInPixel(): Size
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

    protected fun toBitmap(drawable: Drawable, width: Int, height: Int): Bitmap {

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
    private val properties: IconProperties = IconProperties()
) : MarkerIcon {
    // 便利なコンストラクタ
    constructor(
        fillColor: Color = Color.Red,
        strokeColor: Color = Color.White,
        strokeWidth: Dp = 1.dp,
        scale: Float = 1f,
        label: String? = null,
        labelTextColor: Color? = Color.Black,
        labelTextSize: Dp = 10.dp,
        labelTypeFace: Typeface = Typeface.DEFAULT,
        labelStrokeColor: Color = Color.White,
        iconSize: Dp = Settings.Default.iconSize,
    ) : this(IconProperties(fillColor, strokeColor, strokeWidth, scale, label, labelTextColor, labelTextSize, labelTypeFace, labelStrokeColor, iconSize))

    // プロパティの委譲
    val fillColor: Color by properties::fillColor
    val strokeColor: Color by properties::strokeColor
    val strokeWidth: Dp by properties::strokeWidth
    override val scale: Float by properties::scale
    val label: String? by properties::label
    val labelTextColor: Color? by properties::labelTextColor
    val labelTextSize: Dp by properties::labelTextSize
    val labelTypeFace: Typeface by properties::labelTypeFace
    val labelStrokeColor: Color by properties::labelStrokeColor
    override val iconSize: Dp by properties::iconSize
    override val anchor: Offset = Offset(0.5f, 1f)
    override val infoAnchor: Offset = Offset(0.5f, 0f)

    override fun getSizeInPixel(): Size {
        val sizeInPx = ResourceProvider.dpToPx(iconSize.value.toDouble()).toFloat()
        return Size(sizeInPx, sizeInPx)
    }

    // data classのcopyを活用した独自copyメソッド
    fun copy(
        fillColor: Color = this.fillColor,
        strokeColor: Color = this.strokeColor,
        strokeWidth: Dp = this.strokeWidth,
        scale: Float = this.scale,
        label: String? = this.label,
        labelTextColor: Color? = this.labelTextColor,
        labelTextSize: Dp = this.labelTextSize,
        labelTypeFace: Typeface = this.labelTypeFace,
        labelStrokeColor: Color = this.labelStrokeColor,
        iconSize: Dp = this.iconSize,
    ): DefaultIcon {
        return DefaultIcon(
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
            )
        )
    }

    fun copy(scale: Float, iconSize: Dp): DefaultIcon {
        return copy(scale = scale, iconSize = iconSize)
    }

    // equals, hashCode, toStringも委譲
    override fun equals(other: Any?): Boolean {
        return other is DefaultIcon && properties == other.properties
    }

    override fun hashCode(): Int = properties.hashCode()
    override fun toString(): String = "DefaultIcon($properties)"

    override fun toBitmapIcon(): BitmapIcon {
        val id = hashCode()
        BitmapIconCache.get(id)?.let {
            return it
        }
        val originalSize = Size(23.5f, 23.5f)
        val svgOriginalSize = max(originalSize.width, originalSize.height)

        val canvasSize = ResourceProvider.dpToPx(iconSize.value * scale)
        val marginTop = 1.0f
        val marginLeft = (svgOriginalSize - originalSize.width) / 2f

        val bitmap = createBitmap(canvasSize.toInt(), canvasSize.toInt())
        val canvas = Canvas(bitmap)

        val scaleFactor = ((canvasSize - marginTop * 2f) / svgOriginalSize).toFloat()

        val strokePath =
            Path().apply {
                // --- 最初のサブパス (外側の形状) ---
                // "m12 0" -> moveTo(12*s + offsetX, 0*s + offsetY)
                moveTo(12f + marginLeft, 0f + marginTop)

                // "c-4.4183 2.3685e-15 -8 3.5817-8 8" - relative cubicTo (all params scaled)
                rCubicTo(
                    -4.4183f,
                    2.3685e-15f,
                    -8f,
                    3.5817f,
                    -8f,
                    8f,
                )
                // (-5.1546833f, 0f, -9.333333f, 4.17865f, -9.333333f, 9.333333f)

                // "0 1.421 0.3816 2.75 1.0312 3.906" - implicit relative cubicTo
                rCubicTo(
                    0f,
                    1.421f,
                    0.3816f,
                    2.75f,
                    1.0312f,
                    3.906f,
                )
                // (0f, 1.6578333f, 0.4452f, 3.2083333f, 1.2030667f, 4.557f)

                // "0.1079 0.192 0.221 0.381 0.3438 0.563" - implicit relative cubicTo
                rCubicTo(
                    0.1079f,
                    0.192f,
                    0.221f,
                    0.381f,
                    0.3438f,
                    0.563f,
                )
                // (0.12588333f, 0.224f, 0.25783333f, 0.4445f, 0.4011f, 0.6568333f)

                // "l6.625 11.531" - relative lineTo
                rLineTo(6.625f, 11.531f)
                // (7.7291665f, 13.452833f)

                // "6.625-11.531" - implicit relative lineTo
                rLineTo(6.625f, -11.531f)
                // (7.7291665f, -13.452833f)

                // "c0.102-0.151 0.19-0.311 0.281-0.469" - relative cubicTo
                rCubicTo(
                    0.102f,
                    -0.151f,
                    0.19f,
                    -0.311f,
                    0.281f,
                    -0.469f,
                )
                // (0.119f, -0.17616667f, 0.22166666f, -0.36283332f, 0.32783332f, -0.54716665f)

                // "l0.063-0.094" - relative lineTo
                rLineTo(0.063f, -0.094f)
                // (0.0735f, -0.10966667f)

                // "c0.649-1.156 1.031-2.485 1.031-3.906" - relative cubicTo
                rCubicTo(
                    0.649f,
                    -1.156f,
                    1.031f,
                    -2.485f,
                    1.031f,
                    -3.906f,
                )
                // (0.7571667f, -1.3486667f, 1.2028333f, -2.8991666f, 1.2028333f, -4.557f)

                // "0-4.4183-3.582-8-8-8" - implicit relative cubicTo
                rCubicTo(
                    0f,
                    -4.4183f,
                    -3.582f,
                    -8f,
                    -8f,
                    -8f,
                )
                // (0f, -5.1546833f, -4.179f, -9.333333f, -9.333333f, -9.333333f)

                // "z" - closePath
                close() // 最初のサブパスを閉じる
            }

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.style = Paint.Style.FILL
            it.color = fillColor.toArgb()
        }
        val strokePaint = Paint().also {
            it.color = strokeColor.toArgb()
            it.style = Paint.Style.STROKE
            it.strokeWidth = strokeWidth.value
        }

        val shadowPaint = Paint().apply {
            this.color = strokeColor.copy(alpha = strokeColor.alpha / 2f).toArgb()
            this.isAntiAlias = true
            // BlurMaskFilterの半径はピクセル単位。論理半径をピクセルに変換。
            // scaleXとscaleYが異なる場合を考慮し、平均または主要な軸のスケールを使う。ここではscaleYを例に。
            val pixelBlurRadius = 0.5
            this.maskFilter = BlurMaskFilter(pixelBlurRadius.toFloat(), BlurMaskFilter.Blur.OUTER)
        }
        canvas.withScale(scaleFactor, scaleFactor) {
            drawPath(strokePath, shadowPaint)
            drawPath(strokePath, fillPaint)
            drawPath(strokePath, strokePaint)
        }

        // --- 3. ラベルの描画 (labelが指定されている場合) ---
        label?.let { labelText ->
            val oneDp = ResourceProvider.dpToPx(1f).toFloat()
            canvas.apply {
                 val textPaint = Paint().apply {
                    this.color = Color.Black.toArgb()
                    this.textSize = ResourceProvider.dpToPx(labelTextSize.value * scale).toFloat()
                    this.textAlign = Paint.Align.LEFT
                    this.typeface = labelTypeFace
                    this.isAntiAlias = true
                    this.isSubpixelText = true // より滑らかなテキスト描画のため
                }

                // 円の中心
                val textBottom = canvasSize.toFloat() * 0.6f
                val textTop = canvasSize.toFloat() * 0.2f
                val centerYLogical = (textBottom - textTop) * 0.5f + textTop

                // テキストの水平位置の調整
                val textWidth = textPaint.measureText(label)
                val xForText = ((canvasSize - textWidth) / 2f).toFloat()

                // テキストの垂直位置を調整して中央揃えにする
                val metrics = textPaint.fontMetrics
                val textHeight: Float = metrics.descent - metrics.ascent
                val yForText = centerYLogical - textHeight * 0.5f

                // Draw the outline stroke for the label
                drawText(labelText, xForText, yForText + textHeight - metrics.descent, Paint().apply {
                    this.color = labelStrokeColor.copy(alpha = 0.9f).toArgb()
                    this.textSize = ResourceProvider.dpToPx(labelTextSize.value * scale).toFloat()
                    this.textAlign = Paint.Align.LEFT
                    this.typeface = labelTypeFace
                    this.isAntiAlias = true
                    style = Paint.Style.STROKE
                    strokeWidth = oneDp * 5.0f
                    strokeJoin = Paint.Join.ROUND
                    strokeCap = Paint.Cap.ROUND
                })

                // Draw the label
                drawText(labelText, xForText, yForText + textHeight - metrics.descent, textPaint)

                // for Debugging
                /*
                drawRect(xForText, yForText, xForText + textWidth, yForText + textHeight, Paint().apply {
                    this.color = Color.Blue.toArgb()
                    this.style = Paint.Style.STROKE
                    strokeWidth = oneDp
                })

                drawRect(0f, 0f, canvasSize.toFloat() - oneDp, canvasSize.toFloat() - oneDp, Paint().apply {
                    this.color = Color.Green.toArgb()
                    this.style = Paint.Style.STROKE
                    strokeWidth = oneDp
                })
                 */
            }
        }

        val result = BitmapIcon(
            bitmap = bitmap,
            anchor = anchor,
            size = getSizeInPixel(),
        )
        BitmapIconCache.put(id, result)
        return result
    }
}


