package com.mapconductor.core.icons

import android.graphics.Color
import android.graphics.Path
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.mapconductor.core.marker.MarkerIcon
import kotlin.math.cos
import kotlin.math.sin

fun MarkerIcon.Companion.StarInCircle(
    fillColor: Int? = 0xFFFFD800.toInt(),  // 円の塗りつぶし色（黄色）
    strokeColor: Int? = Color.WHITE,
    strokeWidth: Float? = 2f,
): MarkerIcon {
    val path = Path().apply {
        // 黄色い円
        addCircle(14f, 14f, 12f, Path.Direction.CW)
    }

    // 白い五芒星の Path（中心: 14,14, 外半径: 10f）
    fun createWhiteStarPath(): Path {
        val outerRadius = 7f
        val innerRadius = outerRadius * 0.5f
/* 黄金比版
        val innerRadius = outerRadius * sin(Math.toRadians(18.0)).toFloat() /
                sin(Math.toRadians(54.0)).toFloat() // 幾何学的な比率
*/

        val centerX = 14f
        val centerY = 14f
        val path = Path()

        val startAngle = -90.0 // 上方向を起点に

        for (i in 0 until 10) {
            val angleDeg = startAngle + i * 36.0 // 360 / 10
            val angleRad = Math.toRadians(angleDeg)
            val r = if (i % 2 == 0) outerRadius else innerRadius
            val x = centerX + r * cos(angleRad).toFloat()
            val y = centerY + r * sin(angleRad).toFloat()
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        path.close()
        return path
    }

    val starFillPath = createWhiteStarPath()

    return MarkerIcon(
        outsideColor = fillColor,      // 塗りつぶし色（黄色）
        insideColor = strokeColor,  // 星の色
        outsideWidth = strokeWidth,
        scale = 1.5f,
        anchor = Offset(0.5f, 0.5f),
        size = Size(32f, 32f),
        infoAnchor = Offset(0.5f, 0.5f),
        outsidePath = path,
        insidePath = starFillPath, // 円の外周部分と五芒星の外枠
    )
}
