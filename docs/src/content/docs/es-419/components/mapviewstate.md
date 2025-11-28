---
title: "MapViewState"
---

`MapViewState` mantiene el estado de la vista de mapa: posición de la cámara, zoom, región visible, etc. Cada proveedor tiene su propia implementación concreta.

## Implementaciones típicas

- `GoogleMapViewStateImpl`
- `MapboxViewStateImpl`
- `HereViewStateImpl`
- `ArcGISMapViewStateImpl`
- `MapLibreViewStateImpl`

## Especificar la posición inicial de la cámara

```kotlin
val camera = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    zoom = 13.0
)

val mapViewState = rememberGoogleMapViewState(
    cameraPosition = camera,
)
```

## Uso con eventos de cámara

```kotlin
GoogleMapView(
    state = mapViewState,
    onCameraMoveEnd = { event ->
        println("Camera position: ${event.cameraPosition}")
        println("Visible region: ${event.visibleRegion}")
    }
) {
    // Contenido del mapa
}
```

