package com.mapconductor.core.icons


import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import android.graphics.Color
import android.graphics.Path
import android.graphics.drawable.Drawable
import com.mapconductor.core.marker.MarkerIcon

fun MarkerIcon.Companion.Default(
    fillColor: Int? = Color.RED,
    strokeColor: Int? = Color.WHITE,
    strokeWidth: Float? = 1f,
    scale: Float? = 2f,
    label: String? = null,
    labelTextColor: Int? = Color.BLACK,
    labelTextSizeLogical: Float? = 10f,
    fillDrawable: Drawable? = null,
    iconDrawable: Drawable? = null,
) : MarkerIcon {

    val scaleFactor = 28.0f / 24.0f
    val offsetX = 2.0f
    val offsetY = 2.0f

    val dmyPath = Path().apply {}
    val strokePath = Path().apply {

        // --- 最初のサブパス (外側の形状) ---
        // "m12 0" -> moveTo(12*s + offsetX, 0*s + offsetY)
        moveTo(12f * scaleFactor + offsetX, 0f * scaleFactor + offsetY) // (16f, 2f)

        // "c-4.4183 2.3685e-15 -8 3.5817-8 8" - relative cubicTo (all params scaled)
        rCubicTo(
            -4.4183f * scaleFactor,
            2.3685e-15f * scaleFactor,
            -8f * scaleFactor,
            3.5817f * scaleFactor,
            -8f * scaleFactor,
            8f * scaleFactor
        )
        // (-5.1546833f, 0f, -9.333333f, 4.17865f, -9.333333f, 9.333333f)

        // "0 1.421 0.3816 2.75 1.0312 3.906" - implicit relative cubicTo
        rCubicTo(
            0f * scaleFactor,
            1.421f * scaleFactor,
            0.3816f * scaleFactor,
            2.75f * scaleFactor,
            1.0312f * scaleFactor,
            3.906f * scaleFactor
        )
        // (0f, 1.6578333f, 0.4452f, 3.2083333f, 1.2030667f, 4.557f)

        // "0.1079 0.192 0.221 0.381 0.3438 0.563" - implicit relative cubicTo
        rCubicTo(
            0.1079f * scaleFactor,
            0.192f * scaleFactor,
            0.221f * scaleFactor,
            0.381f * scaleFactor,
            0.3438f * scaleFactor,
            0.563f * scaleFactor
        )
        // (0.12588333f, 0.224f, 0.25783333f, 0.4445f, 0.4011f, 0.6568333f)

        // "l6.625 11.531" - relative lineTo
        rLineTo(6.625f * scaleFactor, 11.531f * scaleFactor)
        // (7.7291665f, 13.452833f)

        // "6.625-11.531" - implicit relative lineTo
        rLineTo(6.625f * scaleFactor, -11.531f * scaleFactor)
        // (7.7291665f, -13.452833f)

        // "c0.102-0.151 0.19-0.311 0.281-0.469" - relative cubicTo
        rCubicTo(
            0.102f * scaleFactor,
            -0.151f * scaleFactor,
            0.19f * scaleFactor,
            -0.311f * scaleFactor,
            0.281f * scaleFactor,
            -0.469f * scaleFactor
        )
        // (0.119f, -0.17616667f, 0.22166666f, -0.36283332f, 0.32783332f, -0.54716665f)

        // "l0.063-0.094" - relative lineTo
        rLineTo(0.063f * scaleFactor, -0.094f * scaleFactor)
        // (0.0735f, -0.10966667f)

        // "c0.649-1.156 1.031-2.485 1.031-3.906" - relative cubicTo
        rCubicTo(
            0.649f * scaleFactor,
            -1.156f * scaleFactor,
            1.031f * scaleFactor,
            -2.485f * scaleFactor,
            1.031f * scaleFactor,
            -3.906f * scaleFactor
        )
        // (0.7571667f, -1.3486667f, 1.2028333f, -2.8991666f, 1.2028333f, -4.557f)

        // "0-4.4183-3.582-8-8-8" - implicit relative cubicTo
        rCubicTo(
            0f * scaleFactor,
            -4.4183f * scaleFactor,
            -3.582f * scaleFactor,
            -8f * scaleFactor,
            -8f * scaleFactor,
            -8f * scaleFactor
        )
        // (0f, -5.1546833f, -4.179f, -9.333333f, -9.333333f, -9.333333f)

        // "z" - closePath
        close() // 最初のサブパスを閉じる
    }

    return MarkerIcon(
        fillColor = fillColor,
        strokeColor = strokeColor,
        strokeWidth = strokeWidth,
        scale = scale,
        label = label?.substring(0, 0)?.toString() ?: "",
        labelTextColor = labelTextColor,
        labelTextSizeLogical = labelTextSizeLogical,
        fillDrawable = fillDrawable,
        iconDrawable = iconDrawable,
        anchor = Offset(0.5f, 1.0f),
        size = Size(32f, 32f),
        infoAnchor = Offset(0.5f, 0.5f),
        fillPath = strokePath,
        strokePath = dmyPath,
    )
}