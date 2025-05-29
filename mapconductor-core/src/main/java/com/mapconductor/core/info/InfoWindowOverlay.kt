package com.mapconductor.core.info

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.mapconductor.core.marker.MarkerState
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
internal fun InfoWindowCompose(
    centerOffset: Offset,
    screenOffset: Offset,
    anchor: Offset,
    content: @Composable () -> Unit
) {
    var size by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = Modifier
            .onGloballyPositioned {
                size = it.size
            }
            .offset {
                IntOffset(
                    (screenOffset.x - (anchor.x - 0.5) * size.width - centerOffset.x).toInt(),
                    (screenOffset.y - (anchor.y - 0.5) * size.height - centerOffset.y).toInt(),
                )
            },
    ) {
        content()
    }
}

data class InfoBubbleSpec(
    val state: MarkerState,
    val anchor: Offset = Offset(0.5f, 1f),
    val content: @Composable () -> Unit,
)

val LocalInfoBubbleCollector = compositionLocalOf<MutableStateFlow<List<InfoBubbleSpec>>> {
    error("InfoBubble must be under <MapView />")
}