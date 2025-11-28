---
title: "GeoRectBounds（矩形範囲）"
---

`GeoRectBounds` は、南西と北東の角の点で定義される矩形の地理的エリアを表します。地図領域の定義、グラウンドイメージの境界、ビューポート計算に使用されます。

## コンストラクタ

```kotlin
class GeoRectBounds(
    southWest: GeoPointImpl? = null,
    northEast: GeoPointImpl? = null
)
```

## プロパティ

### 角の点

- **`southWest: GeoPointImpl?`**: 矩形の南西角（左下）
- **`northEast: GeoPointImpl?`**: 矩形の北東角（右上）
- **`isEmpty: Boolean`**: 境界が定義されていない場合は `true` を返します

### 計算されるプロパティ

- **`center: GeoPointImpl?`**: 境界の中心点
- **`toSpan(): GeoPointImpl?`**: GeoPoint として範囲（幅/高さ）を返します

## 基本的な使用方法

### 境界の作成

```kotlin
// 矩形エリアを定義
val bounds = GeoRectBounds(
    southWest = GeoPointImpl.fromLatLong(37.7649, -122.4294),
    northeast = GeoPointImpl.fromLatLong(37.7849, -122.4094)
)

// 空の境界（後で拡張）
val bounds = GeoRectBounds()
```

### GroundImage での使用

```kotlin
@Composable
fun GroundImageExample() {
    val imageBounds = GeoRectBounds(
        southWest = GeoPointImpl.fromLatLong(37.7649, -122.4294),
        northEast = GeoPointImpl.fromLatLong(37.7849, -122.4094)
    )

    // MapView を GoogleMapsView、MapboxMapView などの選択した地図SDKに置き換えてください
    MapView(state = mapViewState) {
        val context = LocalContext.current
        AppCompatResources.getDrawable(context, R.drawable.overlay_image)?.let { drawable ->
            GroundImage(
                bounds = imageBounds,
                image = drawable,
                opacity = 0.7f
            )
        }
    }
}
```

## 動的な境界の構築

### 境界の拡張

境界は新しい点を含むように動的に拡張できます:

```kotlin
fun extend(point: GeoPoint)

// 使用例
val bounds = GeoRectBounds() // 空からスタート

val points = listOf(
    GeoPointImpl.fromLatLong(37.7749, -122.4194),
    GeoPointImpl.fromLatLong(37.7849, -122.4094),
    GeoPointImpl.fromLatLong(37.7649, -122.4294)
)

points.forEach { point ->
    bounds.extend(point) // 各点を含むように境界が拡大
}

println("最終境界: $bounds")
```

### 境界の結合

```kotlin
fun union(other: GeoRectBounds): GeoRectBounds

// 使用例
val bounds1 = GeoRectBounds(
    southWest = GeoPointImpl.fromLatLong(37.7649, -122.4294),
    northEast = GeoPointImpl.fromLatLong(37.7749, -122.4194)
)

val bounds2 = GeoRectBounds(
    southWest = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    northEast = GeoPointImpl.fromLatLong(37.7849, -122.4094)
)

val combinedBounds = bounds1.union(bounds2) // 両方のエリアを含む
```

## 点と境界のテスト

### 点を含むかどうか

```kotlin
fun contains(point: GeoPoint): Boolean

// 使用例
val bounds = GeoRectBounds(
    southWest = GeoPointImpl.fromLatLong(37.7649, -122.4294),
    northEast = GeoPointImpl.fromLatLong(37.7849, -122.4094)
)

val testPoint = GeoPointImpl.fromLatLong(37.7749, -122.4194)
val isInside = bounds.contains(testPoint)

println("点は境界内: $isInside")
```

### 境界の交差

```kotlin
fun intersects(other: GeoRectBounds): Boolean

// 使用例
val bounds1 = GeoRectBounds(/* ... */)
val bounds2 = GeoRectBounds(/* ... */)

val doIntersect = bounds1.intersects(bounds2)
println("境界が交差: $doIntersect")
```

## 実用例

### ビューポート境界の計算

