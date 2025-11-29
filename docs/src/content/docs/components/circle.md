---
title: "Circle"
---

Circles are circular overlays that can be drawn on the map with customizable radius, stroke, and fill properties. They are useful for representing areas, ranges, or zones.

## Composable Functions

### Basic Circle

```kotlin
@Composable
fun MapViewScope.Circle(
    center: GeoPoint,
    radiusMeters: Double,
    strokeColor: Color = Color.Red,
    strokeWidth: Dp = 2.dp,
    fillColor: Color = Color.White.copy(alpha = 0.5f),
    extra: Serializable? = null,
    id: String? = null
)
```

### Circle with State

```kotlin
@Composable
fun MapViewScope.Circle(state: CircleState)
```

## Parameters

- **`center`**: Geographic center point of the circle (`GeoPoint`)
- **`radiusMeters`**: Radius in meters (`Double`)
- **`strokeColor`**: Color of the circle's border (default: `Color.Red`)
- **`strokeWidth`**: Width of the border line (default: `2.dp`)
- **`fillColor`**: Fill color of the circle interior (default: semi-transparent white)
- **`extra`**: Additional data attached to the circle (`Serializable?`)
- **`id`**: Unique identifier for the circle (`String?`)

## Usage Examples

### Basic Circle

```kotlin
// Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(state = mapViewState) {
    Circle(
        center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        radiusMeters = 1000.0, // 1km radius
        strokeColor = Color.Blue,
        fillColor = Color.Blue.copy(alpha = 0.3f),
        id = "downtown-area"
    )
}
```

### Interactive Circle with Markers

Based on the example app, here's how to create an interactive circle with draggable markers:

```kotlin
@Composable
fun InteractiveCircleExample() {
    var centerPosition by remember {
        mutableStateOf(GeoPointImpl.fromLatLong(37.7749, -122.4194))
    }
    var radiusMeters by remember { mutableStateOf(1000.0) }

    // Calculate edge marker position
    val edgePosition = remember(centerPosition, radiusMeters) {
        // Calculate a point that's 'radiusMeters' meters away from center
        // This is simplified - actual calculation would consider Earth's curvature
        val latOffset = radiusMeters / 111000.0 // Rough meters per degree latitude
        GeoPointImpl.fromLatLong(
            centerPosition.latitude + latOffset,
            centerPosition.longitude
        )
    }

    val circleState = CircleState(
        center = centerPosition,
        radiusMeters = radiusMeters,
        strokeColor = Color.Blue,
        fillColor = Color.Blue.copy(alpha = 0.3f),
        clickable = true
    )

    val centerMarker = MarkerState(
        position = centerPosition,
        icon = DefaultIcon(
            fillColor = Color.Blue,
            label = "C"
        ),
        draggable = false
    )

    val edgeMarker = MarkerState(
        position = edgePosition,
        icon = DefaultIcon(
            fillColor = Color.Green,
            label = "E"
        ),
        draggable = true
    )

    // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(
        state = mapViewState,
        onMarkerDrag = { markerState ->
            if (markerState.id == edgeMarker.id) {
                // Calculate new radius based on edge marker position
                val distance = calculateDistance(centerPosition, markerState.position)
                radiusMeters = distance
            }
        },
        onCircleClick = { circleEvent ->
            println("Circle clicked at: ${circleEvent.clicked}")
        }
    ) {
        Circle(circleState)
        Marker(centerMarker)
        Marker(edgeMarker)
    }
}
```

### Multiple Circles with Different Styles

```kotlin
// Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(state = mapViewState) {
    // Solid red circle
    Circle(
        center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        radiusMeters = 500.0,
        strokeColor = Color.Red,
        strokeWidth = 3.dp,
        fillColor = Color.Red.copy(alpha = 0.2f),
        extra = "Red zone"
    )

    // Blue circle with thick border
    Circle(
        center = GeoPointImpl.fromLatLong(37.7849, -122.4194),
        radiusMeters = 750.0,
        strokeColor = Color.Blue,
        strokeWidth = 5.dp,
        fillColor = Color.Transparent,
        extra = "Blue boundary"
    )

    // Green circle with pattern
    Circle(
        center = GeoPointImpl.fromLatLong(37.7649, -122.4194),
        radiusMeters = 300.0,
        strokeColor = Color.Green,
        strokeWidth = 2.dp,
        fillColor = Color.Green.copy(alpha = 0.4f),
        extra = "Green area"
    )
}
```

### Dynamic Circle Updates

