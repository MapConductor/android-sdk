package com.mapconductor.core.marker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.features.IGeoPoint
import java.io.Serializable
import kotlinx.coroutines.launch

@Composable
fun MapViewScope.Marker(state: MarkerState) {
    DisposableEffect(state.fingerPrint()) {
        overflowScope.launch {
            markerAddSharedFlow.emit(state)
        }

        onDispose {
            val newMap = bubbleFlow.value.toMutableMap()
            newMap.remove(state.id)
            bubbleFlow.value = newMap
        }
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
