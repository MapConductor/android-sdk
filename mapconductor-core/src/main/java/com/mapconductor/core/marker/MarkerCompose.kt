package com.mapconductor.core.marker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.features.GeoPointInterface
import java.io.Serializable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

/**
 * Efficiently add many markers without creating one Composable per marker.
 *
 * This avoids large composition overhead (thousands of LaunchedEffects/DisposableEffects)
 * by performing batched add/remove in a single effect.
 */
@Composable
fun MapViewScope.Markers(states: List<MarkerState>) {
    val collector = LocalMarkerCollector.current
    val prevIdsState = remember { mutableStateOf<Set<String>>(emptySet()) }

    LaunchedEffect(states) {
        // For very large marker sets, avoid per-marker SharedFlow emits which can backpressure and
        // block rendering; instead publish the whole map in one StateFlow update.
        withContext(Dispatchers.Default) {
            val nextIds = states.asSequence().map { it.id }.toSet()
            prevIdsState.value = nextIds
            collector.flow.value = states.associateBy { it.id }.toMutableMap()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            // Clear all markers on dispose in one shot.
            collector.flow.value = mutableMapOf()
            prevIdsState.value = emptySet()
        }
    }
}

@Composable
fun MapViewScope.Marker(
    position: GeoPointInterface,
    id: String? = null,
    zIndex: Int? = null,
    clickable: Boolean = true,
    draggable: Boolean = false,
    icon: MarkerIconInterface? = null,
    animation: MarkerAnimation? = null,
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
            animation = animation,
            zIndex = zIndex,
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
