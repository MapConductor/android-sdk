---
title: "GroundImage"
---

Ground images are image overlays that are positioned geographically on the map. They are useful for displaying floor plans, satellite imagery, weather overlays, or any image-based data that needs to be anchored to specific geographic coordinates.

## Composable Functions

### Basic GroundImage

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

### GroundImage with State

```kotlin
@Composable
fun MapViewScope.GroundImage(state: GroundImageState)
```

## Parameters

- **`bounds`**: Geographic rectangular bounds where the image should be positioned (`GeoRectBounds`)
- **`image`**: The drawable image to display (`Drawable`)
- **`opacity`**: Transparency level from 0.0 (transparent) to 1.0 (opaque) (default: `0.5f`)
- **`id`**: Optional unique identifier for the ground image (`String?`)
- **`extra`**: Additional data attached to the ground image (`Serializable?`)

## Usage Examples

### Basic GroundImage

```kotlin
@Composable
fun BasicGroundImageExample() {
    val context = LocalContext.current

    // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
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

### Interactive GroundImage with Bounds Markers

Based on the example app pattern:

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

    // Bounds markers
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
        // Opacity control
        Slider(
            value = opacity,
            onValueChange = { opacity = it },
            valueRange = 0f..1f,
            modifier = Modifier.padding(16.dp)
        )
        Text("Opacity: ${(opacity * 100).toInt()}%", modifier = Modifier.padding(horizontal = 16.dp))

        // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(
            state = mapViewState,
            onMarkerDrag = { markerState ->
                when (markerState.extra as String) {
                    "southwest" -> southwest = markerState.position
                    "northeast" -> northeast = markerState.position
                }
            },
            onGroundImageClick = { groundImageEvent ->
                println("Ground image clicked at: ${groundImageEvent.clicked}")
            }
        ) {
            // Draw ground image
            groundImageState?.let { state ->
                GroundImage(state)
            }

            // Draw corner markers
            Marker(swMarker)
            Marker(neMarker)

            // Draw bounds rectangle for reference
            val boundsPoints = listOf(
                southwest,
                GeoPointImpl.fromLatLong(southwest.latitude, northeast.longitude),
                northeast,
                GeoPointImpl.fromLatLong(northeast.latitude, southwest.longitude),
                southwest
            )

            Polyline(
                points = boundsPoints,
                strokeColor = Color.Blue,
                strokeWidth = 2.dp
            )
        }
    }
}
```

### Multiple GroundImages with Different Opacities

```kotlin
@Composable
fun MultipleGroundImagesExample() {
    val context = LocalContext.current

    // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(state = mapViewState) {
        // Base satellite image (low opacity)
        AppCompatResources.getDrawable(context, R.drawable.satellite_base)?.let { drawable ->
            GroundImage(
                bounds = GeoRectBounds(
                    southwest = GeoPointImpl.fromLatLong(37.7549, -122.4394),
                    northeast = GeoPointImpl.fromLatLong(37.7949, -122.3994)
                ),
                image = drawable,
                opacity = 0.3f,
                extra = "Satellite base"
            )
        }

        // Weather overlay (medium opacity)
        AppCompatResources.getDrawable(context, R.drawable.weather_overlay)?.let { drawable ->
            GroundImage(
                bounds = GeoRectBounds(
                    southwest = GeoPointImpl.fromLatLong(37.7649, -122.4294),
                    northeast = GeoPointImpl.fromLatLong(37.7849, -122.4094)
                ),
                image = drawable,
                opacity = 0.6f,
                extra = "Weather data"
            )
        }

        // Traffic overlay (high opacity)
        AppCompatResources.getDrawable(context, R.drawable.traffic_overlay)?.let { drawable ->
            GroundImage(
                bounds = GeoRectBounds(
                    southwest = GeoPointImpl.fromLatLong(37.7699, -122.4244),
                    northeast = GeoPointImpl.fromLatLong(37.7799, -122.4144)
                ),
                image = drawable,
                opacity = 0.8f,
                extra = "Traffic data"
            )
        }
    }
}
```

### Dynamic GroundImage Loading

```kotlin
@Composable
fun DynamicGroundImageExample() {
    var selectedImageResource by remember { mutableStateOf(R.drawable.overlay1) }
    var isVisible by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val imageDrawable = AppCompatResources.getDrawable(context, selectedImageResource)

    val bounds = GeoRectBounds(
        southwest = GeoPointImpl.fromLatLong(37.7649, -122.4294),
        northeast = GeoPointImpl.fromLatLong(37.7849, -122.4094)
    )

    Column {
        Row {
            Button(onClick = { selectedImageResource = R.drawable.overlay1 }) {
                Text("Image 1")
            }
            Button(onClick = { selectedImageResource = R.drawable.overlay2 }) {
                Text("Image 2")
            }
            Button(onClick = { selectedImageResource = R.drawable.overlay3 }) {
                Text("Image 3")
            }
        }

        Switch(
            checked = isVisible,
            onCheckedChange = { isVisible = it },
            modifier = Modifier.padding(16.dp)
        )

        // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(state = mapViewState) {
            if (isVisible && imageDrawable != null) {
                GroundImage(
                    bounds = bounds,
                    image = imageDrawable,
                    opacity = 0.7f,
                    extra = "Dynamic overlay"
                )
            }

            // Reference markers at corners
            Marker(
                position = bounds.southwest,
                icon = DefaultIcon(fillColor = Color.Green, label = "SW", scale = 0.6f)
            )
            Marker(
                position = bounds.northeast,
                icon = DefaultIcon(fillColor = Color.Red, label = "NE", scale = 0.6f)
            )
        }
    }
}
```

