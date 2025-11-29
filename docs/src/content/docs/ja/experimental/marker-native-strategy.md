---
title: "Marker Native Strategy (Experimental)"
---

`mapconductor-marker-native-strategy` モジュールは、ネイティブC++の空間インデックスを使用した高性能なマーカー管理を提供します。この実験的モジュールは、大量のマーカー（10,000個以上）を持つアプリケーションのパフォーマンスを劇的に向上させます。

> **⚠️ 実験的モジュール**: このモジュールは非常に実験的であり、ネイティブライブラリのサポートが必要です。本番環境での使用には細心の注意を払ってください。

## 概要

marker native strategy は、Java ベースの空間インデックスを最適化された C++ 実装に置き換え、以下を提供します:
- **90%のメモリ削減**: 標準的なマーカー管理と比較して
- **ネイティブ空間クエリ**: 最大パフォーマンスのための C++ 空間インデックス
- **効率的なビューポートカリング**: ビューポート内のマーカーのみをレンダリング
- **並列処理**: マルチスレッドマーカー操作
- **最小限の Java オーバーヘッド**: ネイティブコードを単一の真実の情報源として

## パフォーマンス特性

### メモリ使用量
- **標準 MarkerManager**: 1,000マーカーあたり約1MB
- **NativeMarkerManager**: 1,000マーカーあたり約100KB
- **最適化されたストレージ**: エンティティストレージの重複なし

### クエリパフォーマンス
- **標準空間クエリ**: Java オーバーヘッドを伴う O(log n)
- **ネイティブ空間クエリ**: C++ 最適化による O(log n)
- **大規模データセット**: 100,000以上のマーカーで10倍〜100倍のパフォーマンス向上

## インストール

`build.gradle` に native strategy モジュールを追加します:

```kotlin
dependencies {
    implementation "com.mapconductor:marker-native-strategy"

    // 必須: Core モジュール
    implementation "com.mapconductor:mapconductor-bom:$version"
    // 必須: Core モジュール
    implementation "com.mapconductor:core"

    // 地図SDKを選択
    implementation "com.mapconductor:for-googlemaps"
}
```

### ネイティブライブラリのセットアップ

モジュールにはネイティブC++ライブラリが必要です。アプリが必要なABIをサポートしていることを確認してください:

```kotlin
android {
    defaultConfig {
        ndk {
            abiFilters 'arm64-v8a', 'armeabi-v7a', 'x86', 'x86_64'
        }
    }
}
```

## コアコンポーネント

### NativeMarkerManager

ネイティブ空間インデックスを使用した高性能マーカーマネージャー:

```kotlin
import com.mapconductor.marker.nativestrategy.NativeMarkerManager
import com.mapconductor.core.geocell.HexGeocellImpl

// ネイティブマーカーマネージャーを作成
val nativeManager = NativeMarkerManager<ActualMarker>(
    hexGeocell = HexGeocellImpl.defaultGeocell()
)

// 標準 MarkerManager のように使用
nativeManager.registerEntity(markerEntity)
val nearestMarker = nativeManager.findNearest(position)
val markersInBounds = nativeManager.findMarkersInBounds(bounds)
```

### ネイティブレンダリングストラテジ

#### SimpleNativeParallelStrategy

ネイティブインデックスによる並列マーカーレンダリング:

```kotlin
import com.mapconductor.marker.nativestrategy.SimpleNativeParallelStrategy

val strategy = SimpleNativeParallelStrategy<ActualMarker>(
    expandMargin = 0.2,        // 20% ビューポート拡張
    maxConcurrency = 4,        // 並列スレッド数
    geocell = HexGeocellImpl.defaultGeocell()
)
```

## 基本的な使用方法

### シンプルなネイティブマネージャー

