package com.mapconductor.core.info

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mapconductor.core.marker.MarkerScope
import com.mapconductor.core.marker.MarkerState

@Composable
fun MarkerScope.InfoAnchor(
    props: MarkerState,
    anchor: Offset = Offset(0.5f, 1f),
    content: @Composable () -> Unit,
) {
    val bubble = remember { InfoBubbleSpec(props, anchor, content) }
    SideEffect {
        if (!bubbleCollector.value.contains(bubble)) {
            bubbleCollector.value = bubbleCollector.value + bubble
        }
    }
}

@Composable
fun MarkerScope.InfoBubble(
    markerState: MarkerState,
    modifier: Modifier = Modifier.Companion,
    bubbleColor: Color = Color.Companion.White,
    borderColor: Color = Color.Companion.Black,
    contentPadding: Dp = 16.dp,
    cornerRadius: Dp = 8.dp,
    tailSize: Dp = 16.dp,
    content: @Composable () -> Unit,
) {
    InfoAnchor(
        props = markerState,
    ) {
        DrawInfoBubble(
            modifier = modifier,
            bubbleColor = bubbleColor,
            borderColor = borderColor,
            contentPadding = contentPadding,
            cornerRadius = cornerRadius,
            tailSize = tailSize,
            content = content,
        )
    }
}
