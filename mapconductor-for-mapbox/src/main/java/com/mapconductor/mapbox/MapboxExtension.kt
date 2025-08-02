package com.mapconductor.mapbox

import androidx.compose.ui.graphics.Color
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapconductor.core.marker.BitmapIcon
import android.graphics.Bitmap

internal fun BitmapIcon.toPointAnnotationOptions(): PointAnnotationOptions {
    val iconW = bitmap.width.toDouble()
    val iconH = bitmap.height.toDouble()

    // anchorX, anchorY は 0.0～1.0 の相対値とする
    val anchorPixelX = iconW * this.anchor.x
    val anchorPixelY = iconH * this.anchor.y

    // IconAnchor.BOTTOM は「画像下端の中央」が基準なので、そこからの差分を求める
    val baseX = iconW / 2.0 // center
    val baseY = iconH // bottom

    val offsetX = anchorPixelX - baseX
    val offsetY = anchorPixelY - baseY

    return PointAnnotationOptions()
        .withIconImage(bitmap.copy(Bitmap.Config.ARGB_8888, true))
        .withIconAnchor(IconAnchor.BOTTOM)
        .withIconOffset(listOf(offsetX, offsetY))
}

fun Color.toMapboxColorString(): String {
    val red = this.red * 255
    val green = this.green * 255
    val blue = this.blue * 255
    val alpha = this.alpha
    return "rgba($red, $green, $blue, $alpha)"
}
