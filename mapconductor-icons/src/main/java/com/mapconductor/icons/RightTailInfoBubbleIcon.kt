package com.mapconductor.icons

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.core.content.ContextCompat
import com.mapconductor.core.R
import com.mapconductor.core.marker.AbstractMarkerIcon
import com.mapconductor.core.marker.BitmapIcon
import com.mapconductor.settings.MarkerIconSize
import android.graphics.drawable.Drawable

class RightTailInfoWindow(
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
        IconProperties(iconDrawable, label, snippet, fillColor, labelTextColor, scale, iconSize, debug)
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
    ): RightTailInfoWindow = RightTailInfoWindow(
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
    ): RightTailInfoWindow = copy(scale = scale, iconSize = iconSize)

    override fun equals(other: Any?): Boolean = other is RightTailInfoWindow && properties == other.properties

    override fun hashCode(): Int = properties.hashCode()

    override fun toString(): String = "RightTailInfoWindow($properties)"

    override fun toBitmapIcon(): BitmapIcon {
        return TODO("ビットマップを返却")
    }
}

@Preview
@Composable
fun RightTailInfoWindowPreview() {
    val context = LocalContext.current

    val icon = RightTailInfoWindow(
        properties = RightTailInfoWindow.IconProperties(
            iconDrawable = ContextCompat.getDrawable(context, R.drawable.default_marker)!!,
            label = "5時間37分",
            snippet = "304マイル",
            fillColor = Color.LightGray,
            labelTextColor = Color.Yellow,
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