### Animated GroundImage Opacity

```kotlin
@Composable
fun AnimatedGroundImageExample() {
    var isAnimating by remember { mutableStateOf(false) }
    var opacity by remember { mutableStateOf(0.5f) }

    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            while (isAnimating) {
                delay(50)
                opacity = (sin(System.currentTimeMillis() / 1000.0).toFloat() + 1f) / 2f
            }
        }
    }

    val context = LocalContext.current

    Column {
        Button(
            onClick = { isAnimating = !isAnimating }
        ) {
            Text(if (isAnimating) "Stop Animation" else "Start Animation")
        }

        Text("Current opacity: ${(opacity * 100).toInt()}%")

        // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(state = mapViewState) {
            AppCompatResources.getDrawable(context, R.drawable.animated_overlay)?.let { drawable ->
                GroundImage(
                    bounds = GeoRectBounds(
                        southwest = GeoPointImpl.fromLatLong(37.7649, -122.4294),
                        northeast = GeoPointImpl.fromLatLong(37.7849, -122.4094)
                    ),
                    image = drawable,
                    opacity = opacity,
                    extra = "Animated overlay"
                )
            }
        }
    }
}
```

### Floor Plan Overlay

```kotlin
@Composable
fun FloorPlanExample() {
    val context = LocalContext.current

    // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(state = mapViewState) {
        // Building floor plan
        AppCompatResources.getDrawable(context, R.drawable.building_floor_plan)?.let { drawable ->
            GroundImage(
                bounds = GeoRectBounds(
                    southwest = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                    northeast = GeoPointImpl.fromLatLong(37.7759, -122.4184)
                ),
                image = drawable,
                opacity = 0.8f,
                extra = "Building floor plan"
            )
        }

        // Room markers
        Marker(
            position = GeoPointImpl.fromLatLong(37.7751, -122.4191),
            icon = DefaultIcon(fillColor = Color.Blue, label = "A", scale = 0.6f),
            extra = "Room A"
        )

        Marker(
            position = GeoPointImpl.fromLatLong(37.7754, -122.4189),
            icon = DefaultIcon(fillColor = Color.Red, label = "B", scale = 0.6f),
            extra = "Room B"
        )

        Marker(
            position = GeoPointImpl.fromLatLong(37.7756, -122.4187),
            icon = DefaultIcon(fillColor = Color.Green, label = "C", scale = 0.6f),
            extra = "Room C"
        )
    }
}
```

## Event Handling

GroundImage interactions are handled with your map provider component:

```kotlin
// Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(
    state = mapViewState,
    onGroundImageClick = { groundImageEvent ->
        val groundImage = groundImageEvent.state
        val clickPoint = groundImageEvent.clicked

        println("Ground image clicked:")
        println("  Bounds: ${groundImage.bounds}")
        println("  Opacity: ${groundImage.opacity}")
        println("  Click location: ${clickPoint}")
        println("  Extra data: ${groundImage.extra}")
    }
) {
    GroundImage(
        bounds = bounds,
        image = drawable,
        opacity = 0.7f,
        extra = "Interactive overlay"
    )
}
```

## Image Resources

### Loading from Resources

```kotlin
val context = LocalContext.current
val drawable = AppCompatResources.getDrawable(context, R.drawable.overlay_image)
```

### Loading from Assets

```kotlin
val context = LocalContext.current
val inputStream = context.assets.open("overlays/map_overlay.png")
val drawable = Drawable.createFromStream(inputStream, null)
```

### Loading from Network (with caching)

```kotlin
// Using a library like Coil or Glide
val imageLoader = ImageLoader(context)
val request = ImageRequest.Builder(context)
    .data("https://example.com/overlay.png")
    .build()

LaunchedEffect(request) {
    val drawable = imageLoader.execute(request).drawable
    // Use drawable with GroundImage
}
```

## Best Practices

1. **Image Size**: Use appropriately sized images to balance quality and performance
2. **Opacity**: Use transparency to blend overlays naturally with the map
3. **Bounds Accuracy**: Ensure ground image bounds are precisely aligned with geographic features
4. **Resource Management**: Optimize image resources and consider memory usage
5. **Layer Order**: Consider the drawing order when overlapping multiple ground images
6. **Interactive Elements**: Provide clear visual feedback for interactive ground images
7. **Caching**: Cache image resources efficiently, especially for network-loaded images
8. **Error Handling**: Handle cases where images fail to load
9. **Performance**: Avoid rendering too many large ground images simultaneously
10. **User Experience**: Provide controls for opacity and visibility when appropriate