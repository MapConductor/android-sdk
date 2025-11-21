---
title: "Initialization（初期化）"
---

このページでは、MapConductor の基本的な初期化フローと、各プロバイダの `MapViewState` をどのように組み立てるかを説明します。

## MapConductor の導入ステップ

1. Gradle に依存関係を追加（[インストール](/ja/installation/) 参照）
2. 各地図 SDK をセットアップ（[セットアップ](/ja/setup/) 参照）
3. `MapViewState` を作成
4. 対応する `MapView` コンポーネントを Compose で利用

## MapViewState の初期化例

```kotlin
@Composable
fun rememberGoogleMapViewState(
    cameraPosition: MapCameraPosition = MapCameraPositionImpl(
        position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        zoom = 13.0,
    )
): GoogleMapViewStateImpl
```

```kotlin
val mapViewState = rememberGoogleMapViewState(
    cameraPosition = MapCameraPositionImpl(
        position = GeoPointImpl.fromLatLong(35.6812, 139.7671),
        zoom = 14.0,
    )
)
```

Mapbox / HERE / ArcGIS / MapLibre も同様に、`rememberMapboxMapViewState` などのヘルパーを経由して初期化します。

## MapView の配置

```kotlin
GoogleMapsView(
    state = mapViewState,
    onMapLoaded = { println("Map loaded") }
) {
    // ここに Marker / Circle / Polyline などを追加
}
```

Mapbox の場合は `MapboxMapView`、ArcGIS の場合は `ArcGISMapView` を使うだけで、構造は変わりません。