```kotlin
@Composable
fun BasicNativeExample() {
    // ネイティブマーカーマネージャーを作成
    val nativeManager = remember {
        NativeMarkerManager<GoogleMapActualMarker>(
            hexGeocell = HexGeocellImpl.defaultGeocell()
        )
    }

    // ネイティブマネージャーにマーカーを追加
    LaunchedEffect(Unit) {
        val markers = generateLargeMarkerDataset() // 10,000以上のマーカー
        markers.forEach { markerData ->
            val entity = MarkerEntity(
                state = MarkerState(
                    id = markerData.id,
                    position = markerData.position,
                    icon = DefaultIcon()
                )
            )
            nativeManager.registerEntity(entity)
        }
    }

    // GoogleMapView、MapboxMapView などの選択した地図SDKに置き換えてください
    MapView(state = mapViewState) {
        // マーカーはネイティブストラテジで管理されます
        // Marker コンポーザブルを手動で追加する必要はありません
    }

    DisposableEffect(Unit) {
        onDispose {
            nativeManager.destroy() // 重要: ネイティブリソースのクリーンアップ
        }
    }
}
```

### パフォーマンスモニタリング

```kotlin
@Composable
fun NativePerformanceExample() {
    val nativeManager = remember {
        NativeMarkerManager<GoogleMapActualMarker>(
            hexGeocell = HexGeocellImpl.defaultGeocell()
        )
    }

    var stats by remember { mutableStateOf<NativeMarkerManagerStats?>(null) }

    // パフォーマンスを監視
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000) // 5秒ごとに更新
            stats = nativeManager.getNativeMemoryStats()
        }
    }

    Column {
        stats?.let { s ->
            Text("Markers: ${s.entityCount}")
            Text("Native Index: ${s.nativeIndexCount}")
            Text("Memory: ${s.estimatedMemoryKB} KB")
            Text("Pure Native: ${s.usesPureNativeIndex}")
        }

        // GoogleMapView、MapboxMapView などの選択した地図SDKに置き換えてください
        MapView(state = mapViewState) {
            // ネイティブ管理されたマーカー
        }
    }
}
```

## 高度な使用方法

### 並列レンダリングストラテジ

```kotlin
@Composable
fun ParallelRenderingExample() {
    val parallelStrategy = remember {
        SimpleNativeParallelStrategy<GoogleMapActualMarker>(
            expandMargin = 0.3,        // より大きいビューポート拡張
            maxConcurrency = 6,        // より多くの並列スレッド
            geocell = HexGeocellImpl(
                baseHexSideLength = 1000.0,  // 最適化されたセルサイズ
                zoom = 15.0
            )
        )
    }

    // 並列ストラテジでマーカーコントローラーを構成
    LaunchedEffect(mapViewState) {
        mapViewState.getMapViewHolder()?.let { holder ->
            // マップコントローラーで並列ストラテジをセットアップ
            // 実装は地図SDKに依存
        }
    }

    // GoogleMapView、MapboxMapView などの選択した地図SDKに置き換えてください
    MapView(state = mapViewState) {
        // 並列レンダリングされたマーカー
    }
}
```

### 動的マーカーロード

```kotlin
@Composable
fun DynamicNativeLoadingExample() {
    val nativeManager = remember {
        NativeMarkerManager<GoogleMapActualMarker>(
            hexGeocell = HexGeocellImpl.defaultGeocell()
        )
    }

    var currentBounds by remember { mutableStateOf<GeoRectBounds?>(null) }
    var visibleMarkers by remember { mutableStateOf<List<MarkerEntity<GoogleMapActualMarker>>>(emptyList()) }

    // ビューポートに基づいて動的にマーカーを読み込み
    LaunchedEffect(currentBounds) {
        currentBounds?.let { bounds ->
            // ネイティブ空間クエリは非常に高速
            visibleMarkers = nativeManager.findMarkersInBounds(bounds)
        }
    }

    Column {
        Text("Visible Markers: ${visibleMarkers.size}")

        // GoogleMapView、MapboxMapView などの選択した地図SDKに置き換えてください
        MapView(
            state = mapViewState,
            onCameraMove = { cameraPosition ->
                currentBounds = cameraPosition.visibleRegion?.bounds
            }
        ) {
            // 可視マーカーのみが処理されます
        }
    }
}
```

### ネイティブストラテジによるクラスタリング

