# Event Handlers

MapConductor provides comprehensive event handling for user interactions with the map and its components. All events are handled through the `MapViewContainer` component.

## Map Events

### Map Initialization

```kotlin
onMapViewInitialized: OnMapViewInitializedHandler? = null
```
Called when the map view is first initialized.

```kotlin
onMapLoaded: OnMapLoadedHandler? = null
```
Called when the map has finished loading and is ready for interaction.

### Map Interaction

```kotlin
onMapClick: OnMapEventHandler? = null
```
Called when the user taps on the map (not on any overlay).

**Event Data**: `GeoPoint` - the geographic coordinates of the tap

## Marker Events

All marker events receive a `MarkerState` object containing the marker's current state.

### Click Events

```kotlin
onMarkerClick: OnMarkerEventHandler? = null
```
Called when a marker is tapped.

### Drag Events

```kotlin
onMarkerDragStart: OnMarkerEventHandler? = null
onMarkerDrag: OnMarkerEventHandler? = null
onMarkerDragEnd: OnMarkerEventHandler? = null
```

- **`onMarkerDragStart`**: Called when dragging begins
- **`onMarkerDrag`**: Called continuously during dragging
- **`onMarkerDragEnd`**: Called when dragging ends

### Animation Events

```kotlin
onMarkerAnimateStart: OnMarkerEventHandler? = null
onMarkerAnimateEnd: OnMarkerEventHandler? = null
```

Called when marker animations start and end.

## Overlay Events

### Circle Events

```kotlin
onCircleClick: OnCircleEventHandler? = null
```

**Event Data**: `CircleEvent`
```kotlin
data class CircleEvent(
    val state: CircleState,
    val clicked: GeoPoint
)
```

### Polyline Events

```kotlin
onPolylineClick: OnPolylineEventHandler? = null
```

**Event Data**: `PolylineEvent`
```kotlin
data class PolylineEvent(
    val state: PolylineState,
    val clicked: GeoPoint
)
```

### Polygon Events

```kotlin
onPolygonClick: OnPolygonEventHandler? = null
```

**Event Data**: `PolygonEvent`
```kotlin
data class PolygonEvent(
    val state: PolygonState,
    val clicked: GeoPoint?
)
```

### GroundImage Events

```kotlin
onGroundImageClick: OnGroundImageEventHandler? = null
```

**Event Data**: `GroundImageEvent`
```kotlin
data class GroundImageEvent(
    val state: GroundImageState,
    val clicked: GeoPointImpl?
)
```

## Usage Examples

### Basic Event Handling

```kotlin
// Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(
    state = mapViewState,
    onMapClick = { geoPoint ->
        println("Map clicked at: ${geoPoint.latitude}, ${geoPoint.longitude}")
    },
    onMarkerClick = { markerState ->
        println("Marker clicked: ${markerState.extra}")
    },
    onCircleClick = { circleEvent ->
        println("Circle clicked: ${circleEvent.state.extra}")
    }
) {
    // Map content
}
```

### Advanced Event Handling

```kotlin
@Composable
fun AdvancedEventHandlingExample() {
    var selectedItem by remember { mutableStateOf<String?>(null) }
    var draggedMarker by remember { mutableStateOf<MarkerState?>(null) }

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(
        state = mapViewState,
        onMapClick = { geoPoint ->
            selectedItem = null
            println("Map clicked at: $geoPoint")
        },
        onMarkerClick = { markerState ->
            selectedItem = "Marker: ${markerState.extra}"
        },
        onMarkerDragStart = { markerState ->
            draggedMarker = markerState
            println("Started dragging: ${markerState.id}")
        },
        onMarkerDrag = { markerState ->
            draggedMarker = markerState
            println("Dragging to: ${markerState.position}")
        },
        onMarkerDragEnd = { markerState ->
            draggedMarker = null
            println("Finished dragging: ${markerState.id}")
        },
        onCircleClick = { circleEvent ->
            selectedItem = "Circle: ${circleEvent.state.extra}"
        },
        onPolylineClick = { polylineEvent ->
            selectedItem = "Polyline: ${polylineEvent.state.extra}"
        },
        onPolygonClick = { polygonEvent ->
            selectedItem = "Polygon: ${polygonEvent.state.extra}"
        }
    ) {
        // Map components
    }

    // Show selected item info
    selectedItem?.let { item ->
        Card(modifier = Modifier.padding(16.dp)) {
            Text(item, modifier = Modifier.padding(16.dp))
        }
    }
}
```

