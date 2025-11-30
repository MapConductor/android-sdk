---
title: "GroundImageState"
---

`GroundImageState` gestiona la configuración y el comportamiento de las superposiciones de imagen en el mapa. Proporciona propiedades reactivas para los límites geográficos, el recurso de imagen y la opacidad.

## Constructor

```kotlin
GroundImageState(
    bounds: GeoRectBounds,
    image: Drawable,
    opacity: Float = 1.0f,
    id: String? = null,
    extra: Serializable? = null
)
```

## Propiedades

### Propiedades básicas
- **`id: String`**: Identificador único (se genera automáticamente si no se proporciona)
- **`bounds: GeoRectBounds`**: Rectángulo geográfico donde se dibuja la imagen
- **`image: Drawable`**: Imagen a mostrar
- **`opacity: Float`**: Nivel de transparencia (0.0 = transparente, 1.0 = opaco)
- **`extra: Serializable?`**: Datos adicionales asociados a la imagen superpuesta

## Métodos

```kotlin
fun fingerPrint(): GroundImageFingerPrint // Detección de cambios
fun asFlow(): Flow<GroundImageFingerPrint> // Actualizaciones reactivas
```

## Ejemplos de uso

### GroundImage básico

```kotlin
@Composable
fun BasicGroundImageExample() {
    val context = LocalContext.current
    val drawable = AppCompatResources.getDrawable(context, R.drawable.overlay_image)

    val groundImageState = drawable?.let {
        GroundImageState(
            bounds = GeoRectBounds(
                southwest = GeoPointImpl.fromLatLong(37.7649, -122.4294),
                northeast = GeoPointImpl.fromLatLong(37.7849, -122.4094)
            ),
            image = it,
            opacity = 0.7f,
            extra = "Base overlay"
        )
    }

    // Sustituye MapView por el proveedor de mapas que prefieras, como GoogleMapView o MapboxMapView
MapView(state = mapViewState) {
        groundImageState?.let { state ->
            GroundImage(state)
        }
    }
}
```

### GroundImage interactivo

```kotlin
@Composable
fun InteractiveGroundImageExample() {
    var groundImageState by remember { mutableStateOf<GroundImageState?>(null) }
    var opacity by remember { mutableStateOf(0.7f) }

    val context = LocalContext.current

    LaunchedEffect(opacity) {
        val drawable = AppCompatResources.getDrawable(context, R.drawable.map_overlay)
        drawable?.let {
            groundImageState = GroundImageState(
                bounds = GeoRectBounds(
                    southwest = GeoPointImpl.fromLatLong(37.7649, -122.4294),
                    northeast = GeoPointImpl.fromLatLong(37.7849, -122.4094)
                ),
                image = it,
                opacity = opacity,
                extra = "Interactive overlay"
            )
        }
    }

    Column {
        Slider(
            value = opacity,
            onValueChange = { opacity = it },
            valueRange = 0f..1f,
            modifier = Modifier.padding(16.dp)
        )
        Text("Opacity: ${(opacity * 100).toInt()}%")

        // Sustituye MapView por el proveedor de mapas que prefieras, como GoogleMapView o MapboxMapView
MapView(
            state = mapViewState,
            onGroundImageClick = { event ->
                println("Ground image clicked at: ${event.clicked}")
            }
        ) {
            groundImageState?.let { state ->
                GroundImage(state)
            }
        }
    }
}
```

### Múltiples overlays

```kotlin
@Composable
fun MultiLayerGroundImageExample() {
    val context = LocalContext.current

    val overlayStates = remember {
        listOf(
            Triple(R.drawable.base_layer, 0.3f, "Base satellite"),
            Triple(R.drawable.weather_layer, 0.6f, "Weather data"),
            Triple(R.drawable.traffic_layer, 0.8f, "Traffic info")
        ).mapNotNull { (resId, opacity, description) ->
            AppCompatResources.getDrawable(context, resId)?.let { drawable ->
                GroundImageState(
                    bounds = GeoRectBounds(
                        southwest = GeoPointImpl.fromLatLong(37.7649, -122.4294),
                        northeast = GeoPointImpl.fromLatLong(37.7849, -122.4094)
                    ),
                    image = drawable,
                    opacity = opacity,
                    extra = description
                )
            }
        }
    }

    // Sustituye MapView por el proveedor de mapas que prefieras, como GoogleMapView o MapboxMapView
MapView(state = mapViewState) {
        overlayStates.forEach { state ->
            GroundImage(state)
        }
    }
}
```

