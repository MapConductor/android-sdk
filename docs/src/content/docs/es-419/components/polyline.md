---
title: "Polyline"
---

Las polilíneas son secuencias de segmentos que conectan varios puntos geográficos. Se usan habitualmente para rutas, caminos, límites u otras entidades lineales en el mapa.

## Funciones composable

### Polyline básica

```kotlin
@Composable
fun MapViewScope.Polyline(
    points: List<GeoPoint>,
    id: String? = null,
    strokeColor: Color = Color.Black,
    strokeWidth: Dp = 1.dp,
    geodesic: Boolean = false,
    extra: Serializable? = null
)
```

### Polyline con estado

```kotlin
@Composable
fun MapViewScope.Polyline(state: PolylineState)
```

## Parámetros

- **`points`**: Lista de coordenadas geográficas que definen los segmentos de la línea (`List<GeoPoint>`).
- **`id`**: Identificador único opcional para la polilínea (`String?`).
- **`strokeColor`**: Color de la línea (por defecto `Color.Black`).
- **`strokeWidth`**: Grosor de la línea (por defecto `1.dp`).
- **`geodesic`**: Indica si la línea debe seguir la curvatura de la Tierra (por defecto `false`).
- **`extra`**: Datos adicionales asociados a la polilínea (`Serializable?`).

## Ejemplos de uso

### Polyline básica

```kotlin
// Sustituye MapView por el proveedor que prefieras, como GoogleMapView o MapboxMapView
MapView(state = mapViewState) {
    val routePoints = listOf(
        GeoPointImpl.fromLatLong(37.7749, -122.4194), // San Francisco
        GeoPointImpl.fromLatLong(37.7849, -122.4094), // Punto 2
        GeoPointImpl.fromLatLong(37.7949, -122.3994), // Punto 3
        GeoPointImpl.fromLatLong(37.8049, -122.3894)  // Punto 4
    )

    Polyline(
        points = routePoints,
        strokeColor = Color.Blue,
        strokeWidth = 3.dp
    )
}
```

### Polyline interactiva con puntos de paso

```kotlin
@Composable
fun InteractivePolylineExample() {
    var waypoints by remember {
        mutableStateOf(
            listOf(
                GeoPointImpl.fromLatLong(37.7749, -122.4194),
                GeoPointImpl.fromLatLong(37.7849, -122.4094),
                GeoPointImpl.fromLatLong(37.7949, -122.3994)
            )
        )
    }

    val polylineState = PolylineState(
        points = waypoints,
        strokeColor = Color.Blue,
        strokeWidth = 3.dp
    )

    // Sustituye MapView por el proveedor que prefieras, como GoogleMapView o MapboxMapView
MapView(
        state = mapViewState,
        onMapClick = { geoPoint ->
            // Añadir un nuevo punto de paso al hacer clic
            waypoints = waypoints + geoPoint
        }
    ) {
        Polyline(polylineState)

        // Opcional: marcadores en los puntos de la ruta
        waypoints.forEachIndexed { index, point ->
            Marker(
                position = point,
                icon = DefaultIcon(
                    label = (index + 1).toString(),
                    fillColor = Color.Red
                )
            )
        }
    }
}
```

## Manejo de eventos

```kotlin
MapView(
    state = mapViewState,
    onPolylineClick = { event ->
        println("Polyline clicked: ${event.state.extra}")
    }
) {
    Polyline(
        points = route,
        strokeColor = Color.Cyan,
        strokeWidth = 3.dp,
        extra = "Route A"
    )
}
```
