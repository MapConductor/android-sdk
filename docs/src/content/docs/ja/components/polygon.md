---
title: "Polygon（ポリゴン）"
---

`Polygon` コンポーネントは、複数の `GeoPoint` で囲まれた多角形を地図上に描画します。エリアの可視化や境界表示などに利用できます。

## 基本的な使い方

```kotlin
@Composable
fun MapViewScope.Polygon(
    points: List<GeoPoint>,
    holes: List<List<GeoPoint>> = emptyList(),
    clickable: Boolean = true,
    strokeColor: Color = Color.Black,
    strokeWidth: Dp = 1.dp,
    fillColor: Color = Color(red = 255, green = 255, blue = 255, alpha = 127),
    zIndex: Int? = null,
    id: String? = null,
    extra: Serializable? = null
)
```

- **`points`**: 外周を構成する座標のリスト
- **`holes`**: ポリゴン内部の穴（内側ポリゴン）のリスト
- **`strokeColor` / `strokeWidth`**: 枠線の色と太さ
- **`fillColor`**: 塗りつぶし色

## 利用例

```kotlin
val areaPoints = listOf(
    GeoPointImpl.fromLatLong(37.7749, -122.4194),
    GeoPointImpl.fromLatLong(37.7849, -122.4294),
    GeoPointImpl.fromLatLong(37.7949, -122.4194),
    GeoPointImpl.fromLatLong(37.7849, -122.4094),
)

MapView(
    state = mapViewState,
    onPolygonClick = { event ->
        println("Polygon clicked: ${event.state.extra}")
    }
) {
    Polygon(
        points = areaPoints,
        fillColor = Color.Green.copy(alpha = 0.3f),
        strokeColor = Color.DarkGray,
        strokeWidth = 2.dp,
        extra = "Area A"
    )
}
```

