---
title: "MapViewHolder"
---

`MapViewHolder` provides access to the native map SDK instances for advanced use cases where MapConductor's unified API doesn't cover specific provider functionality. While MapConductor provides a common interface, it doesn't completely wrap all native features, and MapViewHolder bridges this gap.

## Overview

MapConductor aims to provide a unified API across map providers, but complete feature parity is not always possible. MapViewHolder allows developers to access the underlying native map instances when they need provider-specific functionality.

```kotlin
interface MapViewHolder<ActualMapViewType, ActualMapType> {
    val mapView: ActualMapViewType
    val map: ActualMapType
}
```

## Accessing MapViewHolder

Each map provider's `MapViewState` implementation provides access to its specific `MapViewHolder`:

```kotlin
// Google Maps
val googleMapState = GoogleMapViewStateImpl()
val googleHolder: GoogleMapViewHolder? = googleMapState.getMapViewHolder()

// Mapbox
val mapboxState = MapboxViewStateImpl()
val mapboxHolder: MapboxMapViewHolder? = mapboxState.getMapViewHolder()

// HERE Maps
val hereState = HereViewStateImpl()
val hereHolder: HereViewHolder? = hereState.getMapViewHolder()

// ArcGIS
val arcgisState = ArcGISMapViewStateImpl()
val arcgisHolder: ArcGISMapViewHolder? = arcgisState.getMapViewHolder()
```

## Provider-Specific Implementations

### Google Maps

```kotlin
typealias GoogleMapViewHolder = MapViewHolder<MapView, GoogleMap>

// Access native Google Maps APIs
googleHolder?.let { holder ->
    val nativeMapView: MapView = holder.mapView
    val googleMap: GoogleMap = holder.map

    // Use Google Maps specific features
    googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.style_json))
    googleMap.setOnPoiClickListener { poi ->
        // Handle Points of Interest clicks
    }

    nativeMapView.onResume()
    nativeMapView.onPause()
}
```

### Mapbox

```kotlin
typealias MapboxMapViewHolder = MapViewHolder<MapView, MapboxMap>

// Access native Mapbox APIs
mapboxHolder?.let { holder ->
    val mapboxMapView: MapView = holder.mapView
    val mapboxMap: MapboxMap = holder.map

    // Use Mapbox specific features
    mapboxMap.getStyle { style ->
        style.addSource(GeoJsonSource("custom-source", geoJsonData))
        style.addLayer(FillLayer("custom-layer", "custom-source"))
    }

    // Access Mapbox plugins
    val locationComponent = mapboxMap.locationComponent
    locationComponent.activateLocationComponent(context)
}
```

### HERE Maps

```kotlin
typealias HereViewHolder = MapViewHolder<MapView, MapScene>

// Access native HERE SDK APIs
hereHolder?.let { holder ->
    val hereMapView: MapView = holder.mapView
    val mapScene: MapScene = holder.map

    // Use HERE specific features
    val searchEngine = SearchEngine()
    val textQuery = TextQuery("coffee shops", geoCoordinates)

    searchEngine.search(textQuery) { searchError, searchResults ->
        // Handle HERE search results
    }

    // HERE routing
    val routingEngine = RoutingEngine()
    // Configure and use routing
}
```

### ArcGIS

```kotlin
typealias ArcGISMapViewHolder = MapViewHolder<WrapSceneView, SceneView>

// Access native ArcGIS APIs
arcgisHolder?.let { holder ->
    val wrapSceneView: WrapSceneView = holder.mapView
    val sceneView: SceneView = holder.map

    // Use ArcGIS specific features
    val portal = Portal("https://www.arcgis.com/")
    val portalItem = PortalItem(portal, "your-webmap-id")
    val webMap = ArcGISMap(portalItem)

    sceneView.map = webMap

    // ArcGIS analysis tools
    val serviceArea = ServiceAreaTask("https://route-api.arcgis.com/...")
    // Configure and use analysis
}
```

## Common Use Cases

### Custom Styling

```kotlin
@Composable
fun CustomStyledMap() {
    val mapViewState = rememberGoogleMapViewState()

    LaunchedEffect(mapViewState) {
        // Wait for map initialization
        delay(1000)

        mapViewState.getMapViewHolder()?.let { holder ->
            val googleMap = holder.map

            // Apply custom map style
            val styleJson = loadStyleFromAssets(context, "custom_style.json")
            googleMap.setMapStyle(MapStyleOptions(styleJson))

            // Configure map UI
            googleMap.uiSettings.isMyLocationButtonEnabled = false
            googleMap.uiSettings.isCompassEnabled = true
        }
    }

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
    GoogleMapsView(state = mapViewState) {
        // Your MapConductor components
    }
}
```

### Advanced Analytics

