package com.mapconductor.core.marker

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp
import com.mapconductor.core.BitmapIconCache
import com.mapconductor.core.ResourceProvider
import com.mapconductor.settings.Settings
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable

class ImageIcon(
    image: Drawable,
    override val iconSize: Dp = Settings.Default.iconSize,
    override val scale: Float = 1.0f,
    override val anchor: Offset = Offset(0.5f, 0.5f),
    override val infoAnchor: Offset = Offset(0.5f, 0.5f),
    override val debug: Boolean = false,
) : AndroidDrawableIcon(
        drawable = image,
    ) {
    private fun getDrawableIdentity(): Int =
        when (drawable) {
            is BitmapDrawable -> {
                val bmp = drawable.bitmap
                if (bmp == null || bmp.isRecycled) {
                    0
                } else {
                    // Fast identity: generationId changes when bitmap pixels change.
                    // Avoid hashing bitmap contents, which is extremely expensive and makes large
                    // marker sets slow to add.
                    var result = 17
                    result = 31 * result + bmp.width
                    result = 31 * result + bmp.height
                    result = 31 * result + bmp.generationId
                    result = 31 * result + System.identityHashCode(bmp)
                    result
                }
            }
            is ColorDrawable -> drawable.color
            is GradientDrawable -> System.identityHashCode(drawable.constantState ?: drawable)
            else -> System.identityHashCode(drawable.constantState ?: drawable)
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false
        other as ImageIcon
        return getDrawableIdentity() == other.getDrawableIdentity() &&
            iconSize == other.iconSize &&
            scale == other.scale &&
            anchor == other.anchor &&
            infoAnchor == other.infoAnchor &&
            debug == other.debug
    }

    override fun hashCode(): Int {
        var result = getDrawableIdentity()
        result = 31 * result + iconSize.hashCode()
        result = 31 * result + scale.hashCode()
        result = 31 * result + anchor.hashCode()
        result = 31 * result + infoAnchor.hashCode()
        result = 31 * result + debug.hashCode()
        return result
    }

    override fun toBitmapIcon(): BitmapIcon {
        val id = hashCode()
        BitmapIconCache.get(id)?.let {
            return it
        }

        val scaledSize = ResourceProvider.dpToPxForBitmap(iconSize.value) * scale

        val bitmap =
            this.toBitmap(
                drawable = drawable,
                width = scaledSize.toInt(),
                height = scaledSize.toInt(),
            )
        // Set bitmap density based on override (e.g., 1.0 for MapLibre to prevent auto-scaling)
        ResourceProvider.getBitmapDensity().let { density ->
            bitmap.density = (density * android.util.DisplayMetrics.DENSITY_DEFAULT).toInt()
        }

        val result =
            BitmapIcon(
                bitmap = bitmap,
                anchor = anchor,
                size = Size(scaledSize.toFloat(), scaledSize.toFloat()),
            )
        BitmapIconCache.put(id, result)
        return result
    }
}
