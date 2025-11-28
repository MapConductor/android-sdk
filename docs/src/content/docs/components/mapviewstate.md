---
title: "MapViewState"
---

`MapViewState` is the core component that manages map initialization, camera position, and overall map state. Each map provider has its own implementation while maintaining a consistent interface.

## Provider Implementations

MapConductor supports four map providers, each with their own `MapViewState` implementation:

- `GoogleMapViewStateImpl` - Google Maps
- `MapboxViewStateImpl` - Mapbox Maps
- `HereViewStateImpl` - HERE Maps
- `ArcGISMapViewStateImpl` - ArcGIS Maps

## Core Properties

### Initialization State

- **`isInitialized: StateFlow<InitState>`**: Tracks the map initialization status
  - `NotStarted`: Map initialization has not begun
  - `Initializing`: Map is currently being initialized
  - `Initialized`: Map is ready to use
  - `Failed`: Initialization failed

### Camera Management

- **`cameraPosition: StateFlow<MapCameraPositionImpl?>`**: Current camera position
- **`initCameraPosition: MapCameraPositionImpl`**: Initial camera position when map loads

### Map Design

- **`mapDesignType: ActualMapDesignType`**: Map style/design (provider-specific)

## Core Methods

### Initialization

```kotlin
fun initAsync(init: suspend () -> Boolean)
```
Initializes the map asynchronously. Returns `true` if successful.

```kotlin
fun resetInitState()
```
Resets the initialization state to `NotStarted`.

### Camera Movement

```kotlin
fun moveCameraTo(
    cameraPosition: MapCameraPositionImpl,
    durationMills: Long? = 0,
    listener: MoveCameraCallback? = null
)
```
Moves the camera to a specific position with optional animation.

```kotlin
fun moveCameraTo(
    position: GeoPointImpl,
    durationMills: Long? = 0,
    listener: MoveCameraCallback? = null
)
```
Moves the camera to focus on a specific geographic point.

## Usage Example

### Basic Setup

```kotlin
@Composable
fun MapExample() {
    // Create map state (choose your provider)
    val mapViewState = rememberGoogleMapViewState()

    // Monitor initialization state
    val initState by mapViewState.isInitialized.collectAsState()

    when (initState) {
        InitState.NotStarted -> Text("Map not started")
        InitState.Initializing -> CircularProgressIndicator()
        InitState.Initialized -> {
            // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(state = mapViewState) {
                // Add map content here
            }
        }
        InitState.Failed -> Text("Map failed to load")
    }
}
```

### Camera Control

```kotlin
@Composable
fun CameraControlExample() {
    val mapViewState = rememberGoogleMapViewState()

    Column {
        Button(
            onClick = {
                val sanFrancisco = GeoPointImpl.fromLatLong(37.7749, -122.4194)
                mapViewState.moveCameraTo(
                    position = sanFrancisco,
                    durationMills = 1000,
                    listener = object : MapViewState.MoveCameraCallback {
                        override fun onComplete() {
                            println("Camera movement completed")
                        }
                    }
                )
            }
        ) {
            Text("Move to San Francisco")
        }

        // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(state = mapViewState) {
            // Map content
        }
    }
}
```

### Provider Switching

```kotlin
@Composable
fun ProviderSwitchExample() {
    var selectedProvider by remember { mutableStateOf("google") }

    val mapViewState = remember(selectedProvider) {
        when (selectedProvider) {
            "google" -> GoogleMapViewStateImpl()
            "mapbox" -> MapboxViewStateImpl()
            "here" -> HereViewStateImpl()
            "arcgis" -> ArcGISMapViewStateImpl()
            else -> GoogleMapViewStateImpl()
        }
    }

    Column {
        Row {
            Button(onClick = { selectedProvider = "google" }) {
                Text("Google Maps")
            }
            Button(onClick = { selectedProvider = "mapbox" }) {
                Text("Mapbox")
            }
            Button(onClick = { selectedProvider = "here" }) {
                Text("HERE")
            }
            Button(onClick = { selectedProvider = "arcgis" }) {
                Text("ArcGIS")
            }
        }

        // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(state = mapViewState) {
            Marker(
                position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                icon = DefaultIcon(label = selectedProvider)
            )
        }
    }
}
```

## Event Handling

The `MapViewState` works with your chosen map provider component to provide comprehensive event handling:

```kotlin
// Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(
    state = mapViewState,
    onMapViewInitialized = {
        println("Map view initialized")
    },
    onMapLoaded = {
        println("Map loaded successfully")
    },
    onMapClick = { geoPoint ->
        println("Map clicked at: ${geoPoint.latitude}, ${geoPoint.longitude}")
    }
) {
    // Map content
}
```

## Best Practices

1. **Remember State**: Always use `remember` to maintain state across recompositions
2. **Monitor Initialization**: Check initialization state before adding content
3. **Handle Failures**: Provide fallback UI for initialization failures
4. **Provider Abstraction**: Write code that works with any provider implementation
5. **Resource Management**: Allow the SDK to handle lifecycle management automatically
