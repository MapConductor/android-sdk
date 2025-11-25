---
title: "Advanced Usage"
---

Este ejemplo combina varios tipos de overlays y eventos de mapa.

```kotlin
@Composable
fun AdvancedMapScreen() {
    val camera = MapCameraPositionImpl(
        position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        zoom = 15.0
    )
    val mapViewState = rememberGoogleMapViewState(cameraPosition = camera)

    GoogleMapsView(
        state = mapViewState,
        onMarkerClick = { markerState ->
            println("Marker: ${markerState.extra}")
        }
    ) {
        Marker(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = DefaultIcon(label = "Center")
        )

        Circle(
            center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            radius = 1000.0,
            strokeColor = Color.Blue,
            fillColor = Color.Blue.copy(alpha = 0.3f)
        )

        Polyline(
            points = listOf(
                GeoPointImpl.fromLatLong(37.7749, -122.4194),
                GeoPointImpl.fromLatLong(37.7849, -122.4094),
            )
        )
    }
}
```

![Ejemplo de Marker, Circle y Polyline](/img/examples/marker-circle-polyline.jpg)

