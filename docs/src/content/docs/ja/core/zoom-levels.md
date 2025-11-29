---
title: "Zoom Levels（ズームレベル）"
---

MapConductor はズームレベルを使用して地図表示のスケールと詳細度を制御します。ズームレベルシステムはおおよそ Google Maps の規約に従いますが、基盤となる実装の違いにより地図SDK間で若干異なる場合があります。

## ズームレベルの理解

MapConductor のズームレベルは `Double` 値として表され、通常 0 から 21 の範囲です:

- **高い数値** = よりズームイン（より詳細、より小さいエリア）
- **低い数値** = よりズームアウト（より少ない詳細、より大きいエリア）
- **小数値** = 整数レベル間のスムーズな補間

```kotlin
// 異なるズームレベルの例
val worldView = 2.0        // 大陸を見る
val countryView = 6.0      // 国全体を見る
val cityView = 10.0        // 都市を見る
val streetView = 15.0      // 通りを見る
val buildingView = 18.0    // 個別の建物を見る
```

## ズームレベルリファレンス

### グローバルスケール (0-5)

```kotlin
// 世界と大陸レベル
val worldLevel = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(0.0, 0.0),
    zoom = 2.0  // 大陸と海を表示
)

val continentLevel = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(39.8283, -98.5795), // アメリカの中心
    zoom = 4.0  // 大陸全体を表示
)
```

**ユースケース:**
- グローバルデータの可視化
- 大陸の概要
- フライトトラッキングアプリケーション
- 世界の天気図

### 地域スケール (6-9)

```kotlin
// 国と州レベル
val countryLevel = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(39.8283, -98.5795),
    zoom = 6.0  // 国全体を表示
)

val stateLevel = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(36.7783, -119.4179), // カリフォルニアの中心
    zoom = 8.0  // 州または大きな地域を表示
)
```

**ユースケース:**
- 国の統計
- 州レベルのデータ
- 地域の天気
- 交通ネットワーク

### メトロポリタンスケール (10-12)

```kotlin
// 都市と大都市圏レベル
val metropolitanLevel = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194), // サンフランシスコ
    zoom = 10.0  // 大都市圏を表示
)

val cityLevel = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    zoom = 12.0  // 都市中心部と郊外を表示
)
```

**ユースケース:**
- 都市計画
- 公共交通機関
- 配達ゾーン
- 地区の境界

### 近隣スケール (13-15)

```kotlin
// 地区と近隣レベル
val districtLevel = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    zoom = 13.0  // 地区を表示
)

val neighborhoodLevel = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    zoom = 15.0  // 近隣と主要な通りを表示
)
```

**ユースケース:**
- 地元のビジネスディレクトリ
- 学区
- 近隣サービス
- 不動産検索

### ストリートスケール (16-18)

```kotlin
// ストリートとブロックレベル
val streetLevel = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    zoom = 16.0  // ストリートレイアウトを表示
)

val detailedStreetLevel = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    zoom = 18.0  // 個別の通りと小さな建物を表示
)
```

**ユースケース:**
- ナビゲーションアプリケーション
- 住所検索
- ストリートレベルサービス
- 徒歩案内

### 建物スケール (19-21)

```kotlin
// 建物と詳細レベル
val buildingLevel = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    zoom = 19.0  // 個別の建物を表示
)

val maximumDetail = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    zoom = 21.0  // 最大ズームレベル
)
```

**ユースケース:**
- 建物検査
- 資産管理
- 屋内マッピング
- 詳細な測量

## 地図SDKのバリエーション

MapConductor は地図SDK間でズームレベルを正規化しますが、微妙な違いがある可能性があります:

### Google Maps
- 参照スケールに厳密に従う
- スムーズな小数ズームサポート
- 範囲: 0-21

### Mapbox
- Google Maps に類似
- 優れた小数ズーム補間
- 範囲: 0-22（スタイルによって異なる場合があります）

### HERE Maps
- 小さな変動を伴う同等のスケール
- 良好な小数ズームサポート
- 範囲: 0-20

### ArcGIS
- 同等のズームで異なる詳細レベルを持つ可能性があります
- シーンビュー（3D）は異なる動作をする可能性があります
- 範囲はベースマップによって異なります

## 動的なズーム選択

### コンテンツベースのズーム

