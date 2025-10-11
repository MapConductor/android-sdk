# Marker Strategy (Experimental)

The `mapconductor-marker-strategy` module provides advanced marker rendering strategies for optimizing performance and user experience with large datasets. This experimental module offers multiple rendering approaches tailored to different use cases and performance requirements.

> **⚠️ Experimental Module**: This module is experimental and APIs may change. Use in production with caution.

## Overview

The marker strategy module provides sophisticated rendering strategies that go beyond basic marker display:
- **Viewport-Based Rendering**: Only render markers visible in the current viewport
- **Dynamic Add/Remove**: Efficiently manage markers as the viewport changes
- **Spatial Optimization**: Advanced spatial indexing for large datasets
- **Remote Data Integration**: Support for server-side marker data
- **Clustering Support**: Group nearby markers for better performance

## Installation

Add the marker strategy module to your `build.gradle`:

```kotlin
dependencies {
    implementation "com.mapconductor:marker-strategy"

    // Required: Core module
    implementation "com.mapconductor:mapconductor-bom:$version"
    // Required: Core module
    implementation "com.mapconductor:core"

    // Choose your map provider
    implementation "com.mapconductor:for-googlemaps"
}
```

## Core Strategies

### DefaultMarkerStrategy

Optimal for Google Maps and ArcGIS providers that handle add/remove operations efficiently:

```kotlin
import com.mapconductor.marker.strategy.DefaultMarkerStrategy

val defaultStrategy = DefaultMarkerStrategy<GoogleMapActualMarker>(
    expandMargin = 0.2,  // 20% viewport expansion
    semaphore = Semaphore(1),
    geocell = HexGeocellImpl.defaultGeocell()
)
```

#### Key Features
- **Dynamic Add/Remove**: Adds markers entering viewport, removes markers leaving viewport
- **Viewport Expansion**: Preloads markers slightly outside visible area
- **Memory Efficient**: Only keeps visible markers in memory
- **Smooth Scrolling**: Reduces pop-in/pop-out during map movement

### SimpleMarkerStrategy

Lightweight strategy for smaller datasets or providers with different performance characteristics:

```kotlin
import com.mapconductor.marker.strategy.SimpleMarkerStrategy

val simpleStrategy = SimpleMarkerStrategy<MapboxActualMarker>(
    expandMargin = 0.15,
    geocell = HexGeocellImpl.defaultGeocell()
)
```

#### Key Features
- **Simplified Logic**: Less complex viewport management
- **Lower Overhead**: Minimal computational overhead
- **Good for Mapbox**: Optimized for providers that prefer simpler marker management

### SpatialMarkerStrategy

Advanced strategy with spatial clustering and optimization:

```kotlin
import com.mapconductor.marker.strategy.SpatialMarkerStrategy

val spatialStrategy = SpatialMarkerStrategy<HereActualMarker>(
    clusteringEnabled = true,
    clusterRadius = 100.0,      // 100-meter clustering radius
    maxMarkersPerCluster = 10,  // Maximum markers in a cluster
    geocell = HexGeocellImpl.defaultGeocell()
)
```

#### Key Features
- **Spatial Clustering**: Groups nearby markers into clusters
- **Density Management**: Reduces visual clutter in dense areas
- **Performance Scaling**: Handles very large datasets efficiently
- **Customizable Clustering**: Configurable clustering parameters

## Basic Usage

### Setting Up Default Strategy

```kotlin
@Composable
fun DefaultStrategyExample() {
    val mapViewState = rememberGoogleMapViewState()

    val markerStrategy = remember {
        DefaultMarkerStrategy<GoogleMapActualMarker>(
            expandMargin = 0.2
        )
    }

    // Configure strategy with map controller
    LaunchedEffect(mapViewState) {
        // Strategy setup depends on map provider implementation
        // This is typically handled by the map provider's marker controller
    }

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
    GoogleMapsView(state = mapViewState) {
        // Markers are managed by the strategy
        // Add markers programmatically through the strategy
    }
}
```

### Adding Markers to Strategy

```kotlin
@Composable
fun StrategyMarkerManagement() {
    val markerStrategy = remember {
        DefaultMarkerStrategy<GoogleMapActualMarker>()
    }

    LaunchedEffect(Unit) {
        // Add markers to the strategy's manager
        val markers = loadMarkerData() // Your marker data

        markers.forEach { markerData ->
            val entity = MarkerEntity(
                state = MarkerState(
                    id = markerData.id,
                    position = markerData.position,
                    icon = DefaultIcon(fillColor = markerData.color)
                )
            )

            // Register with strategy's marker manager
            markerStrategy.markerManager.registerEntity(entity)
        }
    }

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
    GoogleMapsView(state = mapViewState) {
        // Strategy handles marker rendering automatically
    }
}
```

## Advanced Usage

### Dynamic Marker Loading

