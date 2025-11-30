---
title: "Polygon"
---

Los polígonos son figuras cerradas que definen áreas en el mapa con propiedades de trazo y relleno configurables. Son útiles para representar zonas, regiones, límites u otras entidades basadas en áreas.

## Funciones composable

### Polygon básico

```kotlin
@Composable
fun MapViewScope.Polygon(
    points: List<GeoPoint>,
    id: String? = null,
    strokeColor: Color = Color.Black,
    strokeWidth: Dp = 1.dp,
    fillColor: Color = Color.Transparent,
    geodesic: Boolean = false,
    extra: Serializable? = null
)
```

### Polygon con estado

```kotlin
@Composable
fun MapViewScope.Polygon(state: PolygonState)
```

## Parámetros

- **`points`**: Lista de coordenadas geográficas que definen los vértices del polígono (`List<GeoPoint>`).
- **`id`**: Identificador único opcional para el polígono (`String?`).
- **`strokeColor`**: Color del borde del polígono (por defecto `Color.Black`).
- **`strokeWidth`**: Grosor de la línea de borde (por defecto `1.dp`).
- **`fillColor`**: Color de relleno del interior del polígono (por defecto `Color.Transparent`).
- **`geodesic`**: Indica si se deben dibujar aristas geodésicas (por defecto `false`).
- **`extra`**: Datos adicionales asociados al polígono (`Serializable?`).

## Ejemplos de uso

### Polygon básico

```kotlin
// Sustituye MapView por el proveedor que prefieras, como GoogleMapView o MapboxMapView
MapView(state = mapViewState) {
    val trianglePoints = listOf(
        GeoPointImpl.fromLatLong(37.7749, -122.4194), // Punto 1
        GeoPointImpl.fromLatLong(37.7849, -122.4094), // Punto 2
        GeoPointImpl.fromLatLong(37.7749, -122.3994), // Punto 3
        GeoPointImpl.fromLatLong(37.7749, -122.4194)  // Cierra el polígono
    )

    Polygon(
        points = trianglePoints,
        strokeColor = Color.Blue,
        strokeWidth = 2.dp,
        fillColor = Color.Blue.copy(alpha = 0.3f)
    )
}
```

### Polygon interactivo con vértices

```kotlin
@Composable
fun InteractivePolygonExample() {
    var vertices by remember {
        mutableStateOf(
            listOf(
                GeoPointImpl.fromLatLong(37.7749, -122.4194),
                GeoPointImpl.fromLatLong(37.7849, -122.4094),
                GeoPointImpl.fromLatLong(37.7799, -122.3994),
                GeoPointImpl.fromLatLong(37.7649, -122.4094)
            )
        )
    }

    val polygonState = PolygonState(
        points = vertices + vertices.first(), // Cierra el polígono
        strokeColor = Color.Red,
        strokeWidth = 2.dp,
        fillColor = Color.Red.copy(alpha = 0.2f)
    )

    // Sustituye MapView por el proveedor que prefieras, como GoogleMapView o MapboxMapView
MapView(
        state = mapViewState,
        onPolygonClick = { event ->
            println("Polygon clicked at: ${event.clicked}")
        }
    ) {
        Polygon(polygonState)

        // Marcadores de vértices (sin incluir el punto de cierre)
        vertices.forEachIndexed { index, point ->
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

### Varios polígonos de zona

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

MapView(state = mapViewState) {
    zonePolygons.forEach { zone ->
        Polygon(zone)
    }
}
```

## Buenas prácticas

- Cierra siempre el polígono (primer y último punto iguales) si tu implementación así lo requiere.
- Usa rellenos translúcidos para que la cartografía base siga siendo legible.
- Sé consistente con el orden de los vértices (horario/antihorario) para evitar artefactos visuales.
