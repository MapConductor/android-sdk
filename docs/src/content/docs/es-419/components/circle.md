---
title: "Circle"
---

Los círculos son superposiciones circulares que se dibujan sobre el mapa con radio, trazo y relleno personalizables. Son útiles para representar áreas, rangos o zonas.

## Funciones composable

### Circle básico

```kotlin
@Composable
fun MapViewScope.Circle(
    center: GeoPoint,
    radiusMeters: Double,
    geodesic: Boolean = false,
    strokeColor: Color = Color.Red,
    strokeWidth: Dp = 2.dp,
    fillColor: Color = Color.White.copy(alpha = 0.5f),
    extra: Serializable? = null,
    id: String? = null
)
```

### Circle con estado

```kotlin
@Composable
fun MapViewScope.Circle(state: CircleState)
```

## Parámetros

- **`center`**: Centro geográfico del círculo (`GeoPoint`).
- **`radiusMeters`**: Radio en metros (`Double`).
- **`geodesic`**: Indica si se debe dibujar el círculo usando bordes geodésicos que sigan la curvatura de la Tierra (por defecto `false`).
- **`strokeColor`**: Color del borde del círculo (por defecto `Color.Red`).
- **`strokeWidth`**: Grosor de la línea de borde (por defecto `2.dp`).
- **`fillColor`**: Color de relleno del interior del círculo (por defecto blanco semitransparente).
- **`extra`**: Datos adicionales asociados al círculo (`Serializable?`).
- **`id`**: Identificador único del círculo (`String?`).

## Ejemplos de uso

### Circle básico

```kotlin
// Sustituye MapView por el proveedor de mapas que prefieras, como GoogleMapView o MapboxMapView
MapView(state = mapViewState) {
    Circle(
        center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        radiusMeters = 1000.0, // radio de 1 km
        strokeColor = Color.Blue,
        fillColor = Color.Blue.copy(alpha = 0.3f),
        id = "downtown-area"
    )
}
```

### Circle interactivo con marcadores

A partir del ejemplo de la app de muestra, así puedes crear un círculo interactivo con marcadores arrastrables:

```kotlin
@Composable
fun InteractiveCircleExample() {
    var centerPosition by remember {
        mutableStateOf(GeoPointImpl.fromLatLong(37.7749, -122.4194))
    }
    var radiusMeters by remember { mutableStateOf(1000.0) }

    // Calcular la posición del marcador en el borde
    val edgePosition = remember(centerPosition, radiusMeters) {
        // Calcula un punto a 'radiusMeters' metros del centro
        // Cálculo simplificado: para producción conviene considerar la curvatura de la Tierra
        val latOffset = radiusMeters / 111000.0 // metros aproximados por grado de latitud
        GeoPointImpl.fromLatLong(
            centerPosition.latitude + latOffset,
            centerPosition.longitude
        )
    }

    val circleState = CircleState(
        center = centerPosition,
        radiusMeters = radiusMeters,
        strokeColor = Color.Blue,
        fillColor = Color.Blue.copy(alpha = 0.3f),
        clickable = true
    )

    val centerMarker = MarkerState(
        position = centerPosition,
        icon = DefaultIcon(
            fillColor = Color.Blue,
            label = "C"
        ),
        draggable = false
    )

    val edgeMarker = MarkerState(
        position = edgePosition,
        icon = DefaultIcon(
            fillColor = Color.Green,
            label = "E"
        ),
        draggable = true
    )

    // Sustituye MapView por el proveedor de mapas que prefieras, como GoogleMapView o MapboxMapView
MapView(
        state = mapViewState,
        onMarkerDrag = { markerState ->
            if (markerState.id == edgeMarker.id) {
                // Calcular el nuevo radio según la posición del marcador de borde
                val distance = calculateDistance(centerPosition, markerState.position)
                radiusMeters = distance
            }
        },
        onCircleClick = { circleEvent ->
            println("Circle clicked at: ${circleEvent.clicked}")
        }
    ) {
        Circle(circleState)
        Marker(centerMarker)
        Marker(edgeMarker)
    }
}
```

