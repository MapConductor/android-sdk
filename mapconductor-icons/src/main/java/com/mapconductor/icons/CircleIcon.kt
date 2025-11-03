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
import com.mapconductor.core.BitmapIconCache
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.marker.AbstractMarkerIcon
import com.mapconductor.core.marker.BitmapIcon
import com.mapconductor.settings.Settings
import android.graphics.Canvas
import android.graphics.Paint
import android.util.DisplayMetrics

class CircleIcon(
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
    override val debug: Boolean by properties::debug
    override val anchor: Offset = Offset(0.0f, 0.5f)
    override val infoAnchor: Offset = Offset(0.5f, 0.5f)

    // data classのcopyを活用した独自copyメソッド
    fun copy(
        fillColor: Color = this.fillColor,
        strokeColor: Color = this.strokeColor,
        strokeWidth: Dp = this.strokeWidth,
        scale: Float = this.scale,
        iconSize: Dp = this.iconSize,
    ): CircleIcon =
        CircleIcon(
            properties.copy(
                fillColor = fillColor,
                strokeColor = strokeColor,
                strokeWidth = strokeWidth,
                scale = scale,
                iconSize = iconSize,
            ),
        )

    fun copy(
        scale: Float,
        iconSize: Dp,
    ): CircleIcon = copy(scale = scale, iconSize = iconSize)

    // equals, hashCode, toStringも委譲
    override fun equals(other: Any?): Boolean = other is CircleIcon && properties == other.properties

    override fun hashCode(): Int = properties.hashCode()

    override fun toString(): String = "CircleIcon($properties)"

    override fun toBitmapIcon(): BitmapIcon {
        val id = "circle_icon_${hashCode()}".hashCode()
        BitmapIconCache.get(id)?.let {
            return it
        }

        val canvasSize = ResourceProvider.dpToPx(iconSize.value * scale)

        val bitmap = createBitmap(canvasSize.toInt(), canvasSize.toInt())
        // Set bitmap density to control map provider scaling
        ResourceProvider.getBitmapDensity().let { density ->
            bitmap.density = (density * DisplayMetrics.DENSITY_DEFAULT).toInt()
        }
        val canvas = Canvas(bitmap)
        val centerX = canvas.width / 2.0f
        val centerY = canvas.height / 2.0f
        val overallDotRadius = centerX
        val strokeWidth = ResourceProvider.dpToPx(strokeWidth).toFloat()

        // Draw rectangle frame for debugging
        if (this.debug) {
            Paint()
                .apply {
                    isAntiAlias = true
                    this.strokeWidth = 1f
                    this.color = Color.Black.toArgb()
                    style = Paint.Style.STROKE
                }.also {
                    canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), it)
                }
        }

        Paint()
            .apply {
                color = fillColor.toArgb()
                style = Paint.Style.FILL
                isAntiAlias = true
            }.also {
                canvas.drawCircle(centerX, centerY, overallDotRadius - strokeWidth, it)
            }

        Paint()
            .apply {
                color = strokeColor.toArgb()
                style = Paint.Style.STROKE
                isAntiAlias = true
                this.strokeWidth = strokeWidth
            }.also {
                canvas.drawCircle(centerX, centerY, overallDotRadius - strokeWidth, it)
            }

        // Draw cross-lines for debugging
        if (this.debug) {
            Paint()
                .apply {
                    color = Color.Black.toArgb()
                    isAntiAlias = true
                    style = Paint.Style.STROKE
                    this.strokeWidth = strokeWidth
                }.also {
                    canvas.drawLine(centerX, 0f, centerX, canvas.height.toFloat(), it)
                    canvas.drawLine(0f, centerY, canvas.width.toFloat(), centerY, it)
                }
        }

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
private fun CircleIconPreview() {
    val icon = CircleIcon(debug = true)
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
