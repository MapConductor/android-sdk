---
title: "GeoRectBounds"
---

`GeoRectBounds` represents a rectangular geographic area defined by southwest and northeast corner points. It is used for defining map regions, ground image bounds, and viewport calculations.

## Constructor

```kotlin
class GeoRectBounds(
    southWest: GeoPointImpl? = null,
    northEast: GeoPointImpl? = null
)
```

## Properties

### Corner Points

- **`southWest: GeoPointImpl?`**: Southwest corner (bottom-left) of the rectangle
- **`northEast: GeoPointImpl?`**: Northeast corner (top-right) of the rectangle
- **`isEmpty: Boolean`**: Returns `true` if the bounds are not defined

### Computed Properties

- **`center: GeoPointImpl?`**: Center point of the bounds
- **`toSpan(): GeoPointImpl?`**: Returns the span (width/height) as a GeoPoint

## Basic Usage

### Creating Bounds

```kotlin
// Define a rectangular area
val bounds = GeoRectBounds(
    southWest = GeoPointImpl.fromLatLong(37.7649, -122.4294),
    northeast = GeoPointImpl.fromLatLong(37.7849, -122.4094)
)

// Empty bounds (to be extended later)
val bounds = GeoRectBounds()
```

### Using with GroundImage

```kotlin
@Composable
fun GroundImageExample() {
    val imageBounds = GeoRectBounds(
        southWest = GeoPointImpl.fromLatLong(37.7649, -122.4294),
        northEast = GeoPointImpl.fromLatLong(37.7849, -122.4094)
    )

    // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
    MapView(state = mapViewState) {
        val context = LocalContext.current
        AppCompatResources.getDrawable(context, R.drawable.overlay_image)?.let { drawable ->
            GroundImage(
                bounds = imageBounds,
                image = drawable,
                opacity = 0.7f
            )
        }
    }
}
```

## Dynamic Bounds Construction

### Extending Bounds

The bounds can be dynamically extended to include new points:

```kotlin
fun extend(point: GeoPoint)

// Usage
val bounds = GeoRectBounds() // Start empty

val points = listOf(
    GeoPointImpl.fromLatLong(37.7749, -122.4194),
    GeoPointImpl.fromLatLong(37.7849, -122.4094),
    GeoPointImpl.fromLatLong(37.7649, -122.4294)
)

points.forEach { point ->
    bounds.extend(point) // Bounds grow to include each point
}

println("Final bounds: $bounds")
```

### Union of Bounds

```kotlin
fun union(other: GeoRectBounds): GeoRectBounds

// Usage
val bounds1 = GeoRectBounds(
    southWest = GeoPointImpl.fromLatLong(37.7649, -122.4294),
    northEast = GeoPointImpl.fromLatLong(37.7749, -122.4194)
)

val bounds2 = GeoRectBounds(
    southWest = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    northEast = GeoPointImpl.fromLatLong(37.7849, -122.4094)
)

val combinedBounds = bounds1.union(bounds2) // Contains both areas
```

## Point and Bounds Testing

### Contains Point

```kotlin
fun contains(point: GeoPoint): Boolean

// Usage
val bounds = GeoRectBounds(
    southWest = GeoPointImpl.fromLatLong(37.7649, -122.4294),
    northEast = GeoPointImpl.fromLatLong(37.7849, -122.4094)
)

val testPoint = GeoPointImpl.fromLatLong(37.7749, -122.4194)
val isInside = bounds.contains(testPoint)

println("Point is inside bounds: $isInside")
```

### Bounds Intersection

```kotlin
fun intersects(other: GeoRectBounds): Boolean

// Usage
val bounds1 = GeoRectBounds(/* ... */)
val bounds2 = GeoRectBounds(/* ... */)

val doIntersect = bounds1.intersects(bounds2)
println("Bounds intersect: $doIntersect")
```

## Practical Examples

### Viewport Bounds Calculation

