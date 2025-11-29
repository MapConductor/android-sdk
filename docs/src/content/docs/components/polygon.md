---
title: "Polygon"
---

Polygons are closed shapes that define areas on the map with customizable stroke and fill properties. They are useful for representing zones, regions, boundaries, or any area-based features.

## Composable Functions

### Basic Polygon

```kotlin
@Composable
fun MapViewScope.Polygon(
    points: List<GeoPoint>,
    id: String? = null,
    strokeColor: Color = Color.Black,
    strokeWidth: Dp = 1.dp,
    fillColor: Color = Color.Transparent,
    geodesic: Boolean = false,
    extra: Serializable? = null
)
```

### Polygon with State

```kotlin
@Composable
fun MapViewScope.Polygon(state: PolygonState)
```

## Parameters

- **`points`**: List of geographic coordinates that define the polygon vertices (`List<GeoPoint>`)
- **`id`**: Optional unique identifier for the polygon (`String?`)
- **`strokeColor`**: Color of the polygon border (default: `Color.Black`)
- **`strokeWidth`**: Width of the border line (default: `1.dp`)
- **`fillColor`**: Fill color of the polygon interior (default: `Color.Transparent`)
- **`geodesic`**: Whether to draw geodesic edges (default: `false`)
- **`extra`**: Additional data attached to the polygon (`Serializable?`)

## Usage Examples

### Basic Polygon

```kotlin
// Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(state = mapViewState) {
    val trianglePoints = listOf(
        GeoPointImpl.fromLatLong(37.7749, -122.4194), // Point 1
        GeoPointImpl.fromLatLong(37.7849, -122.4094), // Point 2
        GeoPointImpl.fromLatLong(37.7749, -122.3994), // Point 3
        GeoPointImpl.fromLatLong(37.7749, -122.4194)  // Close the polygon
    )

    Polygon(
        points = trianglePoints,
        strokeColor = Color.Blue,
        strokeWidth = 2.dp,
        fillColor = Color.Blue.copy(alpha = 0.3f)
    )
}
```

### Interactive Polygon with Vertex Markers

Based on the example app pattern:

```kotlin
@Composable
fun InteractivePolygonExample() {
    var vertices by remember {
        mutableStateOf(
            listOf(
                GeoPointImpl.fromLatLong(37.7749, -122.4194),
                GeoPointImpl.fromLatLong(37.7849, -122.4094),
                GeoPointImpl.fromLatLong(37.7799, -122.3994),
                GeoPointImpl.fromLatLong(37.7649, -122.4094)
            )
        )
    }

    // Close the polygon by adding the first point at the end
    val closedVertices = vertices + vertices.first()

    val polygonState = PolygonState(
        points = closedVertices,
        strokeColor = Color.Red,
        strokeWidth = 3.dp,
        fillColor = Color.Red.copy(alpha = 0.2f),
        geodesic = false
    )

    val vertexMarkers = vertices.mapIndexed { index, point ->
        MarkerState(
            position = point,
            icon = DefaultIcon(
                fillColor = Color.Red,
                strokeColor = Color.White,
                label = "${index + 1}",
                scale = 0.8f
            ),
            draggable = true,
            extra = "Vertex $index"
        )
    }

    // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(
        state = mapViewState,
        onMarkerDrag = { markerState ->
            val vertexIndex = markerState.extra.toString().substringAfter("Vertex ").toIntOrNull()
            vertexIndex?.let { index ->
                vertices = vertices.toMutableList().apply {
                    if (index < size) {
                        set(index, markerState.position)
                    }
                }
            }
        },
        onPolygonClick = { polygonEvent ->
            println("Polygon clicked at: ${polygonEvent.clicked}")
        }
    ) {
        // Draw the polygon
        Polygon(polygonState)

        // Draw vertex markers
        vertexMarkers.forEach { marker ->
            Marker(marker)
        }
    }
}
```

### Multiple Polygons with Different Styles

```kotlin
// Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(state = mapViewState) {
    // Solid filled polygon
    Polygon(
        points = listOf(
            GeoPointImpl.fromLatLong(37.7749, -122.4194),
            GeoPointImpl.fromLatLong(37.7849, -122.4194),
            GeoPointImpl.fromLatLong(37.7849, -122.4094),
            GeoPointImpl.fromLatLong(37.7749, -122.4094),
            GeoPointImpl.fromLatLong(37.7749, -122.4194)
        ),
        strokeColor = Color.Blue,
        strokeWidth = 2.dp,
        fillColor = Color.Blue.copy(alpha = 0.4f),
        extra = "Blue zone"
    )

    // Border-only polygon
    Polygon(
        points = listOf(
            GeoPointImpl.fromLatLong(37.7649, -122.4194),
            GeoPointImpl.fromLatLong(37.7749, -122.4194),
            GeoPointImpl.fromLatLong(37.7749, -122.4094),
            GeoPointImpl.fromLatLong(37.7649, -122.4094),
            GeoPointImpl.fromLatLong(37.7649, -122.4194)
        ),
        strokeColor = Color.Red,
        strokeWidth = 3.dp,
        fillColor = Color.Transparent,
        extra = "Red boundary"
    )

    // Semi-transparent polygon
    Polygon(
        points = listOf(
            GeoPointImpl.fromLatLong(37.7549, -122.4194),
            GeoPointImpl.fromLatLong(37.7649, -122.4194),
            GeoPointImpl.fromLatLong(37.7649, -122.4094),
            GeoPointImpl.fromLatLong(37.7549, -122.4094),
            GeoPointImpl.fromLatLong(37.7549, -122.4194)
        ),
        strokeColor = Color.Green,
        strokeWidth = 1.dp,
        fillColor = Color.Green.copy(alpha = 0.2f),
        extra = "Green area"
    )
}
```

