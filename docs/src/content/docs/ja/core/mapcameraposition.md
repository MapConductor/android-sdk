---
title: "MapCameraPosition（カメラ位置）"
---

`MapCameraPosition` は、地図カメラの位置やズーム、方位、傾き、パディングを一括して表すクラスです。

## 基本構造

```kotlin
data class MapCameraPositionImpl(
    val position: GeoPoint,
    val zoom: Double,
    val tilt: Double = 0.0,
    val bearing: Double = 0.0,
    val paddings: MapPaddings? = null
) : MapCameraPosition
```

- **`position`**: カメラが注視する位置（`GeoPoint`）
- **`zoom`**: ズームレベル
- **`tilt`**: 傾き（度）
- **`bearing`**: 方位（度、北を 0 とした時計回り）
- **`paddings`**: UI の余白を考慮したパディング（[Zoom Levels / Paddings](/ja/core/zoom-levels) も参照）

## カメラ位置の指定例

```kotlin
val camera = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    zoom = 14.0,
    tilt = 45.0,
    bearing = 90.0
)

val mapViewState = rememberGoogleMapViewState(
    cameraPosition = camera
)
```

## パディングを利用した UI 対応

```kotlin
val paddings = MapPaddingsImpl(
    top = 80.0,
    bottom = 160.0
)

val paddedCamera = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    zoom = 15.0,
    paddings = paddings
)
```

