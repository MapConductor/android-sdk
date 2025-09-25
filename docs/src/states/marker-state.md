# MarkerState

`MarkerState` manages the configuration and behavior of markers on the map. It provides reactive properties that automatically update the marker's appearance and position when modified.

## Constructor

```kotlin
MarkerState(
    position: GeoPoint,
    id: String? = null,
    extra: Serializable? = null,
    icon: MarkerIcon? = null,
    animation: MarkerAnimation? = null,
    clickable: Boolean = true,
    draggable: Boolean = false
)
```

## Properties

### Core Properties

- **`id: String`**: Unique identifier for the marker (auto-generated if not provided)
- **`position: GeoPoint`**: Current geographic position of the marker
- **`extra: Serializable?`**: Additional data attached to the marker

### Visual Properties

- **`icon: MarkerIcon?`**: Custom icon for the marker (uses default if null)
- **`clickable: Boolean`**: Whether the marker responds to click events
- **`draggable: Boolean`**: Whether the marker can be dragged by the user

### Interactive Properties

- **`isDragging: Boolean`** (read-only): Current dragging state
- **`internalPosition: GeoPoint`** (read-only): Position used for rendering (accounts for drag state)

## Methods

### Animation

```kotlin
fun setAnimation(animation: MarkerAnimation?)
```
Sets or clears the marker animation.

```kotlin
internal fun getAnimation(): MarkerAnimation?
```
Gets the current animation (internal use).

### State Management

```kotlin
fun copy(
    id: String? = this.id,
    position: GeoPoint = this.position,
    extra: Serializable? = this.extra,
    icon: MarkerIcon? = this.icon,
    clickable: Boolean? = this.clickable,
    draggable: Boolean? = this.draggable
): MarkerState
```
Creates a copy of the marker state with modified properties.

### Reactive Updates

```kotlin
fun asFlow(): Flow<MarkerFingerPrint>
```
Returns a flow that emits when the marker state changes.

```kotlin
fun fingerPrint(): MarkerFingerPrint
```
Creates a fingerprint for efficient change detection.

## Usage Examples

### Basic MarkerState

```kotlin
val markerState = MarkerState(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    extra = "San Francisco marker"
)

// Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(state = mapViewState) {
    Marker(markerState)
}
```

### Customized MarkerState

```kotlin
val customMarkerState = MarkerState(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    icon = DefaultIcon(
        fillColor = Color.Blue,
        label = "SF",
        scale = 1.2f
    ),
    clickable = true,
    draggable = true,
    extra = "Draggable San Francisco marker"
)

// Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(
    state = mapViewState,
    onMarkerClick = { markerState ->
        println("Clicked marker: ${markerState.extra}")
    },
    onMarkerDrag = { markerState ->
        println("Marker dragged to: ${markerState.position}")
    }
) {
    Marker(customMarkerState)
}
```

### Dynamic MarkerState Updates

```kotlin
@Composable
fun DynamicMarkerExample() {
    var markerState by remember {
        mutableStateOf(
            MarkerState(
                position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                icon = DefaultIcon(fillColor = Color.Red, label = "1"),
                extra = "Counter: 1"
            )
        )
    }

    var counter by remember { mutableStateOf(1) }

    Column {
        Button(
            onClick = {
                counter++
                markerState = markerState.copy(
                    icon = DefaultIcon(
                        fillColor = Color.Blue,
                        label = counter.toString()
                    ),
                    extra = "Counter: $counter"
                )
            }
        ) {
            Text("Update Marker ($counter)")
        }

        // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(state = mapViewState) {
            Marker(markerState)
        }
    }
}
```

### MarkerState with Position Animation

```kotlin
@Composable
fun AnimatedMarkerExample() {
    val startPosition = GeoPointImpl.fromLatLong(37.7749, -122.4194)
    val endPosition = GeoPointImpl.fromLatLong(37.7849, -122.4094)

    var markerState by remember {
        mutableStateOf(
            MarkerState(
                position = startPosition,
                icon = DefaultIcon(fillColor = Color.Green, label = "Moving"),
                extra = "Animated marker"
            )
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            markerState = markerState.copy(
                position = if (markerState.position == startPosition) endPosition else startPosition
            )
        }
    }

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(state = mapViewState) {
        Marker(markerState)
    }
}
```

### MarkerState with Drag Handling

```kotlin
@Composable
fun DraggableMarkerExample() {
    var markerState by remember {
        mutableStateOf(
            MarkerState(
                position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                icon = DefaultIcon(
                    fillColor = Color.Purple,
                    label = "Drag",
                    scale = 1.2f
                ),
                draggable = true,
                extra = "Draggable marker"
            )
        )
    }

    Column {
        Text("Marker Position:")
        Text("Lat: ${markerState.position.latitude}")
        Text("Lng: ${markerState.position.longitude}")
        Text("Is Dragging: ${markerState.isDragging}")

        // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(
            state = mapViewState,
            onMarkerDragStart = { draggedMarker ->
                println("Drag started: ${draggedMarker.id}")
            },
            onMarkerDrag = { draggedMarker ->
                if (draggedMarker.id == markerState.id) {
                    markerState = markerState.copy(position = draggedMarker.position)
                }
            },
            onMarkerDragEnd = { draggedMarker ->
                println("Drag ended: ${draggedMarker.id}")
            }
        ) {
            Marker(markerState)
        }
    }
}
```

