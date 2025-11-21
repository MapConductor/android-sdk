---
title: "GroundImage（画像オーバーレイ）"
---

`GroundImage` コンポーネントは、地理座標に画像を貼り付けるためのオーバーレイです。現在は Google Maps のみサポートされています。

## 基本的な使い方

```kotlin
@Composable
fun MapViewScope.GroundImage(
    image: Painter,
    bounds: GeoRectBounds,
    transparency: Float = 0.0f,
    clickable: Boolean = false,
    zIndex: Int? = null,
    id: String? = null,
    extra: Serializable? = null
)
```

- **`image`**: 描画する画像
- **`bounds`**: 画像を貼り付ける地理的な矩形（[GeoRectBounds](/ja/core/georectbounds)）
- **`transparency`**: 透明度（0.0 = 不透明、1.0 = 完全に透明）

## 利用例

```kotlin
val bounds = GeoRectBoundsImpl(
    southWest = GeoPointImpl.fromLatLong(37.77, -122.43),
    northEast = GeoPointImpl.fromLatLong(37.79, -122.41),
)

MapView(
    state = mapViewState,
    onGroundImageClick = { event ->
        println("GroundImage clicked: ${event.state.extra}")
    }
) {
    GroundImage(
        image = painterResource(id = R.drawable.overlay_image),
        bounds = bounds,
        transparency = 0.2f,
        extra = "Overlay"
    )
}
```

