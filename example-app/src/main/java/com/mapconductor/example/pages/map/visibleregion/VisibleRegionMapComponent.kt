package com.mapconductor.example.pages.map.visibleregion

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mapconductor.core.OnMapLoadedHandler
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.map.MapCameraPositionInterface
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.example.MapViewContainer

/**
 * 表示領域（VisibleRegion）を数値で見るサンプル。
 *
 * react-sdk の `VisibleRegionPage.tsx` と**同じ内容・同じ並び**にしてある。
 * 3 プラットフォームを並べて見比べるページなので、項目が違うと比較にならない。
 *
 * ## 地図にマーカーを置かない
 *
 * 以前は角の 6 点にマーカーを立てていたが、react と揃えるにあたり外した。
 * このページで見たいのは**数値**であって、マーカーがあると
 * 「マーカーの位置が正しいか」という別の話が混ざる。
 *
 * ## `onCameraMove` で受けること
 *
 * `onCameraMoveEnd` にすると**動かし終わるまで数値が変わらない**。
 * react は動かしている最中も更新されるので、そちらへ揃える。
 */
@Composable
fun VisibleRegionMapComponent(
    modifier: Modifier = Modifier,
    mapViewState: MapViewStateInterface<*>,
    onMapLoaded: OnMapLoadedHandler? = null,
    onCameraChanged: ((MapCameraPositionInterface) -> Unit)? = null,
) {
    var cameraPosition by remember { mutableStateOf<MapCameraPositionInterface?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        MapViewContainer(
            modifier = Modifier.fillMaxSize(),
            state = mapViewState,
            onMapLoaded = onMapLoaded,
            onCameraMove = { position ->
                cameraPosition = position
                onCameraChanged?.invoke(position)
            },
        )

        VisibleRegionInfoPanel(
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .widthIn(max = 350.dp),
            cameraPosition = cameraPosition,
        )
    }
}

@Composable
private fun VisibleRegionInfoPanel(
    modifier: Modifier = Modifier,
    cameraPosition: MapCameraPositionInterface?,
) {
    val visibleRegion = cameraPosition?.visibleRegion
    val bounds = visibleRegion?.bounds

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Visible Region",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            InfoLine("Move the map to update the current camera and visible region.")
            InfoLine("Center: ${formatPoint(cameraPosition?.position)}")
            InfoLine("Zoom: ${cameraPosition?.zoom?.let { "%.2f".format(it) } ?: UNAVAILABLE}")
            InfoLine("Bearing: ${cameraPosition?.bearing?.let { "%.1f".format(it) } ?: UNAVAILABLE} deg")
            InfoLine("Tilt: ${cameraPosition?.tilt?.let { "%.1f".format(it) } ?: UNAVAILABLE} deg")
            InfoLine("Bounds: ${bounds?.takeUnless { it.isEmpty }?.toUrlValue(5) ?: UNAVAILABLE}")
            InfoLine("Near Left: ${formatPoint(visibleRegion?.nearLeft)}")
            InfoLine("Near Right: ${formatPoint(visibleRegion?.nearRight)}")
            InfoLine("Far Left: ${formatPoint(visibleRegion?.farLeft)}")
            InfoLine("Far Right: ${formatPoint(visibleRegion?.farRight)}")
        }
    }
}

@Composable
private fun InfoLine(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 1.dp),
    )
}

/** 取得できないときの表示。react の `Unavailable` と揃える。 */
private const val UNAVAILABLE = "Unavailable"

/**
 * 座標の表示。`toUrlValue(5)` は react / ios と同じ書式なので、
 * 3 プラットフォームの数値をそのまま突き合わせられる。
 */
private fun formatPoint(point: GeoPointInterface?): String =
    point?.let { GeoPoint.from(it).toUrlValue(5) } ?: UNAVAILABLE
