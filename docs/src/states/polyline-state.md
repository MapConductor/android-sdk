# PolylineState

`PolylineState` manages the configuration and behavior of polylines (line segments) on the map. It provides reactive properties for points, styling, and geodesic options.

## Constructor

```kotlin
PolylineState(
    points: List<GeoPoint>,
    id: String? = null,
    strokeColor: Color = Color.Black,
    strokeWidth: Dp = 1.dp,
    geodesic: Boolean = false,
    extra: Serializable? = null
)
```

## Properties

### Core Properties
- **`id: String`**: Unique identifier (auto-generated if not provided)
- **`points: List<GeoPoint>`**: List of geographic coordinates defining the line
- **`extra: Serializable?`**: Additional data attached to the polyline

### Visual Properties
- **`strokeColor: Color`**: Line color
- **`strokeWidth: Dp`**: Line width
- **`geodesic: Boolean`**: Whether to draw geodesic lines (following Earth's curvature)

## Methods

```kotlin
fun copy(...): PolylineState // Create a modified copy
fun fingerPrint(): PolylineFingerPrint // Change detection
fun asFlow(): Flow<PolylineFingerPrint> // Reactive updates
```

## Usage Examples

### Basic Polyline

```kotlin
val polylineState = PolylineState(
    points = listOf(
        GeoPointImpl.fromLatLong(37.7749, -122.4194),
        GeoPointImpl.fromLatLong(37.7849, -122.4094),
        GeoPointImpl.fromLatLong(37.7949, -122.3994)
    ),
    strokeColor = Color.Blue,
    strokeWidth = 3.dp
)

// Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(state = mapViewState) {
    Polyline(polylineState)
}
```

### Dynamic Polyline

```kotlin
@Composable
fun DynamicPolylineExample() {
    var polylineState by remember {
        mutableStateOf(
            PolylineState(
                points = listOf(
                    GeoPointImpl.fromLatLong(37.7749, -122.4194),
                    GeoPointImpl.fromLatLong(37.7849, -122.4094)
                ),
                strokeColor = Color.Red,
                strokeWidth = 4.dp
            )
        )
    }

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(
        state = mapViewState,
        onMapClick = { geoPoint ->
            polylineState = polylineState.copy(
                points = polylineState.points + geoPoint
            )
        }
    ) {
        Polyline(polylineState)
    }
}
```

### Geodesic Polyline

```kotlin
val geodesicPolyline = PolylineState(
    points = listOf(
        GeoPointImpl.fromLatLong(37.7749, -122.4194), // San Francisco
        GeoPointImpl.fromLatLong(40.7128, -74.0060)   // New York
    ),
    strokeColor = Color.Green,
    strokeWidth = 3.dp,
    geodesic = true, // Follows Earth's curvature
    extra = "Cross-country route"
)
```

### Multi-segment Route

```kotlin
@Composable
fun RouteExample() {
    val routeSegments = remember {
        listOf(
            PolylineState(
                points = highway1Points,
                strokeColor = Color.Blue,
                strokeWidth = 6.dp,
                extra = "Highway segment"
            ),
            PolylineState(
                points = localRoadPoints,
                strokeColor = Color.Green,
                strokeWidth = 3.dp,
                extra = "Local road segment"
            ),
            PolylineState(
                points = walkingPathPoints,
                strokeColor = Color.Orange,
                strokeWidth = 2.dp,
                extra = "Walking path"
            )
        )
    }

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(state = mapViewState) {
        routeSegments.forEach { segment ->
            Polyline(segment)
        }
    }
}
```

## Event Handling

Polyline events provide both the state and click location:

```kotlin
data class PolylineEvent(
    val state: PolylineState,
    val clicked: GeoPoint
)

typealias OnPolylineEventHandler = (PolylineEvent) -> Unit
```

## Best Practices

1. **Point Density**: Balance detail with performance - avoid excessive points
2. **Geodesic Lines**: Use for long-distance routes to show accurate paths
3. **Color Coding**: Use different colors to distinguish route types
4. **Width Hierarchy**: Use stroke width to indicate importance
5. **Performance**: Consider simplification for complex polylines at different zoom levels