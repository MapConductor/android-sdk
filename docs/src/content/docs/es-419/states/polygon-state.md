---
title: "PolygonState"
---

`PolygonState` gestiona la configuración y el comportamiento de los polígonos (áreas rellenas) en el mapa. Proporciona propiedades reactivas para los vértices, el trazo, el relleno y las opciones geodésicas.

## Constructor

```kotlin
PolygonState(
    points: List<GeoPoint>,
    id: String? = null,
    strokeColor: Color = Color.Black,
    strokeWidth: Dp = 2.dp,
    fillColor: Color = Color.Transparent,
    geodesic: Boolean = false,
    extra: Serializable? = null
)
```

## Propiedades

### Propiedades básicas
- **`id: String`**: Identificador único (se genera automáticamente si no se proporciona)
- **`points: List<GeoPoint>`**: Lista de vértices que define el polígono (debe estar cerrado)
- **`extra: Serializable?`**: Datos adicionales asociados al polígono

### Propiedades visuales
- **`strokeColor: Color`**: Color del borde
- **`strokeWidth: Dp`**: Grosor del borde
- **`fillColor: Color`**: Color de relleno interior
- **`geodesic: Boolean`**: Indica si los bordes se dibujan como líneas geodésicas

## Métodos

```kotlin
fun copy(...): PolygonState // Crea una copia modificada
fun fingerPrint(): PolygonFingerPrint // Detección de cambios
fun asFlow(): Flow<PolygonFingerPrint> // Actualizaciones reactivas
```

## Ejemplos de uso

### Polígono básico

```kotlin
val polygonState = PolygonState(
    points = listOf(
        GeoPointImpl.fromLatLong(37.7749, -122.4194),
        GeoPointImpl.fromLatLong(37.7849, -122.4094),
        GeoPointImpl.fromLatLong(37.7749, -122.3994),
        GeoPointImpl.fromLatLong(37.7749, -122.4194) // Cerrar el polígono
    ),
    strokeColor = Color.Blue,
    strokeWidth = 2.dp,
    fillColor = Color.Blue.copy(alpha = 0.3f)
)

// Sustituye MapView por el proveedor de mapas que prefieras, como GoogleMapView o MapboxMapView
MapView(state = mapViewState) {
    Polygon(polygonState)
}
```

### Polígono interactivo

```kotlin
@Composable
fun EditablePolygonExample() {
    var polygonState by remember {
        mutableStateOf(
            PolygonState(
                points = listOf(
                    GeoPointImpl.fromLatLong(37.7749, -122.4194),
                    GeoPointImpl.fromLatLong(37.7849, -122.4094),
                    GeoPointImpl.fromLatLong(37.7799, -122.3994),
                    GeoPointImpl.fromLatLong(37.7749, -122.4194)
                ),
                strokeColor = Color.Red,
                fillColor = Color.Red.copy(alpha = 0.2f)
            )
        )
    }

    // Sustituye MapView por el proveedor de mapas que prefieras, como GoogleMapView o MapboxMapView
MapView(
        state = mapViewState,
        onPolygonClick = { event ->
            println("Polygon clicked at: ${event.clicked}")
        }
    ) {
        Polygon(polygonState)

        // Marcadores de vértices (excepto el punto de cierre)
        polygonState.points.dropLast(1).forEachIndexed { index, point ->
            Marker(
                position = point,
                icon = DefaultIcon(
                    fillColor = Color.Red,
                    label = "${index + 1}",
                    scale = 0.8f
                ),
                draggable = true,
                extra = "vertex_$index"
            )
        }
    }
}
```

### Polígonos de zona

```kotlin
val zonePolygons = listOf(
    PolygonState(
        points = restrictedZonePoints,
        strokeColor = Color.Red,
        strokeWidth = 3.dp,
        fillColor = Color.Red.copy(alpha = 0.2f),
        extra = "Restricted Zone"
    ),
    PolygonState(
        points = parkingZonePoints,
        strokeColor = Color.Blue,
        strokeWidth = 2.dp,
        fillColor = Color.Blue.copy(alpha = 0.2f),
        extra = "Parking Zone"
    ),
    PolygonState(
        points = safeZonePoints,
        strokeColor = Color.Green,
        strokeWidth = 2.dp,
        fillColor = Color.Green.copy(alpha = 0.2f),
        extra = "Safe Zone"
    )
)

// Sustituye MapView por el proveedor de mapas que prefieras, como GoogleMapView o MapboxMapView
MapView(state = mapViewState) {
    zonePolygons.forEach { zone ->
        Polygon(zone)
    }
}
```

### Formas complejas

```kotlin
@Composable
fun ComplexPolygonExample() {
    // Polígono en forma de estrella
    val starPolygon = remember {
        val center = GeoPointImpl.fromLatLong(37.7749, -122.4194)
        val points = mutableListOf<GeoPoint>()

        for (i in 0 until 10) {
            val angle = (i * 36.0) * Math.PI / 180.0
            val radiusMeters = if (i % 2 == 0) 0.01 else 0.005
            val lat = center.latitude + radiusMeters * cos(angle)
            val lng = center.longitude + radiusMeters * sin(angle)
            points.add(GeoPointImpl.fromLatLong(lat, lng))
        }
        points.add(points.first()) // Cerrar el polígono

        PolygonState(
            points = points,
            strokeColor = Color.Magenta,
            fillColor = Color.Magenta.copy(alpha = 0.4f),
            extra = "Star shape"
        )
    }

    // Sustituye MapView por el proveedor de mapas que prefieras, como GoogleMapView o MapboxMapView
MapView(state = mapViewState) {
        Polygon(starPolygon)
    }
}
```

## Manejo de eventos

Los eventos de polígono proporcionan tanto el estado como la posición del clic:

```kotlin
data class PolygonEvent(
    val state: PolygonState,
    val clicked: GeoPoint?
)

typealias OnPolygonEventHandler = (PolygonEvent) -> Unit
```

## Buenas prácticas

1. **Cerrar el polígono**: Asegúrate de que el primer y el último punto sean iguales.
2. **Orden de los vértices**: Usa un orden consistente (sentido horario/antihorario).
3. **Rendimiento**: Evita polígonos excesivamente complejos con demasiados vértices.
4. **Diseño visual**: Utiliza rellenos translúcidos para mantener la visibilidad del mapa.
5. **Validación**: Comprueba que el polígono forme una geometría válida.

