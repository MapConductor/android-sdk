---
title: "GroundImageState（画像オーバーレイ状態）"
---


`GroundImageState` は、地図上のグラウンドイメージオーバーレイの構成と動作を管理します。地理的境界、画像リソース、不透明度設定のためのリアクティブなプロパティを提供します。

## コンストラクタ

```kotlin
GroundImageState(
    bounds: GeoRectBounds,
    image: Drawable,
    opacity: Float = 1.0f,
    id: String? = null,
    extra: Serializable? = null
)
```

## プロパティ

### 基本プロパティ
- **`id: String`**: 一意の識別子（指定されていない場合は自動生成）
- **`bounds: GeoRectBounds`**: 画像配置のための地理的矩形境界
- **`image: Drawable`**: 表示する描画可能画像
- **`opacity: Float`**: 透明度レベル（0.0 = 透明、1.0 = 不透明）
- **`extra: Serializable?`**: グラウンドイメージに付加される追加データ

## メソッド

```kotlin
fun fingerPrint(): GroundImageFingerPrint // 変更検知
fun asFlow(): Flow<GroundImageFingerPrint> // リアクティブ更新
```

## 使用例

### 基本的なグラウンドイメージ

```kotlin
@Composable
fun BasicGroundImageExample() {
    val context = LocalContext.current
    val drawable = AppCompatResources.getDrawable(context, R.drawable.overlay_image)

    val groundImageState = drawable?.let {
        GroundImageState(
            bounds = GeoRectBounds(
                southwest = GeoPointImpl.fromLatLong(37.7649, -122.4294),
                northeast = GeoPointImpl.fromLatLong(37.7849, -122.4094)
            ),
            image = it,
            opacity = 0.7f,
            extra = "Base overlay"
        )
    }

    // MapViewは、GoogleMapView、MapboxMapViewなど、選択した地図SDKーに置き換えてください
MapView(state = mapViewState) {
        groundImageState?.let { state ->
            GroundImage(state)
        }
    }
}
```

### インタラクティブなグラウンドイメージ

```kotlin
@Composable
fun InteractiveGroundImageExample() {
    var groundImageState by remember { mutableStateOf<GroundImageState?>(null) }
    var opacity by remember { mutableStateOf(0.7f) }

    val context = LocalContext.current

    LaunchedEffect(opacity) {
        val drawable = AppCompatResources.getDrawable(context, R.drawable.map_overlay)
        drawable?.let {
            groundImageState = GroundImageState(
                bounds = GeoRectBounds(
                    southwest = GeoPointImpl.fromLatLong(37.7649, -122.4294),
                    northeast = GeoPointImpl.fromLatLong(37.7849, -122.4094)
                ),
                image = it,
                opacity = opacity,
                extra = "Interactive overlay"
            )
        }
    }

    Column {
        Slider(
            value = opacity,
            onValueChange = { opacity = it },
            valueRange = 0f..1f,
            modifier = Modifier.padding(16.dp)
        )
        Text("Opacity: ${(opacity * 100).toInt()}%")

        // MapViewは、GoogleMapView、MapboxMapViewなど、選択した地図SDKーに置き換えてください
MapView(
            state = mapViewState,
            onGroundImageClick = { event ->
                println("Ground image clicked at: ${event.clicked}")
            }
        ) {
            groundImageState?.let { state ->
                GroundImage(state)
            }
        }
    }
}
```

### 複数のオーバーレイ

```kotlin
@Composable
fun MultiLayerGroundImageExample() {
    val context = LocalContext.current

    val overlayStates = remember {
        listOf(
            Triple(R.drawable.base_layer, 0.3f, "Base satellite"),
            Triple(R.drawable.weather_layer, 0.6f, "Weather data"),
            Triple(R.drawable.traffic_layer, 0.8f, "Traffic info")
        ).mapNotNull { (resId, opacity, description) ->
            AppCompatResources.getDrawable(context, resId)?.let { drawable ->
                GroundImageState(
                    bounds = GeoRectBounds(
                        southwest = GeoPointImpl.fromLatLong(37.7649, -122.4294),
                        northeast = GeoPointImpl.fromLatLong(37.7849, -122.4094)
                    ),
                    image = drawable,
                    opacity = opacity,
                    extra = description
                )
            }
        }
    }

    // MapViewは、GoogleMapView、MapboxMapViewなど、選択した地図SDKーに置き換えてください
MapView(state = mapViewState) {
        overlayStates.forEach { state ->
            GroundImage(state)
        }
    }
}
```

