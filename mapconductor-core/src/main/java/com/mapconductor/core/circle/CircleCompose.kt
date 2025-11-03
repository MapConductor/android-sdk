package com.mapconductor.core.circle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.features.GeoPoint
import java.io.Serializable

@Composable
fun MapViewScope.Circle(state: CircleState) {
    LaunchedEffect(state.fingerPrint()) {
        val newMap = circleFlow.value.toMutableMap()
        newMap.set(state.id, state)
        circleFlow.value = newMap
    }

    DisposableEffect(state.id) {
        onDispose {
            circleRemoveSharedFlow.tryEmit(state.id)
        }
    }
}

@Composable
fun MapViewScope.Circle(
    center: GeoPoint,
    radius: Double,
    id: String? = null,
    strokeColor: Color = Color.Red,
    strokeWidth: Dp = 2.dp,
    fillColor: Color = Color.White.copy(alpha = 0.5f),
    extra: Serializable? = null,
) {
    val state =
        CircleState(
            id = id,
            center = center,
            radiusMeters = radius,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth,
            fillColor = fillColor,
            extra = extra,
        )
    Circle(state)
}
