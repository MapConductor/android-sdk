# GroundImageState

`GroundImageState` manages the configuration and behavior of ground image overlays on the map. It provides reactive properties for geographic bounds, image resources, and opacity settings.

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

## Properties

### Core Properties
- **`id: String`**: Unique identifier (auto-generated if not provided)
- **`bounds: GeoRectBounds`**: Geographic rectangular bounds for image placement
- **`image: Drawable`**: The drawable image to display
- **`opacity: Float`**: Transparency level (0.0 = transparent, 1.0 = opaque)
- **`extra: Serializable?`**: Additional data attached to the ground image

## Methods

```kotlin
fun fingerPrint(): GroundImageFingerPrint // Change detection
fun asFlow(): Flow<GroundImageFingerPrint> // Reactive updates
```

## Usage Examples

### Basic Ground Image

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

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(state = mapViewState) {
        groundImageState?.let { state ->
            GroundImage(state)
        }
    }
}
```

### Interactive Ground Image

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

        // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
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

### Multiple Overlays

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

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(state = mapViewState) {
        overlayStates.forEach { state ->
            GroundImage(state)
        }
    }
}
```

### Dynamic Bounds

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

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
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

        // Corner markers
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

### Animated Overlay

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

        // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(state = mapViewState) {
            groundImageState?.let { state ->
                GroundImage(state)
            }
        }
    }
}
```

## Event Handling

Ground image events provide both the state and click location:

```kotlin
data class GroundImageEvent(
    val state: GroundImageState,
    val clicked: GeoPointImpl?
)

typealias OnGroundImageEventHandler = (GroundImageEvent) -> Unit
```

## Image Resource Management

### Loading from Different Sources

```kotlin
// From resources
val drawable = AppCompatResources.getDrawable(context, R.drawable.overlay)

// From assets
val inputStream = context.assets.open("overlays/map.png")
val drawable = Drawable.createFromStream(inputStream, null)

// From network (with image loading library)
// Use libraries like Coil, Glide, or Picasso for network images
```

## Best Practices

1. **Image Size**: Optimize image resolution for performance
2. **Bounds Accuracy**: Ensure precise geographic alignment
3. **Opacity**: Use appropriate transparency for visibility
4. **Resource Management**: Cache and reuse drawable resources
5. **Layer Order**: Consider drawing order for multiple overlays
6. **Performance**: Limit the number of simultaneous ground images
7. **Error Handling**: Handle cases where images fail to load