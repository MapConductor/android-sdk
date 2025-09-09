package com.mapconductor.core.marker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.features.IGeoPoint
import java.io.Serializable

@Composable
fun MapViewScope.Marker(state: MarkerState) {
    LaunchedEffect(state.fingerPrint()) {
        markerFlow.value = markerFlow.value.filter { it.id != state.id } + state
    }
}

@Composable
fun MapViewScope.Marker(
    position: IGeoPoint,
    clickable: Boolean = true,
    draggable: Boolean = false,
    icon: MarkerIcon? = null,
    extra: Serializable? = null,
) {
    val state =
        MarkerState(
            position = position,
            extra = extra,
            clickable = clickable,
            draggable = draggable,
            icon = icon,
        )
    Marker(state)
}
