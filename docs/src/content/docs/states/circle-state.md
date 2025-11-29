---
title: "CircleState"
---

`CircleState` manages the configuration and behavior of circular overlays on the map. It provides reactive properties for center position, radiusMeters, styling, and interaction settings.

## Constructor

```kotlin
CircleState(
    center: GeoPoint,
    radiusMeters: Double,
    clickable: Boolean = true,
    strokeColor: Color = Color.Red,
    strokeWidth: Dp = 1.dp,
    fillColor: Color = Color(red = 255, green = 255, blue = 255, alpha = 127),
    id: String? = null,
    zIndex: Int? = null,
    extra: Serializable? = null
)
```

## Properties

### Core Properties
- **`id: String`**: Unique identifier (auto-generated if not provided)
- **`center: GeoPoint`**: Geographic center of the circle
- **`radiusMeters: Double`**: radiusMeters in meters
- **`clickable: Boolean`**: Whether the circle responds to click events
- **`extra: Serializable?`**: Additional data attached to the circle

### Visual Properties
- **`strokeColor: Color`**: Border color
- **`strokeWidth: Dp`**: Border width
- **`fillColor: Color`**: Interior fill color
- **`zIndex: Int?`**: Drawing order (higher values drawn on top)

## Methods

```kotlin
fun copy(...): CircleState // Create a modified copy
fun fingerPrint(): CircleFingerPrint // Change detection
fun asFlow(): Flow<CircleFingerPrint> // Reactive updates
```

## Usage Examples

### Basic Circle

```kotlin
val circleState = CircleState(
    center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    radiusMeters = 1000.0,
    strokeColor = Color.Blue,
    fillColor = Color.Blue.copy(alpha = 0.3f)
)

// Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(state = mapViewState) {
    Circle(circleState)
}
```

### Interactive Circle

```kotlin
@Composable
fun InteractiveCircleExample() {
    var circleState by remember {
        mutableStateOf(
            CircleState(
                center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                radiusMeters = 500.0,
                strokeColor = Color.Red,
                fillColor = Color.Red.copy(alpha = 0.2f),
                clickable = true
            )
        )
    }

    Column {
        Slider(
            value = circleState.radiusMeters.toFloat(),
            onValueChange = {
                circleState = circleState.copy(radiusMeters = it.toDouble())
            },
            valueRange = 100f..2000f
        )

        // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(
            state = mapViewState,
            onCircleClick = { event ->
                println("Circle clicked at: ${event.clicked}")
            }
        ) {
            Circle(circleState)
        }
    }
}
```

### Multiple Circles with Z-Index

```kotlin
val circles = listOf(
    CircleState(
        center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        radiusMeters = 1000.0,
        fillColor = Color.Red.copy(alpha = 0.3f),
        zIndex = 1
    ),
    CircleState(
        center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        radiusMeters = 500.0,
        fillColor = Color.Blue.copy(alpha = 0.5f),
        zIndex = 2
    )
)

// Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(state = mapViewState) {
    circles.forEach { circleState ->
        Circle(circleState)
    }
}
```

## Event Handling

Circle events provide both the state and click location:

```kotlin
data class CircleEvent(
    val state: CircleState,
    val clicked: GeoPoint
)

typealias OnCircleEventHandler = (CircleEvent) -> Unit
```

## Best Practices

1. **Radius Units**: Always specify radius in meters for consistency
2. **Color Alpha**: Use alpha transparency for better map visibility
3. **Z-Index**: Use z-index to control overlapping circles
4. **Performance**: Avoid creating too many large circles
5. **Reactivity**: Update properties directly for efficient re-rendering
