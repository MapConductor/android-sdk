---
title: "Marker"
---

Markers are point annotations that can be placed on the map at specific geographic locations. They support custom icons, interactions, and animations.

## Composable Functions

### Basic Marker

```kotlin
@Composable
fun MapViewScope.Marker(
    position: GeoPoint,
    clickable: Boolean = true,
    draggable: Boolean = false,
    icon: MarkerIcon? = null,
    extra: Serializable? = null,
    id: String? = null
)
```

### Marker with State

```kotlin
@Composable
fun MapViewScope.Marker(state: MarkerState)
```

## Parameters

- **`position`**: Geographic coordinates (`GeoPoint`)
- **`clickable`**: Whether the marker responds to clicks (default: `true`)
- **`draggable`**: Whether the marker can be dragged (default: `false`)
- **`icon`**: Custom icon for the marker (`MarkerIcon?`)
- **`extra`**: Additional data attached to the marker (`Serializable?`)
- **`id`**: Unique identifier for the marker (`String?`)

## Icon Types

### DefaultIcon

Standard marker with customizable appearance:

```kotlin
DefaultIcon(
    scale: Float = 1.0f,
    label: String? = null,
    fillColor: Color = Color.Red,
    strokeColor: Color = Color.Black,
    strokeWidth: Dp = 1.dp,
    labelTextColor: Color = Color.White,
    labelStrokeColor: Color? = null,
    debug: Boolean = false
)
```

### DrawableDefaultIcon

Marker using a drawable resource as background:

```kotlin
DrawableDefaultIcon(
    backgroundDrawable: Drawable,
    scale: Float = 1.0f,
    strokeColor: Color? = null,
    strokeWidth: Dp = 1.dp
)
```

### ImageIcon

Marker using a custom drawable image:

```kotlin
ImageIcon(
    drawable: Drawable,
    anchor: Offset = Offset(0.5f, 0.5f),
    debug: Boolean = false
)
```

## Usage Examples

### Basic Marker

```kotlin
// Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(state = mapViewState) {
    Marker(
        position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        extra = "San Francisco",
        id = "san-francisco-marker"
    )
}
```

### Custom Icon Marker

```kotlin
// Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(state = mapViewState) {
    Marker(
        position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        icon = DefaultIcon(
            scale = 1.5f,
            label = "SF",
            fillColor = Color.Blue,
            strokeColor = Color.White,
            strokeWidth = 2.dp
        ),
        extra = "San Francisco with custom icon",
        id = "custom-sf-marker"
    )
}
```

### Draggable Marker

```kotlin
@Composable
fun DraggableMarkerExample() {
    var markerPosition by remember {
        mutableStateOf(GeoPointImpl.fromLatLong(37.7749, -122.4194))
    }

    // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(
        state = mapViewState,
        onMarkerDrag = { markerState ->
            markerPosition = markerState.position
        }
    ) {
        Marker(
            position = markerPosition,
            draggable = true,
            icon = DefaultIcon(
                label = "Drag me",
                fillColor = Color.Green
            )
        )
    }
}
```

### Multiple Markers with Different Icons

```kotlin
// Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(state = mapViewState) {
    // Default icon with different scales
    Marker(
        position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        icon = DefaultIcon(scale = 0.7f, label = "Small")
    )

    Marker(
        position = GeoPointImpl.fromLatLong(37.7849, -122.4094),
        icon = DefaultIcon(scale = 1.0f, label = "Normal")
    )

    Marker(
        position = GeoPointImpl.fromLatLong(37.7949, -122.3994),
        icon = DefaultIcon(scale = 1.4f, label = "Large")
    )

    // Custom colored marker
    Marker(
        position = GeoPointImpl.fromLatLong(37.7649, -122.4294),
        icon = DefaultIcon(
            fillColor = Color.Yellow,
            strokeColor = Color.Black,
            strokeWidth = 2.dp,
            label = "Custom"
        )
    )

    // Drawable icon marker
    val context = LocalContext.current
    AppCompatResources.getDrawable(context, R.drawable.custom_icon)?.let { drawable ->
        Marker(
            position = GeoPointImpl.fromLatLong(37.7549, -122.4394),
            icon = DrawableDefaultIcon(
                backgroundDrawable = drawable,
                scale = 1.2f
            )
        )
    }
}
```

