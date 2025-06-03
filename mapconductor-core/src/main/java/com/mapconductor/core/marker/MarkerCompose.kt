package com.mapconductor.core.marker

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapViewScope

open class MarkerScope

@Composable
fun MapViewScope.Marker(
    entry: MarkerEntry,
) {
    val rememberEntry = remember { entry }


    if (!allMarkerKeys.contains(rememberEntry.state.id)) {
        allMarkerKeys.add(rememberEntry.state.id)

        SideEffect {
            markerFlow.value = markerFlow.value + rememberEntry
        }
    }
}

@Composable
fun MapViewScope.Marker(
    state: MarkerState,
    onClick: OnMarkerClickHandler? = null,
) {
    val handlers = MarkerHandlers(
        onClick = onClick,
    )
    val entry = MarkerEntry(
        state = state,
        handlers = handlers,
    )
    Marker(entry)
}

@Composable
fun MapViewScope.Marker(
    position: GeoPoint,
    icon: MarkerIcon? = null,
    extra: Parcelable? = null,
    onClick: OnMarkerClickHandler? = null,
) {
    val state = MarkerState(
        position = position,
        extra = extra,
        icon = icon,
    )
    val handlers = MarkerHandlers(
        onClick = onClick,
    )
    val entry = MarkerEntry(
        state = state,
        handlers = handlers,
    )
    Marker(entry)
}