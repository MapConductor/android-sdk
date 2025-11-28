---
title: "Marker Native Strategy (Experimental)"
---

The `mapconductor-marker-native-strategy` module provides high-performance marker management using native C++ spatial indexing. This experimental module dramatically improves performance for applications with large numbers of markers (10,000+).

> **⚠️ Experimental Module**: This module is highly experimental and requires native library support. Use in production with extreme caution.

## Overview

The marker native strategy replaces Java-based spatial indexing with optimized C++ implementations, providing:
- **90% Memory Reduction**: Compared to standard marker management
- **Native Spatial Queries**: C++ spatial indexing for maximum performance
- **Efficient Viewport Culling**: Only renders markers within the viewport
- **Parallel Processing**: Multi-threaded marker operations
- **Minimal Java Overhead**: Native code as single source of truth

## Performance Characteristics

### Memory Usage
- **Standard MarkerManager**: ~1MB per 1,000 markers
- **NativeMarkerManager**: ~100KB per 1,000 markers
- **Optimized Storage**: No duplicate entity storage

### Query Performance
- **Standard Spatial Query**: O(log n) with Java overhead
- **Native Spatial Query**: O(log n) with C++ optimization
- **Large Dataset**: 10x-100x performance improvement for 100,000+ markers

## Installation

Add the native strategy module to your `build.gradle`:

```kotlin
dependencies {
    implementation "com.mapconductor:marker-native-strategy"

    // Required: Core module
    implementation "com.mapconductor:mapconductor-bom:$version"
    // Required: Core module
    implementation "com.mapconductor:core"

    // Choose your map provider
    implementation "com.mapconductor:for-googlemaps"
}
```

### Native Library Setup

The module requires native C++ libraries. Ensure your app supports the required ABIs:

```kotlin
android {
    defaultConfig {
        ndk {
            abiFilters 'arm64-v8a', 'armeabi-v7a', 'x86', 'x86_64'
        }
    }
}
```

## Core Components

### NativeMarkerManager

High-performance marker manager using native spatial indexing:

```kotlin
import com.mapconductor.marker.nativestrategy.NativeMarkerManager
import com.mapconductor.core.geocell.HexGeocellImpl

// Create native marker manager
val nativeManager = NativeMarkerManager<ActualMarker>(
    hexGeocell = HexGeocellImpl.defaultGeocell()
)

// Use like standard MarkerManager
nativeManager.registerEntity(markerEntity)
val nearestMarker = nativeManager.findNearest(position)
val markersInBounds = nativeManager.findMarkersInBounds(bounds)
```

### Native Rendering Strategies

#### SimpleNativeParallelStrategy

Parallel marker rendering with native indexing:

```kotlin
import com.mapconductor.marker.nativestrategy.SimpleNativeParallelStrategy

val strategy = SimpleNativeParallelStrategy<ActualMarker>(
    expandMargin = 0.2,        // 20% viewport expansion
    maxConcurrency = 4,        // Parallel threads
    geocell = HexGeocellImpl.defaultGeocell()
)
```

## Basic Usage

### Simple Native Manager

```kotlin
@Composable
fun BasicNativeExample() {
    // Create native marker manager
    val nativeManager = remember {
        NativeMarkerManager<GoogleMapActualMarker>(
            hexGeocell = HexGeocellImpl.defaultGeocell()
        )
    }

    // Add markers to native manager
    LaunchedEffect(Unit) {
        val markers = generateLargeMarkerDataset() // 10,000+ markers
        markers.forEach { markerData ->
            val entity = MarkerEntity(
                state = MarkerState(
                    id = markerData.id,
                    position = markerData.position,
                    icon = DefaultIcon()
                )
            )
            nativeManager.registerEntity(entity)
        }
    }

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
    MapView(state = mapViewState) {
        // Markers are managed by native strategy
        // No need to manually add Marker composables
    }

    DisposableEffect(Unit) {
        onDispose {
            nativeManager.destroy() // Important: cleanup native resources
        }
    }
}
```

### Performance Monitoring

```kotlin
@Composable
fun NativePerformanceExample() {
    val nativeManager = remember {
        NativeMarkerManager<GoogleMapActualMarker>(
            hexGeocell = HexGeocellImpl.defaultGeocell()
        )
    }

    var stats by remember { mutableStateOf<NativeMarkerManagerStats?>(null) }

    // Monitor performance
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000) // Update every 5 seconds
            stats = nativeManager.getNativeMemoryStats()
        }
    }

    Column {
        stats?.let { s ->
            Text("Markers: ${s.entityCount}")
            Text("Native Index: ${s.nativeIndexCount}")
            Text("Memory: ${s.estimatedMemoryKB} KB")
            Text("Pure Native: ${s.usesPureNativeIndex}")
        }

        // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
        MapView(state = mapViewState) {
            // Native-managed markers
        }
    }
}
```