### Multiple MarkerStates with Different Behaviors

```kotlin
@Composable
fun MultipleMarkersExample() {
    val markerStates = remember {
        listOf(
            MarkerState(
                position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                icon = DefaultIcon(fillColor = Color.Red, label = "Static"),
                clickable = true,
                draggable = false,
                extra = "Static marker"
            ),
            MarkerState(
                position = GeoPointImpl.fromLatLong(37.7849, -122.4094),
                icon = DefaultIcon(fillColor = Color.Green, label = "Drag"),
                clickable = true,
                draggable = true,
                extra = "Draggable marker"
            ),
            MarkerState(
                position = GeoPointImpl.fromLatLong(37.7649, -122.4294),
                icon = DefaultIcon(fillColor = Color.Blue, label = "Info"),
                clickable = true,
                draggable = false,
                extra = "Information marker with long description"
            )
        )
    }

    var selectedMarker by remember { mutableStateOf<MarkerState?>(null) }

    Column {
        selectedMarker?.let { marker ->
            Card(modifier = Modifier.padding(8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Selected: ${marker.extra}")
                    Text("Position: ${marker.position.latitude}, ${marker.position.longitude}")
                    Text("Draggable: ${marker.draggable}")
                }
            }
        }

        // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(
            state = mapViewState,
            onMarkerClick = { clickedMarker ->
                selectedMarker = clickedMarker
            },
            onMapClick = {
                selectedMarker = null
            }
        ) {
            markerStates.forEach { markerState ->
                Marker(markerState)
            }
        }
    }
}
```

### MarkerState with Custom Icons

```kotlin
@Composable
fun CustomIconMarkerExample() {
    val context = LocalContext.current

    val markerStates = remember {
        listOf(
            MarkerState(
                position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                icon = DefaultIcon(
                    scale = 1.5f,
                    fillColor = Color.Red,
                    strokeColor = Color.White,
                    strokeWidth = 2.dp,
                    label = "XL"
                ),
                extra = "Large default icon"
            ),
            MarkerState(
                position = GeoPointImpl.fromLatLong(37.7849, -122.4094),
                icon = AppCompatResources.getDrawable(context, R.drawable.custom_pin)?.let { drawable ->
                    DrawableDefaultIcon(
                        backgroundDrawable = drawable,
                        scale = 1.2f
                    )
                },
                extra = "Custom drawable icon"
            ),
            MarkerState(
                position = GeoPointImpl.fromLatLong(37.7649, -122.4294),
                icon = AppCompatResources.getDrawable(context, R.drawable.weather_icon)?.let { drawable ->
                    ImageIcon(
                        drawable = drawable,
                        anchor = Offset(0.5f, 1.0f)
                    )
                },
                extra = "Weather image icon"
            )
        )
    }

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(state = mapViewState) {
        markerStates.forEach { markerState ->
            Marker(markerState)
        }
    }
}
```

## State Observation

### Observing MarkerState Changes

```kotlin
@Composable
fun MarkerStateObserver() {
    val markerState = remember {
        MarkerState(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            extra = "Observed marker"
        )
    }

    // Observe state changes
    val markerFingerprint by markerState.asFlow().collectAsState(
        initial = markerState.fingerPrint()
    )

    LaunchedEffect(markerFingerprint) {
        println("Marker state changed: $markerFingerprint")
    }

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(state = mapViewState) {
        Marker(markerState)
    }
}
```

## Performance Considerations

### Efficient State Updates

```kotlin
// Good: Update properties directly
markerState.position = newPosition
markerState.icon = newIcon

// Good: Use copy for multiple changes
val updatedState = markerState.copy(
    position = newPosition,
    icon = newIcon,
    extra = newExtra
)

// Avoid: Creating new states unnecessarily
// This creates a new state object each time
val newState = MarkerState(position = newPosition, icon = newIcon)
```

### Memory Management

```kotlin
// Store marker states efficiently
val markerStates = remember { mutableStateMapOf<String, MarkerState>() }

// Add marker
markerStates[markerId] = MarkerState(...)

// Update marker
markerStates[markerId]?.let { state ->
    state.position = newPosition
}

// Remove marker
markerStates.remove(markerId)
```

## Best Practices

1. **Use Meaningful IDs**: Provide custom IDs for markers you need to reference later
2. **Store Useful Data**: Use the `extra` property to store data needed for event handling
3. **Reactive Updates**: Leverage Compose's reactive system - modify properties directly rather than recreating states
4. **Performance**: Use `copy()` when updating multiple properties simultaneously
5. **Memory**: Clean up marker states that are no longer needed
6. **Consistency**: Maintain consistent icon styling across similar marker types
7. **User Feedback**: Provide appropriate visual feedback for interactive markers
8. **Validation**: Ensure position coordinates are valid before creating marker states