```kotlin
@Composable
fun ViewportBoundsExample() {
    var viewportBounds by remember { mutableStateOf<GeoRectBounds?>(null) }

    // Simulate calculating viewport from visible markers
    val markers = remember {
        listOf(
            GeoPointImpl.fromLatLong(37.7749, -122.4194),
            GeoPointImpl.fromLatLong(37.7849, -122.4094),
            GeoPointImpl.fromLatLong(37.7649, -122.4294),
            GeoPointImpl.fromLatLong(37.7949, -122.3994)
        )
    }

    LaunchedEffect(markers) {
        val bounds = GeoRectBounds()
        markers.forEach { marker ->
            bounds.extend(marker)
        }
        viewportBounds = bounds
    }

    // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
    MapView(state = mapViewState) {
        // Show all markers
        markers.forEach { position ->
            Marker(
                position = position,
                icon = DefaultIcon(fillColor = Color.Blue)
            )
        }

        // Show viewport bounds as a polygon
        viewportBounds?.let { bounds ->
            if (!bounds.isEmpty) {
                val sw = bounds.southWest!!
                val ne = bounds.northEast!!

                val boundsPolygon = listOf(
                    sw,
                    GeoPointImpl.fromLatLong(sw.latitude, ne.longitude),
                    ne,
                    GeoPointImpl.fromLatLong(ne.latitude, sw.longitude),
                    sw // Close the polygon
                )

                Polygon(
                    points = boundsPolygon,
                    strokeColor = Color.Red,
                    strokeWidth = 2.dp,
                    fillColor = Color.Red.copy(alpha = 0.1f)
                )
            }
        }
    }
}
```

### Bounds-based Loading

```kotlin
@Composable
fun BoundsBasedLoadingExample() {
    var currentBounds by remember { mutableStateOf<GeoRectBounds?>(null) }
    var markersInBounds by remember { mutableStateOf<List<GeoPointImpl>>(emptyList()) }

    // Simulate all available markers
    val allMarkers = remember {
        List(100) { i ->
            GeoPointImpl.fromLatLong(
                37.7 + (i % 10) * 0.01,
                -122.5 + (i / 10) * 0.01
            )
        }
    }

    // Filter markers based on current bounds
    LaunchedEffect(currentBounds) {
        markersInBounds = currentBounds?.let { bounds ->
            if (bounds.isEmpty) {
                emptyList()
            } else {
                allMarkers.filter { marker ->
                    bounds.contains(marker)
                }
            }
        } ?: emptyList()
    }

    Column {
        Text("Markers in bounds: ${markersInBounds.size}/${allMarkers.size}")

        Button(
            onClick = {
                // Simulate setting viewport bounds
                currentBounds = GeoRectBounds(
                    southWest = GeoPointImpl.fromLatLong(37.70, -122.50),
                    northEast = GeoPointImpl.fromLatLong(37.75, -122.45)
                )
            }
        ) {
            Text("Set Viewport")
        }

        // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
        MapView(state = mapViewState) {
            // Show only markers within bounds
            markersInBounds.forEach { position ->
                Marker(
                    position = position,
                    icon = DefaultIcon(
                        fillColor = Color.Green,
                        scale = 0.8f
                    )
                )
            }

            // Show current bounds
            currentBounds?.let { bounds ->
                if (!bounds.isEmpty) {
                    val sw = bounds.southWest!!
                    val ne = bounds.northEast!!

                    Polygon(
                        points = listOf(
                            sw,
                            GeoPointImpl.fromLatLong(sw.latitude, ne.longitude),
                            ne,
                            GeoPointImpl.fromLatLong(ne.latitude, sw.longitude),
                            sw
                        ),
                        strokeColor = Color.Blue,
                        strokeWidth = 3.dp,
                        fillColor = Color.Blue.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }
}
```

### Interactive Bounds Editor

