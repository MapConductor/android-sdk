---
title: "Polyline（ポリライン）"
---

`Polyline` コンポーネントは、複数の `GeoPoint` を結んだ線分を地図上に描画するために使用します。ルート表示や軌跡表示に適しています。

## 基本的な使い方

```kotlin
@Composable
fun MapViewScope.Polyline(
    points: List<GeoPoint>,
    clickable: Boolean = false,
    color: Color = Color.Blue,
    width: Dp = 2.dp,
    zIndex: Int? = null,
    id: String? = null,
    extra: Serializable? = null
)
```

- **`points`**: 線分を構成する座標のリスト
- **`color` / `width`**: 線の色と太さ
- **`clickable`**: 線をクリックできるかどうか

## 利用例

```kotlin
val route = listOf(
    GeoPointImpl.fromLatLong(37.7749, -122.4194),
    GeoPointImpl.fromLatLong(37.7849, -122.4094),
    GeoPointImpl.fromLatLong(37.7949, -122.3994),
)

MapView(
    state = mapViewState,
    onPolylineClick = { event ->
        println("Polyline clicked: ${event.state.extra}")
    }
) {
    Polyline(
        points = route,
        color = Color.Cyan,
        width = 3.dp,
        extra = "Route A"
    )
}
```

