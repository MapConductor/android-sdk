# Marker Animation

MarkerAnimation provides smooth transitions and visual effects for markers on the map. Animations can be applied to marker appearance, position changes, and lifecycle events.

## MarkerAnimation Interface

```kotlin
interface MarkerAnimation {
    val duration: Long
    val interpolator: TimeInterpolator?

    // Animation lifecycle methods
    fun onStart()
    fun onUpdate(progress: Float)
    fun onEnd()
}
```

## Animation Types

### Position Animations

Smoothly animate marker position changes when updating coordinates.

```kotlin
class PositionAnimation(
    val fromPosition: GeoPoint,
    val toPosition: GeoPoint,
    override val duration: Long = 1000,
    override val interpolator: TimeInterpolator? = AccelerateDecelerateInterpolator()
) : MarkerAnimation
```

#### Usage Example

```kotlin
@Composable
fun AnimatedMarkerExample() {
    var markerPosition by remember {
        mutableStateOf(GeoPointImpl.fromLatLong(37.7749, -122.4194))
    }
    val markerState = remember { MarkerState(position = markerPosition) }

    // Animate marker to new position
    LaunchedEffect(markerPosition) {
        val animation = PositionAnimation(
            fromPosition = markerState.position,
            toPosition = markerPosition,
            duration = 1500
        )
        markerState.setAnimation(animation)
    }

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
    MapView(
        state = mapViewState,
        onMapClick = { clickedPosition ->
            markerPosition = clickedPosition // Triggers animation
        },
        onMarkerAnimateStart = { markerState ->
            println("Animation started for marker: ${markerState.id}")
        },
        onMarkerAnimateEnd = { markerState ->
            println("Animation ended for marker: ${markerState.id}")
        }
    ) {
        Marker(markerState)
    }
}
```

### Scale Animations

Animate marker size changes for emphasis or state transitions.

```kotlin
class ScaleAnimation(
    val fromScale: Float,
    val toScale: Float,
    override val duration: Long = 500,
    override val interpolator: TimeInterpolator? = OvershootInterpolator()
) : MarkerAnimation
```

#### Usage Example

```kotlin
@Composable
fun ScaleAnimationExample() {
    var isExpanded by remember { mutableStateOf(false) }
    val markerState = remember {
        MarkerState(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = DefaultIcon(fillColor = Color.Blue, scale = 1.0f)
        )
    }

    // Animate scale changes
    LaunchedEffect(isExpanded) {
        val animation = ScaleAnimation(
            fromScale = if (isExpanded) 1.0f else 1.5f,
            toScale = if (isExpanded) 1.5f else 1.0f,
            duration = 300,
            interpolator = BounceInterpolator()
        )
        markerState.setAnimation(animation)
    }

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
    MapView(
        state = mapViewState,
        onMarkerClick = {
            isExpanded = !isExpanded
        }
    ) {
        Marker(markerState)
    }
}
```

### Fade Animations

Control marker opacity for appearance and disappearance effects.

```kotlin
class FadeAnimation(
    val fromAlpha: Float,
    val toAlpha: Float,
    override val duration: Long = 800,
    override val interpolator: TimeInterpolator? = AccelerateDecelerateInterpolator()
) : MarkerAnimation
```

#### Usage Example

```kotlin
@Composable
fun FadeAnimationExample() {
    var markersVisible by remember { mutableStateOf(true) }
    val markerStates = remember {
        listOf(
            MarkerState(
                position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                icon = DefaultIcon(fillColor = Color.Red)
            ),
            MarkerState(
                position = GeoPointImpl.fromLatLong(37.7849, -122.4094),
                icon = DefaultIcon(fillColor = Color.Blue)
            )
        )
    }

    // Animate opacity changes
    LaunchedEffect(markersVisible) {
        markerStates.forEach { markerState ->
            val animation = FadeAnimation(
                fromAlpha = if (markersVisible) 0.0f else 1.0f,
                toAlpha = if (markersVisible) 1.0f else 0.0f,
                duration = 600
            )
            markerState.setAnimation(animation)
        }
    }

    Column {
        Button(onClick = { markersVisible = !markersVisible }) {
            Text(if (markersVisible) "Hide Markers" else "Show Markers")
        }

        // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
        MapView(state = mapViewState) {
            markerStates.forEach { markerState ->
                Marker(markerState)
            }
        }
    }
}
```

### Bounce Animation

Creates a bouncing effect when markers appear or are interacted with.

