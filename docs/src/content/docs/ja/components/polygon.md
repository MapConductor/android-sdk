---
title: "Polygon"
---

ポリゴンは、カスタマイズ可能なストロークと塗りつぶしプロパティを持つエリアを定義する閉じた形状です。ゾーン、地域、境界、またはエリアベースの機能を表現するのに便利です。

## Composable 関数

### 基本的な Polygon

```kotlin
@Composable
fun MapViewScope.Polygon(
    points: List<GeoPoint>,
    id: String? = null,
    strokeColor: Color = Color.Black,
    strokeWidth: Dp = 1.dp,
    fillColor: Color = Color.Transparent,
    geodesic: Boolean = false,
    extra: Serializable? = null
)
```

### State を使用した Polygon

```kotlin
@Composable
fun MapViewScope.Polygon(state: PolygonState)
```

## パラメータ

- **`points`**: ポリゴンの頂点を定義する地理座標のリスト（`List<GeoPoint>`）
- **`id`**: ポリゴンのオプションの一意識別子（`String?`）
- **`strokeColor`**: ポリゴンの境界線の色（デフォルト: `Color.Black`）
- **`strokeWidth`**: 境界線の幅（デフォルト: `1.dp`）
- **`fillColor`**: ポリゴン内部の塗りつぶし色（デフォルト: `Color.Transparent`）
- **`geodesic`**: 測地線エッジを描画するかどうか（デフォルト: `false`）
- **`extra`**: ポリゴンに付加する追加データ（`Serializable?`）

## 使用例

### 基本的な Polygon

```kotlin
// MapView を GoogleMapsView、MapboxMapView などのマップ地図SDKに置き換えてください
MapView(state = mapViewState) {
    val trianglePoints = listOf(
        GeoPointImpl.fromLatLong(37.7749, -122.4194), // ポイント 1
        GeoPointImpl.fromLatLong(37.7849, -122.4094), // ポイント 2
        GeoPointImpl.fromLatLong(37.7749, -122.3994), // ポイント 3
        GeoPointImpl.fromLatLong(37.7749, -122.4194)  // ポリゴンを閉じる
    )

    Polygon(
        points = trianglePoints,
        strokeColor = Color.Blue,
        strokeWidth = 2.dp,
        fillColor = Color.Blue.copy(alpha = 0.3f)
    )
}
```

### 頂点マーカー付きのインタラクティブな Polygon

サンプルアプリのパターンに基づいた例:

```kotlin
@Composable
fun InteractivePolygonExample() {
    var vertices by remember {
        mutableStateOf(
            listOf(
                GeoPointImpl.fromLatLong(37.7749, -122.4194),
                GeoPointImpl.fromLatLong(37.7849, -122.4094),
                GeoPointImpl.fromLatLong(37.7799, -122.3994),
                GeoPointImpl.fromLatLong(37.7649, -122.4094)
            )
        )
    }

    // 最初のポイントを最後に追加してポリゴンを閉じる
    val closedVertices = vertices + vertices.first()

    val polygonState = PolygonState(
        points = closedVertices,
        strokeColor = Color.Red,
        strokeWidth = 3.dp,
        fillColor = Color.Red.copy(alpha = 0.2f),
        geodesic = false
    )

    val vertexMarkers = vertices.mapIndexed { index, point ->
        MarkerState(
            position = point,
            icon = DefaultIcon(
                fillColor = Color.Red,
                strokeColor = Color.White,
                label = "${index + 1}",
                scale = 0.8f
            ),
            draggable = true,
            extra = "Vertex $index"
        )
    }

    // MapView を GoogleMapsView、MapboxMapView などのマップ地図SDKに置き換えてください
MapView(
        state = mapViewState,
        onMarkerDrag = { markerState ->
            val vertexIndex = markerState.extra.toString().substringAfter("Vertex ").toIntOrNull()
            vertexIndex?.let { index ->
                vertices = vertices.toMutableList().apply {
                    if (index < size) {
                        set(index, markerState.position)
                    }
                }
            }
        },
        onPolygonClick = { polygonEvent ->
            println("Polygon clicked at: ${polygonEvent.clicked}")
        }
    ) {
        // ポリゴンを描画
        Polygon(polygonState)

        // 頂点マーカーを描画
        vertexMarkers.forEach { marker ->
            Marker(marker)
        }
    }
}
```

