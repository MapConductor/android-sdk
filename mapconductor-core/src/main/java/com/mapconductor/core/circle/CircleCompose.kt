package com.mapconductor.core.circle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.features.GeoPointInterface
import java.io.Serializable

@Composable
fun MapViewScope.Circle(state: CircleState) {
    val collector = LocalCircleCollector.current
    LaunchedEffect(state) {
        collector.add(state)
    }

    DisposableEffect(state.id) {
        onDispose {
            collector.remove(state.id)
        }
    }
}

@Composable
fun MapViewScope.Circle(
    center: GeoPointInterface,
    radiusMeters: Double,
    id: String? = null,
    strokeColor: Color = Color.Red,
    strokeWidth: Dp = 2.dp,
    fillColor: Color = Color.White.copy(alpha = 0.5f),
    zIndex: Int? = null,
    extra: Serializable? = null,
    onClick: OnCircleEventHandler? = null,
) {
    val state =
        CircleState(
            id = id,
            center = center,
            radiusMeters = radiusMeters,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth,
            fillColor = fillColor,
            zIndex = zIndex,
            extra = extra,
            onClick = onClick,
        )
    Circle(state)
}
