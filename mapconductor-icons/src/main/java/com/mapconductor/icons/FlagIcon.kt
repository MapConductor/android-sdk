package com.mapconductor.icons

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import com.mapconductor.core.BitmapIconCache
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.marker.BitmapIcon
import com.mapconductor.core.marker.MarkerIcon
import com.mapconductor.settings.Settings
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path

class FlagIcon(
    private val properties: IconProperties = IconProperties(),
) : MarkerIcon {

    data class IconProperties(
        val fillColor: Color = Color.Red,
        val strokeColor: Color = Color.White,
        val strokeWidth: Dp = 1.dp,
        val scale: Float = 1f,
        val iconSize: Dp = Settings.Default.iconSize,
    )

    constructor(
        fillColor: Color = Color.Red,
        strokeColor: Color = Color.White,
        strokeWidth: Dp = Settings.Default.iconStroke,
        scale: Float = 1f,
        iconSize: Dp = Settings.Default.iconSize,
    ) : this(
        IconProperties(fillColor, strokeColor, strokeWidth, scale, iconSize)
    )

    // プロパティの委譲
    val fillColor: Color by properties::fillColor
    val strokeColor: Color by properties::strokeColor
    val strokeWidth: Dp by properties::strokeWidth
    override val scale: Float by properties::scale
    override val iconSize: Dp by properties::iconSize
    override val anchor: Offset = Offset(0.176f, 0.91f)
    override val infoAnchor: Offset = Offset(0.5f, 0f)


    // data classのcopyを活用した独自copyメソッド
    fun copy(
        fillColor: Color = this.fillColor,
        strokeColor: Color = this.strokeColor,
        strokeWidth: Dp = this.strokeWidth,
        scale: Float = this.scale,
        iconSize: Dp = this.iconSize,
    ): FlagIcon {
        return FlagIcon(
            properties.copy(
                fillColor = fillColor,
                strokeColor = strokeColor,
                strokeWidth = strokeWidth,
                scale = scale,
                iconSize = iconSize,
            )
        )
    }

    fun copy(scale: Float, iconSize: Dp): FlagIcon {
        return copy(scale = scale, iconSize = iconSize)
    }

    // equals, hashCode, toStringも委譲
    override fun equals(other: Any?): Boolean {
        return other is FlagIcon && properties == other.properties
    }

    override fun hashCode(): Int = properties.hashCode()
    override fun toString(): String = "DefaultIcon($properties)"

    fun drawFlagOnCanvas(
        canvas: Canvas,
        fillPaint: Paint,
        strokePaint: Paint,
        width: Float,
        height: Float
    ) {
        // SVGの実際の描画領域を計算
        val svgLeft = 5.161f
        val svgTop = 0f
        val svgRight = 45.931f
        val svgBottom = 51.48f
        val svgWidth = svgRight - svgLeft
        val svgHeight = svgBottom - svgTop

        // アスペクト比を維持したスケール計算
        val scaleX = width / svgWidth
        val scaleY = height / svgHeight
        val scale = minOf(scaleX, scaleY) // 小さい方を選んでアスペクト比維持

        // スケール後のサイズ
        val scaledWidth = svgWidth * scale
        val scaledHeight = svgHeight * scale

        // 中央配置のためのオフセット
        val offsetX = (width - scaledWidth) / 2f
        val offsetY = (height - scaledHeight) / 2f

        canvas.save()
        canvas.translate(offsetX, offsetY)
        canvas.scale(scale, scale)
        canvas.translate(-svgLeft, -svgTop)

        val path = Path()

        // メインフラッグ部分
        path.moveTo(14.16f, 7.037f)
        path.lineTo(41.892f, 7.037f)

        // 右端の装飾的な切り込み
        path.lineTo(42.815f, 8.797f)
        path.cubicTo(43.339f, 9.554f, 43.34f, 10.017f, 42.815f, 10.797f)
        path.lineTo(41.5f, 10.699f)
        path.cubicTo(39.579f, 13.477f, 39.558f, 17.846f, 41.453f, 20.646f)
        path.lineTo(42.845f, 22.7f)
        path.cubicTo(43.295f, 23.365f, 43.386f, 23.884f, 43.28f, 24.084f)
        path.lineTo(41.891f, 24.499f)
        path.lineTo(14.16f, 24.499f)
        path.close()

        // 塗りつぶしと枠線
        canvas.drawPath(path, fillPaint)
        canvas.drawPath(path, strokePaint)

        // 旗竿
        path.reset()
        path.addRect(8.161f, 5.5f, 11.16f, 45.98f, Path.Direction.CW)
        canvas.drawPath(path, fillPaint)
        canvas.drawPath(path, strokePaint)

        // 旗竿上部のキャップ
        canvas.drawCircle(9.66f, 5.5f, 1.5f, fillPaint)
        canvas.drawCircle(9.66f, 5.5f, 1.5f, strokePaint)

        canvas.restore()
    }

    override fun toBitmapIcon(): BitmapIcon {
        val id = "flag_icon_${hashCode()}".hashCode()
        BitmapIconCache.get(id)?.let {
            return it
        }

        val oneDp = ResourceProvider.dpToPx(1.dp).toFloat()
        val canvasSize = ResourceProvider.dpToPx(iconSize.value * scale)

        val bitmap = createBitmap(canvasSize.toInt(), canvasSize.toInt())
        val canvas = Canvas(bitmap)

        val flagPaint = Paint().apply {
            color = fillColor.toArgb()
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val strokePaint = Paint().also {
            it.color = strokeColor.toArgb()
            it.style = Paint.Style.STROKE
            it.strokeWidth = ResourceProvider.dpToPx(strokeWidth).toFloat()
        }

        drawFlagOnCanvas(canvas, flagPaint, strokePaint, canvasSize.toFloat(), canvasSize.toFloat())
        canvas.apply {

            drawRect(0f, 0f, canvasSize.toFloat() - oneDp, canvasSize.toFloat() - oneDp, Paint().apply {
                this.color = Color.Magenta.toArgb()
                this.style = Paint.Style.STROKE
                strokeWidth = oneDp
            })
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
