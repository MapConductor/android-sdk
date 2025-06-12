package com.mapconductor.core.icons

import android.graphics.Color
import android.graphics.Path
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.mapconductor.core.marker.MarkerIcon

fun MarkerIcon.Companion.HeartInCircle(
    fillColor: Int? = 0xFFD71D3B.toInt(),  // 円の塗りつぶし色（赤色）
    strokeColor: Int? = Color.WHITE,
    strokeWidth: Float? = 2f,
): MarkerIcon {
    val centerX = 14f
    val centerY = 15f

    val circlePath = Path().apply {
        // 黄色い円
        addCircle(14f, 14f, 12f, Path.Direction.CW)
    }

    // 白いハート
    val heartPath = Path().apply {
        moveTo(centerX + 0f, centerY + 5f)  // 先端部分（中心から少し下）

        cubicTo(
            centerX - 9.63f, centerY - 2f,
            centerX - 5.69f, centerY - 8.56f,
            centerX + 0f, centerY - 4.19f
        )

        cubicTo(
            centerX + 5.69f, centerY - 8.56f,
            centerX + 9.63f, centerY - 2f,
            centerX + 0f, centerY + 5f
        )

        close()
    }

    return MarkerIcon(
        outsideColor = fillColor,      // 塗りつぶし色（黄色）
        insideColor = strokeColor,  // 星の色
        outsideWidth = strokeWidth,
        scale = 1.5f,
        anchor = Offset(0.5f, 0.5f),
        size = Size(32f, 32f),
        infoAnchor = Offset(0.5f, 0.5f),
        outsidePath = circlePath,
        insidePath = heartPath, // 円の外周部分と五芒星の外枠
    )
}