### Conditional Event Handling

```kotlin
@Composable
fun ConditionalEventExample() {
    var editMode by remember { mutableStateOf(false) }
    var selectedMarkers by remember { mutableStateOf<Set<String>>(emptySet()) }

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(
        state = mapViewState,
        onMapClick = { geoPoint ->
            if (editMode) {
                // Add new marker in edit mode
                addNewMarker(geoPoint)
            } else {
                // Clear selection in view mode
                selectedMarkers = emptySet()
            }
        },
        onMarkerClick = { markerState ->
            if (editMode) {
                // Toggle selection in edit mode
                selectedMarkers = if (markerState.id in selectedMarkers) {
                    selectedMarkers - markerState.id
                } else {
                    selectedMarkers + markerState.id
                }
            } else {
                // Show info in view mode
                showMarkerInfo(markerState)
            }
        },
        onMarkerDragStart = { markerState ->
            if (!editMode) {
                // Prevent dragging in view mode
                return@MapViewContainer
            }
        }
    ) {
        // Map content
    }
}
```

### Event Debouncing

```kotlin
@Composable
fun DebouncedEventExample() {
    var searchLocation by remember { mutableStateOf<GeoPoint?>(null) }

    // Debounce search to avoid excessive API calls
    LaunchedEffect(searchLocation) {
        searchLocation?.let { location ->
            delay(500) // Wait 500ms
            performLocationSearch(location)
        }
    }

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(
        state = mapViewState,
        onMapClick = { geoPoint ->
            searchLocation = geoPoint
        }
    ) {
        searchLocation?.let { location ->
            Marker(
                position = location,
                icon = DefaultIcon(
                    fillColor = Color.Blue,
                    label = "🔍"
                )
            )
        }
    }
}
```

### Multi-touch Gesture Events

```kotlin
@Composable
fun GestureEventExample() {
    var gestureInfo by remember { mutableStateOf("No gesture") }

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(
        state = mapViewState,
        onMapClick = { geoPoint ->
            gestureInfo = "Single tap at: $geoPoint"
        },
        // Note: Long press and other gestures would be handled
        // through the underlying map provider's gesture system
    ) {
        // Display gesture info
        Text(
            text = gestureInfo,
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(8.dp),
            color = Color.White
        )
    }
}
```

## Event Handler Types

```kotlin
typealias OnMapViewInitializedHandler = () -> Unit
typealias OnMapLoadedHandler = () -> Unit
typealias OnMapEventHandler = (GeoPoint) -> Unit
typealias OnMarkerEventHandler = (MarkerState) -> Unit
typealias OnCircleEventHandler = (CircleEvent) -> Unit
typealias OnPolylineEventHandler = (PolylineEvent) -> Unit
typealias OnPolygonEventHandler = (PolygonEvent) -> Unit
typealias OnGroundImageEventHandler = (GroundImageEvent) -> Unit
```

## Best Practices

1. **Performance**: Avoid heavy computations in event handlers
2. **State Updates**: Use event handlers to update your application state
3. **User Feedback**: Provide immediate visual feedback for user interactions
4. **Error Handling**: Handle potential null values and edge cases
5. **Debouncing**: Debounce rapid events like dragging to improve performance
6. **Conditional Logic**: Use application state to determine event behavior
7. **Event Propagation**: Be aware that some events may bubble up or be consumed