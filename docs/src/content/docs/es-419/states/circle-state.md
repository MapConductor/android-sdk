---
title: "CircleState"
---

`CircleState` gestiona la configuración y el comportamiento de las superposiciones circulares en el mapa. Proporciona propiedades reactivas para la posición central, el radio, el estilo y la configuración de interacción.

## Constructor

```kotlin
CircleState(
    center: GeoPoint,
    radiusMeters: Double,
    clickable: Boolean = true,
    strokeColor: Color = Color.Red,
    strokeWidth: Dp = 1.dp,
    fillColor: Color = Color(red = 255, green = 255, blue = 255, alpha = 127),
    id: String? = null,
    zIndex: Int? = null,
    extra: Serializable? = null
)
```

## Propiedades

### Propiedades básicas
- **`id: String`**: Identificador único (se genera automáticamente si no se proporciona)
- **`center: GeoPoint`**: Centro geográfico del círculo
- **`radiusMeters: Double`**: Radio en metros
- **`clickable: Boolean`**: Indica si el círculo responde a eventos de clic
- **`extra: Serializable?`**: Datos adicionales asociados al círculo

### Propiedades visuales
- **`strokeColor: Color`**: Color del borde
- **`strokeWidth: Dp`**: Grosor del borde
- **`fillColor: Color`**: Color de relleno interior
- **`zIndex: Int?`**: Orden de dibujo (los valores más altos se dibujan por encima)

## Métodos

```kotlin
fun copy(...): CircleState // Crea una copia modificada
fun fingerPrint(): CircleFingerPrint // Detección de cambios
fun asFlow(): Flow<CircleFingerPrint> // Actualizaciones reactivas
```

## Ejemplos de uso

### Círculo básico

```kotlin
val circleState = CircleState(
    center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    radiusMeters = 1000.0,
    strokeColor = Color.Blue,
    fillColor = Color.Blue.copy(alpha = 0.3f)
)

// Sustituye MapView por el proveedor de mapas que prefieras, como GoogleMapView o MapboxMapView
MapView(state = mapViewState) {
    Circle(circleState)
}
```

### Círculo interactivo

```kotlin
@Composable
fun InteractiveCircleExample() {
    var circleState by remember {
        mutableStateOf(
            CircleState(
                center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                radiusMeters = 500.0,
                strokeColor = Color.Red,
                fillColor = Color.Red.copy(alpha = 0.2f),
                clickable = true
            )
        )
    }

    Column {
        Slider(
            value = circleState.radiusMeters.toFloat(),
            onValueChange = {
                circleState = circleState.copy(radiusMeters = it.toDouble())
            },
            valueRange = 100f..2000f
        )

        // Sustituye MapView por el proveedor de mapas que prefieras, como GoogleMapView o MapboxMapView
MapView(
            state = mapViewState,
            onCircleClick = { event ->
                println("Circle clicked at: ${event.clicked}")
            }
        ) {
            Circle(circleState)
        }
    }
}
```

### Varios círculos con Z-Index

```kotlin
val circles = listOf(
    CircleState(
        center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        radiusMeters = 1000.0,
        fillColor = Color.Red.copy(alpha = 0.3f),
        zIndex = 1
    ),
    CircleState(
        center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        radiusMeters = 500.0,
        fillColor = Color.Blue.copy(alpha = 0.5f),
        zIndex = 2
    )
)

// Sustituye MapView por el proveedor de mapas que prefieras, como GoogleMapView o MapboxMapView
MapView(state = mapViewState) {
    circles.forEach { circleState ->
        Circle(circleState)
    }
}
```

## Manejo de eventos

Los eventos del círculo proporcionan tanto el estado como la posición del clic:

```kotlin
data class CircleEvent(
    val state: CircleState,
    val clicked: GeoPoint
)

typealias OnCircleEventHandler = (CircleEvent) -> Unit
```

## Buenas prácticas

1. **Unidades de radio**: Especifica siempre el radio en metros para mantener la coherencia.
2. **Alfa de color**: Utiliza transparencia (canal alfa) para mejorar la visibilidad del mapa.
3. **Z-Index**: Usa z-index para controlar la superposición de círculos.
4. **Rendimiento**: Evita crear demasiados círculos grandes.
5. **Reactividad**: Actualiza las propiedades directamente para permitir un renderizado eficiente.
