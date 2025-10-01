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
import com.mapconductor.core.R
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.marker.AbstractMarkerIcon
import com.mapconductor.core.marker.BitmapIcon
import com.mapconductor.settings.MarkerIconSize
import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.Drawable

class RightTailInfoBubbleIcon(
    private val properties: IconProperties,
) : AbstractMarkerIcon() {
    data class IconProperties(
        val iconDrawable: Drawable,
        val label: String,
        val snippet: String,
        val fillColor: Color,
        val labelTextColor: Color,
        val scale: Float,
        val iconSize: Dp,
        val debug: Boolean,
    )

    constructor(
        iconDrawable: Drawable,
        label: String,
        snippet: String,
        fillColor: Color = Color.LightGray,
        labelTextColor: Color = Color.Yellow,
        scale: Float = 1f,
        iconSize: Dp = MarkerIconSize.Small,
        debug: Boolean = false,
    ) : this(
        IconProperties(
            iconDrawable,
            label,
            snippet,
            fillColor,
            labelTextColor,
            scale,
            iconSize,
            debug,
        ),
    )

    val iconDrawable: Drawable by properties::iconDrawable
    val label: String by properties::label
    val snippet: String by properties::snippet
    val fillColor: Color by properties::fillColor
    val labelTextColor: Color by properties::labelTextColor
    override val scale: Float by properties::scale
    override val iconSize: Dp by properties::iconSize
    override val debug: Boolean by properties::debug
    override val anchor: Offset = Offset(0.5f, 1.0f)
    override val infoAnchor: Offset = Offset(0.5f, 1.0f)

    fun copy(
        iconDrawable: Drawable = this.iconDrawable,
        label: String = this.label,
        snippet: String = this.snippet,
        fillColor: Color = this.fillColor,
        scale: Float = this.scale,
        iconSize: Dp,
    ): RightTailInfoBubbleIcon =
        RightTailInfoBubbleIcon(
            properties.copy(
                iconDrawable = iconDrawable,
                label = label,
                snippet = snippet,
                fillColor = fillColor,
                labelTextColor = labelTextColor,
                scale = scale,
                iconSize = iconSize,
            ),
        )

    fun copy(
        scale: Float,
        iconSize: Dp,
    ): RightTailInfoBubbleIcon = copy(scale = scale, iconSize = iconSize)

    override fun equals(other: Any?): Boolean = other is RightTailInfoBubbleIcon && properties == other.properties

    override fun hashCode(): Int = properties.hashCode()

    override fun toString(): String = "RightTailInfoBubbleIcon($properties)"

    override fun toBitmapIcon(): BitmapIcon {
        val id = "right_tail_info_bubble_icon${hashCode()}".hashCode()
        BitmapIconCache.get(id)?.let {
            return it
        }

        val drawableSize = ResourceProvider.dpToPx(iconSize.value * scale).toFloat()
        val drawableInnerPadding = drawableSize * 0.1f
        val contentMargin = drawableSize * 0.2f

        val labelPaint =
            Paint().apply {
                isAntiAlias = true
                color = labelTextColor.toArgb()
                textSize = drawableSize * 0.7f
            }
        val labelWidth = labelPaint.measureText(label)
        val labelHeight = labelPaint.fontMetrics.run { bottom - top }

        val snippetPaint =
            Paint().apply {
                isAntiAlias = true
                color = Color.Gray.toArgb()
                textSize = drawableSize * 0.4f
            }
        val snippetHeight = snippetPaint.fontMetrics.run { bottom - top }

        val canvasWidth = drawableSize + drawableInnerPadding + labelWidth + contentMargin * 2
        val canvasHeight =
            maxOf(
                drawableSize,
                labelHeight,
            ) + drawableInnerPadding + snippetHeight + drawableInnerPadding * 2
        val pointerWidth = canvasWidth / 9f
        val pointerHeight = canvasHeight / 8f

        val bitmap =
            createBitmap(
                canvasWidth.toInt(),
                (canvasHeight + pointerHeight).toInt(),
                Bitmap.Config.ARGB_8888,
            )
        val canvas = Canvas(bitmap)

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

        val backgroundPath =
            Path().apply {
                addRect(
                    0f, 0f, canvasWidth, canvasHeight,
                    Path.Direction.CW,
                )

                // 吹き出しの矢印
                moveTo(canvasWidth - pointerWidth * 2f, canvasHeight)
                lineTo(canvasWidth - pointerWidth, canvasHeight)
                lineTo(canvasWidth - pointerWidth * 1.5f, canvasHeight + pointerHeight)
                close()
            }

        canvas.drawPath(backgroundPath, backgroundPaint)

        iconDrawable.setBounds(
            contentMargin.toInt(),
            contentMargin.toInt(),
            (drawableInnerPadding + drawableSize).toInt(),
            (drawableInnerPadding + drawableSize).toInt(),
        )
        iconDrawable.draw(canvas)

        val labelX = contentMargin + drawableSize + drawableInnerPadding
        val labelY =
            contentMargin + drawableSize / 2f + labelHeight / 2f - labelPaint.fontMetrics.bottom
        canvas.drawText(label, labelX, labelY, labelPaint)

        canvas.drawText(snippet, contentMargin, canvasHeight - contentMargin, snippetPaint)

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
fun RightTailInfoBubbleIconPreview() {
    val context = LocalContext.current

    val icon =
        RightTailInfoBubbleIcon(
            iconDrawable = ContextCompat.getDrawable(context, R.drawable.default_marker)!!,
            label = "5時間37分",
            snippet = "304マイル",
            fillColor = Color.White,
            labelTextColor = Color.Black,
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
