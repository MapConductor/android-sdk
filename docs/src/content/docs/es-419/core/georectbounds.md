---
title: "GeoRectBounds"
---

`GeoRectBounds` representa un área geográfica rectangular definida por dos esquinas: suroeste y noreste. Se utiliza para definir regiones de mapa, límites de imágenes de tipo ground image y para cálculos de viewport.

## Constructor

```kotlin
class GeoRectBounds(
    southWest: GeoPointImpl? = null,
    northEast: GeoPointImpl? = null
)
```

## Propiedades

### Esquinas

- **`southWest: GeoPointImpl?`**: esquina suroeste (inferior izquierda) del rectángulo.
- **`northEast: GeoPointImpl?`**: esquina noreste (superior derecha) del rectángulo.
- **`isEmpty: Boolean`**: devuelve `true` si los límites no están definidos.

### Propiedades calculadas

- **`center: GeoPointImpl?`**: punto central de los límites.
- **`toSpan(): GeoPointImpl?`**: devuelve el “span” (ancho/alto) como un `GeoPoint`.

## Uso básico

### Crear límites

```kotlin
// Definir un área rectangular
val bounds = GeoRectBounds(
    southWest = GeoPointImpl.fromLatLong(37.7649, -122.4294),
    northeast = GeoPointImpl.fromLatLong(37.7849, -122.4094)
)

// Límites vacíos (para extenderlos más tarde)
val emptyBounds = GeoRectBounds()
```

### Uso con GroundImage

```kotlin
@Composable
fun GroundImageExample() {
    val imageBounds = GeoRectBounds(
        southWest = GeoPointImpl.fromLatLong(37.7649, -122.4294),
        northEast = GeoPointImpl.fromLatLong(37.7849, -122.4094)
    )

    // Sustituye MapView por el proveedor de mapas que prefieras, como GoogleMapView o MapboxMapView
    MapView(state = mapViewState) {
        val context = LocalContext.current
        AppCompatResources.getDrawable(context, R.drawable.overlay_image)?.let { drawable ->
            GroundImage(
                bounds = imageBounds,
                image = drawable,
                opacity = 0.7f
            )
        }
    }
}
```

## Buenas prácticas

- Utiliza siempre coordenadas válidas (latitudes entre -90 y 90, longitudes entre -180 y 180).
- Asegúrate de que `southWest` esté realmente al suroeste de `northEast` para evitar resultados inesperados.
- Reutiliza instancias de `GeoRectBounds` cuando sea posible para evitar asignaciones innecesarias en operaciones frecuentes.
