---
title: "初期化"
---

このセクションでは、異なる地図SDKに対して MapConductor を適切に初期化および設定する方法について説明します。

## 基本的な初期化

### Gradle 依存関係

`build.gradle.kts` に MapConductor を追加します:

```kotlin
dependencies {
    implementation "com.mapconductor:mapconductor-bom:$version"
    implementation "com.mapconductor:core"

    // 地図SDKを選択
    implementation "com.mapconductor:for-googlemaps"
    implementation "com.mapconductor:for-mapbox"
    implementation "com.mapconductor:for-here"
    implementation "com.mapconductor:for-arcgis"
}
```

### 地図SDKのセットアップ

各地図SDKには、特定のセットアップと API キーが必要です。

#### Google Maps

```kotlin
// Activity または Fragment 内で
@Composable
fun GoogleMapsExample() {
    val mapViewState = rememberGoogleMapViewState()

    // GoogleMapView、MapboxMapView など、選択した地図SDKに置き換えてください
    MapView(state = mapViewState) {
        // マップコンテンツ
    }
}
```

#### Mapbox

```kotlin
@Composable
fun MapboxExample() {
    val mapViewState = rememberMapboxMapViewState()

    // GoogleMapView、MapboxMapView など、選択した地図SDKに置き換えてください
    MapView(state = mapViewState) {
        // マップコンテンツ
    }
}
```

#### HERE Maps

```kotlin
@Composable
fun HereExample() {
    val mapViewState = rememberHereMapViewState()

    // GoogleMapView、MapboxMapView など、選択した地図SDKに置き換えてください
    MapView(state = mapViewState) {
        // マップコンテンツ
    }
}
```

#### ArcGIS

```kotlin
@Composable
fun ArcGISExample() {
    val mapViewState = rememberArcGISMapViewState()

    // GoogleMapView、MapboxMapView など、選択した地図SDKに置き換えてください
    MapView(state = mapViewState) {
        // マップコンテンツ
    }
}
```

## 高度な初期化

### カスタムマップ設定

```kotlin
@Composable
fun CustomMapConfiguration() {
    val mapViewState = remember {
        GoogleMapViewStateImpl().apply {
            // 初期カメラ位置を設定
            initCameraPosition = MapCameraPositionImpl(
                target = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                zoom = 12f,
                bearing = 45f,
                tilt = 30f
            )

            // マップデザイン/スタイルを設定
            mapDesignType = GoogleMapDesignType.SATELLITE
        }
    }

    // GoogleMapView、MapboxMapView など、選択した地図SDKに置き換えてください
    MapView(
        state = mapViewState,
        onMapLoaded = {
            println("Map loaded and ready")
        }
    ) {
        // マップコンテンツ
    }
}
```

### 実行時の地図SDK選択

```kotlin
@Composable
fun DynamicProviderSelection() {
    var selectedProvider by remember { mutableStateOf("google") }

    val mapViewState = remember(selectedProvider) {
        when (selectedProvider) {
            "google" -> rememberGoogleMapViewState()
            "mapbox" -> rememberMapboxMapViewState()
            "here" -> rememberHereMapViewState()
            "arcgis" -> rememberArcGISMapViewState()
            else -> rememberGoogleMapViewState()
        }
    }

    Column {
        // 地図SDK選択 UI
        LazyRow {
            items(listOf("google", "mapbox", "here", "arcgis")) { provider ->
                Button(
                    onClick = { selectedProvider = provider },
                    colors = if (provider == selectedProvider) {
                        ButtonDefaults.buttonColors(backgroundColor = Color.Blue)
                    } else {
                        ButtonDefaults.buttonColors()
                    }
                ) {
                    Text(provider.capitalize())
                }
            }
        }

        // 選択された地図SDKでマップを表示
        // GoogleMapView、MapboxMapView など、選択した地図SDKに置き換えてください
        MapView(state = mapViewState) {
            Marker(
                position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                icon = DefaultIcon(label = selectedProvider.uppercase())
            )
        }
    }
}
```

### 初期化状態の処理

```kotlin
@Composable
fun InitializationStateExample() {
    val mapViewState = rememberGoogleMapViewState()
    val initState by mapViewState.isInitialized.collectAsState()

    when (initState) {
        InitState.NotStarted -> {
            // 読み込みプレースホルダーを表示
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Text("マップを準備中...")
                }
            }
        }

        InitState.Initializing -> {
            // 初期化の進行状況を表示
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Text("マップを読み込み中...")
                }
            }
        }

        InitState.Initialized -> {
            // マップを表示
            // GoogleMapView、MapboxMapView など、選択した地図SDKに置き換えてください
            MapView(state = mapViewState) {
                // マップはコンテンツを表示する準備ができています
                Marker(
                    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                    icon = DefaultIcon(label = "準備完了!")
                )
            }
        }

        InitState.Failed -> {
            // エラー状態を表示
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = "Error",
                        tint = Color.Red,
                        modifier = Modifier.size(48.dp)
                    )
                    Text("マップの読み込みに失敗しました")
                    Button(
                        onClick = { mapViewState.resetInitState() }
                    ) {
                        Text("再試行")
                    }
                }
            }
        }
    }
}
```

