package com.mapconductor.core.groundimage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.features.GeoRectBounds
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