```kotlin
@Composable
fun DynamicLoadingExample() {
    val mapViewState = rememberGoogleMapViewState()
    var currentBounds by remember { mutableStateOf<GeoRectBounds?>(null) }
    var loadedMarkers by remember { mutableStateOf<Set<String>>(emptySet()) }

    val strategy = remember {
        DefaultMarkerStrategy<GoogleMapActualMarker>(
            expandMargin = 0.3  // Larger margin for preloading
        )
    }

    // Load markers dynamically based on viewport
    LaunchedEffect(currentBounds) {
        currentBounds?.let { bounds ->
            val newMarkers = fetchMarkersForBounds(bounds) // Your API call

            newMarkers.forEach { markerData ->
                if (markerData.id !in loadedMarkers) {
                    val entity = MarkerEntity(
                        state = MarkerState(
                            id = markerData.id,
                            position = markerData.position,
                            icon = DefaultIcon()
                        )
                    )

                    strategy.markerManager.registerEntity(entity)
                    loadedMarkers = loadedMarkers + markerData.id
                }
            }
        }
    }

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
    GoogleMapsView(
        state = mapViewState,
        onCameraMove = { cameraPosition ->
            currentBounds = cameraPosition.visibleRegion?.bounds
        }
    ) {
        // Strategy manages dynamic marker loading
    }
}
```

### Clustering Strategy

```kotlin
@Composable
fun ClusteringStrategyExample() {
    val clusterStrategy = remember {
        SpatialMarkerStrategy<GoogleMapActualMarker>(
            clusteringEnabled = true,
            clusterRadius = 50.0,       // 50-meter clustering
            maxMarkersPerCluster = 5,   // Small clusters
            geocell = HexGeocellImpl(
                baseHexSideLength = 100.0,  // Fine-grained spatial index
                zoom = 18.0
            )
        )
    }

    // Add dense marker dataset
    LaunchedEffect(Unit) {
        val denseMarkers = generateDenseMarkerSet(
            center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            count = 500,
            radius = 200.0  // 200-meter radius
        )

        denseMarkers.forEach { markerData ->
            val entity = MarkerEntity(
                state = MarkerState(
                    id = markerData.id,
                    position = markerData.position,
                    icon = DefaultIcon(
                        fillColor = markerData.category.color,
                        scale = 0.8f
                    )
                )
            )

            clusterStrategy.markerManager.registerEntity(entity)
        }
    }

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
    GoogleMapsView(state = mapViewState) {
        // Clustering strategy automatically groups nearby markers
    }
}
```

### Remote Spatial Strategy

```kotlin
@Composable
fun RemoteSpatialExample() {
    val remoteStrategy = remember {
        RemoteSpatialMarkerStrategy<GoogleMapActualMarker>(
            apiEndpoint = "https://api.example.com/markers",
            cacheTimeout = 300000, // 5 minutes
            maxConcurrentRequests = 3
        )
    }

    // Remote strategy loads markers from server based on viewport
    LaunchedEffect(mapViewState) {
        // Strategy automatically manages server requests
        // No manual marker loading required
    }

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
    GoogleMapsView(state = mapViewState) {
        // Remote strategy handles all marker loading from server
    }
}
```

## Strategy Comparison

### Performance Characteristics

| Strategy | Best For | Memory Usage | Network | Complexity |
|----------|----------|--------------|---------|------------|
| DefaultMarkerStrategy | Google Maps, ArcGIS | Medium | None | Medium |
| SimpleMarkerStrategy | Mapbox, HERE | Low | None | Low |
| SpatialMarkerStrategy | Large datasets | High | None | High |
| RemoteSpatialMarkerStrategy | Server-side data | Low | High | High |

### Use Case Guidelines

#### Choose DefaultMarkerStrategy when:
- Using Google Maps or ArcGIS
- Have moderate marker counts (1,000-50,000)
- Want smooth viewport-based rendering
- Markers are loaded locally

#### Choose SimpleMarkerStrategy when:
- Using Mapbox or HERE Maps
- Have smaller marker counts (<10,000)
- Want minimal overhead
- Simple rendering requirements

#### Choose SpatialMarkerStrategy when:
- Have very large marker datasets (50,000+)
- Need clustering functionality
- Want advanced spatial optimization
- Can afford higher memory usage

#### Choose RemoteSpatialMarkerStrategy when:
- Markers are stored server-side
- Want on-demand loading
- Have network connectivity
- Need to minimize app memory usage

## Custom Strategy Development

### Extending AbstractViewportStrategy

