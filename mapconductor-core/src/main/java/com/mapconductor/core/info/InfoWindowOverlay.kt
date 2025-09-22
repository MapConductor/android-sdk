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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.mapconductor.core.marker.MarkerState
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
internal fun InfoBubbleOverlay(
    positionOffset: Offset, // マーカーのposition
    iconSize: Size, // アイコンのサイズ
    iconOffset: Offset, // アイコンと地図が接続するポイント (0.0 - 1.0)
    infoAnchorOffset: Offset, // アイコンと吹き出しが接続するポイント
    tailOffset: Offset, // 吹き出し側で、アイコンと接続するポイント (0.0 - 1.0)
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var infoWndSize by remember { mutableStateOf(IntSize.Zero) }

    val x =
        positionOffset.x +
            (-tailOffset.x * infoWndSize.width) + // tailOffset.x = 0.5のとき、吹き出しの中央
            ((0.5 - iconOffset.x) * iconSize.width) + // iconOffset.x = 0.5のとき、アイコンの中央とMarker.positionが一致
            ((infoAnchorOffset.x - 0.5) * iconSize.width) // infoAnchorOffset.x = 0.5のとき、アイコンの中央にInfoBubbleを表示

    val y =
        positionOffset.y +
            (-tailOffset.y * infoWndSize.height) + // tailOffset.y = 1.0 のとき、吹き出しの下部
            ((0.5 - iconOffset.y) * iconSize.height) + // iconOffset.y = 0.5のとき、アイコンの中央とMarker.positionが一致
            ((infoAnchorOffset.y - 0.5) * iconSize.height) // infoAnchorOffset.y = 0.5のとき、アイコンの中央にInfoBubbleを表示

    Box(
        modifier =
            modifier
                .onGloballyPositioned {
                    infoWndSize = it.size
                }.offset {
                    IntOffset(x.toInt(), y.toInt())
                },
    ) {
        content()
    }
}

data class InfoBubbleEntry(
    val marker: MarkerState,
    val tailOffset: Offset = Offset(0.5f, 1.0f),
    val content: @Composable () -> Unit,
)

val LocalInfoBubbleCollector =
    compositionLocalOf<MutableStateFlow<MutableMap<String, InfoBubbleEntry>>> {
        error("InfoBubble must be under <MapView />")
    }
