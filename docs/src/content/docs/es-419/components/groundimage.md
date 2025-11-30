---
title: "GroundImage"
---

Las ground images son superposiciones de imagen posicionadas geográficamente sobre el mapa. Son útiles para mostrar planos de plantas, imágenes satelitales, overlays meteorológicos o cualquier dato basado en imagen que deba anclarse a coordenadas específicas.

## Funciones composable

### GroundImage básica

```kotlin
@Composable
fun MapViewScope.GroundImage(
    bounds: GeoRectBounds,
    image: Drawable,
    opacity: Float = 0.5f,
    id: String? = null,
    extra: Serializable? = null
)
```

### GroundImage con estado

```kotlin
@Composable
fun MapViewScope.GroundImage(state: GroundImageState)
```

## Parámetros

- **`bounds`**: límites rectangulares geográficos donde debe colocarse la imagen (`GeoRectBounds`).
- **`image`**: imagen `Drawable` que se va a mostrar.
- **`opacity`**: nivel de opacidad entre 0.0 (transparente) y 1.0 (opaco) (por defecto `0.5f`).
- **`id`**: identificador único opcional para la ground image (`String?`).
- **`extra`**: datos adicionales asociados a la ground image (`Serializable?`).

## Ejemplos de uso

### GroundImage básica

```kotlin
@Composable
fun BasicGroundImageExample() {
    val context = LocalContext.current

    // Sustituye MapView por el proveedor que prefieras, como GoogleMapView o MapboxMapView
MapView(state = mapViewState) {
        AppCompatResources.getDrawable(context, R.drawable.overlay_image)?.let { drawable ->
            GroundImage(
                bounds = GeoRectBounds(
                    southwest = GeoPointImpl.fromLatLong(37.7649, -122.4294),
                    northeast = GeoPointImpl.fromLatLong(37.7849, -122.4094)
                ),
                image = drawable,
                opacity = 0.7f
            )
        }
    }
}
```

### GroundImage interactiva con marcadores de límites

```kotlin
@Composable
fun InteractiveGroundImageExample() {
    var southwest by remember {
        mutableStateOf(GeoPointImpl.fromLatLong(37.7649, -122.4294))
    }
    var northeast by remember {
        mutableStateOf(GeoPointImpl.fromLatLong(37.7849, -122.4094))
    }
    var opacity by remember { mutableStateOf(0.7f) }

    val context = LocalContext.current
    val groundImageDrawable = AppCompatResources.getDrawable(context, R.drawable.map_overlay)

    val bounds = GeoRectBounds(southwest = southwest, northeast = northeast)

    val groundImageState = groundImageDrawable?.let { drawable ->
        GroundImageState(
            bounds = bounds,
            image = drawable,
            opacity = opacity
        )
    }

    // Marcadores de los límites
    val swMarker = MarkerState(
        position = southwest,
        icon = DefaultIcon(
            fillColor = Color.Green,
            label = "SW",
            scale = 0.8f
        ),
        draggable = true,
        extra = "southwest"
    )

    val neMarker = MarkerState(
        position = northeast,
        icon = DefaultIcon(
            fillColor = Color.Red,
            label = "NE",
            scale = 0.8f
        ),
        draggable = true,
        extra = "northeast"
    )

    Column {
        // Control de opacidad
        Slider(
            value = opacity,
            onValueChange = { opacity = it },
            valueRange = 0f..1f,
            modifier = Modifier.padding(16.dp)
        )

        // Sustituye MapView por el proveedor que prefieras, como GoogleMapView o MapboxMapView
MapView(
            state = mapViewState,
            onMarkerDrag = { markerState ->
                when (markerState.extra) {
                    "southwest" -> southwest = markerState.position
                    "northeast" -> northeast = markerState.position
                }
            },
            onGroundImageClick = { event ->
                println("Ground image clicked at: ${event.clicked}")
            }
        ) {
            groundImageState?.let { state ->
                GroundImage(state)
            }

            Marker(swMarker)
            Marker(neMarker)
        }
    }
}
```

## Manejo de eventos

```kotlin
MapView(
    state = mapViewState,
    onGroundImageClick = { event ->
        println("GroundImage clicked: ${event.state.extra}")
    }
) {
    // ...
}
```

Las ground images generan eventos que incluyen tanto el estado (`GroundImageState`) como la posición clicada cuando está disponible.

## Buenas prácticas

- Optimiza el tamaño y resolución de la imagen para evitar un uso excesivo de memoria.
- Define límites (`bounds`) precisos para asegurar que la imagen se alinee correctamente con el mapa.
- Usa opacidades intermedias para permitir que el mapa base siga siendo legible.
- Evita renderizar demasiadas ground images simultáneamente para no afectar al rendimiento.
