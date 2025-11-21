---
title: "Basic Usage（基本的な使い方）"
---

このページでは、MapConductor を使って最小限の地図画面を構築する手順を紹介します。

## 1. MapViewState を準備

```kotlin
@Composable
fun rememberDefaultCamera(): MapCameraPosition =
    MapCameraPositionImpl(
        position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        zoom = 13.0
    )

@Composable
fun rememberDefaultMapViewState(): GoogleMapViewStateImpl =
    rememberGoogleMapViewState(cameraPosition = rememberDefaultCamera())
```

## 2. MapView とコンテンツ

```kotlin
@Composable
fun BasicMapScreen() {
    val mapViewState = rememberDefaultMapViewState()

    GoogleMapsView(
        state = mapViewState,
        onMapClick = { point ->
            println("Clicked at: ${point.latitude}, ${point.longitude}")
        }
    ) {
        Marker(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = DefaultIcon(label = "SF")
        )
    }
}
```

他のプロバイダに切り替えるには、`rememberGoogleMapViewState` と `GoogleMapsView` を対応するものに置き換えるだけです。