### Límites dinámicos

```kotlin
@Composable
fun DynamicBoundsExample() {
    var southwest by remember {
        mutableStateOf(GeoPointImpl.fromLatLong(37.7649, -122.4294))
    }
    var northeast by remember {
        mutableStateOf(GeoPointImpl.fromLatLong(37.7849, -122.4094))
    }

    val context = LocalContext.current
    val drawable = AppCompatResources.getDrawable(context, R.drawable.floor_plan)

    val groundImageState = drawable?.let {
        GroundImageState(
            bounds = GeoRectBounds(southwest = southwest, northeast = northeast),
            image = it,
            opacity = 0.8f,
            extra = "Floor plan"
        )
    }

    // Sustituye MapView por el proveedor de mapas que prefieras, como GoogleMapView o MapboxMapView
MapView(
        state = mapViewState,
        onMarkerDrag = { markerState ->
            when (markerState.extra) {
                "SW" -> southwest = markerState.position
                "NE" -> northeast = markerState.position
            }
        }
    ) {
        groundImageState?.let { state ->
            GroundImage(state)
        }

        // Marcadores de las esquinas
        Marker(
            position = southwest,
            icon = DefaultIcon(fillColor = Color.Green, label = "SW"),
            draggable = true,
            extra = "SW"
        )

        Marker(
            position = northeast,
            icon = DefaultIcon(fillColor = Color.Red, label = "NE"),
            draggable = true,
            extra = "NE"
        )
    }
}
```

### Overlay animado

```kotlin
@Composable
fun AnimatedGroundImageExample() {
    var isAnimating by remember { mutableStateOf(false) }
    var opacity by remember { mutableStateOf(0.5f) }

    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            while (isAnimating) {
                delay(100)
                opacity = (sin(System.currentTimeMillis() / 1000.0).toFloat() + 1f) / 2f
            }
        }
    }

    val context = LocalContext.current
    val drawable = AppCompatResources.getDrawable(context, R.drawable.animated_overlay)

    val groundImageState = drawable?.let {
        GroundImageState(
            bounds = GeoRectBounds(
                southwest = GeoPointImpl.fromLatLong(37.7649, -122.4294),
                northeast = GeoPointImpl.fromLatLong(37.7849, -122.4094)
            ),
            image = it,
            opacity = opacity,
            extra = "Animated overlay"
        )
    }

    Column {
        Button(onClick = { isAnimating = !isAnimating }) {
            Text(if (isAnimating) "Stop" else "Animate")
        }

        // Sustituye MapView por el proveedor de mapas que prefieras, como GoogleMapView o MapboxMapView
MapView(state = mapViewState) {
            groundImageState?.let { state ->
                GroundImage(state)
            }
        }
    }
}
```

## Manejo de eventos

Los eventos de GroundImage proporcionan tanto el estado como la posición del clic:

```kotlin
data class GroundImageEvent(
    val state: GroundImageState,
    val clicked: GeoPointImpl?
)

typealias OnGroundImageEventHandler = (GroundImageEvent) -> Unit
```

## Gestión de recursos de imagen

### Carga desde diferentes orígenes

```kotlin
// Desde recursos
val drawable = AppCompatResources.getDrawable(context, R.drawable.overlay)

// Desde assets
val inputStream = context.assets.open("overlays/map.png")
val drawable = Drawable.createFromStream(inputStream, null)

// Desde red (usa una librería de carga de imágenes)
// Utiliza librerías como Coil, Glide o Picasso para imágenes de red
```

## Buenas prácticas

1. **Tamaño de la imagen**: Optimiza la resolución de la imagen para mejorar el rendimiento.
2. **Precisión de los límites**: Asegura una posición geográfica precisa.
3. **Opacidad**: Usa un nivel de transparencia adecuado para mantener la visibilidad del mapa.
4. **Gestión de recursos**: Cachea y reutiliza los recursos `Drawable` siempre que sea posible.
5. **Orden de capas**: Ten en cuenta el orden de dibujo cuando haya múltiples overlays.
6. **Rendimiento**: Limita el número de GroundImages simultáneos.
7. **Manejo de errores**: Gestiona los errores cuando falle la carga de imágenes.

