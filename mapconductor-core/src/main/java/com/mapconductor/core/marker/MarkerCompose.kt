package com.mapconductor.core.marker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.features.GeoPoint
import java.io.Serializable

@Composable
fun MapViewScope.Marker(state: MarkerState) {
    val collector = LocalMarkerCollector.current
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
fun MapViewScope.Marker(
    position: GeoPoint,
    id: String? = null,
    clickable: Boolean = true,
    draggable: Boolean = false,
    icon: MarkerIcon? = null,
    extra: Serializable? = null,
    onClick: OnMarkerEventHandler? = null,
    onDragStart: OnMarkerEventHandler? = null,
    onDrag: OnMarkerEventHandler? = null,
    onDragEnd: OnMarkerEventHandler? = null,
    onAnimateStart: OnMarkerEventHandler? = null,
    onAnimateEnd: OnMarkerEventHandler? = null,
) {
    val state =
        MarkerState(
            id = id,
            position = position,
            extra = extra,
            clickable = clickable,
            draggable = draggable,
            icon = icon,
            onClick = onClick,
            onDragStart = onDragStart,
            onDrag = onDrag,
            onDragEnd = onDragEnd,
            onAnimateStart = onAnimateStart,
            onAnimateEnd = onAnimateEnd,
        )
    Marker(state)
}
