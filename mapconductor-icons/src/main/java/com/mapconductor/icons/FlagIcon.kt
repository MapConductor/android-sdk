package com.mapconductor.icons

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withTranslation
import com.mapconductor.core.BitmapIconCache
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.marker.AbstractMarkerIcon
import com.mapconductor.core.marker.BitmapIcon
import com.mapconductor.settings.Settings
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.DisplayMetrics

class FlagIcon(
    private val properties: IconProperties,
) : AbstractMarkerIcon() {
    data class IconProperties(
        val fillColor: Color,
        val strokeColor: Color,
        val strokeWidth: Dp,
        val scale: Float,
        val iconSize: Dp,
        val debug: Boolean,
    )

    constructor(
        fillColor: Color = Color.Red,
        strokeColor: Color = Color.White,
        strokeWidth: Dp = Settings.Default.iconStroke,
        scale: Float = 1f,
        iconSize: Dp = Settings.Default.iconSize,
        debug: Boolean = false,
    ) : this(
        IconProperties(fillColor, strokeColor, strokeWidth, scale, iconSize, debug),
    )

    // プロパティの委譲
    val fillColor: Color by properties::fillColor
    val strokeColor: Color by properties::strokeColor
    val strokeWidth: Dp by properties::strokeWidth
    override val scale: Float by properties::scale
    override val iconSize: Dp by properties::iconSize
    override val anchor: Offset = Offset(0.176f, 0.91f)
    override val infoAnchor: Offset = Offset(0.5f, 0f)
    override val debug: Boolean by properties::debug

    // data classのcopyを活用した独自copyメソッド
    fun copy(
        fillColor: Color = this.fillColor,
        strokeColor: Color = this.strokeColor,
        strokeWidth: Dp = this.strokeWidth,
        scale: Float = this.scale,
        iconSize: Dp = this.iconSize,
        debug: Boolean = this.debug,
    ): FlagIcon =
        FlagIcon(
            properties.copy(
                fillColor = fillColor,
                strokeColor = strokeColor,
                strokeWidth = strokeWidth,
                scale = scale,
                iconSize = iconSize,
                debug = debug,
            ),
        )

    fun copy(
        scale: Float,
        iconSize: Dp,
    ): FlagIcon = copy(scale = scale, iconSize = iconSize)

    // equals, hashCode, toStringも委譲
    override fun equals(other: Any?): Boolean = other is FlagIcon && properties == other.properties

    override fun hashCode(): Int = properties.hashCode()

    override fun toString(): String = "FlagIcon($properties)"

    fun drawFlagOnCanvas(
        canvas: Canvas,
        fillPaint: Paint,
        strokePaint: Paint,
        width: Float,
        height: Float,
    ) {
        // SVGの実際の描画領域を計算（マージンを除去）
        val svgLeft = 0f // マージンを0にする
        val svgTop = 0f // マージンを0にする
        val svgRight = 45.931f - 5.161f // 実際の幅
        val svgBottom = 51.48f - 5.161f // 実際の高さ
        val svgWidth = svgRight
        val svgHeight = svgBottom

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

        canvas.withTranslation(offsetX, offsetY) {
            scale(scale, scale)
            // 元のマージン分を差し引いて座標を調整
            translate(-5.161f, -5.161f)

            val path = Path()

            // メインフラッグ部分（縦に拡張）
            path.moveTo(14.16f, 7.037f)
            path.lineTo(41.892f, 7.037f)

            // 右端の装飾的な切り込み
            path.lineTo(42.815f, 9.797f)
            path.cubicTo(43.339f, 10.554f, 43.34f, 11.517f, 42.815f, 12.297f)
            path.lineTo(41.5f, 12.199f)
            path.cubicTo(39.579f, 16.477f, 39.558f, 22.846f, 41.453f, 26.646f)
            path.lineTo(42.845f, 29.2f)
            path.cubicTo(43.295f, 29.865f, 43.386f, 30.384f, 43.28f, 30.584f)
            path.lineTo(41.891f, 30.999f)
            path.lineTo(14.16f, 30.999f)
            path.close()

            // 塗りつぶしと枠線
            drawPath(path, fillPaint)
            drawPath(path, strokePaint)

            // 旗竿（太くする）
            path.reset()
            path.addRect(7.161f, 5.5f, 12.16f, 45.98f, Path.Direction.CW)
            drawPath(path, fillPaint)
            drawPath(path, strokePaint)

            // 旗竿上部のキャップ（太くしたポールに合わせてサイズ調整）
            drawCircle(9.66f, 5.5f, 2.0f, fillPaint)
            drawCircle(9.66f, 5.5f, 2.0f, strokePaint)
        }
    }

    override fun toBitmapIcon(): BitmapIcon {
        val id = "flag_icon_${hashCode()}".hashCode()
        BitmapIconCache.get(id)?.let {
            return it
        }

        val oneDp = ResourceProvider.dpToPx(1.dp).toFloat()
        val canvasSize = ResourceProvider.dpToPx(iconSize.value * scale)

        val bitmap = createBitmap(canvasSize.toInt(), canvasSize.toInt())
        // Set bitmap density to control map provider scaling
        ResourceProvider.getBitmapDensity().let { density ->
            bitmap.density = (density * DisplayMetrics.DENSITY_DEFAULT).toInt()
        }
        val canvas = Canvas(bitmap)

        val flagPaint =
            Paint().apply {
                color = fillColor.toArgb()
                style = Paint.Style.FILL
                isAntiAlias = true
            }

        val strokePaint =
            Paint().also {
                it.color = strokeColor.toArgb()
                it.style = Paint.Style.STROKE
                it.strokeWidth = ResourceProvider.dpToPx(strokeWidth).toFloat()
            }

        drawFlagOnCanvas(canvas, flagPaint, strokePaint, canvasSize.toFloat(), canvasSize.toFloat())
//        canvas.apply {
//
//            drawRect(0f, 0f, canvasSize.toFloat() - oneDp, canvasSize.toFloat() - oneDp, Paint().apply {
//                this.color = Color.Magenta.toArgb()
//                this.style = Paint.Style.STROKE
//                strokeWidth = oneDp
//            })
//        }

        val result =
            BitmapIcon(
                bitmap = bitmap,
                anchor = anchor,
                size = Size(canvasSize.toFloat(), canvasSize.toFloat()),
            )
        BitmapIconCache.put(id, result)
        return result
    }
}

@Preview
@Composable
private fun FlagIconPreview() {
    val icon = FlagIcon(debug = true)
    val bitmapIcon = remember(icon) { icon.toBitmapIcon() }
    val imageBitmap = remember(bitmapIcon) { bitmapIcon.bitmap.asImageBitmap() }

    Box(
        modifier =
            Modifier
                .background(Color.Green)
                .padding(all = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            bitmap = imageBitmap,
            contentDescription = null,
        )
    }
}
