---
title: "MapView コンポーネント"
---

MapConductor は、各地図プロバイダごとに専用の MapView コンポーネントを提供します。API は共通化されており、プロバイダを切り替えても多くのコードをそのまま再利用できます。

## プロバイダ別コンポーネント

代表的なコンポーネントは次のとおりです。

- `GoogleMapsView`（Google Maps）
- `MapboxMapView`（Mapbox）
- `HereMapView`（HERE Maps）
- `ArcGISMapView`（ArcGIS）
- `MapLibreMapView`（MapLibre）

各コンポーネントは、共通のイベントハンドラ（`onMapClick` や `onMarkerClick` など）と、`MapViewScope` 内で使用できる `Marker` / `Circle` / `Polyline` / `Polygon` などのコンポーネントを受け取ります。

## GoogleMapsView の例

```kotlin
GoogleMapsView(
    state = googleMapViewState,
    onMapLoaded = { println("Map loaded") },
    onMapClick = { geoPoint ->
        println("Map clicked at: ${geoPoint.latitude}, ${geoPoint.longitude}")
    },
    onMarkerClick = { markerState ->
        println("Marker clicked: ${markerState.extra}")
    }
) {
    // MapViewScope 内で Marker / Circle などを追加
    Marker(
        position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        icon = DefaultIcon(label = "SF")
    )
}
```

## MapLibreMapView の例（カメライベント付き）

```kotlin
MapLibreMapView(
    state = mapLibreViewState,
    onCameraMoveStart = { event -> println("Camera move start: ${event.position}") },
    onCameraMove = { event -> println("Camera moving: ${event.position}") },
    onCameraMoveEnd = { event -> println("Camera move end: ${event.position}") }
) {
    // コンテンツは他のプロバイダと同じように記述できます
}
```

より詳しい API 一覧は、[API / Initialization](/ja/api/initialization) と [API / Event Handlers](/ja/api/event-handlers) を参照してください。

