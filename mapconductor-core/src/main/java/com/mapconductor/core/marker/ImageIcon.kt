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
    drawable: Drawable,
    override val iconSize: Dp = Settings.Default.iconSize,
    override val scale: Float = 1.0f,
    override val anchor: Offset = Offset(0.5f, 0.5f),
    override val infoAnchor: Offset = Offset(0.5f, 0.5f),
    override val debug: Boolean = false,
) : AndroidDrawableIcon(
        drawable = drawable,
    ) {
    private fun getDrawableIdentity(): Any =
        when (drawable) {
            is BitmapDrawable -> {
                val bmp = drawable.bitmap
                if (bmp == null || bmp.isRecycled) {
                    "BMP_NULL_${drawable.hashCode()}"
                } else {
                    try {
                        val w = bmp.width
                        val h = bmp.height
                        val buffer = IntArray(w * h)
                        bmp.getPixels(buffer, 0, w, 0, 0, w, h)
                        // Combine dimensions and content for stability
                        "BMP_${w}x${h}_${buffer.contentHashCode()}"
                    } catch (e: Exception) {
                        "BMP_ERR_${drawable.hashCode()}"
                    }
                }
            }
            is ColorDrawable -> "COLOR_${drawable.color}"
            is GradientDrawable -> "GRADIENT_${drawable.hashCode()}"
            else -> "${drawable::class.java.name}_${drawable.hashCode()}"
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
        var result = getDrawableIdentity().hashCode()
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
