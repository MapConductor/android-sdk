package com.mapconductor.core.icons

import android.graphics.Color
import android.graphics.Path
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.mapconductor.core.marker.MarkerIcon

fun MarkerIcon.Companion.SquareMarker(
    fillColor: Int? = Color.RED,
    strokeWidth: Float? = 1f,
    scale: Float? = 2f,
): MarkerIcon {
    val scl = 1.5f  // 24 / 16 = 1.5
    val offsetX = (24f - 12f * scl) / 2f  // = 3f
    val offsetY = (24f - 16f * scl) / 2f  // = 0f
    fun sx(x: Float) = x * scl + offsetX
    fun sy(y: Float) = y * scl + offsetY

    val path = Path().apply {
        // スタート：左上角丸の終点（x=8, y=4）
        moveTo(sx(8f), sy(4f))

        // 上辺 → 右上角丸（始点: 8→14）
        lineTo(sx(14f), sy(4f))
        cubicTo(
            sx(14f + 2f), sy(4f),
            sx(18f), sy(4f),
            sx(18f), sy(6f)
        )

        // 右辺 → 右下角丸（始点: y=6→14）
        lineTo(sx(18f), sy(14f))
        cubicTo(
            sx(18f), sy(14f + 2f),
            sx(16f), sy(16f),
            sx(14f), sy(16f)
        )

        // 三角形右辺
        lineTo(sx(14f), sy(16f))

        // 三角形頂点
        lineTo(sx(12f), sy(20f))

        // 三角形左辺
        lineTo(sx(10f), sy(16f))

        // 下辺 → 左下角丸
        lineTo(sx(8f), sy(16f))
        cubicTo(
            sx(6f), sy(16f),
            sx(6f), sy(14f),
            sx(6f), sy(14f)
        )

        // 左辺 → 左上角丸
        lineTo(sx(6f), sy(6f))
        cubicTo(
            sx(6f), sy(4f),
            sx(8f), sy(4f),
            sx(8f), sy(4f)
        )

        close()
    }

    return MarkerIcon(
        outsideColor = fillColor,
        outsideWidth = strokeWidth,
        scale = scale,
        anchor = Offset(0.5f, 0.5f),
        size = Size(32f, 32f),
        infoAnchor = Offset(0.5f, 0.5f),
        outsidePath = path,
    )
}