```kotlin
@Composable
fun ViewportBoundsExample() {
    var viewportBounds by remember { mutableStateOf<GeoRectBounds?>(null) }

    // 表示されているマーカーからビューポートを計算
    val markers = remember {
        listOf(
            GeoPointImpl.fromLatLong(37.7749, -122.4194),
            GeoPointImpl.fromLatLong(37.7849, -122.4094),
            GeoPointImpl.fromLatLong(37.7649, -122.4294),
            GeoPointImpl.fromLatLong(37.7949, -122.3994)
        )
    }

    LaunchedEffect(markers) {
        val bounds = GeoRectBounds()
        markers.forEach { marker ->
            bounds.extend(marker)
        }
        viewportBounds = bounds
    }

    // MapView を GoogleMapsView、MapboxMapView などの選択した地図SDKに置き換えてください
    MapView(state = mapViewState) {
        // すべてのマーカーを表示
        markers.forEach { position ->
            Marker(
                position = position,
                icon = DefaultIcon(fillColor = Color.Blue)
            )
        }

        // ビューポート境界をポリゴンとして表示
        viewportBounds?.let { bounds ->
            if (!bounds.isEmpty) {
                val sw = bounds.southWest!!
                val ne = bounds.northEast!!

                val boundsPolygon = listOf(
                    sw,
                    GeoPointImpl.fromLatLong(sw.latitude, ne.longitude),
                    ne,
                    GeoPointImpl.fromLatLong(ne.latitude, sw.longitude),
                    sw // ポリゴンを閉じる
                )

                Polygon(
                    points = boundsPolygon,
                    strokeColor = Color.Red,
                    strokeWidth = 2.dp,
                    fillColor = Color.Red.copy(alpha = 0.1f)
                )
            }
        }
    }
}
```

### 境界ベースの読み込み

```kotlin
@Composable
fun BoundsBasedLoadingExample() {
    var currentBounds by remember { mutableStateOf<GeoRectBounds?>(null) }
    var markersInBounds by remember { mutableStateOf<List<GeoPointImpl>>(emptyList()) }

    // 利用可能なすべてのマーカーをシミュレート
    val allMarkers = remember {
        List(100) { i ->
            GeoPointImpl.fromLatLong(
                37.7 + (i % 10) * 0.01,
                -122.5 + (i / 10) * 0.01
            )
        }
    }

    // 現在の境界に基づいてマーカーをフィルタ
    LaunchedEffect(currentBounds) {
        markersInBounds = currentBounds?.let { bounds ->
            if (bounds.isEmpty) {
                emptyList()
            } else {
                allMarkers.filter { marker ->
                    bounds.contains(marker)
                }
            }
        } ?: emptyList()
    }

    Column {
        Text("境界内のマーカー: ${markersInBounds.size}/${allMarkers.size}")

        Button(
            onClick = {
                // ビューポート境界を設定
                currentBounds = GeoRectBounds(
                    southWest = GeoPointImpl.fromLatLong(37.70, -122.50),
                    northEast = GeoPointImpl.fromLatLong(37.75, -122.45)
                )
            }
        ) {
            Text("ビューポートを設定")
        }

        // MapView を GoogleMapsView、MapboxMapView などの選択した地図SDKに置き換えてください
        MapView(state = mapViewState) {
            // 境界内のマーカーのみを表示
            markersInBounds.forEach { position ->
                Marker(
                    position = position,
                    icon = DefaultIcon(
                        fillColor = Color.Green,
                        scale = 0.8f
                    )
                )
            }

            // 現在の境界を表示
            currentBounds?.let { bounds ->
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
                        strokeWidth = 3.dp,
                        fillColor = Color.Blue.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }
}
```

### インタラクティブな境界エディタ

