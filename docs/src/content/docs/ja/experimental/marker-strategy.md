---
title: "マーカー戦略（実験的）"
---

`mapconductor-marker-strategy` モジュールは、大規模なデータセットでのパフォーマンスとユーザーエクスペリエンスを最適化するための高度なマーカーレンダリング戦略を提供します。この実験的モジュールは、異なるユースケースとパフォーマンス要件に合わせた複数のレンダリングアプローチを提供します。

> **⚠️ 実験的モジュール**: このモジュールは実験的であり、API が変更される可能性があります。本番環境での使用には注意してください。

## 概要

マーカー戦略モジュールは、基本的なマーカー表示を超えた洗練されたレンダリング戦略を提供します:
- **ビューポートベースのレンダリング**: 現在のビューポートに表示されているマーカーのみをレンダリング
- **動的追加/削除**: ビューポートの変更に応じて効率的にマーカーを管理
- **空間最適化**: 大規模なデータセットのための高度な空間インデックス
- **リモートデータ統合**: サーバーサイドのマーカーデータのサポート
- **クラスタリングサポート**: パフォーマンス向上のために近くのマーカーをグループ化

## インストール

`build.gradle` にマーカー戦略モジュールを追加します:

```kotlin
dependencies {
    implementation "com.mapconductor:marker-strategy"

    // 必須: Core モジュール
    implementation "com.mapconductor:mapconductor-bom:$version"
    // 必須: Core モジュール
    implementation "com.mapconductor:core"

    // 地図プロバイダを選択
    implementation "com.mapconductor:for-googlemaps"
}
```

## コア戦略

### DefaultMarkerStrategy

追加/削除操作を効率的に処理する Google Maps と ArcGIS プロバイダに最適:

```kotlin
import com.mapconductor.marker.strategy.DefaultMarkerStrategy

val defaultStrategy = DefaultMarkerStrategy<GoogleMapActualMarker>(
    expandMargin = 0.2,  // 20% ビューポート拡張
    semaphore = Semaphore(1),
    geocell = HexGeocellImpl.defaultGeocell()
)
```

#### 主な機能
- **動的追加/削除**: ビューポートに入るマーカーを追加し、離れるマーカーを削除
- **ビューポート拡張**: 表示領域の少し外側のマーカーを事前読み込み
- **メモリ効率**: 表示されているマーカーのみをメモリに保持
- **スムーズなスクロール**: マップ移動中のポップイン/ポップアウトを削減

### SimpleMarkerStrategy

小規模なデータセットまたは異なるパフォーマンス特性を持つプロバイダ向けの軽量戦略:

```kotlin
import com.mapconductor.marker.strategy.SimpleMarkerStrategy

val simpleStrategy = SimpleMarkerStrategy<MapboxActualMarker>(
    expandMargin = 0.15,
    geocell = HexGeocellImpl.defaultGeocell()
)
```

#### 主な機能
- **簡略化されたロジック**: より複雑でないビューポート管理
- **低オーバーヘッド**: 最小限の計算オーバーヘッド
- **Mapbox に適している**: よりシンプルなマーカー管理を好むプロバイダ向けに最適化

### SpatialMarkerStrategy

空間クラスタリングと最適化を備えた高度な戦略:

```kotlin
import com.mapconductor.marker.strategy.SpatialMarkerStrategy

val spatialStrategy = SpatialMarkerStrategy<HereActualMarker>(
    clusteringEnabled = true,
    clusterRadius = 100.0,      // 100メートルのクラスタリング半径
    maxMarkersPerCluster = 10,  // クラスター内の最大マーカー数
    geocell = HexGeocellImpl.defaultGeocell()
)
```

#### 主な機能
- **空間クラスタリング**: 近くのマーカーをクラスターにグループ化
- **密度管理**: 密集したエリアでの視覚的な混雑を軽減
- **パフォーマンススケーリング**: 非常に大きなデータセットを効率的に処理
- **カスタマイズ可能なクラスタリング**: 設定可能なクラスタリングパラメータ

## 基本的な使用方法

### デフォルト戦略の設定

