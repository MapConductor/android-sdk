package com.mapconductor.core.marker

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp
import com.mapconductor.core.ResourceProvider
import com.mapconductor.settings.Settings
import android.graphics.drawable.Drawable

class ImageIcon(
    drawable: Drawable,
    override val iconSize: Dp = Settings.Default.iconSize,
    override val scale: Float = 1.0f,
    override val anchor: Offset = Offset(0.5f, 0.5f),
    override val infoAnchor: Offset = Offset(0.5f, 0.5f),
    override val debug: Boolean = false,
) : AndroidDrawableIcon(
        drawable = drawable,
    ) {
    override fun toBitmapIcon(): BitmapIcon {
        val scaledSize = ResourceProvider.dpToPx(iconSize.value) * scale

        val bitmap =
            this.toBitmap(
                drawable = drawable,
                width = scaledSize.toInt(),
                height = scaledSize.toInt(),
            )

        return BitmapIcon(
            bitmap = bitmap,
            anchor = anchor,
            size = Size(scaledSize.toFloat(), scaledSize.toFloat()),
        )
    }
}
