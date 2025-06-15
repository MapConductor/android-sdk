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
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
internal fun InfoWindowCompose(
    positionOffset: Offset, // マーカーのposition
    iconSize: Size, // アイコンのサイズ
    iconOffset: Offset, // アイコンと地図が接続するポイント (0.0 - 1.0)
    infoAnchorOffset: Offset, // アイコンと吹き出しが接続するポイント
    tailOffset: Offset, // 吹き出し側で、アイコンと接続するポイント (0.0 - 1.0)
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var size by remember { mutableStateOf(IntSize.Zero) }

    val x =
        positionOffset.x +
            (-tailOffset.x * size.width) + // tailOffset.x = 0.5のとき、吹き出しの中央
            (-iconOffset.x * iconSize.width) + // iconOffset.x = 0.5のとき、アイコンの中央
            (infoAnchorOffset.x * iconSize.width) // infoAnchorOffset.x = 0.5のとき、アイコンの中央

    val y =
        positionOffset.y +
            (-tailOffset.y * size.height) + // tailOffset.y = 1.0 のとき、吹き出しの下部
            (-iconOffset.y * iconSize.height) + // iconOffset.x = 0.5のとき、アイコンの中央
            (-infoAnchorOffset.y * iconSize.height) // infoAnchorOffset.x = 0.5のとき、アイコンの中央

    Box(
        modifier =
            modifier
                .onGloballyPositioned {
                    size = it.size
                }.offset {
                    IntOffset(x.toInt(), y.toInt())
                },
    ) {
        content()
    }
}

class InfoBubbleState(
    val id: String = UUID.randomUUID().toString(),
    val tailOffset: Offset = Offset(0.5f, 1.0f),
) {
    //    internal val _marker: MutableState<MarkerState?> = mutableStateOf(null)
    var marker by mutableStateOf<MarkerState?>(null)
        private set

    fun open(markerState: MarkerState) {
        this.marker = markerState
    }

    fun close() {
        this.marker = null
    }
}

data class InfoBubbleEntry(
    val state: InfoBubbleState,
    val content: @Composable () -> Unit,
)

val LocalInfoBubbleCollector =
    compositionLocalOf<MutableStateFlow<List<InfoBubbleEntry>>> {
        error("InfoBubble must be under <MapView />")
    }
