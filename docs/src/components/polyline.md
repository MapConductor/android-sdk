# Polyline

Polylines are sequences of line segments that connect multiple geographic points. They are commonly used for routes, paths, boundaries, or any linear features on the map.

## Composable Functions

### Basic Polyline

```kotlin
@Composable
fun MapViewScope.Polyline(
    points: List<GeoPoint>,
    id: String? = null,
    strokeColor: Color = Color.Black,
    strokeWidth: Dp = 1.dp,
    geodesic: Boolean = false,
    extra: Serializable? = null
)
```

### Polyline with State

```kotlin
@Composable
fun MapViewScope.Polyline(state: PolylineState)
```

## Parameters

- **`points`**: List of geographic coordinates that define the line segments (`List<GeoPoint>`)
- **`id`**: Optional unique identifier for the polyline (`String?`)
- **`strokeColor`**: Color of the line (default: `Color.Black`)
- **`strokeWidth`**: Width of the line (default: `1.dp`)
- **`geodesic`**: Whether to draw geodesic lines (following Earth's curvature, default: `false`)
- **`extra`**: Additional data attached to the polyline (`Serializable?`)

## Usage Examples

### Basic Polyline

```kotlin
// Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(state = mapViewState) {
    val routePoints = listOf(
        GeoPointImpl.fromLatLong(37.7749, -122.4194), // San Francisco
        GeoPointImpl.fromLatLong(37.7849, -122.4094), // Point 2
        GeoPointImpl.fromLatLong(37.7949, -122.3994), // Point 3
        GeoPointImpl.fromLatLong(37.8049, -122.3894)  // Point 4
    )

    Polyline(
        points = routePoints,
        strokeColor = Color.Blue,
        strokeWidth = 3.dp
    )
}
```

### Interactive Polyline with Waypoint Markers

Based on the example app pattern:

```kotlin
@Composable
fun InteractivePolylineExample() {
    var waypoints by remember {
        mutableStateOf(
            listOf(
                GeoPointImpl.fromLatLong(37.7749, -122.4194),
                GeoPointImpl.fromLatLong(37.7849, -122.4094),
                GeoPointImpl.fromLatLong(37.7949, -122.3994)
            )
        )
    }

    val polylineState = PolylineState(
        points = waypoints,
        strokeColor = Color.Blue,
        strokeWidth = 4.dp,
        geodesic = true
    )

    val waypointMarkers = waypoints.mapIndexed { index, point ->
        MarkerState(
            position = point,
            icon = DefaultIcon(
                fillColor = Color.Blue,
                label = "${index + 1}"
            ),
            draggable = true,
            extra = "Waypoint $index"
        )
    }

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(
        state = mapViewState,
        onMarkerDrag = { markerState ->
            val markerIndex = markerState.extra.toString().substringAfter("Waypoint ").toIntOrNull()
            markerIndex?.let { index ->
                waypoints = waypoints.toMutableList().apply {
                    if (index < size) {
                        set(index, markerState.position)
                    }
                }
            }
        }
    ) {
        // Draw the polyline
        Polyline(polylineState)

        // Draw waypoint markers
        waypointMarkers.forEach { marker ->
            Marker(marker)
        }
    }
}
```

### Multiple Polylines with Different Styles

```kotlin
// Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(state = mapViewState) {
    // Route 1 - Highway (thick blue line)
    Polyline(
        points = listOf(
            GeoPointImpl.fromLatLong(37.7749, -122.4194),
            GeoPointImpl.fromLatLong(37.7849, -122.4094),
            GeoPointImpl.fromLatLong(37.7949, -122.3994)
        ),
        strokeColor = Color.Blue,
        strokeWidth = 6.dp,
        geodesic = true,
        extra = "Highway route"
    )

    // Route 2 - Walking path (thin green line)
    Polyline(
        points = listOf(
            GeoPointImpl.fromLatLong(37.7749, -122.4194),
            GeoPointImpl.fromLatLong(37.7799, -122.4144),
            GeoPointImpl.fromLatLong(37.7849, -122.4094)
        ),
        strokeColor = Color.Green,
        strokeWidth = 2.dp,
        geodesic = false,
        extra = "Walking path"
    )

    // Route 3 - Bike path (dashed-looking red line)
    Polyline(
        points = listOf(
            GeoPointImpl.fromLatLong(37.7749, -122.4194),
            GeoPointImpl.fromLatLong(37.7729, -122.4174),
            GeoPointImpl.fromLatLong(37.7709, -122.4154)
        ),
        strokeColor = Color.Red,
        strokeWidth = 3.dp,
        extra = "Bike path"
    )
}
```

### Dynamic Polyline Construction

```kotlin
@Composable
fun DynamicPolylineExample() {
    var points by remember { mutableStateOf<List<GeoPoint>>(emptyList()) }
    var isDrawing by remember { mutableStateOf(false) }

    Column {
        Row {
            Button(
                onClick = { isDrawing = !isDrawing }
            ) {
                Text(if (isDrawing) "Stop Drawing" else "Start Drawing")
            }

            Button(
                onClick = { points = emptyList() }
            ) {
                Text("Clear")
            }
        }

        // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(
            state = mapViewState,
            onMapClick = { geoPoint ->
                if (isDrawing) {
                    points = points + geoPoint
                }
            }
        ) {
            if (points.isNotEmpty()) {
                Polyline(
                    points = points,
                    strokeColor = Color.Red,
                    strokeWidth = 3.dp
                )

                // Add markers at each point
                points.forEachIndexed { index, point ->
                    Marker(
                        position = point,
                        icon = DefaultIcon(
                            fillColor = if (index == 0) Color.Green
                                      else if (index == points.size - 1) Color.Red
                                      else Color.Blue,
                            label = "${index + 1}",
                            scale = 0.8f
                        )
                    )
                }
            }
        }
    }
}
```

### Geodesic vs Standard Lines

```kotlin
// Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(state = mapViewState) {
    val longDistancePoints = listOf(
        GeoPointImpl.fromLatLong(37.7749, -122.4194), // San Francisco
        GeoPointImpl.fromLatLong(40.7128, -74.0060)   // New York
    )

    // Standard line (straight on map projection)
    Polyline(
        points = longDistancePoints,
        strokeColor = Color.Red,
        strokeWidth = 3.dp,
        geodesic = false,
        extra = "Straight line"
    )

    // Geodesic line (follows Earth's curvature)
    Polyline(
        points = longDistancePoints,
        strokeColor = Color.Blue,
        strokeWidth = 3.dp,
        geodesic = true,
        extra = "Geodesic line"
    )
}
```

### Animated Route Progress

```kotlin
@Composable
fun AnimatedRouteExample() {
    val fullRoute = remember {
        listOf(
            GeoPointImpl.fromLatLong(37.7749, -122.4194),
            GeoPointImpl.fromLatLong(37.7849, -122.4094),
            GeoPointImpl.fromLatLong(37.7949, -122.3994),
            GeoPointImpl.fromLatLong(37.8049, -122.3894)
        )
    }

    var progress by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(100)
            progress = (progress + 0.01f) % 1f
        }
    }

    val currentPointCount = (fullRoute.size * progress).toInt().coerceAtLeast(2)
    val visibleRoute = fullRoute.take(currentPointCount)

    Column {
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(state = mapViewState) {
            if (visibleRoute.size >= 2) {
                // Completed route (gray)
                Polyline(
                    points = visibleRoute,
                    strokeColor = Color.Blue,
                    strokeWidth = 4.dp
                )

                // Current position marker
                Marker(
                    position = visibleRoute.last(),
                    icon = DefaultIcon(
                        fillColor = Color.Red,
                        label = "🚗"
                    )
                )
            }

            // Full route preview (light gray)
            Polyline(
                points = fullRoute,
                strokeColor = Color.LightGray,
                strokeWidth = 2.dp
            )
        }
    }
}
```

## Event Handling

Polyline interactions are handled with your map provider component:

```kotlin
// Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(
    state = mapViewState,
    onPolylineClick = { polylineEvent ->
        val polyline = polylineEvent.state
        val clickPoint = polylineEvent.clicked

        println("Polyline clicked:")
        println("  Points count: ${polyline.points.size}")
        println("  Click location: ${clickPoint}")
        println("  Extra data: ${polyline.extra}")
    }
) {
    Polyline(
        points = routePoints,
        strokeColor = Color.Blue,
        strokeWidth = 4.dp,
        extra = "Interactive route"
    )
}
```

## Styling Options

### Line Width Variations

```kotlin
// Thin line
Polyline(
    points = points,
    strokeWidth = 1.dp
)

// Medium line
Polyline(
    points = points,
    strokeWidth = 3.dp
)

// Thick line
Polyline(
    points = points,
    strokeWidth = 8.dp
)
```

### Color Variations

```kotlin
// Solid colors
Polyline(points = points, strokeColor = Color.Red)
Polyline(points = points, strokeColor = Color.Blue)
Polyline(points = points, strokeColor = Color.Green)

// Semi-transparent
Polyline(points = points, strokeColor = Color.Red.copy(alpha = 0.7f))

// Custom colors
Polyline(
    points = points,
    strokeColor = Color(0xFF4CAF50) // Material Green
)
```

## Best Practices

1. **Point Density**: Balance between detail and performance - too many points can slow rendering
2. **Geodesic Lines**: Use geodesic lines for long-distance routes to show accurate paths
3. **Visual Hierarchy**: Use different colors and widths to distinguish between different types of routes
4. **Interactive Feedback**: Provide visual feedback when polylines are clickable
5. **Performance**: Consider using simplified geometries for complex polylines at certain zoom levels
6. **Color Contrast**: Ensure polyline colors stand out against the map background
7. **Route Direction**: Consider adding arrows or markers to indicate direction along the route
8. **State Management**: Store polyline data efficiently and update reactively when needed