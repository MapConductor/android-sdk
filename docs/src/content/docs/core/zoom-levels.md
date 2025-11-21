---
title: "Zoom Levels"
---

MapConductor uses zoom levels to control the scale and detail of the map display. The zoom level system approximately follows Google Maps conventions but may vary slightly between map providers due to their underlying implementation differences.

## Understanding Zoom Levels

Zoom levels in MapConductor are represented as `Double` values, typically ranging from 0 to 21, where:

- **Higher numbers** = More zoomed in (more detail, smaller area)
- **Lower numbers** = More zoomed out (less detail, larger area)
- **Fractional values** = Smooth interpolation between integer levels

```kotlin
// Examples of different zoom levels
val worldView = 2.0        // See continents
val countryView = 6.0      // See entire countries
val cityView = 10.0        // See cities
val streetView = 15.0      // See streets
val buildingView = 18.0    // See individual buildings
```

## Zoom Level Reference

### Global Scale (0-5)

```kotlin
// World and continent level
val worldLevel = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(0.0, 0.0),
    zoom = 2.0  // Shows continents and oceans
)

val continentLevel = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(39.8283, -98.5795), // USA center
    zoom = 4.0  // Shows entire continent
)
```

**Use Cases:**
- Global data visualization
- Continental overview
- Flight tracking applications
- World weather maps

### Regional Scale (6-9)

```kotlin
// Country and state level
val countryLevel = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(39.8283, -98.5795),
    zoom = 6.0  // Shows entire country
)

val stateLevel = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(36.7783, -119.4179), // California center
    zoom = 8.0  // Shows state or large region
)
```

**Use Cases:**
- National statistics
- State-level data
- Regional weather
- Transportation networks

### Metropolitan Scale (10-12)

```kotlin
// City and metropolitan area level
val metropolitanLevel = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194), // San Francisco
    zoom = 10.0  // Shows metropolitan area
)

val cityLevel = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    zoom = 12.0  // Shows city center and suburbs
)
```

**Use Cases:**
- City planning
- Public transportation
- Delivery zones
- District boundaries

### Neighborhood Scale (13-15)

```kotlin
// District and neighborhood level
val districtLevel = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    zoom = 13.0  // Shows districts
)

val neighborhoodLevel = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    zoom = 15.0  // Shows neighborhoods and major streets
)
```

**Use Cases:**
- Local business directories
- School districts
- Neighborhood services
- Real estate search

### Street Scale (16-18)

```kotlin
// Street and block level
val streetLevel = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    zoom = 16.0  // Shows street layout
)

val detailedStreetLevel = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    zoom = 18.0  // Shows individual streets and small buildings
)
```

**Use Cases:**
- Navigation applications
- Address lookup
- Street-level services
- Walking directions

### Building Scale (19-21)

```kotlin
// Building and detail level
val buildingLevel = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    zoom = 19.0  // Shows individual buildings
)

val maximumDetail = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    zoom = 21.0  // Maximum zoom level
)
```

**Use Cases:**
- Building inspection
- Asset management
- Indoor mapping
- Detailed surveying

## Provider Variations

While MapConductor normalizes zoom levels across providers, there can be subtle differences:

### Google Maps
- Closely follows the reference scale
- Smooth fractional zoom support
- Range: 0-21

### Mapbox
- Similar to Google Maps
- Excellent fractional zoom interpolation
- Range: 0-22 (may vary by style)

### HERE Maps
- Comparable scale with minor variations
- Good fractional zoom support
- Range: 0-20

### ArcGIS
- May have different detail levels at equivalent zoom
- Scene view (3D) may behave differently
- Range varies by basemap

## Dynamic Zoom Selection

### Content-Based Zoom

```kotlin
@Composable
fun ContentBasedZoom() {
    val markers = remember {
        listOf(
            GeoPointImpl.fromLatLong(37.7749, -122.4194),
            GeoPointImpl.fromLatLong(37.7849, -122.4094),
            GeoPointImpl.fromLatLong(37.7949, -122.3994)
        )
    }

    // Calculate bounds containing all markers
    val bounds = GeoRectBounds()
    markers.forEach { bounds.extend(it) }

    // Select appropriate zoom for content
    val appropriateZoom = when {
        bounds.isEmpty -> 15.0  // Default for single point
        else -> {
            val span = bounds.toSpan()
            when {
                span?.latitude ?: 0.0 > 10.0 -> 4.0   // Large area
                span?.latitude ?: 0.0 > 1.0 -> 8.0    // Medium area
                span?.latitude ?: 0.0 > 0.1 -> 12.0   // Small area
                span?.latitude ?: 0.0 > 0.01 -> 16.0  // Very small area
                else -> 18.0                          // Minimal area
            }
        }
    }

    val cameraPosition = MapCameraPositionImpl(
        position = bounds.center ?: markers.first(),
        zoom = appropriateZoom
    )

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
    MapView(
        state = mapViewState,
        cameraPosition = cameraPosition
    ) {
        markers.forEach { position ->
            Marker(
                position = position,
                icon = DefaultIcon(fillColor = Color.Blue)
            )
        }
    }
}
```

### User-Controlled Zoom

```kotlin
@Composable
fun ZoomControls() {
    var currentZoom by remember { mutableStateOf(15.0) }
    val center = GeoPointImpl.fromLatLong(37.7749, -122.4194)

    Column {
        // Zoom level display
        Text("Zoom Level: ${String.format("%.1f", currentZoom)}")

        // Zoom controls
        Row {
            Button(
                onClick = { currentZoom = (currentZoom + 1).coerceAtMost(21.0) }
            ) {
                Text("+")
            }

            Button(
                onClick = { currentZoom = (currentZoom - 1).coerceAtLeast(0.0) }
            ) {
                Text("-")
            }
        }

        // Preset zoom buttons
        Row {
            Button(onClick = { currentZoom = 12.0 }) { Text("City") }
            Button(onClick = { currentZoom = 15.0 }) { Text("Street") }
            Button(onClick = { currentZoom = 18.0 }) { Text("Building") }
        }

        // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
        MapView(
            state = mapViewState,
            cameraPosition = MapCameraPositionImpl(
                position = center,
                zoom = currentZoom
            )
        ) {
            Marker(
                position = center,
                icon = DefaultIcon(fillColor = Color.Red)
            )
        }
    }
}
```

