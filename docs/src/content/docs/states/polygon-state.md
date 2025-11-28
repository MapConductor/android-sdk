---
title: "PolygonState"
---

`PolygonState` manages the configuration and behavior of polygon (filled area) overlays on the map. It provides reactive properties for vertices, stroke, fill, and geodesic options.

## Constructor

```kotlin
PolygonState(
    points: List<GeoPoint>,
    id: String? = null,
    strokeColor: Color = Color.Black,
    strokeWidth: Dp = 2.dp,
    fillColor: Color = Color.Transparent,
    geodesic: Boolean = false,
    extra: Serializable? = null
)
```

## Properties

### Core Properties
- **`id: String`**: Unique identifier (auto-generated if not provided)
- **`points: List<GeoPoint>`**: List of vertices defining the polygon (should be closed)
- **`extra: Serializable?`**: Additional data attached to the polygon

### Visual Properties
- **`strokeColor: Color`**: Border color
- **`strokeWidth: Dp`**: Border width
- **`fillColor: Color`**: Interior fill color
- **`geodesic: Boolean`**: Whether to draw geodesic edges

## Methods

```kotlin
fun copy(...): PolygonState // Create a modified copy
fun fingerPrint(): PolygonFingerPrint // Change detection
fun asFlow(): Flow<PolygonFingerPrint> // Reactive updates
```

## Usage Examples

### Basic Polygon

```kotlin
val polygonState = PolygonState(
    points = listOf(
        GeoPointImpl.fromLatLong(37.7749, -122.4194),
        GeoPointImpl.fromLatLong(37.7849, -122.4094),
        GeoPointImpl.fromLatLong(37.7749, -122.3994),
        GeoPointImpl.fromLatLong(37.7749, -122.4194) // Close the polygon
    ),
    strokeColor = Color.Blue,
    strokeWidth = 2.dp,
    fillColor = Color.Blue.copy(alpha = 0.3f)
)

// Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(state = mapViewState) {
    Polygon(polygonState)
}
```

### Interactive Polygon

```kotlin
@Composable
fun EditablePolygonExample() {
    var polygonState by remember {
        mutableStateOf(
            PolygonState(
                points = listOf(
                    GeoPointImpl.fromLatLong(37.7749, -122.4194),
                    GeoPointImpl.fromLatLong(37.7849, -122.4094),
                    GeoPointImpl.fromLatLong(37.7799, -122.3994),
                    GeoPointImpl.fromLatLong(37.7749, -122.4194)
                ),
                strokeColor = Color.Red,
                fillColor = Color.Red.copy(alpha = 0.2f)
            )
        )
    }

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(
        state = mapViewState,
        onPolygonClick = { event ->
            println("Polygon clicked at: ${event.clicked}")
        }
    ) {
        Polygon(polygonState)

        // Vertex markers (excluding the closing point)
        polygonState.points.dropLast(1).forEachIndexed { index, point ->
            Marker(
                position = point,
                icon = DefaultIcon(
                    fillColor = Color.Red,
                    label = "${index + 1}",
                    scale = 0.8f
                ),
                draggable = true,
                extra = "vertex_$index"
            )
        }
    }
}
```

### Zone Polygons

```kotlin
val zonePolygons = listOf(
    PolygonState(
        points = restrictedZonePoints,
        strokeColor = Color.Red,
        strokeWidth = 3.dp,
        fillColor = Color.Red.copy(alpha = 0.2f),
        extra = "Restricted Zone"
    ),
    PolygonState(
        points = parkingZonePoints,
        strokeColor = Color.Blue,
        strokeWidth = 2.dp,
        fillColor = Color.Blue.copy(alpha = 0.2f),
        extra = "Parking Zone"
    ),
    PolygonState(
        points = safeZonePoints,
        strokeColor = Color.Green,
        strokeWidth = 2.dp,
        fillColor = Color.Green.copy(alpha = 0.2f),
        extra = "Safe Zone"
    )
)

// Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(state = mapViewState) {
    zonePolygons.forEach { zone ->
        Polygon(zone)
    }
}
```

### Complex Shapes

```kotlin
@Composable
fun ComplexPolygonExample() {
    // Star-shaped polygon
    val starPolygon = remember {
        val center = GeoPointImpl.fromLatLong(37.7749, -122.4194)
        val points = mutableListOf<GeoPoint>()

        for (i in 0 until 10) {
            val angle = (i * 36.0) * Math.PI / 180.0
            val radiusMeters = if (i % 2 == 0) 0.01 else 0.005
            val lat = center.latitude + radiusMeters * cos(angle)
            val lng = center.longitude + radiusMeters * sin(angle)
            points.add(GeoPointImpl.fromLatLong(lat, lng))
        }
        points.add(points.first()) // Close the polygon

        PolygonState(
            points = points,
            strokeColor = Color.Magenta,
            fillColor = Color.Magenta.copy(alpha = 0.4f),
            extra = "Star shape"
        )
    }

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(state = mapViewState) {
        Polygon(starPolygon)
    }
}
```

## Event Handling

Polygon events provide both the state and click location:

```kotlin
data class PolygonEvent(
    val state: PolygonState,
    val clicked: GeoPoint?
)

typealias OnPolygonEventHandler = (PolygonEvent) -> Unit
```

## Best Practices

1. **Close Polygons**: Ensure first and last points are the same
2. **Vertex Order**: Use consistent ordering (clockwise/counter-clockwise)
3. **Performance**: Avoid overly complex polygons with many vertices
4. **Visual Design**: Use appropriate fill transparency for map visibility
5. **Validation**: Ensure polygons form valid geometric shapes