```kotlin
@Composable
fun DynamicCircleExample() {
    var circleRadius by remember { mutableStateOf(500.0) }
    var circleColor by remember { mutableStateOf(Color.Blue) }

    Column {
        // Controls
        Slider(
            value = circleRadius.toFloat(),
            onValueChange = { circleRadius = it.toDouble() },
            valueRange = 100f..2000f,
            modifier = Modifier.padding(16.dp)
        )

        Row {
            Button(onClick = { circleColor = Color.Red }) {
                Text("Red")
            }
            Button(onClick = { circleColor = Color.Blue }) {
                Text("Blue")
            }
            Button(onClick = { circleColor = Color.Green }) {
                Text("Green")
            }
        }

        // Map with dynamic circle
        // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(state = mapViewState) {
            Circle(
                center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                radiusMeters = circleRadius,
                strokeColor = circleColor,
                fillColor = circleColor.copy(alpha = 0.3f)
            )

            // Center marker
            Marker(
                position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                icon = DefaultIcon(
                    fillColor = circleColor,
                    label = "${circleRadius.toInt()}m"
                )
            )
        }
    }
}
```

### Overlapping Circles with Z-Index

```kotlin
// Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(state = mapViewState) {
    val centerPoint = GeoPointImpl.fromLatLong(37.7749, -122.4194)

    // Background circle (larger, lower z-index)
    Circle(
        center = centerPoint,
        radiusMeters = 1000.0,
        strokeColor = Color.Red,
        fillColor = Color.Red.copy(alpha = 0.2f),
        extra = CircleData(zIndex = 1, name = "Outer circle")
    )

    // Foreground circle (smaller, higher z-index)
    Circle(
        center = centerPoint,
        radiusMeters = 500.0,
        strokeColor = Color.Blue,
        fillColor = Color.Blue.copy(alpha = 0.4f),
        extra = CircleData(zIndex = 2, name = "Inner circle")
    )
}
```

## Event Handling

Circle interactions are handled with your map provider component:

```kotlin
// Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(
    state = mapViewState,
    onCircleClick = { circleEvent ->
        val circle = circleEvent.state
        val clickPoint = circleEvent.clicked

        println("Circle clicked:")
        println("  Center: ${circle.center}")
        println("  Radius: ${circle.radiusMeters}m")
        println("  Click point: ${clickPoint}")
        println("  Extra data: ${circle.extra}")
    }
) {
    Circle(
        center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        radius = 1000.0,
        clickable = true,
        extra = "Clickable circle"
    )
}
```

## Styling Options

### Stroke Styles

```kotlin
// Thin border
Circle(
    center = center,
    radiusMeters = 500.0,
    strokeColor = Color.Black,
    strokeWidth = 1.dp
)

// Thick border
Circle(
    center = center,
    radiusMeters = 500.0,
    strokeColor = Color.Black,
    strokeWidth = 5.dp
)

// No border
Circle(
    center = center,
    radiusMeters = 500.0,
    strokeColor = Color.Transparent,
    strokeWidth = 0.dp
)
```

### Fill Styles

```kotlin
// Solid fill
Circle(
    center = center,
    radiusMeters = 500.0,
    fillColor = Color.Red
)

// Semi-transparent fill
Circle(
    center = center,
    radiusMeters = 500.0,
    fillColor = Color.Red.copy(alpha = 0.5f)
)

// No fill
Circle(
    center = center,
    radiusMeters = 500.0,
    fillColor = Color.Transparent
)
```

## Circle Identification

### Using the ID Property

The `id` property provides a unique identifier for circles, enabling efficient tracking and management:

```kotlin
// Creating circles with unique IDs
val circles = listOf(
    Circle(
        center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        radiusMeters = 1000.0,
        strokeColor = Color.Red,
        fillColor = Color.Red.copy(alpha = 0.3f),
        id = "zone-a"
    ),
    Circle(
        center = GeoPointImpl.fromLatLong(37.7849, -122.4094),
        radiusMeters = 1500.0,
        strokeColor = Color.Blue,
        fillColor = Color.Blue.copy(alpha = 0.3f),
        id = "zone-b"
    )
)

// Using IDs for event handling
MapView(
    state = mapViewState,
    onCircleClick = { circleEvent ->
        when (circleEvent.state.id) {
            "zone-a" -> handleZoneA()
            "zone-b" -> handleZoneB()
            else -> handleUnknownZone()
        }
    }
) {
    circles.forEach { circle -> Circle(circle) }
}
```

### Benefits of Using IDs

- **Unique Identification**: Distinguish between circles even when they have similar properties
- **Event Handling**: Simplifies click event handling and area-specific logic
- **State Management**: Enables efficient updates and selection management
- **Performance**: Facilitates optimized rendering and updates

## Best Practices

1. **Use Appropriate Radius**: Consider the map zoom level when setting circle radius
2. **Provide Unique IDs**: Always set unique `id` values when working with multiple circles
3. **Color Contrast**: Ensure stroke and fill colors provide good visibility on the map
4. **Performance**: Avoid creating too many large circles as they may impact rendering performance
5. **Interactive Feedback**: Provide visual feedback when circles are clickable
6. **Consistent Styling**: Maintain consistent circle styling across your application
7. **Extra Data**: Use the `extra` parameter to store metadata for event handling
8. **Z-Index Management**: Consider drawing order when circles overlap