### Marker with Info Bubble

```kotlin
@Composable
fun MarkerWithInfoExample() {
    var selectedMarker by remember { mutableStateOf<MarkerState?>(null) }

    // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(
        state = mapViewState,
        onMapClick = { selectedMarker = null },
        onMarkerClick = { markerState -> selectedMarker = markerState }
    ) {
        val markerState = MarkerState(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = DefaultIcon(label = "SF"),
            extra = "San Francisco - The Golden City"
        )

        Marker(markerState)

        // Show info bubble for selected marker
        selectedMarker?.let { marker ->
            InfoBubble(
                marker = marker,
                bubbleColor = Color.White
            ) {
                Text(
                    text = marker.extra as? String ?: "No info",
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}
```

### Image Icon Marker

```kotlin
// Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(state = mapViewState) {
    val context = LocalContext.current
    AppCompatResources.getDrawable(context, R.drawable.weather_icon)?.let { icon ->
        Marker(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = ImageIcon(
                drawable = icon,
                anchor = Offset(0.5f, 1.0f), // Bottom center anchor
                debug = true
            ),
            extra = "Weather station"
        )
    }
}
```

## Event Handling

Marker events are handled with your map provider component:

```kotlin
// Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(
    state = mapViewState,
    onMarkerClick = { markerState ->
        println("Marker clicked: ${markerState.extra}")
    },
    onMarkerDragStart = { markerState ->
        println("Drag started for marker: ${markerState.id}")
    },
    onMarkerDrag = { markerState ->
        println("Marker dragged to: ${markerState.position}")
    },
    onMarkerDragEnd = { markerState ->
        println("Drag ended for marker: ${markerState.id}")
    }
) {
    // Markers
}
```

## Marker Identification

### Using the ID Property

The `id` property provides a unique identifier for markers, enabling efficient tracking and management:

```kotlin
// Creating markers with unique IDs
val markers = listOf(
    Marker(
        position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        icon = DefaultIcon(fillColor = Color.Red, label = "1"),
        extra = "Location A",
        id = "marker-location-a"
    ),
    Marker(
        position = GeoPointImpl.fromLatLong(37.7849, -122.4094),
        icon = DefaultIcon(fillColor = Color.Blue, label = "2"),
        extra = "Location B",
        id = "marker-location-b"
    )
)

// Using IDs for event handling
MapView(
    state = mapViewState,
    onMarkerClick = { markerState ->
        when (markerState.id) {
            "marker-location-a" -> handleLocationA()
            "marker-location-b" -> handleLocationB()
            else -> handleUnknownMarker()
        }
    }
) {
    markers.forEach { marker -> Marker(marker) }
}
```

### Benefits of Using IDs

- **Unique Identification**: Distinguish between markers even when they have identical properties
- **Event Handling**: Easier to handle click events and interactions
- **State Management**: Simplifies selection and multi-selection scenarios
- **Performance**: Enables efficient updates when marker properties change

## Best Practices

1. **Use Meaningful Extra Data**: Store useful information in the `extra` property for event handling
2. **Provide Unique IDs**: Always set unique `id` values when working with multiple markers
3. **Optimize Icon Resources**: Reuse drawable resources and avoid creating new icons unnecessarily
4. **Handle Drag Events**: When using draggable markers, always handle drag events to update your data
5. **Consider Performance**: Large numbers of markers may impact performance - consider clustering
6. **Anchor Points**: Use appropriate anchor points for custom icons to ensure proper positioning
7. **Consistent Styling**: Maintain consistent icon styling across your application