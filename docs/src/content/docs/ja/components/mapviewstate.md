---
title: "MapViewState"
---

`MapViewState` は、地図の初期化、カメラ位置、および全体的な地図状態を管理するコアコンポーネントです。各地図SDKには独自の実装がありますが、一貫したインターフェースを維持しています。

## 地図SDK実装

MapConductor は4つの地図SDKをサポートしており、それぞれに独自の `MapViewState` 実装があります:

- `GoogleMapViewStateImpl` - Google Maps
- `MapboxViewStateImpl` - Mapbox Maps
- `HereViewStateImpl` - HERE Maps
- `ArcGISMapViewStateImpl` - ArcGIS Maps

## コアプロパティ

### 初期化状態

- **`isInitialized: StateFlow<InitState>`**: 地図の初期化状態を追跡します
  - `NotStarted`: 地図の初期化が開始されていません
  - `Initializing`: 地図は現在初期化中です
  - `Initialized`: 地図は使用準備ができています
  - `Failed`: 初期化が失敗しました

### カメラ管理

- **`cameraPosition: StateFlow<MapCameraPositionImpl?>`**: 現在のカメラ位置
- **`initCameraPosition: MapCameraPositionImpl`**: 地図読み込み時の初期カメラ位置

### 地図デザイン

- **`mapDesignType: ActualMapDesignType`**: 地図のスタイル/デザイン（地図SDK固有）

## コアメソッド

### 初期化

```kotlin
fun initAsync(init: suspend () -> Boolean)
```
地図を非同期で初期化します。成功した場合は `true` を返します。

```kotlin
fun resetInitState()
```
初期化状態を `NotStarted` にリセットします。

### カメラ移動

```kotlin
fun moveCameraTo(
    cameraPosition: MapCameraPositionImpl,
    durationMs: Long? = 0,
    listener: MoveCameraCallback? = null
)
```
オプションのアニメーション付きで、カメラを特定の位置に移動します。

```kotlin
fun moveCameraTo(
    position: GeoPointImpl,
    durationMs: Long? = 0,
    listener: MoveCameraCallback? = null
)
```
カメラを特定の地理的ポイントに焦点を合わせるように移動します。

## 使用例

### 基本的なセットアップ

```kotlin
@Composable
fun MapExample() {
    // 地図状態を作成（地図SDKを選択）
    val mapViewState = rememberGoogleMapViewState()

    // 初期化状態を監視
    val initState by mapViewState.isInitialized.collectAsState()

    when (initState) {
        InitState.NotStarted -> Text("Map not started")
        InitState.Initializing -> CircularProgressIndicator()
        InitState.Initialized -> {
            // MapView を GoogleMapsView、MapboxMapView などのマップ地図SDKに置き換えてください
MapView(state = mapViewState) {
                // ここに地図コンテンツを追加
            }
        }
        InitState.Failed -> Text("Map failed to load")
    }
}
```

### カメラコントロール

```kotlin
@Composable
fun CameraControlExample() {
    val mapViewState = rememberGoogleMapViewState()

    Column {
        Button(
            onClick = {
                val sanFrancisco = GeoPointImpl.fromLatLong(37.7749, -122.4194)
                mapViewState.moveCameraTo(
                    position = sanFrancisco,
                    durationMs = 1000,
                    listener = object : MapViewState.MoveCameraCallback {
                        override fun onComplete() {
                            println("Camera movement completed")
                        }
                    }
                )
            }
        ) {
            Text("Move to San Francisco")
        }

        // MapView を GoogleMapsView、MapboxMapView などのマップ地図SDKに置き換えてください
MapView(state = mapViewState) {
            // 地図コンテンツ
        }
    }
}
```

### 地図SDKの切り替え

```kotlin
@Composable
fun ProviderSwitchExample() {
    var selectedProvider by remember { mutableStateOf("google") }

    val mapViewState = remember(selectedProvider) {
        when (selectedProvider) {
            "google" -> GoogleMapViewStateImpl()
            "mapbox" -> MapboxViewStateImpl()
            "here" -> HereViewStateImpl()
            "arcgis" -> ArcGISMapViewStateImpl()
            else -> GoogleMapViewStateImpl()
        }
    }

    Column {
        Row {
            Button(onClick = { selectedProvider = "google" }) {
                Text("Google Maps")
            }
            Button(onClick = { selectedProvider = "mapbox" }) {
                Text("Mapbox")
            }
            Button(onClick = { selectedProvider = "here" }) {
                Text("HERE")
            }
            Button(onClick = { selectedProvider = "arcgis" }) {
                Text("ArcGIS")
            }
        }

        // MapView を GoogleMapsView、MapboxMapView などのマップ地図SDKに置き換えてください
MapView(state = mapViewState) {
            Marker(
                position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                icon = DefaultIcon(label = selectedProvider)
            )
        }
    }
}
```

## イベント処理

`MapViewState` は選択した地図SDKコンポーネントと連携して、包括的なイベント処理を提供します:

```kotlin
// MapView を GoogleMapsView、MapboxMapView などのマップ地図SDKに置き換えてください
MapView(
    state = mapViewState,
    onMapLoaded = {
        println("Map loaded successfully")
    },
    onMapClick = { geoPoint ->
        println("Map clicked at: ${geoPoint.latitude}, ${geoPoint.longitude}")
    }
) {
    // 地図コンテンツ
}
```

## ベストプラクティス

1. **状態を記憶**: リコンポジション全体で状態を維持するために、常に `remember` を使用してください
2. **初期化を監視**: コンテンツを追加する前に初期化状態を確認してください
3. **失敗を処理**: 初期化失敗のためのフォールバック UI を提供してください
4. **地図SDKの抽象化**: すべての地図SDK実装で動作するコードを書いてください
5. **リソース管理**: SDK がライフサイクル管理を自動的に処理することを許可してください
