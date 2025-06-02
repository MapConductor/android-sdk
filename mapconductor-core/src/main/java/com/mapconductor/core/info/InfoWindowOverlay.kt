package com.mapconductor.core.info

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import java.util.UUID

@Composable
internal fun InfoWindowCompose(
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
                    (screenOffset.x - (anchor.x * size.width)).toInt(),
                    (screenOffset.y - (anchor.y * size.height)).toInt(),
                )
            },
    ) {
        content()
    }
}

class InfoBubbleState(
    val id: String = UUID.randomUUID().toString(),
) {
    internal val marker: MutableState<MarkerState?> = mutableStateOf(null)

    fun open(markerState: MarkerState) {
        this.marker.value = markerState
    }
    fun close() {
        this.marker.value = null
    }
}
data class InfoBubbleEntry(
    val state: InfoBubbleState,
    val content: @Composable () -> Unit,
)

val LocalInfoBubbleCollector = compositionLocalOf<MutableStateFlow<List<InfoBubbleEntry>>> {
    error("InfoBubble must be under <MapView />")
}
