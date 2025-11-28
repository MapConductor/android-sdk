---
title: "GroundImage"
---

GroundImage は、地図上に地理的に配置される画像オーバーレイです。フロアプラン、衛星画像、気象オーバーレイ、または特定の地理座標に固定する必要がある画像ベースのデータを表示するのに便利です。

## Composable 関数

### 基本的な GroundImage

```kotlin
@Composable
fun MapViewScope.GroundImage(
    bounds: GeoRectBounds,
    image: Drawable,
    opacity: Float = 0.5f,
    id: String? = null,
    extra: Serializable? = null
)
```

### State を使用した GroundImage

```kotlin
@Composable
fun MapViewScope.GroundImage(state: GroundImageState)
```

## パラメータ

- **`bounds`**: 画像を配置する地理的な矩形境界（`GeoRectBounds`）
- **`image`**: 表示する drawable 画像（`Drawable`）
- **`opacity`**: 透明度レベル、0.0（透明）から 1.0（不透明）まで（デフォルト: `0.5f`）
- **`id`**: GroundImage のオプションの一意識別子（`String?`）
- **`extra`**: GroundImage に付加する追加データ（`Serializable?`）

## 使用例

### 基本的な GroundImage

```kotlin
@Composable
fun BasicGroundImageExample() {
    val context = LocalContext.current

    // MapView を GoogleMapView、MapboxMapView などのマップ地図SDKに置き換えてください
MapView(state = mapViewState) {
        AppCompatResources.getDrawable(context, R.drawable.overlay_image)?.let { drawable ->
            GroundImage(
                bounds = GeoRectBounds(
                    southwest = GeoPointImpl.fromLatLong(37.7649, -122.4294),
                    northeast = GeoPointImpl.fromLatLong(37.7849, -122.4094)
                ),
                image = drawable,
                opacity = 0.7f
            )
        }
    }
}
```

### 境界マーカー付きのインタラクティブな GroundImage

サンプルアプリのパターンに基づいた例:

```kotlin
@Composable
fun InteractiveGroundImageExample() {
    var southwest by remember {
        mutableStateOf(GeoPointImpl.fromLatLong(37.7649, -122.4294))
    }
    var northeast by remember {
        mutableStateOf(GeoPointImpl.fromLatLong(37.7849, -122.4094))
    }
    var opacity by remember { mutableStateOf(0.7f) }

    val context = LocalContext.current
    val groundImageDrawable = AppCompatResources.getDrawable(context, R.drawable.map_overlay)

    val bounds = GeoRectBounds(southwest = southwest, northeast = northeast)

    val groundImageState = groundImageDrawable?.let { drawable ->
        GroundImageState(
            bounds = bounds,
            image = drawable,
            opacity = opacity
        )
    }

    // 境界マーカー
    val swMarker = MarkerState(
        position = southwest,
        icon = DefaultIcon(
            fillColor = Color.Green,
            label = "SW",
            scale = 0.8f
        ),
        draggable = true,
        extra = "southwest"
    )

    val neMarker = MarkerState(
        position = northeast,
        icon = DefaultIcon(
            fillColor = Color.Red,
            label = "NE",
            scale = 0.8f
        ),
        draggable = true,
        extra = "northeast"
    )

    Column {
        // 不透明度コントロール
        Slider(
            value = opacity,
            onValueChange = { opacity = it },
            valueRange = 0f..1f,
            modifier = Modifier.padding(16.dp)
        )
        Text("Opacity: ${(opacity * 100).toInt()}%", modifier = Modifier.padding(horizontal = 16.dp))

        // MapView を GoogleMapView、MapboxMapView などのマップ地図SDKに置き換えてください
MapView(
            state = mapViewState,
            onMarkerDrag = { markerState ->
                when (markerState.extra as String) {
                    "southwest" -> southwest = markerState.position
                    "northeast" -> northeast = markerState.position
                }
            },
            onGroundImageClick = { groundImageEvent ->
                println("Ground image clicked at: ${groundImageEvent.clicked}")
            }
        ) {
            // GroundImage を描画
            groundImageState?.let { state ->
                GroundImage(state)
            }

            // コーナーマーカーを描画
            Marker(swMarker)
            Marker(neMarker)

            // 参照用の境界矩形を描画
            val boundsPoints = listOf(
                southwest,
                GeoPointImpl.fromLatLong(southwest.latitude, northeast.longitude),
                northeast,
                GeoPointImpl.fromLatLong(northeast.latitude, southwest.longitude),
                southwest
            )

            Polyline(
                points = boundsPoints,
                strokeColor = Color.Blue,
                strokeWidth = 2.dp
            )
        }
    }
}
```

