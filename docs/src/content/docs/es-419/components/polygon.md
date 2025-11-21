---
title: "Polygon"
---

`Polygon` dibuja un área cerrada en el mapa usando una lista de puntos. También puede contener agujeros internos.

## Uso básico

```kotlin
@Composable
fun MapViewScope.Polygon(
    points: List<GeoPoint>,
    holes: List<List<GeoPoint>> = emptyList(),
    clickable: Boolean = true,
    strokeColor: Color = Color.Black,
    strokeWidth: Dp = 1.dp,
    fillColor: Color = Color(red = 255, green = 255, blue = 255, alpha = 127),
    zIndex: Int? = null,
    id: String? = null,
    extra: Serializable? = null
)
```

- **`points`**: Lista de puntos que definen el contorno exterior.
- **`holes`**: Lista de polígonos internos que representan agujeros.

## Ejemplo

```kotlin
val areaPoints = listOf(
    GeoPointImpl.fromLatLong(37.7749, -122.4194),
    GeoPointImpl.fromLatLong(37.7849, -122.4294),
    GeoPointImpl.fromLatLong(37.7949, -122.4194),
    GeoPointImpl.fromLatLong(37.7849, -122.4094),
)

MapView(
    state = mapViewState,
    onPolygonClick = { event ->
        println("Polygon clicked: ${event.state.extra}")
    }
) {
    Polygon(
        points = areaPoints,
        fillColor = Color.Green.copy(alpha = 0.3f),
        strokeColor = Color.DarkGray,
        strokeWidth = 2.dp,
        extra = "Area A"
    )
}
```

