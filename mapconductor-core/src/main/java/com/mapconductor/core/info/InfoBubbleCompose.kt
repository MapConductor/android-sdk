package com.mapconductor.core.info

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.marker.MarkerState

// @Composable
// fun MapViewScope.InfoAnchor(
//    props: MarkerState,
//    anchor: Offset = Offset(0.5f, 1f),
//    content: @Composable () -> Unit,
// ) {
//    val bubble = remember { InfoBubbleSpec(props, anchor, content) }
//    SideEffect {
//        selectedInfo.value = bubble
//    }
// }

@Composable
fun MapViewScope.InfoBubble(
    marker: MarkerState,
    bubbleColor: Color = Color.Companion.White,
    borderColor: Color = Color.Companion.Black,
    contentPadding: Dp = 8.dp,
    cornerRadius: Dp = 4.dp,
    tailSize: Dp = 8.dp,
    content: @Composable () -> Unit,
) {
    val wrapped: @Composable () -> Unit = {
        DrawInfoBubble(
            modifier = Modifier,
            bubbleColor = bubbleColor,
            borderColor = borderColor,
            contentPadding = contentPadding,
            cornerRadius = cornerRadius,
            tailSize = tailSize,
            content = content,
        )
    }

    val entry =
        InfoBubbleEntry(
            marker = marker,
            content = wrapped,
        )

    DisposableEffect(marker) {
        val newMap = bubbleFlow.value.toMutableMap()
        newMap.set(entry.marker.id, entry)
        bubbleFlow.value = newMap

        onDispose {
            val newMap = bubbleFlow.value.toMutableMap()
            newMap.remove(entry.marker.id)
            bubbleFlow.value = newMap
        }
    }
}