### 異なる不透明度を持つ複数の GroundImage

```kotlin
@Composable
fun MultipleGroundImagesExample() {
    val context = LocalContext.current

    // MapView を GoogleMapView、MapboxMapView などのマップ地図SDKに置き換えてください
MapView(state = mapViewState) {
        // ベース衛星画像（低い不透明度）
        AppCompatResources.getDrawable(context, R.drawable.satellite_base)?.let { drawable ->
            GroundImage(
                bounds = GeoRectBounds(
                    southwest = GeoPointImpl.fromLatLong(37.7549, -122.4394),
                    northeast = GeoPointImpl.fromLatLong(37.7949, -122.3994)
                ),
                image = drawable,
                opacity = 0.3f,
                extra = "Satellite base"
            )
        }

        // 気象オーバーレイ（中程度の不透明度）
        AppCompatResources.getDrawable(context, R.drawable.weather_overlay)?.let { drawable ->
            GroundImage(
                bounds = GeoRectBounds(
                    southwest = GeoPointImpl.fromLatLong(37.7649, -122.4294),
                    northeast = GeoPointImpl.fromLatLong(37.7849, -122.4094)
                ),
                image = drawable,
                opacity = 0.6f,
                extra = "Weather data"
            )
        }

        // 交通オーバーレイ（高い不透明度）
        AppCompatResources.getDrawable(context, R.drawable.traffic_overlay)?.let { drawable ->
            GroundImage(
                bounds = GeoRectBounds(
                    southwest = GeoPointImpl.fromLatLong(37.7699, -122.4244),
                    northeast = GeoPointImpl.fromLatLong(37.7799, -122.4144)
                ),
                image = drawable,
                opacity = 0.8f,
                extra = "Traffic data"
            )
        }
    }
}
```

### 動的な GroundImage の読み込み

```kotlin
@Composable
fun DynamicGroundImageExample() {
    var selectedImageResource by remember { mutableStateOf(R.drawable.overlay1) }
    var isVisible by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val imageDrawable = AppCompatResources.getDrawable(context, selectedImageResource)

    val bounds = GeoRectBounds(
        southwest = GeoPointImpl.fromLatLong(37.7649, -122.4294),
        northeast = GeoPointImpl.fromLatLong(37.7849, -122.4094)
    )

    Column {
        Row {
            Button(onClick = { selectedImageResource = R.drawable.overlay1 }) {
                Text("Image 1")
            }
            Button(onClick = { selectedImageResource = R.drawable.overlay2 }) {
                Text("Image 2")
            }
            Button(onClick = { selectedImageResource = R.drawable.overlay3 }) {
                Text("Image 3")
            }
        }

        Switch(
            checked = isVisible,
            onCheckedChange = { isVisible = it },
            modifier = Modifier.padding(16.dp)
        )

        // MapView を GoogleMapView、MapboxMapView などのマップ地図SDKに置き換えてください
MapView(state = mapViewState) {
            if (isVisible && imageDrawable != null) {
                GroundImage(
                    bounds = bounds,
                    image = imageDrawable,
                    opacity = 0.7f,
                    extra = "Dynamic overlay"
                )
            }

            // コーナーの参照マーカー
            Marker(
                position = bounds.southwest,
                icon = DefaultIcon(fillColor = Color.Green, label = "SW", scale = 0.6f)
            )
            Marker(
                position = bounds.northeast,
                icon = DefaultIcon(fillColor = Color.Red, label = "NE", scale = 0.6f)
            )
        }
    }
}
```

### アニメーション化された GroundImage の不透明度

```kotlin
@Composable
fun AnimatedGroundImageExample() {
    var isAnimating by remember { mutableStateOf(false) }
    var opacity by remember { mutableStateOf(0.5f) }

    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            while (isAnimating) {
                delay(50)
                opacity = (sin(System.currentTimeMillis() / 1000.0).toFloat() + 1f) / 2f
            }
        }
    }

    val context = LocalContext.current

    Column {
        Button(
            onClick = { isAnimating = !isAnimating }
        ) {
            Text(if (isAnimating) "Stop Animation" else "Start Animation")
        }

        Text("Current opacity: ${(opacity * 100).toInt()}%")

        // MapView を GoogleMapView、MapboxMapView などのマップ地図SDKに置き換えてください
MapView(state = mapViewState) {
            AppCompatResources.getDrawable(context, R.drawable.animated_overlay)?.let { drawable ->
                GroundImage(
                    bounds = GeoRectBounds(
                        southwest = GeoPointImpl.fromLatLong(37.7649, -122.4294),
                        northeast = GeoPointImpl.fromLatLong(37.7849, -122.4094)
                    ),
                    image = drawable,
                    opacity = opacity,
                    extra = "Animated overlay"
                )
            }
        }
    }
}
```

