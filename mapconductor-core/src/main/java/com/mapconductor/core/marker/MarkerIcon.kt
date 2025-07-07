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
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

interface MarkerIcon {
    val scale: Float
    val anchor: Offset
    val iconSize: Float
    val infoAnchor: Offset

    fun getSize(): Size
    fun toBitmapIcon(): BitmapIcon
}
abstract class AbstractMarkerIcon : MarkerIcon {
    abstract override val scale: Float
    abstract override val anchor: Offset
    abstract override val iconSize: Float
    abstract override val infoAnchor: Offset
}

abstract class AndroidDrawableIcon : AbstractMarkerIcon() {

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


open class DefaultIcon(
    val fillColor: Color = Color.Red,
    val strokeColor: Color = Color.White,
    val strokeWidth: Dp = 1.dp,
    override val scale: Float = 1f,
    val label: String? = null,
    val labelTextColor: Color? = Color.Black,
    val labelTextSize: Dp = 10.dp,
) : AbstractMarkerIcon() {
    override val anchor: Offset = Offset(0.5f, 1f)
    override val iconSize: Float = 32f
    override val infoAnchor: Offset = Offset(0.5f, 0.25f)

    override fun getSize(): Size {
        return Size(iconSize, iconSize)
    }

    override fun hashCode(): Int {
        var result = fillColor.hashCode()
        result = 31 * result + strokeColor.hashCode()
        result = 31 * result + strokeWidth.hashCode()
        result = 31 * result + scale.hashCode()
        label?.let {
            result = 31 * result + it.hashCode()
            result = 31 * result + (labelTextColor?.hashCode() ?: 0)
            result = 31 * result + labelTextSize.hashCode()
        }
        return result
    }

    override fun toBitmapIcon(): BitmapIcon {
        val id = hashCode()
        BitmapIconCache.get(id)?.let {
            return it
        }

        val svgOriginalSize = 26f // SVGの元のviewBoxサイズ
        val size = iconSize * scale * ResourceProvider.density
        val margin = 1.0f

        val bitmap = createBitmap(size.toInt(), size.toInt())
        val canvas = Canvas(bitmap)

        val scaleFactor = ((size - margin * 2f) / svgOriginalSize).toFloat()

        val strokePath =
            Path().apply {
                // --- 最初のサブパス (外側の形状) ---
                // "m12 0" -> moveTo(12*s + offsetX, 0*s + offsetY)
                moveTo(12f + margin, 0f + margin) // (16f, 2f)

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

        val result = BitmapIcon(
            bitmap = bitmap,
            anchor = anchor,
            size = getSize(),
        )
        BitmapIconCache.put(id, result)
        return result
    }
}
//
// fun drawableToBitmap(drawable: Drawable, width: Int = 96, height: Int = 96): Bitmap {
//    val bmpWidth = drawable.intrinsicWidth.takeIf { it > 0 } ?: width
//    val bmpHeight = drawable.intrinsicHeight.takeIf { it > 0 } ?: height
//
//    val bitmap = createBitmap(bmpWidth, bmpHeight)
//    val canvas = Canvas(bitmap)
//    drawable.setBounds(0, 0, canvas.width, canvas.height)
//    drawable.draw(canvas)
//    return bitmap
// }
//
// //
// @Composable
// fun RememberDrawable(@DrawableRes resId: Int): Bitmap {
//    val context = LocalContext.current
//
//    val drawableResId = rememberSaveable { mutableStateOf(resId) }
//
//    return remember(drawableResId.value) {
//        val drawable = ContextCompat.getDrawable(context, drawableResId.value) ?:
//            throw IllegalArgumentException("Resource is not available")
//        drawableToBitmap(drawable)
//    }
// }