### Varios círculos con estilos diferentes

```kotlin
// Sustituye MapView por el proveedor de mapas que prefieras, como GoogleMapView o MapboxMapView
MapView(state = mapViewState) {
    // Círculo rojo sólido
    Circle(
        center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        radiusMeters = 500.0,
        strokeColor = Color.Red,
        strokeWidth = 3.dp,
        fillColor = Color.Red.copy(alpha = 0.2f),
        extra = "Red zone"
    )

    // Círculo azul con borde grueso
    Circle(
        center = GeoPointImpl.fromLatLong(37.7849, -122.4194),
        radiusMeters = 750.0,
        strokeColor = Color.Blue,
        strokeWidth = 5.dp,
        fillColor = Color.Transparent,
        extra = "Blue boundary"
    )

    // Círculo verde con relleno parcial
    Circle(
        center = GeoPointImpl.fromLatLong(37.7649, -122.4194),
        radiusMeters = 300.0,
        strokeColor = Color.Green,
        strokeWidth = 2.dp,
        fillColor = Color.Green.copy(alpha = 0.4f),
        extra = "Green area"
    )
}
```

## Manejo de eventos

Las interacciones con círculos se gestionan desde el componente de mapa:

```kotlin
// Sustituye MapView por el proveedor de mapas que prefieras, como GoogleMapView o MapboxMapView
MapView(
    state = mapViewState,
    onCircleClick = { circleEvent ->
        val circle = circleEvent.state
        val clickPoint = circleEvent.clicked

        println("Circle clicked:")
        println("  Center: ${circle.center}")
        println("  Radius: ${circle.radiusMeters}m")
        println("  Click point: ${clickPoint}")
        println("  Extra data: ${circle.extra}")
    }
) {
    Circle(
        center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        radiusMeters = 1000.0,
        clickable = true,
        extra = "Clickable circle"
    )
}
```

## Opciones de estilo

### Estilos de trazo

```kotlin
// Borde fino
Circle(
    center = center,
    radiusMeters = 500.0,
    strokeColor = Color.Black,
    strokeWidth = 1.dp
)

// Borde grueso
Circle(
    center = center,
    radiusMeters = 500.0,
    strokeColor = Color.Black,
    strokeWidth = 5.dp
)

// Sin borde
Circle(
    center = center,
    radiusMeters = 500.0,
    strokeColor = Color.Transparent,
    strokeWidth = 0.dp
)
```

### Estilos de relleno

```kotlin
// Relleno sólido
Circle(
    center = center,
    radiusMeters = 500.0,
    fillColor = Color.Red
)

// Relleno semitransparente
Circle(
    center = center,
    radiusMeters = 500.0,
    fillColor = Color.Red.copy(alpha = 0.5f)
)

// Sin relleno
Circle(
    center = center,
    radiusMeters = 500.0,
    fillColor = Color.Transparent
)
```

## Identificación de círculos

### Uso de la propiedad ID

La propiedad `id` proporciona un identificador único para cada círculo y permite un seguimiento y gestión eficientes:

```kotlin
// Crear círculos con IDs únicos
val circles = listOf(
    Circle(
        center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        radiusMeters = 1000.0,
        strokeColor = Color.Red,
        fillColor = Color.Red.copy(alpha = 0.3f),
        id = "zone-a"
    ),
    Circle(
        center = GeoPointImpl.fromLatLong(37.7849, -122.4094),
        radiusMeters = 1500.0,
        strokeColor = Color.Blue,
        fillColor = Color.Blue.copy(alpha = 0.3f),
        id = "zone-b"
    )
)

// Uso de IDs en el manejo de eventos
MapView(
    state = mapViewState,
    onCircleClick = { circleEvent ->
        when (circleEvent.state.id) {
            "zone-a" -> handleZoneA()
            "zone-b" -> handleZoneB()
            else -> handleUnknownZone()
        }
    }
) {
    circles.forEach { circle -> Circle(circle) }
}
```

