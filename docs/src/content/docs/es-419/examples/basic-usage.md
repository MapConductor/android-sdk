---
title: "Basic Usage"
---

En esta página se muestra cómo construir una pantalla de mapa sencilla con MapConductor.

## Preparar MapViewState

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

## Renderizar el mapa

```kotlin
@Composable
fun BasicMapScreen() {
    val mapViewState = rememberDefaultMapViewState()

    GoogleMapView(
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