```kotlin
@Composable
fun NativeClusteringExample() {
    val clusteringStrategy = remember {
        NativeSpatialMarkerStrategy<GoogleMapActualMarker>(
            clusteringEnabled = true,
            clusterThreshold = 50,     // 近くに50個以上のマーカーがある場合にクラスタ化
            clusterRadius = 100.0,     // 100メートルのクラスタリング半径
            geocell = HexGeocellImpl.defaultGeocell()
        )
    }

    val nativeManager = remember {
        NativeMarkerManager<GoogleMapActualMarker>(
            hexGeocell = HexGeocellImpl.defaultGeocell()
        )
    }

    // クラスタリング用の大規模データセットを追加
    LaunchedEffect(Unit) {
        val denseMarkers = generateDenseMarkerCluster(
            center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            count = 1000,
            radiusMeters = 500.0 // 500メートル
        )

        denseMarkers.forEach { markerData ->
            val entity = MarkerEntity(
                state = MarkerState(
                    id = markerData.id,
                    position = markerData.position,
                    icon = DefaultIcon(fillColor = Color.Blue, scale = 0.8f)
                )
            )
            nativeManager.registerEntity(entity)
        }
    }

    // GoogleMapView、MapboxMapView などの選択した地図SDKに置き換えてください
    MapView(state = mapViewState) {
        // ネイティブクラスタリングは近くのマーカーを自動的にグループ化
    }
}
```

## ネイティブインデックス操作

### 直接空間クエリ

```kotlin
fun performNativeQueries(nativeManager: NativeMarkerManager<ActualMarker>) {
    val center = GeoPointImpl.fromLatLong(37.7749, -122.4194)
    val bounds = GeoRectBounds(
        southWest = GeoPointImpl.fromLatLong(37.7700, -122.4250),
        northEast = GeoPointImpl.fromLatLong(37.7800, -122.4150)
    )

    // ネイティブ空間クエリは非常に高速
    val nearestMarker = nativeManager.findNearest(center)
    val boundedMarkers = nativeManager.findMarkersInBounds(bounds)
    val totalMarkers = nativeManager.allEntities().size

    // メモリとパフォーマンスの統計
    val stats = nativeManager.getNativeMemoryStats()
    println("Query performance: ${stats.nativeIndexCount} indexed markers")
    println("Memory usage: ${stats.estimatedMemoryKB} KB")
}
```

### バッチ操作

```kotlin
suspend fun batchNativeOperations(nativeManager: NativeMarkerManager<ActualMarker>) {
    val markersBatch = generateMarkerBatch(10000) // 10,000マーカー

    // 効率的なバッチ登録
    withContext(Dispatchers.Default) {
        markersBatch.forEach { markerData ->
            val entity = MarkerEntity(
                state = MarkerState(
                    id = markerData.id,
                    position = markerData.position,
                    icon = DefaultIcon()
                )
            )
            nativeManager.registerEntity(entity)
        }
    }

    // 登録を検証
    val totalMarkers = nativeManager.allEntities().size
    println("Registered $totalMarkers markers in native index")
}
```

## メモリ管理

### リソースクリーンアップ

```kotlin
@Composable
fun ResourceManagementExample() {
    val nativeManager = remember {
        NativeMarkerManager<GoogleMapActualMarker>(
            hexGeocell = HexGeocellImpl.defaultGeocell()
        )
    }

    // 適切なクリーンアップはネイティブリソースにとって重要
    DisposableEffect(nativeManager) {
        onDispose {
            // ネイティブリソースをクリーンアップ
            nativeManager.destroy()
        }
    }

    // メモリ使用量を監視
    var memoryStats by remember { mutableStateOf<NativeMarkerManagerStats?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(10000) // 10秒ごとにチェック
            memoryStats = nativeManager.getNativeMemoryStats()

            // 監視のためにメモリ使用量をログ
            memoryStats?.let { stats ->
                if (stats.estimatedMemoryKB > 10000) { // > 10MB
                    println("High memory usage detected: ${stats.estimatedMemoryKB} KB")
                }
            }
        }
    }

    // GoogleMapView、MapboxMapView などの選択した地図SDKに置き換えてください
    MapView(state = mapViewState) {
        // ネイティブ管理されたマーカー
    }
}
```

## パフォーマンス最適化

### 設定チューニング

