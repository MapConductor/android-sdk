package com.mapconductor.core.marker

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import com.mapconductor.core.BitmapIconCache
import com.mapconductor.core.ResourceProvider
import com.mapconductor.settings.Settings
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable


interface MarkerIcon  {
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

    // 便利なコンストラクタ
    constructor(
        fillColor: Color = Color.Red,
        strokeColor: Color = Color.White,
        strokeWidth: Dp = Settings.Default.iconStroke,
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
    override val infoAnchor: Offset = Offset(0.1f, 0f)

//    override fun getSizeInPixel(): Size {
//        val sizeInPx = ResourceProvider.dpToPx(iconSize.value.toDouble()).toFloat()
//        return Size(sizeInPx, sizeInPx)
//    }

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
        val id = "default_icon_${hashCode()}".hashCode()
        BitmapIconCache.get(id)?.let {
            return it
        }

        // オリジナルサイズ（SVGの元サイズ）
        val originalSize = Size(23.5f, 25.6f)

        // Canvas全体のサイズ（iconSize * scale）
        val canvasSize = ResourceProvider.dpToPx(iconSize.value * scale)

        val bitmap = createBitmap(canvasSize.toInt(), canvasSize.toInt())
        val canvas = Canvas(bitmap)
        val oneDp = ResourceProvider.dpToPx(2f).toFloat()

        // マーカーの描画 - アンカーポイント(0.5f, 1f)を考慮した配置
        val strokePath = Path().apply {
            // マーカーをCanvasサイズに合わせてスケール
            val scaleX = canvasSize.toFloat() / originalSize.width
            val scaleY = canvasSize.toFloat() / originalSize.height
            val markerScale = minOf(scaleX, scaleY) // アスペクト比を保持

            // スケール後のマーカーサイズ
            val scaledWidth = originalSize.width * markerScale
            val scaledHeight = originalSize.height * markerScale

            // アンカーポイント(0.5f, 1f)に基づいた配置
            // 水平方向: 中央配置
            val offsetX = (canvasSize.toFloat() - scaledWidth) / 2f
            // 垂直方向: 下端をCanvasの底に合わせる
            val offsetY = canvasSize.toFloat() - scaledHeight + oneDp

            // --- マーカーの形状（スケール済み座標） ---
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

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.style = Paint.Style.FILL
            it.color = fillColor.toArgb()
        }

        val strokePaint = Paint().also {
            it.color = strokeColor.toArgb()
            it.style = Paint.Style.STROKE
            it.strokeWidth = ResourceProvider.dpToPx(strokeWidth.value * scale).toFloat()
            it.isAntiAlias = true
        }

        val shadowPaint = Paint().apply {
            this.color = strokeColor.copy(alpha = strokeColor.alpha / 2f).toArgb()
            this.isAntiAlias = true
            val pixelBlurRadius = 0.1f * scale
            this.maskFilter = BlurMaskFilter(pixelBlurRadius, BlurMaskFilter.Blur.OUTER)
        }

        // 直接描画
        canvas.drawPath(strokePath, shadowPaint)
        canvas.drawPath(strokePath, fillPaint)
        canvas.drawPath(strokePath, strokePaint)

        // --- ラベルの描画 ---
        label?.let { labelText ->
            canvas.apply {
                val textPaint = Paint().apply {
                    this.color = Color.Black.toArgb()
                    this.textSize = ResourceProvider.dpToPx(labelTextSize.value * scale).toFloat()
                    this.textAlign = Paint.Align.LEFT
                    this.typeface = labelTypeFace
                    this.isAntiAlias = true
                    this.isSubpixelText = true
                }

                // マーカーの配置情報を再計算
                val markerScale = minOf(canvasSize.toFloat() / originalSize.width, canvasSize.toFloat() / originalSize.height)
                val scaledWidth = originalSize.width * markerScale
                val scaledHeight = originalSize.height * markerScale
                val offsetX = (canvasSize.toFloat() - scaledWidth) / 2f
                val offsetY = canvasSize.toFloat() - scaledHeight

                // マーカーの円形部分の中心座標（オリジナル座標系では x=12, y=8）
                val markerCenterX = 12f * markerScale + offsetX
                val markerCenterY = 8f * markerScale + offsetY

                // テキストの測定
                val textWidth = textPaint.measureText(labelText)
                val metrics = textPaint.fontMetrics
                val textHeight = metrics.descent - metrics.ascent

                // テキストを円の中心に配置
                val xForText = markerCenterX - textWidth / 2f
                val yForText = markerCenterY - textHeight / 2f

                // ラベルのアウトライン描画
                drawText(labelText, xForText, yForText + textHeight - metrics.descent, Paint().apply {
                    this.color = labelStrokeColor.copy(alpha = 0.9f).toArgb()
                    this.textSize = ResourceProvider.dpToPx(labelTextSize.value * scale).toFloat()
                    this.textAlign = Paint.Align.LEFT
                    this.typeface = labelTypeFace
                    this.isAntiAlias = true
                    style = Paint.Style.STROKE
                    strokeWidth = oneDp
                    strokeJoin = Paint.Join.ROUND
                    strokeCap = Paint.Cap.ROUND
                })

                // ラベルテキスト描画
                drawText(labelText, xForText, yForText + textHeight - metrics.descent, textPaint)

                // デバッグ用の枠線（本番では削除可能）
                drawRect(xForText, yForText, xForText + textWidth, yForText + textHeight, Paint().apply {
                    this.color = Color.Blue.toArgb()
                    this.style = Paint.Style.STROKE
                    strokeWidth = oneDp
                })

                drawRect(0f, 0f, canvasSize.toFloat() - oneDp, canvasSize.toFloat() - oneDp, Paint().apply {
                    this.color = Color.Magenta.toArgb()
                    this.style = Paint.Style.STROKE
                    strokeWidth = oneDp
                })
            }
        }

        val result = BitmapIcon(
            bitmap = bitmap,
            anchor = anchor,
            size = Size(canvasSize.toFloat(), canvasSize.toFloat()),
        )
        BitmapIconCache.put(id, result)
        return result
    }
}