```kotlin
@Composable
fun DefaultStrategyExample() {
    val mapViewState = rememberGoogleMapViewState()

    val markerStrategy = remember {
        DefaultMarkerStrategy<GoogleMapActualMarker>(
            expandMargin = 0.2
        )
    }

    // マップコントローラで戦略を設定
    LaunchedEffect(mapViewState) {
        // 戦略の設定は地図プロバイダの実装に依存します
        // これは通常、地図プロバイダのマーカーコントローラによって処理されます
    }

    // GoogleMapsView、MapboxMapView など、選択した地図プロバイダに置き換えてください
    GoogleMapsView(state = mapViewState) {
        // マーカーは戦略によって管理されます
        // 戦略を通じてプログラムでマーカーを追加します
    }
}
```

### 戦略へのマーカーの追加

```kotlin
@Composable
fun StrategyMarkerManagement() {
    val markerStrategy = remember {
        DefaultMarkerStrategy<GoogleMapActualMarker>()
    }

    LaunchedEffect(Unit) {
        // 戦略のマネージャーにマーカーを追加
        val markers = loadMarkerData() // マーカーデータ

        markers.forEach { markerData ->
            val entity = MarkerEntity(
                state = MarkerState(
                    id = markerData.id,
                    position = markerData.position,
                    icon = DefaultIcon(fillColor = markerData.color)
                )
            )

            // 戦略のマーカーマネージャーに登録
            markerStrategy.markerManager.registerEntity(entity)
        }
    }

    // GoogleMapsView、MapboxMapView など、選択した地図プロバイダに置き換えてください
    GoogleMapsView(state = mapViewState) {
        // 戦略がマーカーレンダリングを自動的に処理します
    }
}
```

## 高度な使用方法

### 動的マーカー読み込み

```kotlin
@Composable
fun DynamicLoadingExample() {
    val mapViewState = rememberGoogleMapViewState()
    var currentBounds by remember { mutableStateOf<GeoRectBounds?>(null) }
    var loadedMarkers by remember { mutableStateOf<Set<String>>(emptySet()) }

    val strategy = remember {
        DefaultMarkerStrategy<GoogleMapActualMarker>(
            expandMargin = 0.3  // 事前読み込み用のより大きなマージン
        )
    }

    // ビューポートに基づいてマーカーを動的に読み込む
    LaunchedEffect(currentBounds) {
        currentBounds?.let { bounds ->
            val newMarkers = fetchMarkersForBounds(bounds) // API コール

            newMarkers.forEach { markerData ->
                if (markerData.id !in loadedMarkers) {
                    val entity = MarkerEntity(
                        state = MarkerState(
                            id = markerData.id,
                            position = markerData.position,
                            icon = DefaultIcon()
                        )
                    )

                    strategy.markerManager.registerEntity(entity)
                    loadedMarkers = loadedMarkers + markerData.id
                }
            }
        }
    }

    // GoogleMapsView、MapboxMapView など、選択した地図プロバイダに置き換えてください
    GoogleMapsView(
        state = mapViewState,
        onCameraMove = { cameraPosition ->
            currentBounds = cameraPosition.visibleRegion?.bounds
        }
    ) {
        // 戦略が動的マーカー読み込みを管理します
    }
}
```

### クラスタリング戦略

```kotlin
@Composable
fun ClusteringStrategyExample() {
    val clusterStrategy = remember {
        SpatialMarkerStrategy<GoogleMapActualMarker>(
            clusteringEnabled = true,
            clusterRadius = 50.0,       // 50メートルのクラスタリング
            maxMarkersPerCluster = 5,   // 小さなクラスター
            geocell = HexGeocellImpl(
                baseHexSideLength = 100.0,  // きめ細かな空間インデックス
                zoom = 18.0
            )
        )
    }

    // 密集したマーカーデータセットを追加
    LaunchedEffect(Unit) {
        val denseMarkers = generateDenseMarkerSet(
            center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            count = 500,
            radius = 200.0  // 200メートル半径
        )

        denseMarkers.forEach { markerData ->
            val entity = MarkerEntity(
                state = MarkerState(
                    id = markerData.id,
                    position = markerData.position,
                    icon = DefaultIcon(
                        fillColor = markerData.category.color,
                        scale = 0.8f
                    )
                )
            )

            clusterStrategy.markerManager.registerEntity(entity)
        }
    }

    // GoogleMapsView、MapboxMapView など、選択した地図プロバイダに置き換えてください
    GoogleMapsView(state = mapViewState) {
        // クラスタリング戦略が近くのマーカーを自動的にグループ化します
    }
}
```

