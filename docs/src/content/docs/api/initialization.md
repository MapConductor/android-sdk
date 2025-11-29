---
title: "Initialization"
---

This section covers how to properly initialize and configure MapConductor for different map providers.

## Basic Initialization

### Gradle Dependencies

Add MapConductor to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation "com.mapconductor:mapconductor-bom:$version"
    implementation "com.mapconductor:core"

    // Choose your map provider(s)
    implementation "com.mapconductor:for-googlemaps"
    implementation "com.mapconductor:for-mapbox"
    implementation "com.mapconductor:for-here"
    implementation "com.mapconductor:for-arcgis"
}
```

### Map Provider Setup

Each map provider requires specific setup and API keys.

#### Google Maps

```kotlin
// In your Activity or Fragment
@Composable
fun GoogleMapsExample() {
    val mapViewState = rememberGoogleMapViewState()

    // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(state = mapViewState) {
        // Map content
    }
}
```

#### Mapbox

```kotlin
@Composable
fun MapboxExample() {
    val mapViewState = rememberMapboxMapViewState()

    // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(state = mapViewState) {
        // Map content
    }
}
```

#### HERE Maps

```kotlin
@Composable
fun HereExample() {
    val mapViewState = rememberHereMapViewState()

    // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(state = mapViewState) {
        // Map content
    }
}
```

#### ArcGIS

```kotlin
@Composable
fun ArcGISExample() {
    val mapViewState = rememberArcGISMapViewState()

    // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(state = mapViewState) {
        // Map content
    }
}
```

## Advanced Initialization

### Custom Map Configuration

```kotlin
@Composable
fun CustomMapConfiguration() {
    val mapViewState = remember {
        GoogleMapViewStateImpl().apply {
            // Set initial camera position
            initCameraPosition = MapCameraPositionImpl(
                target = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                zoom = 12f,
                bearing = 45f,
                tilt = 30f
            )

            // Set map design/style
            mapDesignType = GoogleMapDesignType.SATELLITE
        }
    }

    // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(
        state = mapViewState,
        onMapViewInitialized = {
            println("Map view initialized")
        },
        onMapLoaded = {
            println("Map loaded and ready")
        }
    ) {
        // Map content
    }
}
```

### Provider Selection at Runtime

```kotlin
@Composable
fun DynamicProviderSelection() {
    var selectedProvider by remember { mutableStateOf("google") }

    val mapViewState = remember(selectedProvider) {
        when (selectedProvider) {
            "google" -> rememberGoogleMapViewState()
            "mapbox" -> rememberMapboxMapViewState()
            "here" -> rememberHereMapViewState()
            "arcgis" -> rememberArcGISMapViewState()
            else -> rememberGoogleMapViewState()
        }
    }

    Column {
        // Provider selection UI
        LazyRow {
            items(listOf("google", "mapbox", "here", "arcgis")) { provider ->
                Button(
                    onClick = { selectedProvider = provider },
                    colors = if (provider == selectedProvider) {
                        ButtonDefaults.buttonColors(backgroundColor = Color.Blue)
                    } else {
                        ButtonDefaults.buttonColors()
                    }
                ) {
                    Text(provider.capitalize())
                }
            }
        }

        // Map with selected provider
        // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(state = mapViewState) {
            Marker(
                position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                icon = DefaultIcon(label = selectedProvider.uppercase())
            )
        }
    }
}
```

### Initialization State Handling

```kotlin
@Composable
fun InitializationStateExample() {
    val mapViewState = rememberGoogleMapViewState()
    val initState by mapViewState.isInitialized.collectAsState()

    when (initState) {
        InitState.NotStarted -> {
            // Show loading placeholder
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Text("Preparing map...")
                }
            }
        }

        InitState.Initializing -> {
            // Show initialization progress
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Text("Loading map...")
                }
            }
        }

        InitState.Initialized -> {
            // Show the map
            // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(state = mapViewState) {
                // Map is ready for content
                Marker(
                    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                    icon = DefaultIcon(label = "Ready!")
                )
            }
        }

        InitState.Failed -> {
            // Show error state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = "Error",
                        tint = Color.Red,
                        modifier = Modifier.size(48.dp)
                    )
                    Text("Failed to load map")
                    Button(
                        onClick = { mapViewState.resetInitState() }
                    ) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}