```kotlin
@Composable
fun BoundsEditorExample() {
    var bounds by remember {
        mutableStateOf(
            GeoRectBounds(
                southWest = GeoPointImpl.fromLatLong(37.7649, -122.4294),
                northEast = GeoPointImpl.fromLatLong(37.7849, -122.4094)
            )
        )
    }

    // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
    MapView(
        state = mapViewState,
        onMarkerDrag = { markerState ->
            val newPosition = markerState.position
            when (markerState.extra) {
                "SW" -> {
                    bounds = GeoRectBounds(
                        southWest = newPosition as GeoPointImpl,
                        northEast = bounds.northEast
                    )
                }
                "NE" -> {
                    bounds = GeoRectBounds(
                        southWest = bounds.southWest,
                        northEast = newPosition as GeoPointImpl
                    )
                }
            }
        }
    ) {
        // Bounds visualization
        if (!bounds.isEmpty) {
            val sw = bounds.southWest!!
            val ne = bounds.northEast!!

            // Bounds rectangle
            Polygon(
                points = listOf(
                    sw,
                    GeoPointImpl.fromLatLong(sw.latitude, ne.longitude),
                    ne,
                    GeoPointImpl.fromLatLong(ne.latitude, sw.longitude),
                    sw
                ),
                strokeColor = Color.Red,
                fillColor = Color.Red.copy(alpha = 0.2f)
            )

            // Corner markers
            Marker(
                position = sw,
                icon = DefaultIcon(
                    fillColor = Color.Green,
                    label = "SW"
                ),
                draggable = true,
                extra = "SW"
            )

            Marker(
                position = ne,
                icon = DefaultIcon(
                    fillColor = Color.Red,
                    label = "NE"
                ),
                draggable = true,
                extra = "NE"
            )
        }
    }
}
```

## Special Handling

### International Date Line

`GeoRectBounds` handles rectangles that cross the International Date Line (longitude ±180°):

```kotlin
// Bounds crossing the date line
val pacificBounds = GeoRectBounds(
    southWest = GeoPointImpl.fromLatLong(20.0, 170.0),  // West of date line
    northEast = GeoPointImpl.fromLatLong(40.0, -170.0)  // East of date line
)

val pointInPacific = GeoPointImpl.fromLatLong(30.0, 175.0)
val containsPoint = pacificBounds.contains(pointInPacific) // true
```

### Empty Bounds Handling

```kotlin
val emptyBounds = GeoRectBounds()

println(emptyBounds.isEmpty) // true
println(emptyBounds.center) // null
println(emptyBounds.toSpan()) // null

// Extending empty bounds
emptyBounds.extend(GeoPointImpl.fromLatLong(37.7749, -122.4194))
println(emptyBounds.isEmpty) // false
```

## Utility Methods

### String Representation

```kotlin
// For debugging
val bounds = GeoRectBounds(
    southWest = GeoPointImpl.fromLatLong(37.7649, -122.4294),
    northEast = GeoPointImpl.fromLatLong(37.7849, -122.4094)
)

println(bounds.toString())
// Output: ((37.7649, -122.4294), (37.7849, -122.4094))
```

### URL Value

```kotlin
fun toUrlValue(precision: Int = 6): String

// Usage for API calls
val urlString = bounds.toUrlValue() // "37.764900,-122.429400,37.784900,-122.409400"
val preciseString = bounds.toUrlValue(precision = 8)
```

### Equality Comparison

```kotlin
fun equalsTo(other: GeoRectBounds): Boolean

// Usage
val bounds1 = GeoRectBounds(/* ... */)
val bounds2 = GeoRectBounds(/* ... */)

val areEqual = bounds1.equalsTo(bounds2)
```

## Best Practices

1. **Coordinate Order**: Always use southwest (bottom-left) and northeast (top-right) for consistency
2. **Empty Bounds**: Check `isEmpty` before using bounds in calculations
3. **Date Line Crossing**: The class handles international date line crossing automatically
4. **Dynamic Construction**: Use `extend()` to build bounds from a collection of points
5. **Performance**: Cache bounds calculations for frequently accessed regions
6. **Validation**: Ensure southwest is actually southwest of northeast
7. **Precision**: Use appropriate precision for URL values based on your use case