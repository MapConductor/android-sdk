package com.mapconductor.core.marker

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

interface MarkerIcon {
    val scale: Float
    val anchor: Offset
    val iconSize: Dp
    val infoAnchor: Offset
    val debug: Boolean

    fun toBitmapIcon(): BitmapIcon
}

abstract class AbstractMarkerIcon : MarkerIcon {
    abstract override val scale: Float
    abstract override val anchor: Offset
    abstract override val iconSize: Dp
    abstract override val infoAnchor: Offset
    abstract override val debug: Boolean
}

abstract class AndroidDrawableIcon(
    val drawable: Drawable,
) : AbstractMarkerIcon() {
    protected fun toBitmap(
        drawable: Drawable,
        width: Int,
        height: Int,
    ): Bitmap {
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