```

### Deferred Initialization

```kotlin
@Composable
fun DeferredInitializationExample() {
    var shouldInitialize by remember { mutableStateOf(false) }
    val mapViewState = rememberGoogleMapViewState()

    Column {
        if (!shouldInitialize) {
            Button(
                onClick = { shouldInitialize = true },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text("Load Map")
            }
        }

        if (shouldInitialize) {
            // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(
                state = mapViewState,
                shouldInitialize = shouldInitialize
            ) {
                Marker(
                    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                    icon = DefaultIcon(label = "Loaded!")
                )
            }
        }
    }
}
```

### Custom Rendering Strategy

```kotlin
@Composable
fun CustomRenderingExample() {
    val mapViewState = rememberGoogleMapViewState()

    // Custom marker rendering strategy
    val customStrategy = remember {
        object : MarkerRenderingStrategy<GoogleMapActualMarker> {
            override suspend fun render(
                markers: Map<String, MarkerState>,
                controller: MapViewController
            ) {
                // Custom rendering logic
                // This is an advanced feature for specific use cases
            }
        }
    }

    // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(
        state = mapViewState,
        renderingStrategy = customStrategy
    ) {
        // Map content with custom rendering
    }
}
```

## Configuration Options

### Camera Position

```kotlin
val cameraPosition = MapCameraPositionImpl(
    target = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    zoom = 15f,
    bearing = 0f,  // Rotation in degrees
    tilt = 0f      // Tilt angle in degrees
)

mapViewState.initCameraPosition = cameraPosition
```

### Map Bounds

```kotlin
// Set initial map bounds
val bounds = GeoRectBounds(
    southwest = GeoPointImpl.fromLatLong(37.7049, -122.4794),
    northeast = GeoPointImpl.fromLatLong(37.8049, -122.3594)
)

// Move camera to show bounds
mapViewState.moveCameraTo(bounds)
```

## Error Handling

### Initialization Failures

```kotlin
@Composable
fun RobustInitializationExample() {
    val mapViewState = rememberGoogleMapViewState()
    val initState by mapViewState.isInitialized.collectAsState()
    var retryCount by remember { mutableStateOf(0) }

    LaunchedEffect(initState) {
        if (initState == InitState.Failed) {
            // Log error and potentially retry
            println("Map initialization failed (attempt ${retryCount + 1})")

            if (retryCount < 3) {
                delay(1000) // Wait before retry
                retryCount++
                mapViewState.resetInitState()
            }
        } else if (initState == InitState.Initialized) {
            retryCount = 0 // Reset on success
        }
    }

    when (initState) {
        InitState.Failed -> {
            if (retryCount >= 3) {
                ErrorScreen(onRetry = {
                    retryCount = 0
                    mapViewState.resetInitState()
                })
            } else {
                LoadingScreen("Retrying...")
            }
        }
        else -> {
            // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(state = mapViewState) {
                // Map content
            }
        }
    }
}

@Composable
fun ErrorScreen(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Map failed to load")
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
fun LoadingScreen(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Text(message)
        }
    }
}
```

## Best Practices

1. **Use remember**: Always wrap MapViewState creation in `remember`
2. **Handle States**: Properly handle all initialization states
3. **Error Recovery**: Implement retry logic for failed initializations
4. **Resource Management**: Let the SDK handle lifecycle management
5. **API Keys**: Ensure proper API key configuration for each provider
6. **Performance**: Consider deferred initialization for better app startup
7. **Testing**: Test with different providers to ensure compatibility