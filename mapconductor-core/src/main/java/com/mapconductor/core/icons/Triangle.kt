package com.mapconductor.core.icons

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.mapconductor.core.marker.MarkerIcon
import android.graphics.Path

fun MarkerIcon.Companion.Triangle(
    outsideColor: Int? = 0xFF008000.toInt(), // 円の塗りつぶし色（緑）
    strokeWidth: Float? = 2f,
    triangleHeight: Float? = 24f,
    triangleWidth: Float? = 24f,
): MarkerIcon {
    // 下向き三角
    val trianglePath =
        Path().apply {
            moveTo(0f, 0f) // 左上
            lineTo(triangleWidth!!, 0f) // 右上
            lineTo((triangleWidth / 2), triangleHeight!!) // 下中央
            close() // 始点に戻って閉じる
        }

    // 塗りつぶし色（黄色）で円を塗り、五芒星を白色で塗りつぶす
    return MarkerIcon(
        outsideColor = outsideColor, // 塗りつぶし色（黄色）
        outsideWidth = strokeWidth,
        scale = 1.5f,
        anchor = Offset(0.5f, 0.5f),
        size = Size(32f, 32f),
        infoAnchor = Offset(0.5f, 0.5f),
        outsidePath = trianglePath,
    )
}
