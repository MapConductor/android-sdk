package com.mapconductor.example.pages.rasterlayer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.raster.RasterLayerSource
import com.mapconductor.core.raster.RasterLayerState
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold

/**
 * `RasterHeaderInstrumentedTest` が使う計測ページ。
 *
 * `RasterLayerState` の `userAgent` / `extraHeaders` が**実際にタイル要求に載るか**を
 * プロバイダごとに確かめる。タイルの取得はプロバイダのネイティブ SDK が握っているので、
 * 送り出す側のコードを読んでも「載っているか」は分からない。受け取る側を立てて
 * 届いたヘッダをそのまま読む。
 *
 * タイルの配信元 URL は [probeTemplate] にテストが入れる。Android の計装テストは
 * アプリと**同じプロセス**で動くので、テスト側で立てたサーバの URL をそのまま渡せる。
 *
 * ページを別に用意しているのは、通常の `RasterLayerMapPage` は地理院タイルを見に行く
 * ためで、計測には使えないから。プロバイダの選択は `--es provider <key>` に従う
 * （[DemoMapPageScaffold] が見る既存の仕組み）。
 */
@Composable
fun RasterHeaderProbePage(onToggleSidebar: () -> Unit = {}) {
    val camera =
        remember {
            MapCameraPosition(
                position = GeoPoint(latitude = 35.681236, longitude = 139.767125),
                zoom = 8.0,
            )
        }
    val rasterLayerState =
        remember {
            RasterLayerState(
                source =
                    RasterLayerSource.UrlTemplate(
                        template = probeTemplate ?: "http://127.0.0.1:1/tiles/{z}/{x}/{y}.png",
                        tileSize = 256,
                    ),
                userAgent = probeUserAgent,
                extraHeaders = probeExtraHeaders,
                id = "raster_header_probe",
            )
        }

    var mapViewState by remember { mutableStateOf<MapViewStateInterface<*>?>(null) }

    DemoMapPageScaffold(
        menuItems = DefaultMapViewItems(camera),
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = { mapViewState = it },
    ) {
        RasterLayerMapComponent(
            mapViewState = mapViewState,
            rasterLayerState = rasterLayerState,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/** テストが立てた計測サーバの URL テンプレート。 */
var probeTemplate: String? = null

/** 送信を期待する User-Agent。 */
var probeUserAgent: String = RasterLayerState.DEFAULT_USER_AGENT

/** 送信を期待する追加ヘッダ。 */
var probeExtraHeaders: Map<String, String>? = null