### リモート空間戦略

```kotlin
@Composable
fun RemoteSpatialExample() {
    val remoteStrategy = remember {
        RemoteSpatialMarkerStrategy<GoogleMapActualMarker>(
            apiEndpoint = "https://api.example.com/markers",
            cacheTimeout = 300000, // 5分
            maxConcurrentRequests = 3
        )
    }

    // リモート戦略はビューポートに基づいてサーバーからマーカーを読み込みます
    LaunchedEffect(mapViewState) {
        // 戦略が自動的にサーバーリクエストを管理します
        // 手動でのマーカー読み込みは不要です
    }

    // GoogleMapsView、MapboxMapView など、選択した地図プロバイダに置き換えてください
    GoogleMapsView(state = mapViewState) {
        // リモート戦略がサーバーからのすべてのマーカー読み込みを処理します
    }
}
```

## 戦略の比較

### パフォーマンス特性

| 戦略 | 最適な用途 | メモリ使用量 | ネットワーク | 複雑さ |
|----------|----------|--------------|---------|------------|
| DefaultMarkerStrategy | Google Maps、ArcGIS | 中 | なし | 中 |
| SimpleMarkerStrategy | Mapbox、HERE | 低 | なし | 低 |
| SpatialMarkerStrategy | 大規模なデータセット | 高 | なし | 高 |
| RemoteSpatialMarkerStrategy | サーバーサイドデータ | 低 | 高 | 高 |

### ユースケースガイドライン

#### DefaultMarkerStrategy を選択する場合:
- Google Maps または ArcGIS を使用している
- 中程度のマーカー数（1,000～50,000）
- スムーズなビューポートベースのレンダリングが必要
- マーカーがローカルに読み込まれる

#### SimpleMarkerStrategy を選択する場合:
- Mapbox または HERE Maps を使用している
- 小規模なマーカー数（<10,000）
- 最小限のオーバーヘッドが必要
- シンプルなレンダリング要件

#### SpatialMarkerStrategy を選択する場合:
- 非常に大きなマーカーデータセット（50,000以上）
- クラスタリング機能が必要
- 高度な空間最適化が必要
- より高いメモリ使用量を許容できる

#### RemoteSpatialMarkerStrategy を選択する場合:
- マーカーがサーバーサイドに保存されている
- オンデマンド読み込みが必要
- ネットワーク接続がある
- アプリのメモリ使用量を最小限に抑える必要がある

## カスタム戦略開発

### AbstractViewportStrategy の拡張

```kotlin
class CustomMarkerStrategy<ActualMarker>(
    semaphore: Semaphore = Semaphore(1),
    geocell: HexGeocell = HexGeocellImpl.defaultGeocell()
) : AbstractViewportStrategy<ActualMarker>(semaphore, geocell) {

    override suspend fun onCameraChanged(
        cameraPosition: MapCameraPositionImpl,
        renderer: MarkerOverlayRenderer<ActualMarker>
    ) {
        semaphore.withPermit {
            // カスタムレンダリングロジック
            val visibleBounds = cameraPosition.visibleRegion?.bounds ?: return

            // カスタムマーカー管理をここに記述
            val markersToShow = determineVisibleMarkers(visibleBounds)
            val markersToHide = determineHiddenMarkers(visibleBounds)

            // レンダラーを使用して表示を更新
            if (markersToHide.isNotEmpty()) {
                renderer.onRemove(markersToHide)
            }

            if (markersToShow.isNotEmpty()) {
                val addParams = markersToShow.map { entity ->
                    object : MarkerOverlayRenderer.AddParams {
                        override val state: MarkerState = entity.state
                        override val bitmapIcon: BitmapIcon = entity.state.icon?.toBitmapIcon() ?: defaultIcon
                    }
                }
                renderer.onAdd(addParams)
            }

            renderer.onPostProcess()
        }
    }

    private fun determineVisibleMarkers(bounds: GeoRectBounds): List<MarkerEntity<ActualMarker>> {
        // どのマーカーを表示すべきかを決定するカスタムロジック
        return markerManager.findMarkersInBounds(bounds)
    }

    private fun determineHiddenMarkers(bounds: GeoRectBounds): List<MarkerEntity<ActualMarker>> {
        // どのマーカーを非表示にすべきかを決定するカスタムロジック
        return markerManager.allEntities().filter { entity ->
            entity.isRendered && !bounds.contains(entity.state.position)
        }
    }
}
```

