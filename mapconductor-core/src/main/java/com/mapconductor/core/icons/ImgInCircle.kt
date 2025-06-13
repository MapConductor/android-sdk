package com.mapconductor.core.icons

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.geometry.Size
import com.mapconductor.core.R
import com.mapconductor.core.marker.MarkerIcon

fun MarkerIcon.Companion.ImgInCircle(
    outsideColor: Int? = 0xFF008000.toInt(),  // 円の塗りつぶし色（緑）
    resId: Int? = R.drawable.def,
    strokeWidth: Float? = 2f,
): MarkerIcon {

    return MarkerIcon(
        outsideColor = outsideColor,      // 塗りつぶし色（黄色）
        outsideWidth = strokeWidth,
        scale = 1.5f,
        anchor = Offset(0.5f, 0.5f),
        size = Size(32f, 32f),
        infoAnchor = Offset(0.5f, 0.5f),
        outsidePath = null,
    )
}
