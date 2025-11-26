---
title: "MapCameraPosition（カメラ位置）"
---

`MapCameraPosition` は、カメラの視野位置、方向、および地図上の可視領域を表します。カメラがどこを見ているか、地図のどの程度が表示されるか、地図を表示する視点を定義します。

## インターフェースと実装

### MapCameraPosition インターフェース

```kotlin
interface MapCameraPosition {
    val position: GeoPoint
    val zoom: Double
    val bearing: Double
    val tilt: Double
    val paddings: MapPaddings?
    val visibleRegion: VisibleRegion?
}
```

### MapCameraPositionImpl

メイン実装は不変のカメラ位置データを提供します:

```kotlin
data class MapCameraPositionImpl(
    override val position: GeoPointImpl,
    override val zoom: Double = 0.0,
    override val bearing: Double = 0.0,
    override val tilt: Double = 0.0,
    override val paddings: MapPaddings? = MapPaddingsImpl.Zeros,
    override val visibleRegion: VisibleRegion? = null
) : MapCameraPosition
```

## プロパティ

### カメラ位置

- **`position: GeoPoint`**: カメラの視野の地理的中心点
- **`zoom: Double`**: ズームレベル（おおよそ Google Maps のスケールに従います）
- **`bearing: Double`**: コンパスの方向（度数）（0 = 北、90 = 東）
- **`tilt: Double`**: カメラの傾斜角度（度数）（0 = 真上から、90 = 水平）

### ビュー構成

- **`paddings: MapPaddings?`**: 可視領域に影響するビューポートのパディング
- **`visibleRegion: VisibleRegion?`**: 画面上で実際に表示される地理的境界

## 生成メソッド

### デフォルト位置

```kotlin
// 原点のデフォルトカメラ位置
val defaultPosition = MapCameraPositionImpl.Default
```

### カスタム位置

```kotlin
// 基本的なカメラ位置
val sanFrancisco = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    zoom = 15.0
)

// 方位角と傾きを持つカメラ
val aerialView = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    zoom = 18.0,
    bearing = 45.0,  // 北東方向
    tilt = 60.0      // 角度のある視野
)

// UI 要素のためのパディングを持つカメラ
val paddedView = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    zoom = 14.0,
    paddings = MapPaddingsImpl(
        top = 100.0,
        left = 50.0,
        bottom = 200.0,
        right = 50.0
    )
)
```

## ズームレベル

MapConductor のズームレベルは、おおよそ Google Maps のスケールに従いますが、プロバイダ間で若干異なる場合があります:

- **0-2**: 世界ビュー、大陸が表示される
- **3-5**: 国レベル
- **6-9**: 州/地域レベル
- **10-12**: 都市レベル
- **13-15**: 地区/近隣レベル
- **16-18**: ストリートレベル
- **19-21**: 建物レベル（高詳細）

```kotlin
// 異なる用途のための異なるズームレベル
val worldView = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(0.0, 0.0),
    zoom = 2.0  // 大陸を表示
)

val cityView = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    zoom = 12.0  // 都市全体を表示
)

val streetView = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    zoom = 17.0  // 個別の通りを表示
)
```

## 方位角と傾き

### 方位角（回転）

方位角は中心点を中心に地図を回転させます:

```kotlin
// 北向き（デフォルト）
val northUp = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    bearing = 0.0
)

// 東向き
val eastUp = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    bearing = 90.0
)

// ルート方位角に従う
val routeBearing = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    bearing = 135.0,  // 南東
    zoom = 18.0
)
```

### 傾き（3D 視点）

傾きは 3D 表示角度を提供します:

```kotlin
// 真上から見た視野（デフォルト）
val topDown = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    tilt = 0.0
)

// 深度のための軽い角度
val angled = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    tilt = 30.0,
    zoom = 16.0
)

// ストリートレベルビューのための最大傾き
val streetLevel = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    tilt = 80.0,
    bearing = 45.0,
    zoom = 19.0
)
```

## 可視領域

可視領域は、カメラ位置、ズームレベル、方位角、傾き、およびビューポートのパディングを考慮した後、画面上に表示される実際の地理的エリアを記述します。

### VisibleRegion クラス

