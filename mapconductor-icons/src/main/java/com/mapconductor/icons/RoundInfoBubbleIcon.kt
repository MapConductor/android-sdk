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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.mapconductor.core.BitmapIconCache
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.marker.AbstractMarkerIcon
import com.mapconductor.core.marker.BitmapIcon
import com.mapconductor.settings.MarkerIconSize
import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics

class RoundInfoBubbleIcon(
    private val properties: IconProperties,
) : AbstractMarkerIcon() {
    data class IconProperties(
        val iconDrawable: Drawable,
        val label: String,
        val fillColor: Color,
        val scale: Float,
        val iconSize: Dp,
        val debug: Boolean,
    )

    constructor(
        iconDrawable: Drawable,
        label: String,
        fillColor: Color = Color.White,
        scale: Float = 1f,
        iconSize: Dp = MarkerIconSize.Small,
        debug: Boolean = false,
    ) : this(
        IconProperties(iconDrawable, label, fillColor, scale, iconSize, debug),
    )

    val iconDrawable: Drawable by properties::iconDrawable
    val label: String by properties::label
    val fillColor: Color by properties::fillColor
    override val scale: Float by properties::scale
    override val iconSize: Dp by properties::iconSize
    override val debug: Boolean by properties::debug
    override val anchor: Offset = Offset(0.5f, 1.0f)
    override val infoAnchor: Offset = Offset(0.5f, 1.0f)

    fun copy(
        iconDrawable: Drawable = this.iconDrawable,
        label: String = this.label,
        fillColor: Color = this.fillColor,
        scale: Float = this.scale,
        iconSize: Dp,
    ): RoundInfoBubbleIcon =
        RoundInfoBubbleIcon(
            properties.copy(
                iconDrawable = iconDrawable,
                label = label,
                fillColor = fillColor,
                scale = scale,
                iconSize = iconSize,
            ),
        )

    fun copy(
        scale: Float,
        iconSize: Dp,
    ): RoundInfoBubbleIcon = copy(scale = scale, iconSize = iconSize)

    override fun equals(other: Any?): Boolean = other is RoundInfoBubbleIcon && properties == other.properties

    override fun hashCode(): Int = properties.hashCode()

    override fun toString(): String = "RoundInfoBubbleIcon($properties)"

    override fun toBitmapIcon(): BitmapIcon {
        val id = "round_info_bubble_icon_${hashCode()}".hashCode()
        BitmapIconCache.get(id)?.let {
            return it
        }

        val drawableSize = ResourceProvider.dpToPx(iconSize.value * scale).toFloat()
        val innerPadding = drawableSize * 0.1f

        val textPaint =
            Paint().apply {
                isAntiAlias = true
                color = android.graphics.Color.BLACK
                textSize = drawableSize * 0.5f
            }
        val textWidth = textPaint.measureText(label)
        val textHeight = textPaint.fontMetrics.run { bottom - top }

        val canvasWidth = drawableSize + innerPadding + textWidth + innerPadding * 3
        val canvasHeight = maxOf(drawableSize, textHeight) + innerPadding * 2
        val pointerHeight = canvasHeight / 8f

        val bitmap =
            createBitmap(
                canvasWidth.toInt(),
                (canvasHeight + pointerHeight).toInt(),
                Bitmap.Config.ARGB_8888,
            )
        val canvas = Canvas(bitmap)
        // Set bitmap density to control map provider scaling
        ResourceProvider.getBitmapDensity().let { density ->
            bitmap.density = (density * DisplayMetrics.DENSITY_DEFAULT).toInt()
        }

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

        val backgroundPaint =
            Paint().apply {
                color = fillColor.toArgb()
                style = Paint.Style.FILL
                isAntiAlias = true
            }

        val path =
            Path().apply {
                addRoundRect(
                    RectF(0f, 0f, canvasWidth, canvasHeight),
                    canvasHeight / 2, canvasHeight / 2, Path.Direction.CW,
                )

                moveTo(canvasWidth / 2f - pointerHeight / 1f, canvasHeight)
                lineTo(canvasWidth / 2f + pointerHeight / 1f, canvasHeight)
                lineTo(canvasWidth / 2f, canvasHeight + pointerHeight)
                close()
            }

        canvas.drawPath(path, backgroundPaint)

        iconDrawable.setBounds(
            innerPadding.toInt(),
            innerPadding.toInt(),
            (innerPadding + drawableSize).toInt(),
            (innerPadding + drawableSize).toInt(),
        )
        iconDrawable.draw(canvas)

        val textX = innerPadding + drawableSize + innerPadding
        val textY =
            innerPadding + drawableSize / 2f + textHeight / 2f - textPaint.fontMetrics.bottom
        canvas.drawText(label, textX, textY, textPaint)

        val result =
            BitmapIcon(
                bitmap = bitmap, anchor = Offset(0.5f, 1.0f),
                size = Size(canvasWidth, canvasHeight + pointerHeight),
            )

        BitmapIconCache.put(id, result)
        return result
    }
}

@Preview
@Composable
fun RoundInfoBubbleIconPreview() {
    val context = LocalContext.current

    val icon =
        RoundInfoBubbleIcon(
            iconDrawable =
                ContextCompat.getDrawable(
                    context,
                    com.mapconductor.core.R.drawable.default_marker,
                )!!,
            label = "$197",
            fillColor = Color.White,
            scale = 1f,
            iconSize = MarkerIconSize.Small,
            debug = true,
        )
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
