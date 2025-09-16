package com.mapconductor.icons

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.mapconductor.core.BitmapIconCache
import com.mapconductor.core.marker.AbstractMarkerIcon
import com.mapconductor.core.marker.BitmapIcon
import com.mapconductor.settings.MarkerIconSize
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

        return TODO("BitMapを返却")
    }
}
