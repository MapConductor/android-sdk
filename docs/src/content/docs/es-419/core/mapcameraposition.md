---
title: "MapCameraPosition"
---

`MapCameraPosition` describe la posición de la cámara del mapa, incluyendo el punto objetivo, el nivel de zoom, la inclinación, el rumbo y los rellenos (`paddings`).

## Estructura básica

```kotlin
data class MapCameraPositionImpl(
    val position: GeoPoint,
    val zoom: Double,
    val tilt: Double = 0.0,
    val bearing: Double = 0.0,
    val paddings: MapPaddings? = null
) : MapCameraPosition
```

## Ejemplo

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