### 動的な境界

```kotlin
@Composable
fun DynamicBoundsExample() {
    var southwest by remember {
        mutableStateOf(GeoPointImpl.fromLatLong(37.7649, -122.4294))
    }
    var northeast by remember {
        mutableStateOf(GeoPointImpl.fromLatLong(37.7849, -122.4094))
    }

    val context = LocalContext.current
    val drawable = AppCompatResources.getDrawable(context, R.drawable.floor_plan)

    val groundImageState = drawable?.let {
        GroundImageState(
            bounds = GeoRectBounds(southwest = southwest, northeast = northeast),
            image = it,
            opacity = 0.8f,
            extra = "Floor plan"
        )
    }

    // MapViewは、GoogleMapView、MapboxMapViewなど、選択した地図SDKーに置き換えてください
MapView(
        state = mapViewState,
        onMarkerDrag = { markerState ->
            when (markerState.extra) {
                "SW" -> southwest = markerState.position
                "NE" -> northeast = markerState.position
            }
        }
    ) {
        groundImageState?.let { state ->
            GroundImage(state)
        }

        // 角のマーカー
        Marker(
            position = southwest,
            icon = DefaultIcon(fillColor = Color.Green, label = "SW"),
            draggable = true,
            extra = "SW"
        )

        Marker(
            position = northeast,
            icon = DefaultIcon(fillColor = Color.Red, label = "NE"),
            draggable = true,
            extra = "NE"
        )
    }
}
```

### アニメーション付きオーバーレイ

```kotlin
@Composable
fun AnimatedGroundImageExample() {
    var isAnimating by remember { mutableStateOf(false) }
    var opacity by remember { mutableStateOf(0.5f) }

    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            while (isAnimating) {
                delay(100)
                opacity = (sin(System.currentTimeMillis() / 1000.0).toFloat() + 1f) / 2f
            }
        }
    }

    val context = LocalContext.current
    val drawable = AppCompatResources.getDrawable(context, R.drawable.animated_overlay)

    val groundImageState = drawable?.let {
        GroundImageState(
            bounds = GeoRectBounds(
                southwest = GeoPointImpl.fromLatLong(37.7649, -122.4294),
                northeast = GeoPointImpl.fromLatLong(37.7849, -122.4094)
            ),
            image = it,
            opacity = opacity,
            extra = "Animated overlay"
        )
    }

    Column {
        Button(onClick = { isAnimating = !isAnimating }) {
            Text(if (isAnimating) "Stop" else "Animate")
        }

        // MapViewは、GoogleMapView、MapboxMapViewなど、選択した地図SDKーに置き換えてください
MapView(state = mapViewState) {
            groundImageState?.let { state ->
                GroundImage(state)
            }
        }
    }
}
```

## イベント処理

グラウンドイメージイベントは状態とクリック位置の両方を提供します：

```kotlin
data class GroundImageEvent(
    val state: GroundImageState,
    val clicked: GeoPointImpl?
)

typealias OnGroundImageEventHandler = (GroundImageEvent) -> Unit
```

## 画像リソース管理

### 異なるソースからの読み込み

```kotlin
// リソースから
val drawable = AppCompatResources.getDrawable(context, R.drawable.overlay)

// アセットから
val inputStream = context.assets.open("overlays/map.png")
val drawable = Drawable.createFromStream(inputStream, null)

// ネットワークから（画像読み込みライブラリを使用）
// Coil、Glide、Picassoなどのライブラリをネットワーク画像に使用してください
```

## ベストプラクティス

1. **画像サイズ**: パフォーマンスのために画像解像度を最適化してください
2. **境界の精度**: 正確な地理的配置を確保してください
3. **不透明度**: 視認性のために適切な透明度を使用してください
4. **リソース管理**: 描画可能リソースをキャッシュして再利用してください
5. **レイヤー順序**: 複数のオーバーレイの描画順序を考慮してください
6. **パフォーマンス**: 同時グラウンドイメージの数を制限してください
7. **エラー処理**: 画像の読み込みに失敗した場合を処理してください
