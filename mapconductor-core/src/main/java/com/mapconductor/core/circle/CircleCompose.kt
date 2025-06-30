package com.mapconductor.core.circle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.features.IGeoPoint

@Composable
fun MapViewScope.Circle(state: CircleState) {

}

@Composable
fun MapViewScope.Circle(
    center: IGeoPoint,
    radius: Int,
    strokeColor: Color = Color.Red,
    strokeWidth: Float = 2.0f,
    fillColor: Color = Color.White.copy(alpha = 0.5f),
) {
    val state =
        remember {
            CircleState(
                center = center,
                radius = radius,
                strokeColor = strokeColor.toArgb(),
                strokeWidth = strokeWidth,
                fillColor = fillColor.toArgb(),
            )
        }
    Circle(state)
}
