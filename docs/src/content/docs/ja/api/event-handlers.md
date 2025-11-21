---
title: "Event Handlers（イベントハンドラ）"
---

このページでは、MapConductor が提供する主なイベントハンドラと、その使い方を説明します。

## 地図イベント

- **`onMapViewInitialized`**: 基礎となる MapView が初期化されたタイミング
- **`onMapLoaded`**: タイルやベクターデータを含め、地図が表示可能になったタイミング
- **`onMapClick`**: ユーザーが地図をクリックしたとき

```kotlin
GoogleMapsView(
    state = mapViewState,
    onMapLoaded = { println("Map loaded") },
    onMapClick = { geoPoint ->
        println("Clicked at: ${geoPoint.latitude}, ${geoPoint.longitude}")
    }
) { /* コンテンツ */ }
```

## マーカーイベント

- **`onMarkerClick`**
- **`onMarkerDragStart` / `onMarkerDrag` / `onMarkerDragEnd`**
- **`onMarkerAnimateStart` / `onMarkerAnimateEnd`**

```kotlin
GoogleMapsView(
    state = mapViewState,
    onMarkerClick = { markerState ->
        println("Clicked marker: ${markerState.extra}")
    },
    onMarkerDrag = { markerState ->
        println("Dragging marker: ${markerState.position}")
    }
) {
    Marker(
        position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        draggable = true
    )
}
```

## 円・線・ポリゴンのイベント

- **`onCircleClick`**
- **`onPolylineClick`**
- **`onPolygonClick`**
- **`onGroundImageClick`**（Google Maps のみ）

各イベントは、対象の状態（`CircleState` など）とクリック位置（必要な場合）を含むイベントオブジェクトを受け取ります。

## カメライベント（MapLibre など）

一部プロバイダでは、カメラの移動イベントも提供されます。

- **`onCameraMoveStart`**
- **`onCameraMove`**
- **`onCameraMoveEnd`**

```kotlin
MapLibreMapView(
    state = mapLibreViewState,
    onCameraMoveEnd = { event ->
        println("Camera: ${event.cameraPosition}")
    }
) { /* コンテンツ */ }
```

