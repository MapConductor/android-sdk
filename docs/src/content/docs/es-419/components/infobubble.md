---
title: "InfoBubble"
---

`InfoBubble` es un componente que muestra contenido personalizado en una burbuja de información asociada a un marcador en el mapa. Permite mostrar detalles sobre un marcador sin saturar la interfaz del mapa.

## Descripción general

InfoBubble crea una superposición flotante que aparece sobre un marcador concreto, con estilo y contenido personalizables. La burbuja se posiciona automáticamente respecto al marcador y sigue su posición cuando el usuario desplaza o hace zoom en el mapa.

## Función composable

```kotlin
@Composable
fun MapViewScope.InfoBubble(
    marker: MarkerState,
    bubbleColor: Color = Color.White,
    borderColor: Color = Color.Black,
    contentPadding: Dp = 8.dp,
    cornerRadius: Dp = 4.dp,
    tailSize: Dp = 8.dp,
    content: @Composable () -> Unit
)
```

## Parámetros

- **`marker`**: `MarkerState` al que se adjunta la burbuja.
- **`bubbleColor`**: color de fondo de la burbuja (por defecto blanco).
- **`borderColor`**: color del borde de la burbuja (por defecto negro).
- **`contentPadding`**: padding interno alrededor del contenido (por defecto 8 dp).
- **`cornerRadius`**: radio de las esquinas redondeadas (por defecto 4 dp).
- **`tailSize`**: tamaño de la “cola” de la burbuja que apunta al marcador (por defecto 8 dp).
- **`content`**: contenido composable que se muestra dentro de la burbuja.

## Uso básico

### Burbuja de texto simple

```kotlin
@Composable
fun SimpleInfoBubbleExample() {
    val mapViewState = rememberGoogleMapViewState()
    var selectedMarker by remember { mutableStateOf<MarkerState?>(null) }

    // Sustituye MapView por el proveedor que prefieras, como GoogleMapView o MapboxMapView
MapView(
        state = mapViewState,
        onMapClick = { selectedMarker = null },
        onMarkerClick = { markerState -> selectedMarker = markerState }
    ) {
        val markerState = MarkerState(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = DefaultIcon(fillColor = Color.Blue, label = "SF"),
            extra = "San Francisco - The Golden Gate City",
            id = "sf-marker"
        )

        Marker(markerState)

        // Mostrar info bubble para el marcador seleccionado
        selectedMarker?.let { marker ->
            InfoBubble(marker = marker) {
                Text(
                    text = marker.extra as? String ?: "Sin información",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}
```

### Burbuja con estilo personalizado

```kotlin
@Composable
fun StyledInfoBubbleExample() {
    val mapViewState = rememberGoogleMapViewState()
    var selectedMarker by remember { mutableStateOf<MarkerState?>(null) }

    MapView(
        state = mapViewState,
        onMapClick = { selectedMarker = null },
        onMarkerClick = { markerState -> selectedMarker = markerState }
    ) {
        val markerState = MarkerState(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = DefaultIcon(fillColor = Color.Green, label = "POI"),
            extra = "Punto de interés importante",
            id = "poi-marker"
        )

        Marker(markerState)

        selectedMarker?.let { marker ->
            InfoBubble(
                marker = marker,
                bubbleColor = Color(0xFF212121),
                borderColor = Color.White,
                contentPadding = 12.dp,
                cornerRadius = 8.dp,
                tailSize = 10.dp
            ) {
                Column {
                    Text(
                        text = "Detalle",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = marker.extra as? String ?: "Sin información",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}
```

## Buenas prácticas

- Muestra solo una o pocas burbujas a la vez para no saturar la vista.
- Usa `extra` en `MarkerState` para almacenar el contenido que quieras mostrar en la burbuja.
- Cierra/hide la burbuja cuando el usuario haga clic fuera del marcador (`onMapClick`).
