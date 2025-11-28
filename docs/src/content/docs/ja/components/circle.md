---
title: "Circle"
---

円は、カスタマイズ可能な半径、ストローク、塗りつぶしプロパティを持つ円形のオーバーレイで、地図上に描画できます。エリア、範囲、またはゾーンを表現するのに便利です。

## Composable 関数

### 基本的な Circle

```kotlin
@Composable
fun MapViewScope.Circle(
    center: GeoPoint,
    radiusMeters: Double,
    strokeColor: Color = Color.Red,
    strokeWidth: Dp = 2.dp,
    fillColor: Color = Color.White.copy(alpha = 0.5f),
    extra: Serializable? = null,
    id: String? = null
)
```

### State を使用した Circle

```kotlin
@Composable
fun MapViewScope.Circle(state: CircleState)
```

## パラメータ

- **`center`**: 円の地理的中心点（`GeoPoint`）
- **`radiusMeters`**: 半径（メートル単位、`Double`）
- **`strokeColor`**: 円の境界線の色（デフォルト: `Color.Red`）
- **`strokeWidth`**: 境界線の幅（デフォルト: `2.dp`）
- **`fillColor`**: 円の内部の塗りつぶし色（デフォルト: 半透明の白）
- **`extra`**: 円に付加する追加データ（`Serializable?`）
- **`id`**: 円の一意識別子（`String?`）

## 使用例

### 基本的な Circle

```kotlin
// MapView を GoogleMapsView、MapboxMapView などのマップ地図SDKに置き換えてください
MapView(state = mapViewState) {
    Circle(
        center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        radiusMeters = 1000.0, // 半径1km
        strokeColor = Color.Blue,
        fillColor = Color.Blue.copy(alpha = 0.3f),
        id = "downtown-area"
    )
}
```

### マーカーを使ったインタラクティブな Circle

サンプルアプリに基づいて、ドラッグ可能なマーカーを使ったインタラクティブな円の作成方法を示します:

```kotlin
@Composable
fun InteractiveCircleExample() {
    var centerPosition by remember {
        mutableStateOf(GeoPointImpl.fromLatLong(37.7749, -122.4194))
    }
    var radiusMeters by remember { mutableStateOf(1000.0) }

    // エッジマーカーの位置を計算
    val edgePosition = remember(centerPosition, radiusMeters) {
        // 中心から 'radiusMeters' メートル離れた点を計算
        // これは簡略化されています - 実際の計算では地球の曲率を考慮します
        val latOffset = radiusMeters / 111000.0 // 緯度1度あたりのおおよそのメートル数
        GeoPointImpl.fromLatLong(
            centerPosition.latitude + latOffset,
            centerPosition.longitude
        )
    }

    val circleState = CircleState(
        center = centerPosition,
        radiusMeters = radiusMeters,
        strokeColor = Color.Blue,
        fillColor = Color.Blue.copy(alpha = 0.3f),
        clickable = true
    )

    val centerMarker = MarkerState(
        position = centerPosition,
        icon = DefaultIcon(
            fillColor = Color.Blue,
            label = "C"
        ),
        draggable = false
    )

    val edgeMarker = MarkerState(
        position = edgePosition,
        icon = DefaultIcon(
            fillColor = Color.Green,
            label = "E"
        ),
        draggable = true
    )

    // MapView を GoogleMapsView、MapboxMapView などのマップ地図SDKに置き換えてください
MapView(
        state = mapViewState,
        onMarkerDrag = { markerState ->
            if (markerState.id == edgeMarker.id) {
                // エッジマーカーの位置に基づいて新しい半径を計算
                val distance = calculateDistance(centerPosition, markerState.position)
                radiusMeters = distance
            }
        },
        onCircleClick = { circleEvent ->
            println("Circle clicked at: ${circleEvent.clicked}")
        }
    ) {
        Circle(circleState)
        Marker(centerMarker)
        Marker(edgeMarker)
    }
}
```

### 異なるスタイルの複数の Circle

```kotlin
// MapView を GoogleMapsView、MapboxMapView などのマップ地図SDKに置き換えてください
MapView(state = mapViewState) {
    // ソリッドな赤い円
    Circle(
        center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        radiusMeters = 500.0,
        strokeColor = Color.Red,
        strokeWidth = 3.dp,
        fillColor = Color.Red.copy(alpha = 0.2f),
        extra = "Red zone"
    )

    // 太い境界線の青い円
    Circle(
        center = GeoPointImpl.fromLatLong(37.7849, -122.4194),
        radiusMeters = 750.0,
        strokeColor = Color.Blue,
        strokeWidth = 5.dp,
        fillColor = Color.Transparent,
        extra = "Blue boundary"
    )

    // パターン付きの緑の円
    Circle(
        center = GeoPointImpl.fromLatLong(37.7649, -122.4194),
        radiusMeters = 300.0,
        strokeColor = Color.Green,
        strokeWidth = 2.dp,
        fillColor = Color.Green.copy(alpha = 0.4f),
        extra = "Green area"
    )
}
```

### 動的な Circle の更新

