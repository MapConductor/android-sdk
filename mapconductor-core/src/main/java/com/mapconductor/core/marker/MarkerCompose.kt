package com.mapconductor.core.marker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.features.GeoPoint
import android.os.Parcelable

@Composable
fun MapViewScope.Marker(entry: MarkerEntry) {
    SideEffect {
        markerFlow.value = markerFlow.value + entry
    }
}

@Composable
fun MapViewScope.Marker(
    state: MarkerState,
    onClick: OnMarkerClickHandler? = null,
) {
    val handlers =
        remember {
            MarkerHandlers(
                onClick = onClick,
            )
        }

    val entry =
        remember {
            MarkerEntry(
                state = state,
                handlers = handlers,
            )
        }

    Marker(entry)
}

@Composable
fun MapViewScope.Marker(
    position: GeoPoint,
    icon: MarkerIcon? = null,
    extra: Parcelable? = null,
    onClick: OnMarkerClickHandler? = null,
) {
    val state =
        MarkerState(
            position = position,
            extra = extra,
            icon = icon,
        )
    val handlers =
        MarkerHandlers(
            onClick = onClick,
        )
    val entry =
        MarkerEntry(
            state = state,
            handlers = handlers,
        )
    Marker(entry)
}