```kotlin
class BounceAnimation(
    val bounceHeight: Float = 50f,
    override val duration: Long = 1000,
    override val interpolator: TimeInterpolator? = BounceInterpolator()
) : MarkerAnimation
```

#### Usage Example

```kotlin
@Composable
fun BounceAnimationExample() {
    var triggerBounce by remember { mutableStateOf(false) }
    val markerState = remember {
        MarkerState(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = DefaultIcon(fillColor = Color.Green, label = "Bounce!")
        )
    }

    // Trigger bounce animation
    LaunchedEffect(triggerBounce) {
        if (triggerBounce) {
            val animation = BounceAnimation(
                bounceHeight = 30f,
                duration = 800
            )
            markerState.setAnimation(animation)
            triggerBounce = false
        }
    }

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
    MapView(
        state = mapViewState,
        onMarkerClick = {
            triggerBounce = true
        }
    ) {
        Marker(markerState)
    }
}
```

## Complex Animations

### Sequential Animations

Chain multiple animations together for complex effects.

```kotlin
class SequentialAnimation(
    private val animations: List<MarkerAnimation>
) : MarkerAnimation {
    private var currentIndex = 0
    private var currentAnimation: MarkerAnimation? = null

    override val duration: Long = animations.sumOf { it.duration }
    override val interpolator: TimeInterpolator? = null

    fun playNext() {
        if (currentIndex < animations.size) {
            currentAnimation = animations[currentIndex++]
            currentAnimation?.onStart()
        }
    }
}
```

#### Usage Example

```kotlin
@Composable
fun SequentialAnimationExample() {
    val markerState = remember {
        MarkerState(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = DefaultIcon(fillColor = Color.Purple)
        )
    }

    LaunchedEffect(Unit) {
        val sequentialAnimation = SequentialAnimation(
            animations = listOf(
                ScaleAnimation(fromScale = 0.5f, toScale = 1.5f, duration = 500),
                FadeAnimation(fromAlpha = 1.0f, toAlpha = 0.3f, duration = 300),
                FadeAnimation(fromAlpha = 0.3f, toAlpha = 1.0f, duration = 300),
                ScaleAnimation(fromScale = 1.5f, toScale = 1.0f, duration = 400)
            )
        )
        markerState.setAnimation(sequentialAnimation)
    }

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
    MapView(state = mapViewState) {
        Marker(markerState)
    }
}
```

### Path Animation

Animate marker along a predefined path.

```kotlin
class PathAnimation(
    val waypoints: List<GeoPoint>,
    override val duration: Long = 3000,
    override val interpolator: TimeInterpolator? = LinearInterpolator()
) : MarkerAnimation {

    fun getPositionAtProgress(progress: Float): GeoPoint {
        // Calculate position along path based on progress (0.0 to 1.0)
        val totalDistance = calculateTotalDistance()
        val targetDistance = totalDistance * progress

        // Find segment and interpolate position
        return interpolateAlongPath(targetDistance)
    }
}
```

#### Usage Example

```kotlin
@Composable
fun PathAnimationExample() {
    val waypoints = remember {
        listOf(
            GeoPointImpl.fromLatLong(37.7749, -122.4194), // Start
            GeoPointImpl.fromLatLong(37.7849, -122.4094), // Waypoint 1
            GeoPointImpl.fromLatLong(37.7949, -122.3994), // Waypoint 2
            GeoPointImpl.fromLatLong(37.8049, -122.3894)  // End
        )
    }

    val markerState = remember {
        MarkerState(
            position = waypoints.first(),
            icon = DefaultIcon(fillColor = Color.Orange, label = "🚗")
        )
    }

    // Start path animation
    LaunchedEffect(Unit) {
        val pathAnimation = PathAnimation(
            waypoints = waypoints,
            duration = 5000 // 5 seconds to complete path
        )
        markerState.setAnimation(pathAnimation)
    }

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
    MapView(state = mapViewState) {
        // Show path
        Polyline(
            points = waypoints,
            strokeColor = Color.Blue,
            strokeWidth = 3.dp
        )

        // Animated marker
        Marker(markerState)

        // Waypoint markers
        waypoints.forEachIndexed { index, point ->
            Marker(
                position = point,
                icon = DefaultIcon(
                    fillColor = Color.Gray,
                    scale = 0.6f,
                    label = "$index"
                )
            )
        }
    }
}
```

## Animation Event Handling

