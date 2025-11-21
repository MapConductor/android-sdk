---
title: "Advanced Usage（応用例）"
---

このページでは、複数のオーバーレイやカメライベント、リアクティブな状態管理を組み合わせた応用例を紹介します。

## マーカー・円・ポリラインの組み合わせ

```kotlin
@Composable
fun AdvancedMapScreen() {
    val camera = MapCameraPositionImpl(
        position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        zoom = 12.0
    )
    val mapViewState = rememberGoogleMapViewState(cameraPosition = camera)

    GoogleMapsView(
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

さらに高度な例については、リポジトリの `example-app` のコードも参照してください。

