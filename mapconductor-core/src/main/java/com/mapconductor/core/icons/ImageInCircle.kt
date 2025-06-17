package com.mapconductor.core.icons

import androidx.compose.ui.geometry.Offset
import com.mapconductor.core.marker.MarkerIcon
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

fun MarkerIcon.Companion.ImageInCircle(
    outsideColor: Int? = Color.WHITE,
    outsideStrokeColor: Int? =  Color.BLACK,
    image: Drawable,
    strokeWidth: Float? = 2f,
): MarkerIcon {

    val iconSizePx = 32f
    val circleRadius = 12f
    val circleCenter = Offset(iconSizePx / 2f, iconSizePx / 2f)

    // 円形Pathの作成
    val circlePath = android.graphics.Path().apply {
        addCircle(circleCenter.x, circleCenter.y, circleRadius, android.graphics.Path.Direction.CW)
    }

    return MarkerIcon(
        fillDrawable = image,
        outsideColor = outsideColor,
        outsideStrokeColor = outsideStrokeColor,
        outsideWidth = strokeWidth,
        outsidePath = circlePath,
    )
}