### 異なるスタイルの複数の Polygon

```kotlin
// MapView を GoogleMapsView、MapboxMapView などのマップ地図SDKに置き換えてください
MapView(state = mapViewState) {
    // ソリッドな塗りつぶしのポリゴン
    Polygon(
        points = listOf(
            GeoPointImpl.fromLatLong(37.7749, -122.4194),
            GeoPointImpl.fromLatLong(37.7849, -122.4194),
            GeoPointImpl.fromLatLong(37.7849, -122.4094),
            GeoPointImpl.fromLatLong(37.7749, -122.4094),
            GeoPointImpl.fromLatLong(37.7749, -122.4194)
        ),
        strokeColor = Color.Blue,
        strokeWidth = 2.dp,
        fillColor = Color.Blue.copy(alpha = 0.4f),
        extra = "Blue zone"
    )

    // 境界線のみのポリゴン
    Polygon(
        points = listOf(
            GeoPointImpl.fromLatLong(37.7649, -122.4194),
            GeoPointImpl.fromLatLong(37.7749, -122.4194),
            GeoPointImpl.fromLatLong(37.7749, -122.4094),
            GeoPointImpl.fromLatLong(37.7649, -122.4094),
            GeoPointImpl.fromLatLong(37.7649, -122.4194)
        ),
        strokeColor = Color.Red,
        strokeWidth = 3.dp,
        fillColor = Color.Transparent,
        extra = "Red boundary"
    )

    // 半透明のポリゴン
    Polygon(
        points = listOf(
            GeoPointImpl.fromLatLong(37.7549, -122.4194),
            GeoPointImpl.fromLatLong(37.7649, -122.4194),
            GeoPointImpl.fromLatLong(37.7649, -122.4094),
            GeoPointImpl.fromLatLong(37.7549, -122.4094),
            GeoPointImpl.fromLatLong(37.7549, -122.4194)
        ),
        strokeColor = Color.Green,
        strokeWidth = 1.dp,
        fillColor = Color.Green.copy(alpha = 0.2f),
        extra = "Green area"
    )
}
```

### 複雑なポリゴン形状

```kotlin
@Composable
fun ComplexPolygonExample() {
    // 星形のポリゴン
    val starPoints = remember {
        val centerLat = 37.7749
        val centerLng = -122.4194
        val outerRadius = 0.01
        val innerRadius = 0.005

        buildList {
            for (i in 0 until 10) {
                val angle = (i * 36.0) * Math.PI / 180.0
                val radiusMeters = if (i % 2 == 0) outerRadius else innerRadius
                val lat = centerLat + radiusMeters * cos(angle)
                val lng = centerLng + radiusMeters * sin(angle)
                add(GeoPointImpl.fromLatLong(lat, lng))
            }
            // ポリゴンを閉じる
            add(first())
        }
    }

    // MapView を GoogleMapsView、MapboxMapView などのマップ地図SDKに置き換えてください
MapView(state = mapViewState) {
        Polygon(
            points = starPoints,
            strokeColor = Color.Magenta,
            strokeWidth = 2.dp,
            fillColor = Color.Magenta.copy(alpha = 0.3f),
            extra = "Star polygon"
        )

        // 中心マーカー
        Marker(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = DefaultIcon(
                fillColor = Color.Magenta,
                label = "⭐"
            )
        )
    }
}
```

### 動的なポリゴン作成

