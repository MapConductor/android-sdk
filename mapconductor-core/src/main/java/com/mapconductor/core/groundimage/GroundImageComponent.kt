package com.mapconductor.core.groundimage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.features.GeoRectBounds
import android.graphics.drawable.Drawable
import android.os.Parcelable

@Composable
fun MapViewScope.GroundImage(state: GroundImageState) {
    LaunchedEffect(state.fingerPrint()) {
        val newMap = groundImageFlow.value.toMutableMap()
        newMap.set(state.id, state)
        groundImageFlow.value = newMap
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
