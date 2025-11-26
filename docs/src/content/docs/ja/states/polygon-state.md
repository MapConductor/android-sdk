---
title: "PolygonState（ポリゴン状態）"
---

`PolygonState` は、地図上のポリゴン（塗りつぶされた領域）オーバーレイの構成と動作を管理します。頂点、ストローク、塗りつぶし、測地線オプションのためのリアクティブなプロパティを提供します。

## コンストラクタ

```kotlin
PolygonState(
    points: List<GeoPoint>,
    id: String? = null,
    strokeColor: Color = Color.Black,
    strokeWidth: Dp = 2.dp,
    fillColor: Color = Color.Transparent,
    geodesic: Boolean = false,
    extra: Serializable? = null
)
```

## プロパティ

### 基本プロパティ
- **`id: String`**: 一意の識別子（指定されていない場合は自動生成）
- **`points: List<GeoPoint>`**: ポリゴンを定義する頂点のリスト（閉じている必要があります）
- **`extra: Serializable?`**: ポリゴンに付加される追加データ

### ビジュアルプロパティ
- **`strokeColor: Color`**: 枠線の色
- **`strokeWidth: Dp`**: 枠線の太さ
- **`fillColor: Color`**: 内部の塗りつぶし色
- **`geodesic: Boolean`**: 測地線エッジを描画するかどうか

## メソッド

```kotlin
fun copy(...): PolygonState // 変更されたコピーを作成
fun fingerPrint(): PolygonFingerPrint // 変更検知
fun asFlow(): Flow<PolygonFingerPrint> // リアクティブ更新
```

## 使用例

### 基本的なポリゴン

```kotlin
val polygonState = PolygonState(
    points = listOf(
        GeoPointImpl.fromLatLong(37.7749, -122.4194),
        GeoPointImpl.fromLatLong(37.7849, -122.4094),
        GeoPointImpl.fromLatLong(37.7749, -122.3994),
        GeoPointImpl.fromLatLong(37.7749, -122.4194) // ポリゴンを閉じる
    ),
    strokeColor = Color.Blue,
    strokeWidth = 2.dp,
    fillColor = Color.Blue.copy(alpha = 0.3f)
)

// MapViewは、GoogleMapsView、MapboxMapViewなど、選択した地図プロバイダーに置き換えてください
MapView(state = mapViewState) {
    Polygon(polygonState)
}
```

### インタラクティブなポリゴン

```kotlin
@Composable
fun EditablePolygonExample() {
    var polygonState by remember {
        mutableStateOf(
            PolygonState(
                points = listOf(
                    GeoPointImpl.fromLatLong(37.7749, -122.4194),
                    GeoPointImpl.fromLatLong(37.7849, -122.4094),
                    GeoPointImpl.fromLatLong(37.7799, -122.3994),
                    GeoPointImpl.fromLatLong(37.7749, -122.4194)
                ),
                strokeColor = Color.Red,
                fillColor = Color.Red.copy(alpha = 0.2f)
            )
        )
    }

    // MapViewは、GoogleMapsView、MapboxMapViewなど、選択した地図プロバイダーに置き換えてください
MapView(
        state = mapViewState,
        onPolygonClick = { event ->
            println("Polygon clicked at: ${event.clicked}")
        }
    ) {
        Polygon(polygonState)

        // 頂点マーカー（閉じる点を除く）
        polygonState.points.dropLast(1).forEachIndexed { index, point ->
            Marker(
                position = point,
                icon = DefaultIcon(
                    fillColor = Color.Red,
                    label = "${index + 1}",
                    scale = 0.8f
                ),
                draggable = true,
                extra = "vertex_$index"
            )
        }
    }
}
```

### ゾーンポリゴン

```kotlin
val zonePolygons = listOf(
    PolygonState(
        points = restrictedZonePoints,
        strokeColor = Color.Red,
        strokeWidth = 3.dp,
        fillColor = Color.Red.copy(alpha = 0.2f),
        extra = "Restricted Zone"
    ),
    PolygonState(
        points = parkingZonePoints,
        strokeColor = Color.Blue,
        strokeWidth = 2.dp,
        fillColor = Color.Blue.copy(alpha = 0.2f),
        extra = "Parking Zone"
    ),
    PolygonState(
        points = safeZonePoints,
        strokeColor = Color.Green,
        strokeWidth = 2.dp,
        fillColor = Color.Green.copy(alpha = 0.2f),
        extra = "Safe Zone"
    )
)

// MapViewは、GoogleMapsView、MapboxMapViewなど、選択した地図プロバイダーに置き換えてください
MapView(state = mapViewState) {
    zonePolygons.forEach { zone ->
        Polygon(zone)
    }
}
```

### 複雑な形状

```kotlin
@Composable
fun ComplexPolygonExample() {
    // 星型ポリゴン
    val starPolygon = remember {
        val center = GeoPointImpl.fromLatLong(37.7749, -122.4194)
        val points = mutableListOf<GeoPoint>()

        for (i in 0 until 10) {
            val angle = (i * 36.0) * Math.PI / 180.0
            val radius = if (i % 2 == 0) 0.01 else 0.005
            val lat = center.latitude + radius * cos(angle)
            val lng = center.longitude + radius * sin(angle)
            points.add(GeoPointImpl.fromLatLong(lat, lng))
        }
        points.add(points.first()) // ポリゴンを閉じる

        PolygonState(
            points = points,
            strokeColor = Color.Magenta,
            fillColor = Color.Magenta.copy(alpha = 0.4f),
            extra = "Star shape"
        )
    }

    // MapViewは、GoogleMapsView、MapboxMapViewなど、選択した地図プロバイダーに置き換えてください
MapView(state = mapViewState) {
        Polygon(starPolygon)
    }
}
```

## イベント処理

ポリゴンイベントは状態とクリック位置の両方を提供します：

```kotlin
data class PolygonEvent(
    val state: PolygonState,
    val clicked: GeoPoint?
)

typealias OnPolygonEventHandler = (PolygonEvent) -> Unit
```

## ベストプラクティス

1. **ポリゴンを閉じる**: 最初と最後の点が同じであることを確認してください
2. **頂点の順序**: 一貫した順序（時計回り/反時計回り）を使用してください
3. **パフォーマンス**: 多数の頂点を持つ過度に複雑なポリゴンは避けてください
4. **ビジュアルデザイン**: 地図の視認性のために適切な塗りつぶし透過を使用してください
5. **検証**: ポリゴンが有効な幾何学的形状を形成することを確認してください
