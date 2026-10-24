package com.mapconductor.simplemapapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.maplibre.MapLibreDesign
import com.mapconductor.maplibre.MapLibreMapView
import com.mapconductor.maplibre.rememberMapLibreMapViewState

// コントロールは自分の状態を持ちません。ボタンが押されたことを伝えるだけで、
// それが何を意味するかを決めるのは画面の側です。
@Composable
fun DesignPanel(
    onToner: () -> Unit,
    onOsmBright: () -> Unit,
    onBasic: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(onClick = onToner) {
            Text("Toner")
        }

        // コンポーザブルそのものがスタイルです。FilledTonalButton、ElevatedButton、
        // OutlinedButton、TextButton が Material 3 の他の形です。
        // 地図の上では、背面に面を持つものが読みやすいままです。
        FilledTonalButton(onClick = onOsmBright) {
            Text("OSM Bright")
        }

        ElevatedButton(onClick = onBasic) {
            Text("Basic")
        }
    }
}

// 一つひとつに名前が付いたあとに残るもの — state と、それを誰が受け取るか。

// setContent から呼び出されているものと仮定しています（Activity がそうするように）。
// そしてこのプレビューはシミュレーターです。実機に近いだけで、実機ではありません。
@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    val mapViewState = rememberMapLibreMapViewState(
        mapDesign = MapLibreDesign.OsmBrightJa,
        cameraPosition = MapCameraPosition(
            position = GeoPoint(latitude = 35.0, longitude = 137.0),
            zoom = 6.0,
            bearing = 24.0,
            tilt = 45.0,
        ),
    )

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
        MapLibreMapView(
            state = mapViewState,
            modifier = Modifier.fillMaxSize(),
        )

        DesignPanel(
            // デザインは state のプロパティなので、ボタンから変えられます。
            // 新しいスタイル URL で地図が貼り直されます。
            onToner = {
                mapViewState.mapDesignType = MapLibreDesign.MapTilerTonerJa
                // この出力は、端末の下のコンソールに出ます。
                println("design -> ${mapViewState.mapDesignType.id}")
            },
            onOsmBright = {
                mapViewState.mapDesignType = MapLibreDesign.OsmBrightJa
            },
            onBasic = {
                mapViewState.mapDesignType = MapLibreDesign.MapTilerBasicJa
            },
        )
    }
}
