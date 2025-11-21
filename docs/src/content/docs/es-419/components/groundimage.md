---
title: "GroundImage"
---

`GroundImage` coloca una imagen sobre el mapa en una región geográfica concreta. Actualmente está soportado solo en Google Maps.

## Uso básico

```kotlin
@Composable
fun MapViewScope.GroundImage(
    image: Painter,
    bounds: GeoRectBounds,
    transparency: Float = 0.0f,
    clickable: Boolean = false,
    zIndex: Int? = null,
    id: String? = null,
    extra: Serializable? = null
)
```

- **`image`**: Imagen a mostrar.
- **`bounds`**: Rectángulo geográfico donde se dibuja la imagen.
- **`transparency`**: Transparencia (0.0 = opaco, 1.0 = totalmente transparente).

## Ejemplo

```kotlin
val bounds = GeoRectBoundsImpl(
    southWest = GeoPointImpl.fromLatLong(37.77, -122.43),
    northEast = GeoPointImpl.fromLatLong(37.79, -122.41),
)

MapView(
    state = mapViewState,
    onGroundImageClick = { event ->
        println("GroundImage clicked: ${event.state.extra}")
    }
) {
    GroundImage(
        image = painterResource(id = R.drawable.overlay_image),
        bounds = bounds,
        transparency = 0.2f,
        extra = "Overlay"
    )
}
```