```kotlin
data class VisibleRegion(
    val bounds: GeoRectBounds,        // 全体的な境界矩形
    val nearLeft: GeoPoint?,          // 左下角（カメラに近い）
    val nearRight: GeoPoint?,         // 右下角（カメラに近い）
    val farLeft: GeoPoint?,           // 左上角（カメラから遠い）
    val farRight: GeoPoint?           // 右上角（カメラから遠い）
)
```

### プロパティ

- **`bounds: GeoRectBounds`**: 可視領域全体を包含する矩形の地理的境界。これは、すべての可視コンテンツを含む最小の境界矩形です。

- **`nearLeft: GeoPoint?`**: 可視領域の左下角の地理的座標。「near」はカメラ位置に最も近い側を指します。

- **`nearRight: GeoPoint?`**: 可視領域の右下角の地理的座標。

- **`farLeft: GeoPoint?`**: 可視領域の左上角の地理的座標。「far」はカメラ位置から最も遠い側を指します。

- **`farRight: GeoPoint?`**: 可視領域の右上角の地理的座標。

### カメラパラメータによる動作

可視領域はすべてのカメラパラメータの影響を受けます:

- **ズームレベル**: 高いズームは小さい地理的エリアを表示しますが、より詳細です
- **方位角**: カメラの回転により、画面の端に対応する地理的方向が変わります
- **傾き**: 3D 視点が可視領域の形状に影響します
- **パディング**: 効果的なビューポートを減らし、カメラの中心を移動せずに可視領域を変更します

### 角の点と境界

`bounds` はシンプルな矩形境界を提供しますが、角の点（`nearLeft`、`nearRight`、`farLeft`、`farRight`）は各画面の角の正確な地理的座標を提供します。これは特に以下の場合に重要です:

- カメラが方位角回転を持つ場合（角が基本方位に整列していない）
- カメラが傾きを持つ場合（可視領域が完全に矩形でない可能性がある）
- 高精度の境界検出が必要な場合

### 可視領域の使用

```kotlin
@Composable
fun VisibleRegionExample() {
    var cameraPosition by remember { mutableStateOf<MapCameraPosition?>(null) }

    // MapView を GoogleMapsView、MapboxMapView などの選択した地図プロバイダに置き換えてください
    MapView(
        state = mapViewState,
    ) {
        // 可視領域情報を表示
        cameraPosition?.visibleRegion?.let { region ->
            // 境界をポリゴンとして表示
            region.bounds?.let { bounds ->
                if (!bounds.isEmpty) {
                    val sw = bounds.southWest!!
                    val ne = bounds.northEast!!

                    Polygon(
                        points = listOf(
                            sw,
                            GeoPointImpl.fromLatLong(sw.latitude, ne.longitude),
                            ne,
                            GeoPointImpl.fromLatLong(ne.latitude, sw.longitude),
                            sw
                        ),
                        strokeColor = Color.Blue,
                        strokeWidth = 2.dp,
                        fillColor = Color.Blue.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }
}
```

## アニメーションと遷移

### スムーズなカメラ移動

```kotlin
@Composable
fun AnimatedCameraExample() {
    val locations = listOf(
        GeoPointImpl.fromLatLong(37.7749, -122.4194), // サンフランシスコ
        GeoPointImpl.fromLatLong(40.7128, -74.0060),  // ニューヨーク
        GeoPointImpl.fromLatLong(51.5074, -0.1278)    // ロンドン
    )

    val mapViewState = rememberHereMapViewState(
        cameraPosition = MapCameraPositionImpl(
            position = locations[0],
            zoom = 6.0
        ),
    )

    var currentIndex by remember { mutableStateOf(0) }

    // 5秒ごとに次の位置にアニメーション
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            currentIndex = (currentIndex + 1) % locations.size

            val targetPosition = MapCameraPositionImpl(
                position = locations[currentIndex],
                zoom = 6.0,
                bearing = 0.0,
                tilt = 0.0
            )
            mapViewState.moveCameraTo(targetPosition, 1000)
        }
    }


    // MapView を GoogleMapsView、MapboxMapView などの選択した地図プロバイダに置き換えてください
    HereMapView(
        state = mapViewState,
        modifier = modifier,
    ) {
        // 各位置にマーカーを追加
        locations.forEachIndexed { index, location ->
            Marker(
                position = location,
                icon = DefaultIcon(
                    fillColor = if (index == currentIndex) Color.Red else Color.Gray,
                    label = when (index) {
                        0 -> "SF"
                        1 -> "NYC"
                        2 -> "LON"
                        else -> "$index"
                    }
                )
            )
        }
    }
}
```

