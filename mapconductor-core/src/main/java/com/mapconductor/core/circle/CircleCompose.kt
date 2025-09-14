package com.mapconductor.core.circle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.features.IGeoPoint
import android.os.Parcelable

@Composable
fun MapViewScope.Circle(state: CircleState) {
    LaunchedEffect(state.fingerPrint()) {
        val newMap = circleFlow.value.toMutableMap()
        newMap.set(state.id, state)
        circleFlow.value = newMap
    }
}

@Composable
fun MapViewScope.Circle(
    center: IGeoPoint,
    radius: Double,
    strokeColor: Color = Color.Red,
    strokeWidth: Dp = 2.dp,
    fillColor: Color = Color.White.copy(alpha = 0.5f),
    extra: Parcelable? = null,
) {
    val state =
        CircleState(
            center = center,
            radiusMeters = radius,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth,
            fillColor = fillColor,
            extra = extra,
        )
    Circle(state)
}