```kotlin
@Composable
fun DynamicPolygonExample() {
    var polygonPoints by remember { mutableStateOf<List<GeoPoint>>(emptyList()) }
    var isCreating by remember { mutableStateOf(false) }

    Column {
        Row {
            Button(
                onClick = {
                    isCreating = !isCreating
                    if (!isCreating && polygonPoints.isNotEmpty()) {
                        // ポリゴンを閉じる
                        polygonPoints = polygonPoints + polygonPoints.first()
                    }
                }
            ) {
                Text(if (isCreating) "Finish Polygon" else "Create Polygon")
            }

            Button(
                onClick = { polygonPoints = emptyList() }
            ) {
                Text("Clear")
            }
        }

        Text("Points: ${polygonPoints.size}")

        // MapView を GoogleMapsView、MapboxMapView などのマップ地図SDKに置き換えてください
MapView(
            state = mapViewState,
            onMapClick = { geoPoint ->
                if (isCreating) {
                    polygonPoints = polygonPoints + geoPoint
                }
            }
        ) {
            if (polygonPoints.size >= 3) {
                val displayPoints = if (isCreating) polygonPoints
                                  else polygonPoints // すでに閉じている

                Polygon(
                    points = displayPoints,
                    strokeColor = Color.Purple,
                    strokeWidth = 2.dp,
                    fillColor = if (isCreating)
                        Color.Purple.copy(alpha = 0.1f)
                    else
                        Color.Purple.copy(alpha = 0.3f)
                )
            }

            // 頂点マーカーを表示
            polygonPoints.forEachIndexed { index, point ->
                Marker(
                    position = point,
                    icon = DefaultIcon(
                        fillColor = if (index == 0) Color.Green
                                  else if (index == polygonPoints.size - 1 && !isCreating) Color.Red
                                  else Color.Blue,
                        label = "${index + 1}",
                        scale = 0.7f
                    )
                )
            }
        }
    }
}
```

## イベント処理

Polygon のインタラクションは、マップ地図SDKコンポーネントで処理されます:

```kotlin
// MapView を GoogleMapsView、MapboxMapView などのマップ地図SDKに置き換えてください
MapView(
    state = mapViewState,
    onPolygonClick = { polygonEvent ->
        val polygon = polygonEvent.state
        val clickPoint = polygonEvent.clicked

        println("Polygon clicked:")
        println("  Vertices: ${polygon.points.size}")
        println("  Click location: ${clickPoint}")
        println("  Extra data: ${polygon.extra}")

        // 面積を計算（簡略化）
        val area = calculatePolygonArea(polygon.points)
        println("  Approximate area: $area sq meters")
    }
) {
    Polygon(
        points = polygonPoints,
        strokeColor = Color.Green,
        fillColor = Color.Green.copy(alpha = 0.3f),
        extra = "Interactive polygon"
    )
}
```

## スタイルオプション

### 塗りつぶしのバリエーション

```kotlin
// ソリッドな塗りつぶし
Polygon(
    points = points,
    fillColor = Color.Red
)

// 半透明の塗りつぶし
Polygon(
    points = points,
    fillColor = Color.Red.copy(alpha = 0.5f)
)

// 塗りつぶしなし（境界線のみ）
Polygon(
    points = points,
    fillColor = Color.Transparent
)

// グラデーションのような効果を持つ複数のポリゴン
Polygon(points = points, fillColor = Color.Blue.copy(alpha = 0.1f))
Polygon(points = smallerPoints, fillColor = Color.Blue.copy(alpha = 0.2f))
```

### ストロークのバリエーション

```kotlin
// 細い境界線
Polygon(
    points = points,
    strokeColor = Color.Black,
    strokeWidth = 1.dp
)

// 太い境界線
Polygon(
    points = points,
    strokeColor = Color.Black,
    strokeWidth = 5.dp
)

// 境界線なし
Polygon(
    points = points,
    strokeColor = Color.Transparent,
    strokeWidth = 0.dp
)
```

## ベストプラクティス

1. **ポリゴンを閉じる**: 常に最後のポイントが最初のポイントと等しいことを確認して、ポリゴンを適切に閉じてください
2. **頂点の順序**: 予測可能な結果を得るために、一貫した頂点の順序（時計回りまたは反時計回り）を使用してください
3. **パフォーマンス**: 数百の頂点を持つ過度に複雑なポリゴンは避けてください
4. **視覚的な明瞭さ**: 良好な視認性を得るために適切な色と透明度を使用してください
5. **インタラクティブなフィードバック**: ポリゴンがクリック可能な場合は、視覚的なフィードバックを提供してください
6. **穴の処理**: 異なる色を持つ重なり合うポリゴンを使用して穴をシミュレートしてください
7. **測地線エッジ**: 大きなポリゴンには、地球の曲率を考慮するために測地線エッジを使用してください
8. **状態管理**: ポリゴンの頂点データを効率的に管理し、更新をリアクティブに処理してください
9. **検証**: 有効な形状を形成することを確認するためにポリゴンのジオメトリを検証してください
10. **エラー処理**: 3つ未満の頂点を持つポリゴンなどのエッジケースを処理してください
