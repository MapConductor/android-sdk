package com.mapconductor.core.info

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mapconductor.core.map.MapViewScope

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
    state: InfoBubbleState,
    bubbleColor: Color = Color.Companion.White,
    borderColor: Color = Color.Companion.Black,
    contentPadding: Dp = 8.dp,
    cornerRadius: Dp = 4.dp,
    tailSize: Dp = 8.dp,
    content: @Composable () -> Unit,
) {
    val entry =
        remember {
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

            mutableStateOf(
                InfoBubbleEntry(
                    state = state,
                    content = wrapped,
                ),
            )
        }

    if (!allBubblesKeys.contains(state.id)) {
        SideEffect {
            bubbleFlow.value = bubbleFlow.value + entry.value
        }
    }
}
