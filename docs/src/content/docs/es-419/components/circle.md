---
title: "Circle"
---

El componente `Circle` dibuja una superposición circular en el mapa. Puedes especificar el centro, el radio y los estilos de borde y relleno.

## Uso básico

```kotlin
@Composable
fun MapViewScope.Circle(
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

- **`center`**: Centro del círculo (`GeoPoint`).
- **`radiusMeters`**: Radio en metros.
- **`strokeColor` / `strokeWidth`**: Color y grosor del borde.
- **`fillColor`**: Color de relleno.

## Con CircleState

Combinado con `CircleState`, puedes actualizar el círculo de forma reactiva (consulta [CircleState](/es-419/states/circle-state)).

```kotlin
val circleState = CircleState(
    center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    radiusMeters = 1000.0,
    strokeColor = Color.Blue,
    fillColor = Color.Blue.copy(alpha = 0.3f)
)

MapView(
    state = mapViewState,
    onCircleClick = { event ->
        println("Circle clicked at: ${event.clicked}")
    }
) {
    Circle(circleState)
}
```