```kotlin
@Composable
fun DynamicCircleExample() {
    var circleRadius by remember { mutableStateOf(500.0) }
    var circleColor by remember { mutableStateOf(Color.Blue) }

    Column {
        // コントロール
        Slider(
            value = circleRadius.toFloat(),
            onValueChange = { circleRadius = it.toDouble() },
            valueRange = 100f..2000f,
            modifier = Modifier.padding(16.dp)
        )

        Row {
            Button(onClick = { circleColor = Color.Red }) {
                Text("Red")
            }
            Button(onClick = { circleColor = Color.Blue }) {
                Text("Blue")
            }
            Button(onClick = { circleColor = Color.Green }) {
                Text("Green")
            }
        }

        // 動的な円を持つマップ
        // MapView を GoogleMapsView、MapboxMapView などのマップ地図SDKに置き換えてください
MapView(state = mapViewState) {
            Circle(
                center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                radiusMeters = circleRadius,
                strokeColor = circleColor,
                fillColor = circleColor.copy(alpha = 0.3f)
            )

            // 中心マーカー
            Marker(
                position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                icon = DefaultIcon(
                    fillColor = circleColor,
                    label = "${circleRadius.toInt()}m"
                )
            )
        }
    }
}
```

### Z-Index を使った重なり合う Circle

```kotlin
// MapView を GoogleMapsView、MapboxMapView などのマップ地図SDKに置き換えてください
MapView(state = mapViewState) {
    val centerPoint = GeoPointImpl.fromLatLong(37.7749, -122.4194)

    // 背景の円（より大きく、低い z-index）
    Circle(
        center = centerPoint,
        radiusMeters = 1000.0,
        strokeColor = Color.Red,
        fillColor = Color.Red.copy(alpha = 0.2f),
        extra = CircleData(zIndex = 1, name = "Outer circle")
    )

    // 前景の円（より小さく、高い z-index）
    Circle(
        center = centerPoint,
        radiusMeters = 500.0,
        strokeColor = Color.Blue,
        fillColor = Color.Blue.copy(alpha = 0.4f),
        extra = CircleData(zIndex = 2, name = "Inner circle")
    )
}
```

## イベント処理

Circle のインタラクションは、マップ地図SDKコンポーネントで処理されます:

```kotlin
// MapView を GoogleMapsView、MapboxMapView などのマップ地図SDKに置き換えてください
MapView(
    state = mapViewState,
    onCircleClick = { circleEvent ->
        val circle = circleEvent.state
        val clickPoint = circleEvent.clicked

        println("Circle clicked:")
        println("  Center: ${circle.center}")
        println("  Radius: ${circle.radiusMeters}m")
        println("  Click point: ${clickPoint}")
        println("  Extra data: ${circle.extra}")
    }
) {
    Circle(
        center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        radiusMeters = 1000.0,
        clickable = true,
        extra = "Clickable circle"
    )
}
```

## スタイルオプション

### ストロークスタイル

```kotlin
// 細い境界線
Circle(
    center = center,
    radiusMeters = 500.0,
    strokeColor = Color.Black,
    strokeWidth = 1.dp
)

// 太い境界線
Circle(
    center = center,
    radiusMeters = 500.0,
    strokeColor = Color.Black,
    strokeWidth = 5.dp
)

// 境界線なし
Circle(
    center = center,
    radiusMeters = 500.0,
    strokeColor = Color.Transparent,
    strokeWidth = 0.dp
)
```

### 塗りつぶしスタイル

```kotlin
// ソリッドな塗りつぶし
Circle(
    center = center,
    radiusMeters = 500.0,
    fillColor = Color.Red
)

// 半透明の塗りつぶし
Circle(
    center = center,
    radiusMeters = 500.0,
    fillColor = Color.Red.copy(alpha = 0.5f)
)

// 塗りつぶしなし
Circle(
    center = center,
    radiusMeters = 500.0,
    fillColor = Color.Transparent
)
```

## Circle の識別

### ID プロパティの使用

`id` プロパティは円に一意の識別子を提供し、効率的な追跡と管理を可能にします:

```kotlin
// 一意の ID を持つ円の作成
val circles = listOf(
    Circle(
        center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        radiusMeters = 1000.0,
        strokeColor = Color.Red,
        fillColor = Color.Red.copy(alpha = 0.3f),
        id = "zone-a"
    ),
    Circle(
        center = GeoPointImpl.fromLatLong(37.7849, -122.4094),
        radiusMeters = 1500.0,
        strokeColor = Color.Blue,
        fillColor = Color.Blue.copy(alpha = 0.3f),
        id = "zone-b"
    )
)

// イベント処理での ID の使用
MapView(
    state = mapViewState,
    onCircleClick = { circleEvent ->
        when (circleEvent.state.id) {
            "zone-a" -> handleZoneA()
            "zone-b" -> handleZoneB()
            else -> handleUnknownZone()
        }
    }
) {
    circles.forEach { circle -> Circle(circle) }
}
```

### ID 使用のメリット

- **一意の識別**: 似たプロパティを持つ円でも区別できます
- **イベント処理**: クリックイベント処理とエリア固有のロジックを簡素化します
- **状態管理**: 効率的な更新と選択管理を可能にします
- **パフォーマンス**: 最適化されたレンダリングと更新を促進します

## ベストプラクティス

1. **適切な半径を使用**: 円の半径を設定する際は、マップのズームレベルを考慮してください
2. **一意の ID を提供**: 複数の円を扱う場合は、常に一意の `id` 値を設定してください
3. **色のコントラスト**: ストロークと塗りつぶしの色が地図上で良好な視認性を提供することを確認してください
4. **パフォーマンス**: 大きな円を多数作成すると、レンダリングパフォーマンスに影響を与える可能性があるため避けてください
5. **インタラクティブなフィードバック**: 円がクリック可能な場合は、視覚的なフィードバックを提供してください
6. **一貫したスタイル**: アプリケーション全体で一貫した円のスタイルを維持してください
7. **追加データ**: `extra` パラメータを使用して、イベント処理のためのメタデータを保存してください
8. **Z-Index の管理**: 円が重なる場合は、描画順序を考慮してください
