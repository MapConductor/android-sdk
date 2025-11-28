---
title: "MapCameraPosition"
---

`MapCameraPosition` represents the camera's viewing position, orientation, and visible area on the map. It defines where the camera is looking, how much of the map is visible, and the perspective from which the map is viewed.

## Interface and Implementation

### MapCameraPosition Interface

```kotlin
interface MapCameraPosition {
    val position: GeoPoint
    val zoom: Double
    val bearing: Double
    val tilt: Double
    val paddings: MapPaddings?
    val visibleRegion: VisibleRegion?
}
```

### MapCameraPositionImpl

The main implementation provides immutable camera position data:

```kotlin
data class MapCameraPositionImpl(
    override val position: GeoPointImpl,
    override val zoom: Double = 0.0,
    override val bearing: Double = 0.0,
    override val tilt: Double = 0.0,
    override val paddings: MapPaddings? = MapPaddingsImpl.Zeros,
    override val visibleRegion: VisibleRegion? = null
) : MapCameraPosition
```

## Properties

### Camera Location

- **`position: GeoPoint`**: Geographic center point of the camera's view
- **`zoom: Double`**: Zoom level (approximately follows Google Maps scale)
- **`bearing: Double`**: Compass direction in degrees (0 = North, 90 = East)
- **`tilt: Double`**: Camera tilt angle in degrees (0 = top-down, 90 = horizontal)

### View Configuration

- **`paddings: MapPaddings?`**: Viewport padding that affects the visible region
- **`visibleRegion: VisibleRegion?`**: The actual geographic bounds visible on screen

## Creation Methods

### Default Position

```kotlin
// Default camera position at origin
val defaultPosition = MapCameraPositionImpl.Default
```

### Custom Position

```kotlin
// Basic camera position
val sanFrancisco = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    zoom = 15.0
)

// Camera with bearing and tilt
val aerialView = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    zoom = 18.0,
    bearing = 45.0,  // Northeast direction
    tilt = 60.0      // Angled view
)

// Camera with padding for UI elements
val paddedView = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    zoom = 14.0,
    paddings = MapPaddingsImpl(
        top = 100.0,
        left = 50.0,
        bottom = 200.0,
        right = 50.0
    )
)
```

## Zoom Levels

MapConductor zoom levels approximately follow Google Maps scale but may vary slightly between providers:

- **0-2**: World view, continents visible
- **3-5**: Country level
- **6-9**: State/region level
- **10-12**: City level
- **13-15**: District/neighborhood level
- **16-18**: Street level
- **19-21**: Building level (high detail)

```kotlin
// Different zoom levels for different use cases
val worldView = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(0.0, 0.0),
    zoom = 2.0  // Show continents
)

val cityView = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    zoom = 12.0  // Show entire city
)

val streetView = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    zoom = 17.0  // Show individual streets
)
```

## Bearing and Tilt

### Bearing (Rotation)

Bearing rotates the map around the center point:

```kotlin
// North-up (default)
val northUp = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    bearing = 0.0
)

// East-up
val eastUp = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    bearing = 90.0
)

// Following a route bearing
val routeBearing = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    bearing = 135.0,  // Southeast
    zoom = 18.0
)
```

### Tilt (3D Perspective)

Tilt provides 3D viewing angles:

```kotlin
// Top-down view (default)
val topDown = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    tilt = 0.0
)

// Slight angle for depth
val angled = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    tilt = 30.0,
    zoom = 16.0
)

// Maximum tilt for street-level view
val streetLevel = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    tilt = 80.0,
    bearing = 45.0,
    zoom = 19.0
)
```

## Visible Region

The visible region describes the actual geographic area shown on screen after considering camera position, zoom level, bearing, tilt, and viewport padding.

### VisibleRegion Class

```kotlin
data class VisibleRegion(
    val bounds: GeoRectBounds,        // Overall bounding rectangle
    val nearLeft: GeoPoint?,          // Bottom-left corner (near to camera)
    val nearRight: GeoPoint?,         // Bottom-right corner (near to camera)
    val farLeft: GeoPoint?,           // Top-left corner (far from camera)
    val farRight: GeoPoint?           // Top-right corner (far from camera)
)
```

### Properties

- **`bounds: GeoRectBounds`**: The rectangular geographic bounds that encompass the entire visible area. This is the minimum bounding rectangle that contains all visible content.

- **`nearLeft: GeoPoint?`**: The geographic coordinate of the bottom-left corner of the visible region. "Near" refers to the side closest to the camera position.

- **`nearRight: GeoPoint?`**: The geographic coordinate of the bottom-right corner of the visible region.

- **`farLeft: GeoPoint?`**: The geographic coordinate of the top-left corner of the visible region. "Far" refers to the side furthest from the camera position.

- **`farRight: GeoPoint?`**: The geographic coordinate of the top-right corner of the visible region.

### Behavior with Camera Parameters

The visible region is affected by all camera parameters:

- **Zoom Level**: Higher zoom shows smaller geographic area but with more detail
- **Bearing**: Camera rotation changes which geographic directions correspond to screen edges
- **Tilt**: 3D perspective affects the shape of the visible region
- **Padding**: Reduces the effective viewport, changing the visible area without moving the camera center

### Corner Points vs Bounds

While `bounds` provides a simple rectangular boundary, the corner points (`nearLeft`, `nearRight`, `farLeft`, `farRight`) provide the exact geographic coordinates of each screen corner. This is especially important when:

- Camera has bearing rotation (corners are not aligned with cardinal directions)
- Camera has tilt (visible region may not be perfectly rectangular)
- High precision boundary detection is needed

