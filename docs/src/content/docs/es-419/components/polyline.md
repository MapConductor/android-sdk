---
title: "Polyline"
---

`Polyline` dibuja una línea que conecta varios `GeoPoint` en el mapa. Es útil para rutas o trayectorias.

## Uso básico

```kotlin
@Composable
fun MapViewScope.Polyline(
    points: List<GeoPoint>,
    clickable: Boolean = false,
    color: Color = Color.Blue,
    width: Dp = 2.dp,
    zIndex: Int? = null,
    id: String? = null,
    extra: Serializable? = null
)
```

- **`points`**: Lista de puntos que forman la línea.
- **`color` / `width`**: Color y grosor de la línea.

## Ejemplo

```kotlin
val route = listOf(
    GeoPointImpl.fromLatLong(37.7749, -122.4194),
    GeoPointImpl.fromLatLong(37.7849, -122.4094),
    GeoPointImpl.fromLatLong(37.7949, -122.3994),
)

MapView(
    state = mapViewState,
    onPolylineClick = { event ->
        println("Polyline clicked: ${event.state.extra}")
    }
) {
    Polyline(
        points = route,
        color = Color.Cyan,
        width = 3.dp,
        extra = "Route A"
    )
}
```