## Advanced Usage

### Parallel Rendering Strategy

```kotlin
@Composable
fun ParallelRenderingExample() {
    val parallelStrategy = remember {
        SimpleNativeParallelStrategy<GoogleMapActualMarker>(
            expandMargin = 0.3,        // Larger viewport expansion
            maxConcurrency = 6,        // More parallel threads
            geocell = HexGeocellImpl(
                baseHexSideLength = 1000.0,  // Optimized cell size
                zoom = 15.0
            )
        )
    }

    // Configure marker controller with parallel strategy
    LaunchedEffect(mapViewState) {
        mapViewState.getMapViewHolder()?.let { holder ->
            // Setup parallel strategy with map controller
            // Implementation depends on map provider
        }
    }

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
    MapView(state = mapViewState) {
        // Parallel-rendered markers
    }
}
```

### Dynamic Marker Loading

```kotlin
@Composable
fun DynamicNativeLoadingExample() {
    val nativeManager = remember {
        NativeMarkerManager<GoogleMapActualMarker>(
            hexGeocell = HexGeocellImpl.defaultGeocell()
        )
    }

    var currentBounds by remember { mutableStateOf<GeoRectBounds?>(null) }
    var visibleMarkers by remember { mutableStateOf<List<MarkerEntity<GoogleMapActualMarker>>>(emptyList()) }

    // Load markers dynamically based on viewport
    LaunchedEffect(currentBounds) {
        currentBounds?.let { bounds ->
            // Native spatial query is extremely fast
            visibleMarkers = nativeManager.findMarkersInBounds(bounds)
        }
    }

    Column {
        Text("Visible Markers: ${visibleMarkers.size}")

        // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
        MapView(
            state = mapViewState,
            onCameraMove = { cameraPosition ->
                currentBounds = cameraPosition.visibleRegion?.bounds
            }
        ) {
            // Only visible markers are processed
        }
    }
}
```

### Clustering with Native Strategy

```kotlin
@Composable
fun NativeClusteringExample() {
    val clusteringStrategy = remember {
        NativeSpatialMarkerStrategy<GoogleMapActualMarker>(
            clusteringEnabled = true,
            clusterThreshold = 50,     // Cluster when > 50 markers nearby
            clusterRadius = 100.0,     // 100-meter clustering radius
            geocell = HexGeocellImpl.defaultGeocell()
        )
    }

    val nativeManager = remember {
        NativeMarkerManager<GoogleMapActualMarker>(
            hexGeocell = HexGeocellImpl.defaultGeocell()
        )
    }

    // Add large dataset for clustering
    LaunchedEffect(Unit) {
        val denseMarkers = generateDenseMarkerCluster(
            center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            count = 1000,
            radiusMeters = 500.0 // 500 meters
        )

        denseMarkers.forEach { markerData ->
            val entity = MarkerEntity(
                state = MarkerState(
                    id = markerData.id,
                    position = markerData.position,
                    icon = DefaultIcon(fillColor = Color.Blue, scale = 0.8f)
                )
            )
            nativeManager.registerEntity(entity)
        }
    }

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
    MapView(state = mapViewState) {
        // Native clustering automatically groups nearby markers
    }
}
```

## Native Index Operations

### Direct Spatial Queries

```kotlin
fun performNativeQueries(nativeManager: NativeMarkerManager<ActualMarker>) {
    val center = GeoPointImpl.fromLatLong(37.7749, -122.4194)
    val bounds = GeoRectBounds(
        southWest = GeoPointImpl.fromLatLong(37.7700, -122.4250),
        northEast = GeoPointImpl.fromLatLong(37.7800, -122.4150)
    )

    // Native spatial queries are extremely fast
    val nearestMarker = nativeManager.findNearest(center)
    val boundedMarkers = nativeManager.findMarkersInBounds(bounds)
    val totalMarkers = nativeManager.allEntities().size

    // Memory and performance stats
    val stats = nativeManager.getNativeMemoryStats()
    println("Query performance: ${stats.nativeIndexCount} indexed markers")
    println("Memory usage: ${stats.estimatedMemoryKB} KB")
}
```

### Batch Operations

```kotlin
suspend fun batchNativeOperations(nativeManager: NativeMarkerManager<ActualMarker>) {
    val markersBatch = generateMarkerBatch(10000) // 10,000 markers

    // Efficient batch registration
    withContext(Dispatchers.Default) {
        markersBatch.forEach { markerData ->
            val entity = MarkerEntity(
                state = MarkerState(
                    id = markerData.id,
                    position = markerData.position,
                    icon = DefaultIcon()
                )
            )
            nativeManager.registerEntity(entity)
        }
    }

    // Verify registration
    val totalMarkers = nativeManager.allEntities().size
    println("Registered $totalMarkers markers in native index")
}
```