```kotlin
// 大規模データセット向けの最適設定
val optimizedGeocell = HexGeocellImpl(
    baseHexSideLength = 500.0,  // 密なデータ用の小さなセル
    zoom = 18.0                 // 高解像度
)

val optimizedManager = NativeMarkerManager<ActualMarker>(
    hexGeocell = optimizedGeocell
)

// 最適な並列ストラテジ
val optimizedStrategy = SimpleNativeParallelStrategy<ActualMarker>(
    expandMargin = 0.1,         // パフォーマンスのための小さな拡張
    maxConcurrency = Runtime.getRuntime().availableProcessors(),
    geocell = optimizedGeocell
)
```

### ベンチマークテスト

```kotlin
suspend fun benchmarkNativePerformance() {
    val standardManager = MarkerManager<ActualMarker>(HexGeocellImpl.defaultGeocell())
    val nativeManager = NativeMarkerManager<ActualMarker>(HexGeocellImpl.defaultGeocell())

    val testMarkers = generateTestDataset(50000) // 50,000マーカー

    // 登録のベンチマーク
    val standardTime = measureTimeMillis {
        testMarkers.forEach { standardManager.registerEntity(it) }
    }

    val nativeTime = measureTimeMillis {
        testMarkers.forEach { nativeManager.registerEntity(it) }
    }

    // 空間クエリのベンチマーク
    val testBounds = GeoRectBounds(
        southWest = GeoPointImpl.fromLatLong(37.7700, -122.4250),
        northEast = GeoPointImpl.fromLatLong(37.7800, -122.4150)
    )

    val standardQueryTime = measureTimeMillis {
        repeat(1000) { standardManager.findMarkersInBounds(testBounds) }
    }

    val nativeQueryTime = measureTimeMillis {
        repeat(1000) { nativeManager.findMarkersInBounds(testBounds) }
    }

    println("Registration - Standard: ${standardTime}ms, Native: ${nativeTime}ms")
    println("Queries - Standard: ${standardQueryTime}ms, Native: ${nativeQueryTime}ms")
}
```

## ベストプラクティス

1. **リソース管理**: 使用後は必ず NativeMarkerManager で `destroy()` を呼び出す
2. **バッチ操作**: 大規模データセットにはバッチ登録を使用する
3. **メモリ監視**: 本番環境でネイティブメモリ使用量を監視する
4. **テスト**: 特定のデータパターンで徹底的にテストする
5. **フォールバックストラテジ**: サポートされていないデバイス向けに非ネイティブフォールバックを用意する

## 制限事項と考慮事項

1. **プラットフォームサポート**: ターゲットABI向けのネイティブライブラリサポートが必要
2. **メモリ管理**: ネイティブメモリはガベージコレクトされない
3. **デバッグ**: ネイティブクラッシュはJavaクラッシュよりもデバッグが困難
4. **バイナリサイズ**: ネイティブライブラリによりAPKサイズが増加
5. **互換性**: すべてのデバイス/エミュレータで動作しない可能性がある

## トラブルシューティング

### ネイティブライブラリ読み込みの問題

```kotlin
// ネイティブライブラリの可用性を確認
try {
    val nativeManager = NativeMarkerManager<ActualMarker>(
        hexGeocell = HexGeocellImpl.defaultGeocell()
    )
    // ネイティブライブラリが正常に読み込まれました
} catch (e: UnsatisfiedLinkError) {
    // 標準マーカーマネージャーにフォールバック
    val standardManager = MarkerManager<ActualMarker>(
        hexGeocell = HexGeocellImpl.defaultGeocell()
    )
}
```

### メモリリーク防止

```kotlin
class MarkerActivity : ComponentActivity() {
    private var nativeManager: NativeMarkerManager<ActualMarker>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        nativeManager = NativeMarkerManager(HexGeocellImpl.defaultGeocell())
    }

    override fun onDestroy() {
        super.onDestroy()

        // 重要: ネイティブリソースをクリーンアップ
        nativeManager?.destroy()
        nativeManager = null
    }
}
```

marker native strategy モジュールは、マーカーを多用するアプリケーションに大幅なパフォーマンス向上をもたらしますが、実験的な性質のため、慎重なリソース管理と徹底的なテストが必要です。