```kotlin
@Composable
fun ContentBasedZoom() {
    val markers = remember {
        listOf(
            GeoPointImpl.fromLatLong(37.7749, -122.4194),
            GeoPointImpl.fromLatLong(37.7849, -122.4094),
            GeoPointImpl.fromLatLong(37.7949, -122.3994)
        )
    }

    // すべてのマーカーを含む境界を計算
    val bounds = GeoRectBounds()
    markers.forEach { bounds.extend(it) }

    // コンテンツに適切なズームを選択
    val appropriateZoom = when {
        bounds.isEmpty -> 15.0  // 単一点のデフォルト
        else -> {
            val span = bounds.toSpan()
            when {
                span?.latitude ?: 0.0 > 10.0 -> 4.0   // 大きいエリア
                span?.latitude ?: 0.0 > 1.0 -> 8.0    // 中程度のエリア
                span?.latitude ?: 0.0 > 0.1 -> 12.0   // 小さいエリア
                span?.latitude ?: 0.0 > 0.01 -> 16.0  // 非常に小さいエリア
                else -> 18.0                          // 最小エリア
            }
        }
    }

    val cameraPosition = MapCameraPositionImpl(
        position = bounds.center ?: markers.first(),
        zoom = appropriateZoom
    )

    // MapView を GoogleMapView、MapboxMapView などの選択した地図SDKに置き換えてください
    MapView(
        state = mapViewState,
        cameraPosition = cameraPosition
    ) {
        markers.forEach { position ->
            Marker(
                position = position,
                icon = DefaultIcon(fillColor = Color.Blue)
            )
        }
    }
}
```

### ユーザー制御ズーム

```kotlin
@Composable
fun ZoomControls() {
    var currentZoom by remember { mutableStateOf(15.0) }
    val center = GeoPointImpl.fromLatLong(37.7749, -122.4194)

    Column {
        // ズームレベル表示
        Text("ズームレベル: ${String.format("%.1f", currentZoom)}")

        // ズームコントロール
        Row {
            Button(
                onClick = { currentZoom = (currentZoom + 1).coerceAtMost(21.0) }
            ) {
                Text("+")
            }

            Button(
                onClick = { currentZoom = (currentZoom - 1).coerceAtLeast(0.0) }
            ) {
                Text("-")
            }
        }

        // プリセットズームボタン
        Row {
            Button(onClick = { currentZoom = 12.0 }) { Text("都市") }
            Button(onClick = { currentZoom = 15.0 }) { Text("ストリート") }
            Button(onClick = { currentZoom = 18.0 }) { Text("建物") }
        }

        // MapView を GoogleMapView、MapboxMapView などの選択した地図SDKに置き換えてください
        MapView(
            state = mapViewState,
            cameraPosition = MapCameraPositionImpl(
                position = center,
                zoom = currentZoom
            )
        ) {
            Marker(
                position = center,
                icon = DefaultIcon(fillColor = Color.Red)
            )
        }
    }
}
```

## 適応的ズーム戦略

### パフォーマンスベースのズーム

```kotlin
@Composable
fun PerformanceAdaptiveZoom() {
    val markerCount = 1000
    var displayedMarkers by remember { mutableStateOf<List<GeoPoint>>(emptyList()) }

    val cameraPosition = MapCameraPositionImpl(
        position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        zoom = 12.0
    )

    LaunchedEffect(cameraPosition) {
        // ズームレベルに基づいてマーカー表示を適応
        displayedMarkers = when {
            cameraPosition.zoom < 8.0 -> {
                // 非常に低いズーム: 主要なマーカーのみを表示
                allMarkers.filter { it.importance > 0.8 }
            }
            cameraPosition.zoom < 12.0 -> {
                // 中程度のズーム: 重要なマーカーを表示
                allMarkers.filter { it.importance > 0.5 }
            }
            cameraPosition.zoom < 16.0 -> {
                // 高いズーム: ほとんどのマーカーを表示
                allMarkers.filter { it.importance > 0.2 }
            }
            else -> {
                // 最大ズーム: すべてのマーカーを表示
                allMarkers
            }
        }
    }

    // MapView を GoogleMapView、MapboxMapView などの選択した地図SDKに置き換えてください
    MapView(
        state = mapViewState,
        cameraPosition = cameraPosition
    ) {
        displayedMarkers.forEach { marker ->
            Marker(
                position = marker,
                icon = DefaultIcon(
                    scale = when {
                        cameraPosition.zoom < 10.0 -> 1.2f
                        cameraPosition.zoom < 15.0 -> 1.0f
                        else -> 0.8f
                    }
                )
            )
        }
    }
}
```