### フロアプランオーバーレイ

```kotlin
@Composable
fun FloorPlanExample() {
    val context = LocalContext.current

    // MapView を GoogleMapView、MapboxMapView などのマップ地図SDKに置き換えてください
MapView(state = mapViewState) {
        // 建物のフロアプラン
        AppCompatResources.getDrawable(context, R.drawable.building_floor_plan)?.let { drawable ->
            GroundImage(
                bounds = GeoRectBounds(
                    southwest = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                    northeast = GeoPointImpl.fromLatLong(37.7759, -122.4184)
                ),
                image = drawable,
                opacity = 0.8f,
                extra = "Building floor plan"
            )
        }

        // 部屋のマーカー
        Marker(
            position = GeoPointImpl.fromLatLong(37.7751, -122.4191),
            icon = DefaultIcon(fillColor = Color.Blue, label = "A", scale = 0.6f),
            extra = "Room A"
        )

        Marker(
            position = GeoPointImpl.fromLatLong(37.7754, -122.4189),
            icon = DefaultIcon(fillColor = Color.Red, label = "B", scale = 0.6f),
            extra = "Room B"
        )

        Marker(
            position = GeoPointImpl.fromLatLong(37.7756, -122.4187),
            icon = DefaultIcon(fillColor = Color.Green, label = "C", scale = 0.6f),
            extra = "Room C"
        )
    }
}
```

## イベント処理

GroundImage のインタラクションは、マップ地図SDKコンポーネントで処理されます:

```kotlin
// MapView を GoogleMapView、MapboxMapView などのマップ地図SDKに置き換えてください
MapView(
    state = mapViewState,
    onGroundImageClick = { groundImageEvent ->
        val groundImage = groundImageEvent.state
        val clickPoint = groundImageEvent.clicked

        println("Ground image clicked:")
        println("  Bounds: ${groundImage.bounds}")
        println("  Opacity: ${groundImage.opacity}")
        println("  Click location: ${clickPoint}")
        println("  Extra data: ${groundImage.extra}")
    }
) {
    GroundImage(
        bounds = bounds,
        image = drawable,
        opacity = 0.7f,
        extra = "Interactive overlay"
    )
}
```

## 画像リソース

### リソースからの読み込み

```kotlin
val context = LocalContext.current
val drawable = AppCompatResources.getDrawable(context, R.drawable.overlay_image)
```

### Assets からの読み込み

```kotlin
val context = LocalContext.current
val inputStream = context.assets.open("overlays/map_overlay.png")
val drawable = Drawable.createFromStream(inputStream, null)
```

### ネットワークからの読み込み（キャッシュ付き）

```kotlin
// Coil や Glide などのライブラリを使用
val imageLoader = ImageLoader(context)
val request = ImageRequest.Builder(context)
    .data("https://example.com/overlay.png")
    .build()

LaunchedEffect(request) {
    val drawable = imageLoader.execute(request).drawable
    // GroundImage で drawable を使用
}
```

## ベストプラクティス

1. **画像サイズ**: 品質とパフォーマンスのバランスをとるために、適切なサイズの画像を使用してください
2. **不透明度**: 透明度を使用して、オーバーレイを地図と自然に融合させてください
3. **境界の精度**: GroundImage の境界が地理的特徴と正確に整列していることを確認してください
4. **リソース管理**: 画像リソースを最適化し、メモリ使用量を考慮してください
5. **レイヤー順序**: 複数の GroundImage を重ねる場合は、描画順序を考慮してください
6. **インタラクティブ要素**: インタラクティブな GroundImage には明確な視覚的フィードバックを提供してください
7. **キャッシング**: 特にネットワークから読み込まれる画像については、画像リソースを効率的にキャッシュしてください
8. **エラー処理**: 画像の読み込みに失敗した場合を処理してください
9. **パフォーマンス**: 大きな GroundImage を同時に多数レンダリングすることは避けてください
10. **ユーザーエクスペリエンス**: 適切な場合は、不透明度と可視性のコントロールを提供してください
