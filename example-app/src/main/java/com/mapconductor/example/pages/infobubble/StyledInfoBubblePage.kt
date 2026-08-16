package com.mapconductor.example.pages.infobubble

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mapconductor.compose.info.InfoBubble
import com.mapconductor.compose.marker.Marker
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.marker.DefaultMarkerIcon
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.example.MapViewContainer
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold

/**
 * react の StyledInfoBubblePage.tsx / ios の StyledInfoBubblePage.swift と同一仕様:
 * マーカー 1 個と常時表示の InfoBubble を置き、パネルの 8 色スウォッチ 4 行
 * （バブル塗り / バブル枠線 / 文字 / マーカー）と 2 本のスライダー
 * （枠線幅・マーカースケール 0.5〜2.0、0.25 刻み）でスタイルを組み替える。
 *
 * palette は 4 行で共有する 8 色。白と黒を含めておくと
 * 塗り＝白 / 文字＝黒の既定も同じ列で選べる。
 */

private val palette =
    listOf(
        Color(0xFFFFFFFF),
        Color(0xFF111827),
        Color(0xFFEF4444),
        Color(0xFFF97316),
        Color(0xFFEAB308),
        Color(0xFF22C55E),
        Color(0xFF3B82F6),
        Color(0xFFA855F7),
    )

private val startPosition = GeoPoint.fromLatLong(35.6812, 139.7671)

@Composable
private fun SwatchRow(
    label: String,
    selected: Color,
    onSelect: (Color) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(64.dp),
        )
        palette.forEach { color ->
            Box(
                modifier =
                    Modifier
                        .padding(end = 6.dp)
                        .size(22.dp)
                        .border(
                            width = if (selected == color) 2.dp else 1.dp,
                            color = if (selected == color) Color(0xFF2563EB) else Color(0x40111827),
                            shape = CircleShape,
                        ).padding(3.dp)
                        .background(color, CircleShape)
                        .clickable { onSelect(color) },
            )
        }
    }
}

@Composable
fun StyledInfoBubblePage(onToggleSidebar: () -> Unit = {}) {
    val initCameraPosition =
        remember {
            MapCameraPosition(
                position = startPosition,
                zoom = 14.0,
            )
        }
    var mapViewState by remember { mutableStateOf<MapViewStateInterface<Any>?>(null) }

    var fillColor by remember { mutableStateOf(Color(0xFFFFFFFF)) }
    var strokeColor by remember { mutableStateOf(Color(0xFF111827)) }
    var fontColor by remember { mutableStateOf(Color(0xFF111827)) }
    var markerColor by remember { mutableStateOf(Color(0xFFEF4444)) }
    var strokeWidth by remember { mutableFloatStateOf(2.0f) }
    var markerScale by remember { mutableFloatStateOf(1.0f) }

    val marker =
        remember {
            MarkerState(
                id = "styled-bubble-marker",
                position = startPosition,
                icon = DefaultMarkerIcon(fillColor = Color(0xFFEF4444)),
            )
        }

    LaunchedEffect(markerColor, markerScale) {
        marker.icon = DefaultMarkerIcon(fillColor = markerColor, scale = markerScale)
    }

    DemoMapPageScaffold(
        menuItems = DefaultMapViewItems(initCameraPosition),
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = { state ->
            @Suppress("UNCHECKED_CAST")
            mapViewState = state as MapViewStateInterface<Any>
        },
    ) { paddingValues ->
        mapViewState?.let {
            MapViewContainer(
                modifier = Modifier.fillMaxSize(),
                state = mapViewState,
            ) {
                Marker(marker)
                // InfoBubble はスタイルとマーカーアイコンを登録時に焼き込むので、
                // 変更のたびに key で登録し直して最新の見た目・配置に揃える。
                key(fillColor, strokeColor, fontColor, strokeWidth, markerScale) {
                    InfoBubble(
                        marker = marker,
                        bubbleColor = fillColor,
                        borderColor = strokeColor,
                        borderWidth = strokeWidth.dp,
                        cornerRadius = 6.dp,
                        contentPadding = 10.dp,
                    ) {
                        Text(
                            text = "Custom Styled Bubble",
                            color = fontColor,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }

        // react / ios と同じ左下配置
        Card(
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        bottom = paddingValues.calculateBottomPadding() + 16.dp,
                        start = 16.dp,
                    ),
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                SwatchRow(label = "Fill", selected = fillColor) { fillColor = it }
                SwatchRow(label = "Stroke", selected = strokeColor) { strokeColor = it }
                SwatchRow(label = "Font", selected = fontColor) { fontColor = it }
                SwatchRow(label = "Marker", selected = markerColor) { markerColor = it }
                Text(
                    text = "Stroke Width: %.2f".format(strokeWidth),
                    style = MaterialTheme.typography.bodySmall,
                )
                Slider(
                    value = strokeWidth,
                    onValueChange = { strokeWidth = it },
                    valueRange = 0.5f..2.0f,
                    steps = 5,
                )
                Text(
                    text = "Marker Scale: %.2f".format(markerScale),
                    style = MaterialTheme.typography.bodySmall,
                )
                Slider(
                    value = markerScale,
                    onValueChange = { markerScale = it },
                    valueRange = 0.5f..2.0f,
                    steps = 5,
                )
            }
        }
    }
}
