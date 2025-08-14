package com.mapconductor.core.marker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.features.IGeoPoint
import android.os.Parcelable

@Composable
fun MapViewScope.Marker(state: MarkerState) {
    val rememberMarker = remember(state.id) { state }
    SideEffect {
        markerFlow.value = markerFlow.value.filter { it.id != rememberMarker.id } + rememberMarker
    }
}

@Composable
fun MapViewScope.Marker(
    position: IGeoPoint,
    clickable: Boolean = true,
    draggable: Boolean = false,
    icon: MarkerIcon? = null,
    extra: Parcelable? = null,
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
