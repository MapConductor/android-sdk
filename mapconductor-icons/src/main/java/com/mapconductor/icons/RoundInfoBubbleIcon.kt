package com.mapconductor.icons

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
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
        IconProperties(iconDrawable, label, fillColor, scale, iconSize, debug)
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
    ): RoundInfoBubbleIcon = RoundInfoBubbleIcon(
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

        val iconSize = ResourceProvider.dpToPx(iconSize.value * scale).toFloat()
        val iconInnerPadding = iconSize * 0.1f

        val textPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.BLACK
            textSize = iconSize * 0.5f
        }
        val textWidth = textPaint.measureText(label)
        val textHeight = textPaint.fontMetrics.run { bottom - top }

        val width = iconSize + iconInnerPadding + textWidth + iconInnerPadding * 3
        val height = maxOf(iconSize, textHeight) + iconInnerPadding * 2
        val pointerHeight = height /8f

        val bitmap = createBitmap(
            width.toInt(), (height + pointerHeight).toInt(), Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)

        val bgPaint = Paint().apply {
            color = fillColor.toArgb()
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val rectLeft = 0f
        val rectTop = 0f
        val rectRight = width
        val rectBottom = height

        val path = Path().apply {
            addRoundRect(
                RectF(rectLeft, rectTop, rectRight, rectBottom), height / 2, height / 2, Path.Direction.CW
            )

            moveTo(width / 2f - pointerHeight / 1f, rectBottom)
            lineTo(width / 2f + pointerHeight / 1f, rectBottom)
            lineTo(width / 2f, rectBottom + pointerHeight)
            close()
        }

        canvas.drawPath(path, bgPaint)

        val iconTop = iconInnerPadding
        val iconLeft = iconInnerPadding
        iconDrawable.setBounds(
            iconLeft.toInt(), iconTop.toInt(), (iconLeft + iconSize).toInt(), (iconTop + iconSize).toInt()
        )
        iconDrawable.draw(canvas)

        val textX = iconLeft + iconSize + iconInnerPadding
        val textY = iconTop + iconSize / 2f + textHeight / 2f - textPaint.fontMetrics.bottom
        canvas.drawText(label, textX, textY, textPaint)

        val result = BitmapIcon(
            bitmap = bitmap, anchor = Offset(0.5f, 1.0f),
            size = Size(width, height + pointerHeight)
        )

        BitmapIconCache.put(id, result)
        return result
    }
}

@Preview
@Composable
fun RoundInfoBubbleIconPreview() {
    val context = LocalContext.current

    val icon = RoundInfoBubbleIcon(
        properties = RoundInfoBubbleIcon.IconProperties(
            iconDrawable = ContextCompat.getDrawable(context, com.mapconductor.core.R.drawable.default_marker)!!,
            label = "$197",
            fillColor = Color.White,
            scale = 1f,
            iconSize = MarkerIconSize.Small,
            debug = false,
        )
    )
    val bitmapIcon = remember(icon) { icon.toBitmapIcon() }
    val imageBitmap = remember(bitmapIcon) { bitmapIcon.bitmap.asImageBitmap() }

    Image(
        bitmap = imageBitmap,
        contentDescription = null,
    )
}