## Adaptive Zoom Strategies

### Performance-Based Zoom

```kotlin
@Composable
fun PerformanceAdaptiveZoom() {
    val markerCount = 1000
    var displayedMarkers by remember { mutableStateOf<List<GeoPoint>>(emptyList()) }

    val cameraPosition = MapCameraPositionImpl(
        position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        zoom = 12.0
    )

    LaunchedEffect(cameraPosition) {
        // Adapt marker display based on zoom level
        displayedMarkers = when {
            cameraPosition.zoom < 8.0 -> {
                // Very low zoom: show only major markers
                allMarkers.filter { it.importance > 0.8 }
            }
            cameraPosition.zoom < 12.0 -> {
                // Medium zoom: show important markers
                allMarkers.filter { it.importance > 0.5 }
            }
            cameraPosition.zoom < 16.0 -> {
                // High zoom: show most markers
                allMarkers.filter { it.importance > 0.2 }
            }
            else -> {
                // Maximum zoom: show all markers
                allMarkers
            }
        }
    }

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
    MapView(
        state = mapViewState,
        cameraPosition = cameraPosition
    ) {
        displayedMarkers.forEach { marker ->
            Marker(
                position = marker,
                icon = DefaultIcon(
                    scale = when {
                        cameraPosition.zoom < 10.0 -> 1.2f
                        cameraPosition.zoom < 15.0 -> 1.0f
                        else -> 0.8f
                    }
                )
            )
        }
    }
}
```

### Context-Aware Zoom

```kotlin
@Composable
fun ContextAwareZoom() {
    val userLocation = GeoPointImpl.fromLatLong(37.7749, -122.4194)
    val searchResults = remember { /* your search results */ }

    // Select zoom based on context
    val contextualZoom = when {
        searchResults.isEmpty() -> 15.0  // User location focus
        searchResults.size == 1 -> 16.0  // Single result
        searchResults.size < 5 -> 14.0   // Few results
        else -> {
            // Calculate appropriate zoom for all results
            val bounds = GeoRectBounds()
            searchResults.forEach { bounds.extend(it.location) }
            calculateZoomForBounds(bounds)
        }
    }

    val cameraPosition = MapCameraPositionImpl(
        position = if (searchResults.isEmpty()) userLocation else bounds.center!!,
        zoom = contextualZoom
    )

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
    MapView(
        state = mapViewState,
        cameraPosition = cameraPosition
    ) {
        // User location
        Marker(
            position = userLocation,
            icon = DefaultIcon(fillColor = Color.Blue, label = "You")
        )

        // Search results
        searchResults.forEach { result ->
            Marker(
                position = result.location,
                icon = DefaultIcon(fillColor = Color.Red)
            )
        }
    }
}
```

## Best Practices

### Zoom Level Selection

1. **Content-Driven**: Choose zoom based on what you want to show
2. **User Context**: Consider user's current task and location
3. **Performance**: Higher zoom levels require more detail rendering
4. **Provider Limitations**: Test across all target map providers

### Smooth Transitions

```kotlin
// Smooth zoom animation
fun animateToZoom(targetZoom: Double) {
    val animator = ValueAnimator.ofFloat(currentZoom.toFloat(), targetZoom.toFloat())
    animator.duration = 1000
    animator.addUpdateListener { animation ->
        val newZoom = animation.animatedValue as Float
        updateCameraPosition(currentPosition.copy(zoom = newZoom.toDouble()))
    }
    animator.start()
}
```

### Responsive Design

```kotlin
@Composable
fun ResponsiveZoom() {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    // Adjust zoom for screen size
    val baseZoom = if (isTablet) 14.0 else 16.0

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
    MapView(
        state = mapViewState,
        cameraPosition = MapCameraPositionImpl(
            position = center,
            zoom = baseZoom
        )
    ) {
        // Content
    }
}
```

## Common Pitfalls

1. **Provider Inconsistency**: Test zoom levels across all target providers
2. **Performance Impact**: Higher zoom levels increase rendering cost
3. **Fractional Zoom**: Not all providers handle fractional zoom identically
4. **Minimum/Maximum**: Respect provider-specific zoom limits
5. **Gesture Conflicts**: Consider user zoom gestures vs programmatic zoom

## Testing Zoom Levels

```kotlin
@Composable
fun ZoomLevelTester() {
    val testZoomLevels = listOf(0.0, 5.0, 10.0, 15.0, 20.0)
    var currentTestIndex by remember { mutableStateOf(0) }

    Column {
        Text("Testing Zoom Level: ${testZoomLevels[currentTestIndex]}")

        Button(
            onClick = {
                currentTestIndex = (currentTestIndex + 1) % testZoomLevels.size
            }
        ) {
            Text("Next Zoom Level")
        }

        // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
        MapView(
            state = mapViewState,
            cameraPosition = MapCameraPositionImpl(
                position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                zoom = testZoomLevels[currentTestIndex]
            )
        ) {
            Marker(
                position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                icon = DefaultIcon(fillColor = Color.Red)
            )
        }
    }
}
```

Understanding and properly utilizing zoom levels is crucial for creating effective mapping applications that provide the right level of detail for your users' needs.