### インタラクティブなカメラ制御

```kotlin
@Composable
fun CameraControlExample(modifier: Modifier = Modifier) {
    val mapViewState = rememberMapLibreMapViewState(
        mapDesign = MapLibreDesignType(
            id = "debug-tiles",
            styleJsonURL = "https://demotiles.maplibre.org/debug-tiles/style.json",
        ),
        cameraPosition = MapCameraPositionImpl(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            zoom = 15.0
        ),
    )

    Column(
        modifier = modifier,
    ) {
        // カメラコントロール
        Row {
            Button(
                onClick = {
                    val newZoom = mapViewState.cameraPosition.copy(
                        zoom = (mapViewState.cameraPosition.zoom + 1)
                            .coerceAtMost(21.0),
                    )
                    mapViewState.moveCameraTo(newZoom, 500)
                }
            ) {
                Text("ズームイン")
            }

            Button(
                onClick = {
                    val newZoom = mapViewState.cameraPosition.copy(
                        zoom = (mapViewState.cameraPosition.zoom - 1)
                            .coerceAtMost(21.0),
                    )
                    mapViewState.moveCameraTo(newZoom, 500)
                }
            ) {
                Text("ズームアウト")
            }

            Button(
                onClick = {
                    val newZoom = mapViewState.cameraPosition.copy(
                        bearing = (mapViewState.cameraPosition.bearing + 45) % 360,
                    )
                    mapViewState.moveCameraTo(newZoom, 500)
                }
            ) {
                Text("回転")
            }
        }

        // 傾きスライダー
        Slider(
            value = mapViewState.cameraPosition.tilt.toFloat(),
            onValueChange = { tilt ->
                val newZoom = mapViewState.cameraPosition.copy(
                    tilt = tilt.toDouble(),
                )
                mapViewState.moveCameraTo(newZoom)
            },
            valueRange = 0f..80f
        )

        // MapView を GoogleMapsView、MapboxMapView などの選択した地図プロバイダに置き換えてください
        MapLibreMapView(
            state = mapViewState,
        ) {
            Marker(
                position = mapViewState.cameraPosition.position,
                icon = DefaultIcon(fillColor = Color.Red)
            )
        }
    }
}
```

## ベストプラクティス

1. **ズームレベルの選択**: ユースケースに適切なズームレベルを選択
2. **スムーズな遷移**: より良いユーザー体験のため段階的なカメラ移動を使用
3. **パディング管理**: カメラ位置を設定する際に UI 要素を考慮
4. **パフォーマンス**: 高価な再描画を引き起こす頻繁なカメラ位置の変更を避ける
5. **ユーザーコンテキスト**: 初期カメラ位置を設定する際にユーザーが見る必要があるものを考慮
6. **アクセシビリティ**: ジェスチャーベースのカメラ移動を使用できないユーザーのためのコントロールを提供

## 一般的なユースケース

### ユーザー位置にフォーカス

```kotlin
val userLocationCamera = MapCameraPositionImpl(
    position = userCurrentLocation,
    zoom = 16.0,  // ストリートレベルの詳細
    bearing = 0.0,
    tilt = 0.0
)
```

### 複数の点を表示

```kotlin
// すべての点を含む境界を計算し、適切なズームを設定
val allPoints = listOf(/* your points */)
val bounds = GeoRectBounds()
allPoints.forEach { bounds.extend(it) }

val centerCamera = MapCameraPositionImpl(
    position = bounds.center ?: GeoPointImpl.fromLatLong(0.0, 0.0),
    zoom = calculateZoomForBounds(bounds),  // カスタム計算
    bearing = 0.0,
    tilt = 0.0
)
```

### ナビゲーションモード

```kotlin
val navigationCamera = MapCameraPositionImpl(
    position = currentRoutePosition,
    zoom = 18.0,
    bearing = currentHeading,  // 移動方向
    tilt = 60.0               // ストリートビューのための角度
)
```