```kotlin
@Composable
fun AnalyticsIntegration() {
    val mapViewState = remember { MapboxViewStateImpl() }

    LaunchedEffect(mapViewState) {
        mapViewState.getMapViewHolder()?.let { holder ->
            val mapboxMap = holder.map

            // Track custom map events
            mapboxMap.addOnMapClickListener { point ->
                Analytics.track("map_click", mapOf(
                    "lat" to point.latitude,
                    "lng" to point.longitude,
                    "zoom" to mapboxMap.cameraPosition.zoom
                ))
                true
            }

            // Track style changes
            mapboxMap.addOnStyleLoadedListener {
                Analytics.track("style_loaded", mapOf(
                    "style_id" to mapboxMap.style?.styleURI
                ))
            }
        }
    }

    MapboxMapView(state = mapViewState) {
        // Your MapConductor components
    }
}
```

### Performance Optimization

```kotlin
@Composable
fun PerformanceOptimizedMap() {
    val mapViewState = remember { HereViewStateImpl() }

    LaunchedEffect(mapViewState) {
        mapViewState.getMapViewHolder()?.let { holder ->
            val mapScene = holder.map

            // HERE-specific performance settings
            mapScene.setLayerVisibility(MapScene.Layers.TRAFFIC_FLOW, VisibilityState.VISIBLE)

            // Optimize for specific use case
            val mapSettings = mapScene.mapSettings
            mapSettings.isTiltGesturesEnabled = false
            mapSettings.isRotateGesturesEnabled = false

            // Custom level of detail settings
            mapScene.setLevelOfDetail(LevelOfDetail.HIGH)
        }
    }

    HereMapView(state = mapViewState) {
        // Your MapConductor components
    }
}
```

### Integration with Third-Party Services

```kotlin
@Composable
fun ThirdPartyIntegration() {
    val mapViewState = rememberArcGISMapViewState()

    LaunchedEffect(mapViewState) {
        mapViewState.getMapViewHolder()?.let { holder ->
            val sceneView = holder.map

            // Integrate with ArcGIS Online services
            val featureTable = ServiceFeatureTable("https://services.arcgis.com/...")
            val featureLayer = FeatureLayer(featureTable)

            sceneView.map.basemap.baseLayers.add(featureLayer)

            // Query features
            val queryParams = QueryParameters().apply {
                whereClause = "STATE_NAME = 'California'"
            }

            featureTable.queryFeaturesAsync(queryParams).addDoneListener {
                val results = it.result
                // Process feature query results
            }
        }
    }

    ArcGISMapView(state = mapViewState) {
        // Your MapConductor components
    }
}
```

## Lifecycle Management

### Map Lifecycle Events

```kotlin
@Composable
fun LifecycleAwareMap() {
    val mapViewState = rememberGoogleMapViewState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            mapViewState.getMapViewHolder()?.let { holder ->
                val mapView = holder.mapView

                when (event) {
                    Lifecycle.Event.ON_RESUME -> mapView.onResume()
                    Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                    Lifecycle.Event.ON_START -> mapView.onStart()
                    Lifecycle.Event.ON_STOP -> mapView.onStop()
                    Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                    else -> { }
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    GoogleMapsView(state = mapViewState) {
        // Your MapConductor components
    }
}
```

## Error Handling and Safety

### Null Safety

```kotlin
fun accessNativeMap(mapViewState: GoogleMapViewStateImpl) {
    val holder = mapViewState.getMapViewHolder()

    if (holder != null) {
        // Safe to access native APIs
        val googleMap = holder.map
        val mapView = holder.mapView

        // Use native features
        googleMap.setOnMarkerClickListener { marker ->
            // Handle marker click
            true
        }
    } else {
        // Map not yet initialized or unavailable
        Log.w("MapAccess", "MapViewHolder not available")
    }
}
```

### Initialization Timing

```kotlin
@Composable
fun SafeNativeAccess() {
    val mapViewState = remember { MapboxViewStateImpl() }
    var isMapReady by remember { mutableStateOf(false) }

    LaunchedEffect(mapViewState) {
        // Poll until map is ready
        while (!isMapReady) {
            delay(100)
            isMapReady = mapViewState.getMapViewHolder() != null
        }

        // Now safe to use native APIs
        mapViewState.getMapViewHolder()?.let { holder ->
            val mapboxMap = holder.map
            // Configure native features
        }
    }

    MapboxMapView(state = mapViewState) {
        // Your MapConductor components
    }
}
```

## Best Practices

1. **Initialization Check**: Always check if `getMapViewHolder()` returns non-null before accessing native APIs
2. **Lifecycle Awareness**: Properly handle map lifecycle events when using native APIs
3. **Error Handling**: Wrap native API calls in try-catch blocks as provider APIs may throw exceptions
4. **Documentation**: Refer to each provider's official documentation for native API usage
5. **Testing**: Test thoroughly across all target map providers when using native APIs
6. **Fallback**: Provide fallback behavior when native features are unavailable
7. **Version Compatibility**: Ensure native API usage is compatible with the SDK versions you're targeting

## Limitations and Considerations

1. **Platform Dependency**: Native API usage ties your code to specific map providers
2. **Maintenance Overhead**: Provider API changes may require updates to native API usage
3. **Testing Complexity**: More complex testing required to cover provider-specific code paths
4. **Feature Parity**: Not all providers support equivalent native features
5. **MapConductor Integration**: Native changes may not integrate with MapConductor's state management

MapViewHolder is a powerful escape hatch for advanced use cases, but should be used judiciously to maintain the benefits of MapConductor's unified API approach.