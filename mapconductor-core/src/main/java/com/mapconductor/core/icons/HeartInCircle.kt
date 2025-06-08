package com.mapconductor.core.icons

import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Path
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.mapconductor.core.marker.MarkerIcon
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Canvas
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp

fun MarkerIcon.Companion.HeartInCircle(
    fillColor: Int? = 0xFFD71D3B.toInt(),  // 円の塗りつぶし色（赤色）
    strokeColor: Int? = Color.WHITE,
    strokeWidth: Float? = 2f,
    scale: Float? = 2f,
): MarkerIcon {
    val centerX = 14f
    val centerY = 15f

    val path = Path().apply {
        // 黄色い円
        addCircle(14f, 14f, 12f, Path.Direction.CW)
    }

    // 白いハート
//    val scale = 20f / 128f  // 約 0.15625
    val heartPath = Path().apply {
//        moveTo(64f * scale, 120f * scale)
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
/*
        // ハートの先端部分
        moveTo(centerX, centerY + 6f)  // 先端部分（中心から少し下）

        // 左側のカーブ
        cubicTo(centerX - 8f, centerY + 4f, centerX - 8f, centerY - 8f, centerX, centerY - 6f)

        // 右側のカーブ（左右対称）
        cubicTo(centerX + 8f, centerY - 8f, centerX + 8f, centerY + 4f, centerX, centerY + 6f)

        close()  // パスを閉じる
*/
    }

    // 塗りつぶし色（黄色）で円を塗り、五芒星を白色で塗りつぶす
    return MarkerIcon(
        fillColor = fillColor,      // 塗りつぶし色（黄色）
        strokeColor = strokeColor,  // 星の色
        strokeWidth = strokeWidth,
        scale = 1.5f,
        anchor = Offset(0.5f, 0.5f),
        size = Size(32f, 32f),
        infoAnchor = Offset(0.5f, 0.5f),
        fillPath = path,
        strokePath = heartPath, // 円の外周部分と五芒星の外枠
    )
}