### 遅延初期化

```kotlin
@Composable
fun DeferredInitializationExample() {
    var shouldInitialize by remember { mutableStateOf(false) }
    val mapViewState = rememberGoogleMapViewState()

    Column {
        if (!shouldInitialize) {
            Button(
                onClick = { shouldInitialize = true },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text("マップを読み込む")
            }
        }

        if (shouldInitialize) {
            // GoogleMapView、MapboxMapView など、選択した地図SDKに置き換えてください
            MapView(
                state = mapViewState,
                shouldInitialize = shouldInitialize
            ) {
                Marker(
                    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                    icon = DefaultIcon(label = "読み込み完了!")
                )
            }
        }
    }
}
```

### カスタムレンダリング戦略

```kotlin
@Composable
fun CustomRenderingExample() {
    val mapViewState = rememberGoogleMapViewState()

    // カスタムマーカーレンダリング戦略
    val customStrategy = remember {
        object : MarkerRenderingStrategy<GoogleMapActualMarker> {
            override suspend fun render(
                markers: Map<String, MarkerState>,
                controller: MapViewController
            ) {
                // カスタムレンダリングロジック
                // これは特定のユースケースのための高度な機能です
            }
        }
    }

    // GoogleMapView、MapboxMapView など、選択した地図SDKに置き換えてください
    MapView(
        state = mapViewState,
        renderingStrategy = customStrategy
    ) {
        // カスタムレンダリングを使用したマップコンテンツ
    }
}
```

## 設定オプション

### カメラ位置

```kotlin
val cameraPosition = MapCameraPositionImpl(
    target = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    zoom = 15f,
    bearing = 0f,  // 回転角度（度数法）
    tilt = 0f      // 傾斜角度（度数法）
)

mapViewState.initCameraPosition = cameraPosition
```

### マップの境界

```kotlin
// 初期マップ境界を設定
val bounds = GeoRectBounds(
    southwest = GeoPointImpl.fromLatLong(37.7049, -122.4794),
    northeast = GeoPointImpl.fromLatLong(37.8049, -122.3594)
)

// 境界が表示されるようにカメラを移動
mapViewState.moveCameraTo(bounds)
```

## エラー処理

### 初期化失敗

```kotlin
@Composable
fun RobustInitializationExample() {
    val mapViewState = rememberGoogleMapViewState()
    val initState by mapViewState.isInitialized.collectAsState()
    var retryCount by remember { mutableStateOf(0) }

    LaunchedEffect(initState) {
        if (initState == InitState.Failed) {
            // エラーをログに記録し、必要に応じて再試行
            println("マップ初期化失敗 (試行 ${retryCount + 1})")

            if (retryCount < 3) {
                delay(1000) // 再試行前に待機
                retryCount++
                mapViewState.resetInitState()
            }
        } else if (initState == InitState.Initialized) {
            retryCount = 0 // 成功時にリセット
        }
    }

    when (initState) {
        InitState.Failed -> {
            if (retryCount >= 3) {
                ErrorScreen(onRetry = {
                    retryCount = 0
                    mapViewState.resetInitState()
                })
            } else {
                LoadingScreen("再試行中...")
            }
        }
        else -> {
            // GoogleMapView、MapboxMapView など、選択した地図SDKに置き換えてください
            MapView(state = mapViewState) {
                // マップコンテンツ
            }
        }
    }
}

@Composable
fun ErrorScreen(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("マップの読み込みに失敗しました")
        Button(onClick = onRetry) {
            Text("再試行")
        }
    }
}

@Composable
fun LoadingScreen(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Text(message)
        }
    }
}
```

## ベストプラクティス

1. **remember を使用**: 常に `remember` で MapViewState の作成をラップする
2. **状態の処理**: すべての初期化状態を適切に処理する
3. **エラー回復**: 失敗した初期化に対して再試行ロジックを実装する
4. **リソース管理**: SDK にライフサイクル管理を任せる
5. **API キー**: 各地図SDKの適切な API キー設定を確認する
6. **パフォーマンス**: アプリ起動を高速化するために遅延初期化を検討する
7. **テスト**: 互換性を確保するために異なる地図SDKでテストする