### コンテキスト認識ズーム

```kotlin
@Composable
fun ContextAwareZoom() {
    val userLocation = GeoPointImpl.fromLatLong(37.7749, -122.4194)
    val searchResults = remember { /* your search results */ }

    // コンテキストに基づいてズームを選択
    val contextualZoom = when {
        searchResults.isEmpty() -> 15.0  // ユーザー位置フォーカス
        searchResults.size == 1 -> 16.0  // 単一結果
        searchResults.size < 5 -> 14.0   // 少数の結果
        else -> {
            // すべての結果に適切なズームを計算
            val bounds = GeoRectBounds()
            searchResults.forEach { bounds.extend(it.location) }
            calculateZoomForBounds(bounds)
        }
    }

    val cameraPosition = MapCameraPositionImpl(
        position = if (searchResults.isEmpty()) userLocation else bounds.center!!,
        zoom = contextualZoom
    )

    // MapView を GoogleMapView、MapboxMapView などの選択した地図SDKに置き換えてください
    MapView(
        state = mapViewState,
        cameraPosition = cameraPosition
    ) {
        // ユーザー位置
        Marker(
            position = userLocation,
            icon = DefaultIcon(fillColor = Color.Blue, label = "あなた")
        )

        // 検索結果
        searchResults.forEach { result ->
            Marker(
                position = result.location,
                icon = DefaultIcon(fillColor = Color.Red)
            )
        }
    }
}
```

## ベストプラクティス

### ズームレベルの選択

1. **コンテンツ駆動**: 表示したいものに基づいてズームを選択
2. **ユーザーコンテキスト**: ユーザーの現在のタスクと位置を考慮
3. **パフォーマンス**: 高いズームレベルはより多くの詳細レンダリングが必要
4. **地図SDKの制限**: すべてのターゲット地図SDKでテスト

### スムーズな遷移

```kotlin
// スムーズなズームアニメーション
fun animateToZoom(targetZoom: Double) {
    val animator = ValueAnimator.ofFloat(currentZoom.toFloat(), targetZoom.toFloat())
    animator.duration = 1000
    animator.addUpdateListener { animation ->
        val newZoom = animation.animatedValue as Float
        updateCameraPosition(currentPosition.copy(zoom = newZoom.toDouble()))
    }
    animator.start()
}
```

### レスポンシブデザイン

```kotlin
@Composable
fun ResponsiveZoom() {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    // 画面サイズに合わせてズームを調整
    val baseZoom = if (isTablet) 14.0 else 16.0

    // MapView を GoogleMapView、MapboxMapView などの選択した地図SDKに置き換えてください
    MapView(
        state = mapViewState,
        cameraPosition = MapCameraPositionImpl(
            position = center,
            zoom = baseZoom
        )
    ) {
        // コンテンツ
    }
}
```

## 一般的な落とし穴

1. **地図SDKの不一致**: すべてのターゲット地図SDKでズームレベルをテスト
2. **パフォーマンスへの影響**: 高いズームレベルはレンダリングコストが増加
3. **小数ズーム**: すべての地図SDKが小数ズームを同様に処理するわけではありません
4. **最小/最大**: 地図SDK固有のズーム制限を尊重
5. **ジェスチャーの競合**: ユーザーのズームジェスチャー vs プログラマティックズームを考慮

## ズームレベルのテスト

```kotlin
@Composable
fun ZoomLevelTester() {
    val testZoomLevels = listOf(0.0, 5.0, 10.0, 15.0, 20.0)
    var currentTestIndex by remember { mutableStateOf(0) }

    Column {
        Text("テスト中のズームレベル: ${testZoomLevels[currentTestIndex]}")

        Button(
            onClick = {
                currentTestIndex = (currentTestIndex + 1) % testZoomLevels.size
            }
        ) {
            Text("次のズームレベル")
        }

        // MapView を GoogleMapView、MapboxMapView などの選択した地図SDKに置き換えてください
        MapView(
            state = mapViewState,
            cameraPosition = MapCameraPositionImpl(
                position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                zoom = testZoomLevels[currentTestIndex]
            )
        ) {
            Marker(
                position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                icon = DefaultIcon(fillColor = Color.Red)
            )
        }
    }
}
```

ズームレベルを理解し適切に利用することは、ユーザーのニーズに適切な詳細レベルを提供する効果的なマッピングアプリケーションを作成するために重要です。
