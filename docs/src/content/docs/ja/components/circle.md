---
title: "Circle（円）"
---

`Circle` コンポーネントは、地図上に円形のオーバーレイを表示するためのコンポーネントです。ジオメトリの中心位置と半径、枠線や塗りつぶしのスタイルを指定できます。

## 基本的な使い方

```kotlin
@Composable
fun MapViewScope.Circle(
    center: GeoPoint,
    radius: Double,
    clickable: Boolean = true,
    strokeColor: Color = Color.Red,
    strokeWidth: Dp = 1.dp,
    fillColor: Color = Color(red = 255, green = 255, blue = 255, alpha = 127),
    id: String? = null,
    zIndex: Int? = null,
    extra: Serializable? = null
)
```

- **`center`**: 円の中心（`GeoPoint`）
- **`radius`**: 半径（メートル単位）
- **`strokeColor` / `strokeWidth`**: 枠線の色と太さ
- **`fillColor`**: 塗りつぶし色
- **`clickable`**: クリックイベントを受け取るかどうか

## CircleState と組み合わせて使う

`CircleState` を使うと、半径や色をリアクティブに変更できます（[CircleState](/ja/states/circle-state) を参照）。

```kotlin
val circleState = CircleState(
    center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    radius = 1000.0,
    strokeColor = Color.Blue,
    fillColor = Color.Blue.copy(alpha = 0.3f)
)

MapView(
    state = mapViewState,
    onCircleClick = { event ->
        println("Circle clicked at: ${event.clicked}")
    }
) {
    Circle(circleState)
}
```