## Memory Management

### Resource Cleanup

```kotlin
@Composable
fun ResourceManagementExample() {
    val nativeManager = remember {
        NativeMarkerManager<GoogleMapActualMarker>(
            hexGeocell = HexGeocellImpl.defaultGeocell()
        )
    }

    // Proper cleanup is crucial for native resources
    DisposableEffect(nativeManager) {
        onDispose {
            // Cleanup native resources
            nativeManager.destroy()
        }
    }

    // Monitor memory usage
    var memoryStats by remember { mutableStateOf<NativeMarkerManagerStats?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(10000) // Check every 10 seconds
            memoryStats = nativeManager.getNativeMemoryStats()

            // Log memory usage for monitoring
            memoryStats?.let { stats ->
                if (stats.estimatedMemoryKB > 10000) { // > 10MB
                    println("High memory usage detected: ${stats.estimatedMemoryKB} KB")
                }
            }
        }
    }

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
    MapView(state = mapViewState) {
        // Native-managed markers
    }
}
```

## Performance Optimization

### Configuration Tuning

```kotlin
// Optimal configuration for large datasets
val optimizedGeocell = HexGeocellImpl(
    baseHexSideLength = 500.0,  // Smaller cells for dense data
    zoom = 18.0                 // Higher resolution
)

val optimizedManager = NativeMarkerManager<ActualMarker>(
    hexGeocell = optimizedGeocell
)

// Optimal parallel strategy
val optimizedStrategy = SimpleNativeParallelStrategy<ActualMarker>(
    expandMargin = 0.1,         // Smaller expansion for performance
    maxConcurrency = Runtime.getRuntime().availableProcessors(),
    geocell = optimizedGeocell
)
```

### Benchmark Testing

```kotlin
suspend fun benchmarkNativePerformance() {
    val standardManager = MarkerManager<ActualMarker>(HexGeocellImpl.defaultGeocell())
    val nativeManager = NativeMarkerManager<ActualMarker>(HexGeocellImpl.defaultGeocell())

    val testMarkers = generateTestDataset(50000) // 50,000 markers

    // Benchmark registration
    val standardTime = measureTimeMillis {
        testMarkers.forEach { standardManager.registerEntity(it) }
    }

    val nativeTime = measureTimeMillis {
        testMarkers.forEach { nativeManager.registerEntity(it) }
    }

    // Benchmark spatial queries
    val testBounds = GeoRectBounds(
        southWest = GeoPointImpl.fromLatLong(37.7700, -122.4250),
        northEast = GeoPointImpl.fromLatLong(37.7800, -122.4150)
    )

    val standardQueryTime = measureTimeMillis {
        repeat(1000) { standardManager.findMarkersInBounds(testBounds) }
    }

    val nativeQueryTime = measureTimeMillis {
        repeat(1000) { nativeManager.findMarkersInBounds(testBounds) }
    }

    println("Registration - Standard: ${standardTime}ms, Native: ${nativeTime}ms")
    println("Queries - Standard: ${standardQueryTime}ms, Native: ${nativeQueryTime}ms")
}
```

## Best Practices

1. **Resource Management**: Always call `destroy()` on NativeMarkerManager when done
2. **Batch Operations**: Use batch registration for large datasets
3. **Memory Monitoring**: Monitor native memory usage in production
4. **Testing**: Thoroughly test with your specific data patterns
5. **Fallback Strategy**: Have non-native fallback for unsupported devices

## Limitations and Considerations

1. **Platform Support**: Requires native library support for target ABIs
2. **Memory Management**: Native memory is not garbage collected
3. **Debugging**: Native crashes are harder to debug than Java crashes
4. **Binary Size**: Increases APK size due to native libraries
5. **Compatibility**: May not work on all devices/emulators

## Troubleshooting

### Native Library Loading Issues

```kotlin
// Check native library availability
try {
    val nativeManager = NativeMarkerManager<ActualMarker>(
        hexGeocell = HexGeocellImpl.defaultGeocell()
    )
    // Native library loaded successfully
} catch (e: UnsatisfiedLinkError) {
    // Fallback to standard marker manager
    val standardManager = MarkerManager<ActualMarker>(
        hexGeocell = HexGeocellImpl.defaultGeocell()
    )
}
```

### Memory Leak Prevention

```kotlin
class MarkerActivity : ComponentActivity() {
    private var nativeManager: NativeMarkerManager<ActualMarker>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        nativeManager = NativeMarkerManager(HexGeocellImpl.defaultGeocell())
    }

    override fun onDestroy() {
        super.onDestroy()

        // Critical: cleanup native resources
        nativeManager?.destroy()
        nativeManager = null
    }
}
```

The marker native strategy module provides significant performance improvements for marker-heavy applications, but requires careful resource management and thorough testing due to its experimental nature.