```kotlin
class CustomMarkerStrategy<ActualMarker>(
    semaphore: Semaphore = Semaphore(1),
    geocell: HexGeocell = HexGeocellImpl.defaultGeocell()
) : AbstractViewportStrategy<ActualMarker>(semaphore, geocell) {

    override suspend fun onCameraChanged(
        cameraPosition: MapCameraPositionImpl,
        renderer: MarkerOverlayRenderer<ActualMarker>
    ) {
        semaphore.withPermit {
            // Custom rendering logic
            val visibleBounds = cameraPosition.visibleRegion?.bounds ?: return

            // Your custom marker management here
            val markersToShow = determineVisibleMarkers(visibleBounds)
            val markersToHide = determineHiddenMarkers(visibleBounds)

            // Use renderer to update display
            if (markersToHide.isNotEmpty()) {
                renderer.onRemove(markersToHide)
            }

            if (markersToShow.isNotEmpty()) {
                val addParams = markersToShow.map { entity ->
                    object : MarkerOverlayRenderer.AddParams {
                        override val state: MarkerState = entity.state
                        override val bitmapIcon: BitmapIcon = entity.state.icon?.toBitmapIcon() ?: defaultIcon
                    }
                }
                renderer.onAdd(addParams)
            }

            renderer.onPostProcess()
        }
    }

    private fun determineVisibleMarkers(bounds: GeoRectBounds): List<MarkerEntity<ActualMarker>> {
        // Your custom logic to determine which markers should be visible
        return markerManager.findMarkersInBounds(bounds)
    }

    private fun determineHiddenMarkers(bounds: GeoRectBounds): List<MarkerEntity<ActualMarker>> {
        // Your custom logic to determine which markers should be hidden
        return markerManager.allEntities().filter { entity ->
            entity.isRendered && !bounds.contains(entity.state.position)
        }
    }
}
```

## Performance Optimization

### Strategy Configuration

```kotlin
// High-performance configuration
val performanceStrategy = DefaultMarkerStrategy<ActualMarker>(
    expandMargin = 0.1,  // Smaller margin for less preloading
    semaphore = Semaphore(2),  // Allow some parallelism
    geocell = HexGeocellImpl(
        baseHexSideLength = 1000.0,  // Larger cells for better performance
        zoom = 15.0  // Lower resolution for speed
    )
)

// Memory-optimized configuration
val memoryStrategy = SimpleMarkerStrategy<ActualMarker>(
    expandMargin = 0.05,  // Minimal expansion
    geocell = HexGeocellImpl(
        baseHexSideLength = 2000.0,  // Very large cells
        zoom = 12.0  // Lower resolution
    )
)
```

### Monitoring Performance

```kotlin
@Composable
fun StrategyPerformanceMonitoring() {
    val strategy = remember { DefaultMarkerStrategy<GoogleMapActualMarker>() }
    var performanceStats by remember { mutableStateOf<String>("") }

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)

            val stats = strategy.markerManager.getMemoryStats()
            performanceStats = buildString {
                appendLine("Entities: ${stats.entityCount}")
                appendLine("Memory: ${stats.estimatedMemoryKB} KB")
                appendLine("Spatial Index: ${stats.hasSpatialIndex}")
            }
        }
    }

    Column {
        Text(performanceStats)

        // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
        GoogleMapsView(state = mapViewState) {
            // Strategy-managed markers
        }
    }
}
```

## Best Practices

1. **Strategy Selection**: Choose strategy based on your specific use case and map provider
2. **Viewport Margins**: Balance preloading (larger margin) vs performance (smaller margin)
3. **Spatial Configuration**: Tune geocell parameters for your data density
4. **Memory Monitoring**: Monitor memory usage in production, especially with large datasets
5. **Testing**: Test with realistic data volumes and usage patterns
6. **Fallback**: Have simpler strategy as fallback for performance issues

## Common Pitfalls

1. **Over-Engineering**: Don't use complex strategies for simple marker scenarios
2. **Memory Leaks**: Ensure proper cleanup of strategy resources
3. **Provider Mismatch**: Using wrong strategy for your map provider can hurt performance
4. **Excessive Preloading**: Large expand margins can cause memory pressure
5. **Thread Safety**: Strategies handle concurrency, but be careful with external modifications

## Migration Guide

### From Basic Marker Management

```kotlin
// Before: Basic marker management
@Composable
fun BasicMarkers() {
    MapView(state = mapViewState) {
        markers.forEach { markerData ->
            Marker(
                position = markerData.position,
                icon = DefaultIcon()
            )
        }
    }
}

// After: Strategy-based management
@Composable
fun StrategyMarkers() {
    val strategy = remember { DefaultMarkerStrategy<ActualMarker>() }

    LaunchedEffect(markers) {
        markers.forEach { markerData ->
            val entity = MarkerEntity(
                state = MarkerState(
                    id = markerData.id,
                    position = markerData.position,
                    icon = DefaultIcon()
                )
            )
            strategy.markerManager.registerEntity(entity)
        }
    }

    MapView(state = mapViewState) {
        // Strategy handles all marker rendering
    }
}
```

The marker strategy module provides sophisticated marker management capabilities for applications requiring high performance with large datasets or complex rendering requirements.
