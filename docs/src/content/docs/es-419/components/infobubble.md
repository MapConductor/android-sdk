---
title: "InfoBubble"
---

`InfoBubble` es una ventana de información que se puede asociar a un marcador o a una posición arbitraria en el mapa.

## Uso básico

```kotlin
@Composable
fun MapViewScope.InfoBubble(
    target: GeoPoint,
    modifier: Modifier = Modifier,
    anchor: Offset = Offset(0.5f, 1.0f),
    onDismissRequest: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
)
```

- **`target`**: Posición geográfica donde se muestra la burbuja.
- **`anchor`**: Punto de anclaje relativo.

## Ejemplo con marcador

```kotlin
@Composable
fun MarkerWithInfoBubbleExample() {
    var selectedMarker by remember { mutableStateOf<MarkerState?>(null) }

    MapView(
        state = mapViewState,
        onMarkerClick = { markerState ->
            selectedMarker = markerState
        }
    ) {
        val sanFrancisco = GeoPointImpl.fromLatLong(37.7749, -122.4194)
        val markerState = MarkerState(position = sanFrancisco, extra = "San Francisco")

        Marker(markerState)

        selectedMarker?.let { state ->
            InfoBubble(target = state.position, onDismissRequest = { selectedMarker = null }) {
                Text("Marker: ${state.extra}")
            }
        }
    }
}
```