## パフォーマンス最適化

### 戦略設定

```kotlin
// 高パフォーマンス設定
val performanceStrategy = DefaultMarkerStrategy<ActualMarker>(
    expandMargin = 0.1,  // 事前読み込みを減らすためのより小さなマージン
    semaphore = Semaphore(2),  // 若干の並列性を許可
    geocell = HexGeocellImpl(
        baseHexSideLength = 1000.0,  // パフォーマンス向上のためのより大きなセル
        zoom = 15.0  // 速度のための低解像度
    )
)

// メモリ最適化設定
val memoryStrategy = SimpleMarkerStrategy<ActualMarker>(
    expandMargin = 0.05,  // 最小限の拡張
    geocell = HexGeocellImpl(
        baseHexSideLength = 2000.0,  // 非常に大きなセル
        zoom = 12.0  // 低解像度
    )
)
```

### パフォーマンスの監視

```kotlin
@Composable
fun StrategyPerformanceMonitoring() {
    val strategy = remember { DefaultMarkerStrategy<GoogleMapActualMarker>() }
    var performanceStats by remember { mutableStateOf<String>("") }

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)

            val stats = strategy.markerManager.getMemoryStats()
            performanceStats = buildString {
                appendLine("エンティティ: ${stats.entityCount}")
                appendLine("メモリ: ${stats.estimatedMemoryKB} KB")
                appendLine("空間インデックス: ${stats.hasSpatialIndex}")
            }
        }
    }

    Column {
        Text(performanceStats)

        // GoogleMapsView、MapboxMapView など、選択した地図プロバイダに置き換えてください
        GoogleMapsView(state = mapViewState) {
            // 戦略管理されたマーカー
        }
    }
}
```

## ベストプラクティス

1. **戦略の選択**: 特定のユースケースと地図プロバイダに基づいて戦略を選択する
2. **ビューポートマージン**: 事前読み込み（より大きなマージン）とパフォーマンス（より小さなマージン）のバランスを取る
3. **空間設定**: データ密度に合わせてジオセルパラメータを調整する
4. **メモリ監視**: 特に大規模なデータセットでは、本番環境でメモリ使用量を監視する
5. **テスト**: 現実的なデータ量と使用パターンでテストする
6. **フォールバック**: パフォーマンス問題のためのより単純な戦略をフォールバックとして用意する

## よくある落とし穴

1. **過剰設計**: シンプルなマーカーシナリオに複雑な戦略を使用しない
2. **メモリリーク**: 戦略リソースの適切なクリーンアップを確保する
3. **プロバイダの不一致**: 地図プロバイダに間違った戦略を使用すると、パフォーマンスが低下する可能性がある
4. **過剰な事前読み込み**: 大きな拡張マージンはメモリ圧迫を引き起こす可能性がある
5. **スレッドセーフティ**: 戦略は並行性を処理しますが、外部の変更には注意が必要

## 移行ガイド

### 基本的なマーカー管理から

```kotlin
// 変更前: 基本的なマーカー管理
@Composable
fun BasicMarkers() {
    MapView(state = mapViewState) {
        markers.forEach { markerData ->
            Marker(
                position = markerData.position,
                icon = DefaultIcon()
            )
        }
    }
}

// 変更後: 戦略ベースの管理
@Composable
fun StrategyMarkers() {
    val strategy = remember { DefaultMarkerStrategy<ActualMarker>() }

    LaunchedEffect(markers) {
        markers.forEach { markerData ->
            val entity = MarkerEntity(
                state = MarkerState(
                    id = markerData.id,
                    position = markerData.position,
                    icon = DefaultIcon()
                )
            )
            strategy.markerManager.registerEntity(entity)
        }
    }

    MapView(state = mapViewState) {
        // 戦略がすべてのマーカーレンダリングを処理します
    }
}
```

マーカー戦略モジュールは、大規模なデータセットや複雑なレンダリング要件を必要とするアプリケーションのための洗練されたマーカー管理機能を提供します。
