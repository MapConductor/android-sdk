---
title: "GeoRectBounds（矩形範囲）"
---

`GeoRectBounds` は、南西（southWest）と北東（northEast）の 2 点で表される地理的な矩形範囲を表すクラスです。カメラの可視領域や GroundImage の貼り付け範囲などに利用されます。

## 基本構造

```kotlin
data class GeoRectBoundsImpl(
    val southWest: GeoPoint,
    val northEast: GeoPoint
) : GeoRectBounds
```

## 利用例

### GroundImage の範囲として利用

```kotlin
val bounds = GeoRectBoundsImpl(
    southWest = GeoPointImpl.fromLatLong(37.77, -122.43),
    northEast = GeoPointImpl.fromLatLong(37.79, -122.41),
)

MapView(state = mapViewState) {
    GroundImage(
        image = painterResource(id = R.drawable.overlay_image),
        bounds = bounds
    )
}
```

### カメラを矩形に合わせて移動

一部のプロバイダでは、`GeoRectBounds` を使ってカメラを指定範囲にフィットさせるヘルパー関数を提供できます。

