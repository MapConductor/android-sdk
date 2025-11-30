---
title: "Advanced Usage"
---

Esta página muestra ejemplos avanzados que combinan varios overlays, eventos de cámara y gestión de estado reactiva.

## Combinando marcadores, círculos y polilíneas

```kotlin
@Composable
fun AdvancedMapScreen() {
    val camera = MapCameraPositionImpl(
        position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        zoom = 15.0
    )
    val mapViewState = rememberGoogleMapViewState(cameraPosition = camera)

    GoogleMapView(
        state = mapViewState,
        onMarkerClick = { markerState ->
            println("Marker: ${markerState.extra}")
        }
    ) {
        // Marker
        Marker(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = DefaultIcon(label = "Center")
        )

        // Circle
        Circle(
            center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            radiusMeters = 1000.0,
            strokeColor = Color.Blue,
            fillColor = Color.Blue.copy(alpha = 0.3f)
        )

        // Polyline
        Polyline(
            points = listOf(
                GeoPointImpl.fromLatLong(37.7749, -122.4194),
                GeoPointImpl.fromLatLong(37.7849, -122.4094),
                GeoPointImpl.fromLatLong(37.7949, -122.3994)
            ),
            strokeColor = Color.Red,
            strokeWidth = 3.dp
        )
    }
}
```

![Ejemplo de Marker, Circle y Polyline](/img/examples/marker-circle-polyline.jpg)
