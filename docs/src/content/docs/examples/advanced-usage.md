---
title: "Advanced Usage"
---

This page demonstrates advanced examples combining multiple overlays, camera events, and reactive state management.

## Combining Markers, Circles, and Polylines

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
            )
        )
    }
}
```

![Example with Marker, Circle, and Polyline](/img/examples/marker-circle-polyline.jpg)

For more advanced examples, refer to the `example-app` code in the repository.
