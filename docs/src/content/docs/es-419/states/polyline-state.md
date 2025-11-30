---
title: "PolylineState"
---

`PolylineState` gestiona la configuración y el comportamiento de polilíneas (segmentos de línea) en el mapa. Proporciona propiedades reactivas para los puntos, el estilo y las opciones geodésicas.

## Constructor

```kotlin
PolylineState(
    points: List<GeoPoint>,
    id: String? = null,
    strokeColor: Color = Color.Black,
    strokeWidth: Dp = 1.dp,
    geodesic: Boolean = false,
    extra: Serializable? = null
)
```

## Propiedades

### Propiedades básicas
- **`id: String`**: Identificador único (se genera automáticamente si no se proporciona)
- **`points: List<GeoPoint>`**: Lista de coordenadas geográficas que definen la línea
- **`extra: Serializable?`**: Datos adicionales asociados a la polilínea

### Propiedades visuales
- **`strokeColor: Color`**: Color de la línea
- **`strokeWidth: Dp`**: Grosor de la línea
- **`geodesic: Boolean`**: Indica si la línea debe seguir la curvatura de la Tierra

## Métodos

```kotlin
fun copy(...): PolylineState // Crea una copia modificada
fun fingerPrint(): PolylineFingerPrint // Detección de cambios
fun asFlow(): Flow<PolylineFingerPrint> // Actualizaciones reactivas
```

## Ejemplos de uso

### Polilínea básica

```kotlin
val polylineState = PolylineState(
    points = listOf(
        GeoPointImpl.fromLatLong(37.7749, -122.4194),
        GeoPointImpl.fromLatLong(37.7849, -122.4094),
        GeoPointImpl.fromLatLong(37.7949, -122.3994)
    ),
    strokeColor = Color.Blue,
    strokeWidth = 3.dp
)

// Sustituye MapView por el proveedor de mapas que prefieras, como GoogleMapView o MapboxMapView
MapView(state = mapViewState) {
    Polyline(polylineState)
}
```

### Polilínea dinámica

```kotlin
@Composable
fun DynamicPolylineExample() {
    var polylineState by remember {
        mutableStateOf(
            PolylineState(
                points = listOf(
                    GeoPointImpl.fromLatLong(37.7749, -122.4194),
                    GeoPointImpl.fromLatLong(37.7849, -122.4094)
                ),
                strokeColor = Color.Red,
                strokeWidth = 4.dp
            )
        )
    }

    // Sustituye MapView por el proveedor de mapas que prefieras, como GoogleMapView o MapboxMapView
MapView(
        state = mapViewState,
        onMapClick = { geoPoint ->
            polylineState = polylineState.copy(
                points = polylineState.points + geoPoint
            )
        }
    ) {
        Polyline(polylineState)
    }
}
```

### Polilínea geodésica

```kotlin
val geodesicPolyline = PolylineState(
    points = listOf(
        GeoPointImpl.fromLatLong(37.7749, -122.4194), // San Francisco
        GeoPointImpl.fromLatLong(40.7128, -74.0060)   // Nueva York
    ),
    strokeColor = Color.Green,
    strokeWidth = 3.dp,
    geodesic = true, // Seguir la curvatura de la Tierra
    extra = "Cross-country route"
)
```

### Ruta con varios segmentos

```kotlin
@Composable
fun RouteExample() {
    val routeSegments = remember {
        listOf(
            PolylineState(
                points = highway1Points,
                strokeColor = Color.Blue,
                strokeWidth = 6.dp,
                extra = "Highway segment"
            ),
            PolylineState(
                points = localRoadPoints,
                strokeColor = Color.Green,
                strokeWidth = 3.dp,
                extra = "Local road segment"
            ),
            PolylineState(
                points = walkingPathPoints,
                strokeColor = Color.Orange,
                strokeWidth = 2.dp,
                extra = "Walking path"
            )
        )
    }

    // Sustituye MapView por el proveedor de mapas que prefieras, como GoogleMapView o MapboxMapView
MapView(state = mapViewState) {
        routeSegments.forEach { segment ->
            Polyline(segment)
        }
    }
}
```

## Manejo de eventos

Los eventos de polilínea proporcionan tanto el estado como la posición del clic:

```kotlin
data class PolylineEvent(
    val state: PolylineState,
    val clicked: GeoPoint
)

typealias OnPolylineEventHandler = (PolylineEvent) -> Unit
```

## Buenas prácticas

1. **Densidad de puntos**: Equilibra el detalle y el rendimiento; evita usar demasiados puntos.
2. **Geodésicas**: Usa líneas geodésicas para rutas de larga distancia a fin de representar trayectos más precisos.
3. **Colores**: Utiliza colores distintos para diferenciar tipos de rutas.
4. **Jerarquía de grosores**: Usa el grosor de la línea para indicar la importancia.
5. **Rendimiento**: Considera simplificar polilíneas complejas en distintos niveles de zoom.

