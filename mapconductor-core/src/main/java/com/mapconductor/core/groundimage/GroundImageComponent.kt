package com.mapconductor.core.groundimage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.features.GeoRectBounds
import java.io.Serializable
import android.graphics.drawable.Drawable

@Composable
fun MapViewScope.GroundImage(state: GroundImageState) {
    val collector = LocalGroundImageCollector.current
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
fun MapViewScope.GroundImage(
    bounds: GeoRectBounds,
    image: Drawable,
    opacity: Float = 0.5f,
    tileSize: Int = GroundImageTileProvider.DEFAULT_TILE_SIZE,
    id: String? = null,
    extra: Serializable? = null,
    onClick: OnGroundImageEventHandler? = null,
) {
    val state =
        GroundImageState(
            bounds = bounds,
            image = image,
            opacity = opacity,
            tileSize = tileSize,
            id = id,
            extra = extra,
            onClick = onClick,
        )
    GroundImage(state)
}
