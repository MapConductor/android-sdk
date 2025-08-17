package com.mapconductor.core.groundimage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.circle.Circle
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.marker.MarkerState
import android.graphics.drawable.Drawable
import android.os.Parcelable
import kotlinx.coroutines.flow.filter

@Composable
fun MapViewScope.GroundImage(state: GroundImageState) {
    val rememberState = remember(state.fingerPrint()) { state }
    SideEffect {
        groundImageFlow.value = groundImageFlow.value.filter { it.id != state.id } + rememberState
    }
}

@Composable
fun MapViewScope.SouthWest(state: MarkerState) {
    val rememberState = remember(state.fingerPrint()) { state }
    SideEffect {
        markerFlow.value = markerFlow.value.filter { it.id != state.id } + rememberState
    }
}

@Composable
fun MapViewScope.NorthEast(state: MarkerState) {
    val rememberState = remember(state.fingerPrint()) { state }
    SideEffect {
        markerFlow.value = markerFlow.value.filter { it.id != state.id } + rememberState
    }
}

@Composable
fun MapViewScope.GroundImage(
    bounds: GeoRectBounds,
    image: Drawable,
    opacity: Float = 0.5f,
    id: String? = null,
    extra: Parcelable? = null,
) {
    val state =
        GroundImageState(
            bounds = bounds,
            image = image,
            opacity = opacity,
            id = id,
            extra = extra,
        )
    GroundImage(state)
}
