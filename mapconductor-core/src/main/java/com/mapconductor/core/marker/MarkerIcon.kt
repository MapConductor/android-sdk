package com.mapconductor.core.marker

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

interface MarkerIconInterface {
    val scale: Float
    val anchor: Offset
    val iconSize: Dp
    val infoAnchor: Offset
    val debug: Boolean

    fun toBitmapIcon(): BitmapIcon
}

abstract class AbstractMarkerIcon : MarkerIconInterface {
    abstract override val scale: Float
    abstract override val anchor: Offset
    abstract override val iconSize: Dp
    abstract override val infoAnchor: Offset
    abstract override val debug: Boolean

    /**
     * デバッグ用の枠描画
     */
    protected fun drawDebugFrame(canvas: Canvas) {
        Paint()
            .apply {
                isAntiAlias = true
                strokeWidth = 1f
                color = Color.Black.toArgb()
                style = Paint.Style.STROKE
            }.also {
                canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), it)
            }
    }
}

abstract class AndroidDrawableIcon(
    val drawable: Drawable,
) : AbstractMarkerIcon() {
    protected fun toBitmap(
        drawable: Drawable,
        width: Int,
        height: Int,
    ): Bitmap {
        return if (drawable is BitmapDrawable && !debug) {
            drawable.bitmap.scale(width, height)
        } else {
            val bitmap = createBitmap(width, height)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            if (debug) {
                drawDebugFrame(canvas)
            }
            return bitmap
        }
    }
}
