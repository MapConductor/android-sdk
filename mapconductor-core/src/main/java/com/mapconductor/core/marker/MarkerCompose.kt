package com.mapconductor.core.marker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.features.GeoPoint
import android.os.Parcelable

@Composable
fun MapViewScope.Marker(state: MarkerState) {
    SideEffect {
        markerFlow.value = markerFlow.value + state
    }
}

@Composable
fun MapViewScope.Marker(
    position: GeoPoint,
    icon: MarkerIcon? = null,
    extra: Parcelable? = null,
) {
    val state =
        remember {
            MarkerState(
                position = position,
                extra = extra,
                icon = icon,
            )
        }
    Marker(state)
}