### Animation Callbacks

Handle animation lifecycle events in your map component:

```kotlin
// Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(
    state = mapViewState,
    onMarkerAnimateStart = { markerState ->
        println("Animation started for marker: ${markerState.id}")
        // Update UI state, start loading indicators, etc.
    },
    onMarkerAnimateEnd = { markerState ->
        println("Animation completed for marker: ${markerState.id}")
        // Clean up resources, update final state, etc.
    }
) {
    // Markers with animations
}
```

### Custom Animation Progress

Monitor animation progress for custom behaviors:

```kotlin
class ProgressTrackingAnimation(
    private val baseAnimation: MarkerAnimation,
    private val onProgress: (Float) -> Unit
) : MarkerAnimation by baseAnimation {

    override fun onUpdate(progress: Float) {
        baseAnimation.onUpdate(progress)
        onProgress(progress)
    }
}

// Usage
val trackingAnimation = ProgressTrackingAnimation(
    baseAnimation = PositionAnimation(from, to, 2000)
) { progress ->
    // Custom progress handling
    println("Animation is ${(progress * 100).toInt()}% complete")
}
```

## Performance Considerations

### Animation Optimization

1. **Limit Concurrent Animations**: Too many simultaneous animations can impact performance
2. **Use Appropriate Duration**: Very long animations may feel sluggish
3. **Choose Efficient Interpolators**: Some interpolators are more computationally expensive

```kotlin
// Good: Reasonable animation count and duration
val animations = markerStates.take(10).map { markerState ->
    PositionAnimation(
        fromPosition = markerState.position,
        toPosition = newPosition,
        duration = 800 // Reasonable duration
    )
}

// Avoid: Too many long animations
val badAnimations = markerStates.take(100).map { markerState ->
    PositionAnimation(
        fromPosition = markerState.position,
        toPosition = newPosition,
        duration = 5000 // Too long
    )
}
```

### Memory Management

```kotlin
// Clear animations when no longer needed
LaunchedEffect(shouldClearAnimations) {
    if (shouldClearAnimations) {
        markerStates.forEach { it.setAnimation(null) }
    }
}
```

## Best Practices

### Animation Guidelines

1. **Provide Feedback**: Use animations to provide visual feedback for user interactions
2. **Maintain Context**: Animations should help users understand spatial relationships
3. **Be Consistent**: Use similar animation styles throughout your application
4. **Consider Accessibility**: Provide options to reduce or disable animations

### Common Patterns

```kotlin
// Entrance animation for new markers
fun createEntranceAnimation(): MarkerAnimation = SequentialAnimation(
    listOf(
        ScaleAnimation(fromScale = 0.0f, toScale = 1.2f, duration = 200),
        ScaleAnimation(fromScale = 1.2f, toScale = 1.0f, duration = 100)
    )
)

// Attention-grabbing animation
fun createAttentionAnimation(): MarkerAnimation = SequentialAnimation(
    listOf(
        ScaleAnimation(fromScale = 1.0f, toScale = 1.3f, duration = 150),
        ScaleAnimation(fromScale = 1.3f, toScale = 1.0f, duration = 150),
        ScaleAnimation(fromScale = 1.0f, toScale = 1.3f, duration = 150),
        ScaleAnimation(fromScale = 1.3f, toScale = 1.0f, duration = 150)
    )
)

// Smooth exit animation
fun createExitAnimation(): MarkerAnimation = SequentialAnimation(
    listOf(
        FadeAnimation(fromAlpha = 1.0f, toAlpha = 0.0f, duration = 300),
        ScaleAnimation(fromScale = 1.0f, toScale = 0.0f, duration = 200)
    )
)
```

## Troubleshooting

### Common Issues

1. **Animations Not Starting**: Verify `MarkerState.setAnimation()` is called
2. **Jerky Movement**: Check interpolator choice and frame rate
3. **Memory Leaks**: Clear animations when markers are removed
4. **Performance Issues**: Limit concurrent animations and use shorter durations

### Debug Animation

```kotlin
// Enable animation debugging
val debugAnimation = object : MarkerAnimation {
    override val duration = 1000L
    override val interpolator = AccelerateDecelerateInterpolator()

    override fun onStart() {
        Log.d("Animation", "Animation started")
    }

    override fun onUpdate(progress: Float) {
        Log.d("Animation", "Progress: $progress")
    }

    override fun onEnd() {
        Log.d("Animation", "Animation completed")
    }
}
```