```kotlin
@Composable
fun BoundsEditorExample() {
    var bounds by remember {
        mutableStateOf(
            GeoRectBounds(
                southWest = GeoPointImpl.fromLatLong(37.7649, -122.4294),
                northEast = GeoPointImpl.fromLatLong(37.7849, -122.4094)
            )
        )
    }

    // MapView を GoogleMapsView、MapboxMapView などの選択した地図SDKに置き換えてください
    MapView(
        state = mapViewState,
        onMarkerDrag = { markerState ->
            val newPosition = markerState.position
            when (markerState.extra) {
                "SW" -> {
                    bounds = GeoRectBounds(
                        southWest = newPosition as GeoPointImpl,
                        northEast = bounds.northEast
                    )
                }
                "NE" -> {
                    bounds = GeoRectBounds(
                        southWest = bounds.southWest,
                        northEast = newPosition as GeoPointImpl
                    )
                }
            }
        }
    ) {
        // 境界の可視化
        if (!bounds.isEmpty) {
            val sw = bounds.southWest!!
            val ne = bounds.northEast!!

            // 境界の矩形
            Polygon(
                points = listOf(
                    sw,
                    GeoPointImpl.fromLatLong(sw.latitude, ne.longitude),
                    ne,
                    GeoPointImpl.fromLatLong(ne.latitude, sw.longitude),
                    sw
                ),
                strokeColor = Color.Red,
                fillColor = Color.Red.copy(alpha = 0.2f)
            )

            // 角のマーカー
            Marker(
                position = sw,
                icon = DefaultIcon(
                    fillColor = Color.Green,
                    label = "SW"
                ),
                draggable = true,
                extra = "SW"
            )

            Marker(
                position = ne,
                icon = DefaultIcon(
                    fillColor = Color.Red,
                    label = "NE"
                ),
                draggable = true,
                extra = "NE"
            )
        }
    }
}
```

## 特別な処理

### 国際日付変更線

`GeoRectBounds` は国際日付変更線（経度 ±180°）をまたぐ矩形を処理します:

```kotlin
// 日付変更線をまたぐ境界
val pacificBounds = GeoRectBounds(
    southWest = GeoPointImpl.fromLatLong(20.0, 170.0),  // 日付変更線の西側
    northEast = GeoPointImpl.fromLatLong(40.0, -170.0)  // 日付変更線の東側
)

val pointInPacific = GeoPointImpl.fromLatLong(30.0, 175.0)
val containsPoint = pacificBounds.contains(pointInPacific) // true
```

### 空の境界の処理

```kotlin
val emptyBounds = GeoRectBounds()

println(emptyBounds.isEmpty) // true
println(emptyBounds.center) // null
println(emptyBounds.toSpan()) // null

// 空の境界を拡張
emptyBounds.extend(GeoPointImpl.fromLatLong(37.7749, -122.4194))
println(emptyBounds.isEmpty) // false
```

## ユーティリティメソッド

### 文字列表現

```kotlin
// デバッグ用
val bounds = GeoRectBounds(
    southWest = GeoPointImpl.fromLatLong(37.7649, -122.4294),
    northEast = GeoPointImpl.fromLatLong(37.7849, -122.4094)
)

println(bounds.toString())
// 出力: ((37.7649, -122.4294), (37.7849, -122.4094))
```

### URL 値

```kotlin
fun toUrlValue(precision: Int = 6): String

// API 呼び出し用の使用
val urlString = bounds.toUrlValue() // "37.764900,-122.429400,37.784900,-122.409400"
val preciseString = bounds.toUrlValue(precision = 8)
```

### 等価性の比較

```kotlin
fun equalsTo(other: GeoRectBounds): Boolean

// 使用例
val bounds1 = GeoRectBounds(/* ... */)
val bounds2 = GeoRectBounds(/* ... */)

val areEqual = bounds1.equalsTo(bounds2)
```

## ベストプラクティス

1. **座標の順序**: 一貫性のため常に南西（左下）と北東（右上）を使用
2. **空の境界**: 計算に使用する前に `isEmpty` を確認
3. **日付変更線の交差**: クラスは国際日付変更線の交差を自動的に処理
4. **動的な構築**: 点のコレクションから境界を構築するには `extend()` を使用
5. **パフォーマンス**: 頻繁にアクセスされる領域の境界計算をキャッシュ
6. **検証**: 南西が実際に北東の南西であることを確認
7. **精度**: ユースケースに基づいて URL 値に適切な精度を使用
