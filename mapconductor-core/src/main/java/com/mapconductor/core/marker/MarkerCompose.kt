package com.mapconductor.core.marker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.features.GeoPoint
import android.os.Parcelable

@Composable
fun MapViewScope.Marker(state: MarkerState) {
    val rememberMarker = remember { state }
    SideEffect {
        markerFlow.value = markerFlow.value + rememberMarker
    }
}

@Composable
fun MapViewScope.Marker(
    position: GeoPoint,
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