### Complex Polygon Shapes

```kotlin
@Composable
fun ComplexPolygonExample() {
    // Star-shaped polygon
    val starPoints = remember {
        val centerLat = 37.7749
        val centerLng = -122.4194
        val outerRadius = 0.01
        val innerRadius = 0.005

        buildList {
            for (i in 0 until 10) {
                val angle = (i * 36.0) * Math.PI / 180.0
                val radiusMeters = if (i % 2 == 0) outerRadius else innerRadius
                val lat = centerLat + radiusMeters * cos(angle)
                val lng = centerLng + radiusMeters * sin(angle)
                add(GeoPointImpl.fromLatLong(lat, lng))
            }
            // Close the polygon
            add(first())
        }
    }

    // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(state = mapViewState) {
        Polygon(
            points = starPoints,
            strokeColor = Color.Magenta,
            strokeWidth = 2.dp,
            fillColor = Color.Magenta.copy(alpha = 0.3f),
            extra = "Star polygon"
        )

        // Center marker
        Marker(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = DefaultIcon(
                fillColor = Color.Magenta,
                label = "⭐"
            )
        )
    }
}
```

### Dynamic Polygon Creation

```kotlin
@Composable
fun DynamicPolygonExample() {
    var polygonPoints by remember { mutableStateOf<List<GeoPoint>>(emptyList()) }
    var isCreating by remember { mutableStateOf(false) }

    Column {
        Row {
            Button(
                onClick = {
                    isCreating = !isCreating
                    if (!isCreating && polygonPoints.isNotEmpty()) {
                        // Close the polygon
                        polygonPoints = polygonPoints + polygonPoints.first()
                    }
                }
            ) {
                Text(if (isCreating) "Finish Polygon" else "Create Polygon")
            }

            Button(
                onClick = { polygonPoints = emptyList() }
            ) {
                Text("Clear")
            }
        }

        Text("Points: ${polygonPoints.size}")

        // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(
            state = mapViewState,
            onMapClick = { geoPoint ->
                if (isCreating) {
                    polygonPoints = polygonPoints + geoPoint
                }
            }
        ) {
            if (polygonPoints.size >= 3) {
                val displayPoints = if (isCreating) polygonPoints
                                  else polygonPoints // Already closed

                Polygon(
                    points = displayPoints,
                    strokeColor = Color.Purple,
                    strokeWidth = 2.dp,
                    fillColor = if (isCreating)
                        Color.Purple.copy(alpha = 0.1f)
                    else
                        Color.Purple.copy(alpha = 0.3f)
                )
            }

            // Show vertex markers
            polygonPoints.forEachIndexed { index, point ->
                Marker(
                    position = point,
                    icon = DefaultIcon(
                        fillColor = if (index == 0) Color.Green
                                  else if (index == polygonPoints.size - 1 && !isCreating) Color.Red
                                  else Color.Blue,
                        label = "${index + 1}",
                        scale = 0.7f
                    )
                )
            }
        }
    }
}
```

## Event Handling

Polygon interactions are handled with your map provider component:

```kotlin
// Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(
    state = mapViewState,
    onPolygonClick = { polygonEvent ->
        val polygon = polygonEvent.state
        val clickPoint = polygonEvent.clicked

        println("Polygon clicked:")
        println("  Vertices: ${polygon.points.size}")
        println("  Click location: ${clickPoint}")
        println("  Extra data: ${polygon.extra}")

        // Calculate area (simplified)
        val area = calculatePolygonArea(polygon.points)
        println("  Approximate area: $area sq meters")
    }
) {
    Polygon(
        points = polygonPoints,
        strokeColor = Color.Green,
        fillColor = Color.Green.copy(alpha = 0.3f),
        extra = "Interactive polygon"
    )
}
```

## Styling Options

### Fill Variations

```kotlin
// Solid fill
Polygon(
    points = points,
    fillColor = Color.Red
)

// Semi-transparent fill
Polygon(
    points = points,
    fillColor = Color.Red.copy(alpha = 0.5f)
)

// No fill (border only)
Polygon(
    points = points,
    fillColor = Color.Transparent
)

// Gradient-like effect with multiple polygons
Polygon(points = points, fillColor = Color.Blue.copy(alpha = 0.1f))
Polygon(points = smallerPoints, fillColor = Color.Blue.copy(alpha = 0.2f))
```

### Stroke Variations

```kotlin
// Thin border
Polygon(
    points = points,
    strokeColor = Color.Black,
    strokeWidth = 1.dp
)

// Thick border
Polygon(
    points = points,
    strokeColor = Color.Black,
    strokeWidth = 5.dp
)

// No border
Polygon(
    points = points,
    strokeColor = Color.Transparent,
    strokeWidth = 0.dp
)
```

## Best Practices

1. **Close Polygons**: Always ensure the last point equals the first point to properly close the polygon
2. **Vertex Order**: Use consistent vertex ordering (clockwise or counter-clockwise) for predictable results
3. **Performance**: Avoid overly complex polygons with hundreds of vertices
4. **Visual Clarity**: Use appropriate colors and transparency for good visibility
5. **Interactive Feedback**: Provide visual feedback when polygons are clickable
6. **Hole Handling**: Use overlapping polygons with different colors to simulate holes
7. **Geodesic Edges**: Use geodesic edges for large polygons to account for Earth's curvature
8. **State Management**: Efficiently manage polygon vertex data and handle updates reactively
9. **Validation**: Validate polygon geometry to ensure it forms a valid shape
10. **Error Handling**: Handle edge cases like polygons with fewer than 3 vertices
