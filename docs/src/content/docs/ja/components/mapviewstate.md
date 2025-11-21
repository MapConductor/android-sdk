---
title: "MapViewState"
---

`MapViewState` は、地図のカメラ位置やズーム、可視範囲など、マップビューの状態を保持・管理するためのクラスです。プロバイダごとに実装は異なりますが、共通の役割を持ちます。

## 主な実装

例として、以下のような実装があります。

- `GoogleMapViewStateImpl`
- `MapboxViewStateImpl`
- `HereViewStateImpl`
- `ArcGISMapViewStateImpl`
- `MapLibreViewStateImpl`

これらはいずれも、`MapCameraPosition` を介してカメラ位置を管理します（[MapCameraPosition](/ja/core/mapcameraposition) を参照）。

## 初期カメラ位置の指定

```kotlin
val camera = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    zoom = 13.0
)

val mapViewState = rememberGoogleMapViewState(
    cameraPosition = camera,
)
```

## MapViewState とイベント

`MapViewState` は、カメラの移動や可視範囲の変更を追跡するためにも利用されます。

```kotlin
GoogleMapsView(
    state = mapViewState,
    onCameraMoveEnd = { event ->
        println("Camera position: ${event.cameraPosition}")
        println("Visible region: ${event.visibleRegion}")
    }
) {
    // 地図コンテンツ
}
```

プロバイダごとの詳細なプロパティや補助 API は、それぞれの実装の KDoc を参照してください。