### Using Visible Region

```kotlin
@Composable
fun VisibleRegionExample() {
    var cameraPosition by remember { mutableStateOf<MapCameraPosition?>(null) }

    // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
    MapView(
        state = mapViewState,
    ) {
        // Display visible region info
        cameraPosition?.visibleRegion?.let { region ->
            // Show bounds as a polygon
            region.bounds?.let { bounds ->
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
                        strokeWidth = 2.dp,
                        fillColor = Color.Blue.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }
}
```

## Animation and Transitions

### Smooth Camera Movement

```kotlin
@Composable
fun AnimatedCameraExample() {
    val locations = listOf(
        GeoPointImpl.fromLatLong(37.7749, -122.4194), // San Francisco
        GeoPointImpl.fromLatLong(40.7128, -74.0060),  // New York
        GeoPointImpl.fromLatLong(51.5074, -0.1278)    // London
    )

    val mapViewState = rememberHereMapViewState(
        cameraPosition = MapCameraPositionImpl(
            position = locations[0],
            zoom = 6.0
        ),
    )

    var currentIndex by remember { mutableStateOf(0) }

    // Animate to next location every 5 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            currentIndex = (currentIndex + 1) % locations.size

            val targetPosition = MapCameraPositionImpl(
                position = locations[currentIndex],
                zoom = 6.0,
                bearing = 0.0,
                tilt = 0.0
            )
            mapViewState.moveCameraTo(targetPosition, 1000)
        }
    }


    // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
    HereMapView(
        state = mapViewState,
        modifier = modifier,
    ) {
        // Add markers for each location
        locations.forEachIndexed { index, location ->
            Marker(
                position = location,
                icon = DefaultIcon(
                    fillColor = if (index == currentIndex) Color.Red else Color.Gray,
                    label = when (index) {
                        0 -> "SF"
                        1 -> "NYC"
                        2 -> "LON"
                        else -> "$index"
                    }
                )
            )
        }
    }
}
```

### Interactive Camera Control

```kotlin
@Composable
fun CameraControlExample(modifier: Modifier = Modifier) {
    val mapViewState = rememberMapLibreMapViewState(
        mapDesign = MapLibreDesignType(
            id = "debug-tiles",
            styleJsonURL = "https://demotiles.maplibre.org/debug-tiles/style.json",
        ),
        cameraPosition = MapCameraPositionImpl(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            zoom = 15.0
        ),
    )

    Column(
        modifier = modifier,
    ) {
        // Camera controls
        Row {
            Button(
                onClick = {
                    val newZoom = mapViewState.cameraPosition.copy(
                        zoom = (mapViewState.cameraPosition.zoom + 1)
                            .coerceAtMost(21.0),
                    )
                    mapViewState.moveCameraTo(newZoom, 500)
                }
            ) {
                Text("Zoom In")
            }

            Button(
                onClick = {
                    val newZoom = mapViewState.cameraPosition.copy(
                        zoom = (mapViewState.cameraPosition.zoom - 1)
                            .coerceAtMost(21.0),
                    )
                    mapViewState.moveCameraTo(newZoom, 500)
                }
            ) {
                Text("Zoom Out")
            }

            Button(
                onClick = {
                    val newZoom = mapViewState.cameraPosition.copy(
                        bearing = (mapViewState.cameraPosition.bearing + 45) % 360,
                    )
                    mapViewState.moveCameraTo(newZoom, 500)
                }
            ) {
                Text("Rotate")
            }
        }

        // Tilt slider
        Slider(
            value = mapViewState.cameraPosition.tilt.toFloat(),
            onValueChange = { tilt ->
                val newZoom = mapViewState.cameraPosition.copy(
                    tilt = tilt.toDouble(),
                )
                mapViewState.moveCameraTo(newZoom)
            },
            valueRange = 0f..80f
        )

        // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
        MapLibreMapView(
            state = mapViewState,
        ) {
            Marker(
                position = mapViewState.cameraPosition.position,
                icon = DefaultIcon(fillColor = Color.Red)
            )
        }
    }
}
```

## Best Practices

1. **Zoom Level Selection**: Choose appropriate zoom levels for your use case
2. **Smooth Transitions**: Use gradual camera movements for better user experience
3. **Padding Management**: Account for UI elements when setting camera position
4. **Performance**: Avoid frequent camera position changes that trigger expensive redraws
5. **User Context**: Consider what the user needs to see when setting initial camera position
6. **Accessibility**: Provide controls for users who cannot use gesture-based camera movement

## Common Use Cases

### Focus on User Location

```kotlin
val userLocationCamera = MapCameraPositionImpl(
    position = userCurrentLocation,
    zoom = 16.0,  // Street level detail
    bearing = 0.0,
    tilt = 0.0
)
```

### Show Multiple Points

```kotlin
// Calculate bounds containing all points, then set appropriate zoom
val allPoints = listOf(/* your points */)
val bounds = GeoRectBounds()
allPoints.forEach { bounds.extend(it) }

val centerCamera = MapCameraPositionImpl(
    position = bounds.center ?: GeoPointImpl.fromLatLong(0.0, 0.0),
    zoom = calculateZoomForBounds(bounds),  // Custom calculation
    bearing = 0.0,
    tilt = 0.0
)
```

### Navigation Mode

```kotlin
val navigationCamera = MapCameraPositionImpl(
    position = currentRoutePosition,
    zoom = 18.0,
    bearing = currentHeading,  // Direction of travel
    tilt = 60.0               // Angled for street view
)
```
