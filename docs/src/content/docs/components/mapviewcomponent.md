---
title: "MapViewComponent"
---

MapConductor provides provider-specific map view components that serve as the foundation for displaying maps in your application. Each map provider has its own implementation while maintaining a consistent API interface.

## Provider-Specific Components

MapConductor supports multiple map providers, each with their dedicated component:

### GoogleMapsView
For Google Maps integration:
```kotlin
GoogleMapsView(
    state: GoogleMapViewStateImpl,
    modifier: Modifier = Modifier,
    markerRenderingStrategy: MarkerRenderingStrategy<GoogleMapActualMarker>? = null,
    onMapViewInitialized: OnMapViewInitializedHandler? = null,
    onMapLoaded: OnMapLoadedHandler? = null,
    onMapClick: OnMapEventHandler? = null,
    onMarkerClick: OnMarkerEventHandler? = null,
    onMarkerDragStart: OnMarkerEventHandler? = null,
    onMarkerDrag: OnMarkerEventHandler? = null,
    onMarkerDragEnd: OnMarkerEventHandler? = null,
    onMarkerAnimateStart: OnMarkerEventHandler? = null,
    onMarkerAnimateEnd: OnMarkerEventHandler? = null,
    onCircleClick: OnCircleEventHandler? = null,
    onPolylineClick: OnPolylineEventHandler? = null,
    onPolygonClick: OnPolygonEventHandler? = null,
    onGroundImageClick: OnGroundImageEventHandler? = null,
    content: (@Composable MapViewScope.() -> Unit)? = null
)
```

### MapboxMapView
For Mapbox integration:
```kotlin
MapboxMapView(
    state: MapboxViewStateImpl,
    modifier: Modifier = Modifier,
    markerRenderingStrategy: MarkerRenderingStrategy<MapboxActualMarker>? = null,
    onMapViewInitialized: OnMapViewInitializedHandler? = null,
    onMapLoaded: OnMapLoadedHandler? = null,
    onMapClick: OnMapEventHandler? = null,
    onMarkerClick: OnMarkerEventHandler? = null,
    onMarkerDragStart: OnMarkerEventHandler? = null,
    onMarkerDrag: OnMarkerEventHandler? = null,
    onMarkerDragEnd: OnMarkerEventHandler? = null,
    onMarkerAnimateStart: OnMarkerEventHandler? = null,
    onMarkerAnimateEnd: OnMarkerEventHandler? = null,
    onCircleClick: OnCircleEventHandler? = null,
    onPolylineClick: OnPolylineEventHandler? = null,
    onPolygonClick: OnPolygonEventHandler? = null,
    content: (@Composable MapViewScope.() -> Unit)? = null
)
```

### HereMapView
For HERE Maps integration:
```kotlin
HereMapView(
    state: HereViewStateImpl,
    modifier: Modifier = Modifier,
    markerRenderingStrategy: MarkerRenderingStrategy<HereActualMarker>? = null,
    onMapViewInitialized: OnMapViewInitializedHandler? = null,
    onMapLoaded: OnMapLoadedHandler? = null,
    onMapClick: OnMapEventHandler? = null,
    onMarkerClick: OnMarkerEventHandler? = null,
    onMarkerDragStart: OnMarkerEventHandler? = null,
    onMarkerDrag: OnMarkerEventHandler? = null,
    onMarkerDragEnd: OnMarkerEventHandler? = null,
    onMarkerAnimateStart: OnMarkerEventHandler? = null,
    onMarkerAnimateEnd: OnMarkerEventHandler? = null,
    onCircleClick: OnCircleEventHandler? = null,
    onPolylineClick: OnPolylineEventHandler? = null,
    onPolygonClick: OnPolygonEventHandler? = null,
    content: (@Composable MapViewScope.() -> Unit)? = null
)
```

### ArcGISMapView
For ArcGIS integration:
```kotlin
ArcGISMapView(
    state: ArcGISMapViewStateImpl,
    modifier: Modifier = Modifier,
    markerRenderingStrategy: MarkerRenderingStrategy<ArcGISActualMarker>? = null,
    onMapViewInitialized: OnMapViewInitializedHandler? = null,
    onMapLoaded: OnMapLoadedHandler? = null,
    onMapClick: OnMapEventHandler? = null,
    onMarkerClick: OnMarkerEventHandler? = null,
    onMarkerDragStart: OnMarkerEventHandler? = null,
    onMarkerDrag: OnMarkerEventHandler? = null,
    onMarkerDragEnd: OnMarkerEventHandler? = null,
    onMarkerAnimateStart: OnMarkerEventHandler? = null,
    onMarkerAnimateEnd: OnMarkerEventHandler? = null,
    onCircleClick: OnCircleEventHandler? = null,
    onPolylineClick: OnPolylineEventHandler? = null,
    onPolygonClick: OnPolygonEventHandler? = null,
    content: (@Composable MapViewScope.() -> Unit)? = null
)
```

### MapLibreMapView
For MapLibre integration:
```kotlin
MapLibreMapView(
    state: MapLibreViewStateImpl,
    modifier: Modifier = Modifier,
    markerRenderingStrategy: MarkerRenderingStrategy<MapLibreActualMarker>? = null,
    onMapLoaded: OnMapLoadedHandler? = null,
    onMapClick: OnMapEventHandler? = null,
    onCameraMoveStart: OnCameraMoveHandler? = null,
    onCameraMove: OnCameraMoveHandler? = null,
    onCameraMoveEnd: OnCameraMoveHandler? = null,
    onMarkerClick: OnMarkerEventHandler? = null,
    onMarkerDragStart: OnMarkerEventHandler? = null,
    onMarkerDrag: OnMarkerEventHandler? = null,
    onMarkerDragEnd: OnMarkerEventHandler? = null,
    onMarkerAnimateStart: OnMarkerEventHandler? = null,
    onMarkerAnimateEnd: OnMarkerEventHandler? = null,
    onPolylineClick: OnPolylineEventHandler? = null,
    onCircleClick: OnCircleEventHandler? = null,
    onPolygonClick: OnPolygonEventHandler? = null,
    content: (@Composable MapLibreMapViewScope.() -> Unit)? = null
)
```

## Common Parameters

All map view components share the following parameters:

### Core Parameters

- **`modifier`**: Compose modifier for styling and layout
- **`state`**: Provider-specific map view state implementation
- **`content`**: Composable content containing map overlays (markers, circles, etc.)

### Event Handlers

- **`onMapViewInitialized`**: Called when the map view is first initialized
- **`onMapLoaded`**: Called when the map has finished loading
- **`onMapClick`**: Called when the user taps on the map
- **Camera Events**: `onCameraMoveStart`, `onCameraMove`, `onCameraMoveEnd`
- **Marker Events**: `onMarkerClick`, `onMarkerDragStart`, `onMarkerDrag`, `onMarkerDragEnd`, `onMarkerAnimateStart`, `onMarkerAnimateEnd`
- **Overlay Events**: `onCircleClick`, `onPolylineClick`, `onPolygonClick`, `onGroundImageClick`

### Advanced Parameters

- **`markerRenderingStrategy`**: Custom marker rendering strategy for performance optimization

## Usage Examples

### Basic Google Maps Implementation

```kotlin
@Composable
fun GoogleMapsExample() {
    val mapViewState = rememberGoogleMapViewState()

    GoogleMapsView(
        state = mapViewState,
        onMapClick = { geoPoint ->
            println("Map clicked at: ${geoPoint.latitude}, ${geoPoint.longitude}")
        },
        onMarkerClick = { markerState ->
            println("Marker clicked: ${markerState.extra}")
        }
    ) {
        Marker(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = DefaultIcon(label = "SF"),
            extra = "San Francisco"
        )

        Circle(
            center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            radius = 1000.0,
            strokeColor = Color.Blue,
            fillColor = Color.Blue.copy(alpha = 0.3f)
        )
    }
}
```
![](/img/introduction/basic-googlemaps-example.jpg)

### Basic Mapbox Implementation

```kotlin
@Composable
fun MapboxExample() {
    val mapViewState = remember { MapboxViewStateImpl() }

    MapboxMapView(
        state = mapViewState,
        onMapClick = { geoPoint ->
            println("Map clicked at: ${geoPoint.latitude}, ${geoPoint.longitude}")
        }
    ) {
        Marker(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = DefaultIcon(label = "MB"),
            extra = "Mapbox marker"
        )

        Polyline(
            points = listOf(
                GeoPointImpl.fromLatLong(37.7749, -122.4194),
                GeoPointImpl.fromLatLong(37.7849, -122.4094),
                GeoPointImpl.fromLatLong(37.7949, -122.3994)
            ),
            strokeColor = Color.Red,
            strokeWidth = 3.dp
        )
    }
}
```
![](/img/introduction/basic-mapbox-example.jpg)

### Provider-Agnostic Pattern

While each provider requires its specific component, you can create provider-agnostic content:

```kotlin
@Composable
fun MapContent() {
    Marker(
        position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        icon = DefaultIcon(label = "Point"),
        extra = "Common marker"
    )

    Circle(
        center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        radius = 500.0,
        strokeColor = Color.Green,
        fillColor = Color.Green.copy(alpha = 0.2f)
    )
}

@Composable
fun GoogleMapsScreen() {
    val state = rememberGoogleMapViewState()
    GoogleMapsView(state = state) {
        MapContent() // Reusable content
    }
}

@Composable
fun MapboxScreen() {
    val state = remember { MapboxViewStateImpl() }
    MapboxMapView(state = state) {
        MapContent() // Same content, different provider
    }
}
```

### Advanced Event Handling

```kotlin
@Composable
fun AdvancedMapExample() {
    val mapViewState = rememberGoogleMapViewState()
    var selectedMarker by remember { mutableStateOf<MarkerState?>(null) }

    GoogleMapsView(
        state = mapViewState,
        onMapViewInitialized = {
            println("Map initialized successfully")
        },
        onMapLoaded = {
            println("Map loaded and ready")
        },
        onMapClick = { geoPoint ->
            selectedMarker = null // Deselect on map click
        },
        onMarkerClick = { markerState ->
            selectedMarker = markerState
        },
        onMarkerDragStart = { markerState ->
            println("Started dragging marker: ${markerState.id}")
        },
        onMarkerDrag = { markerState ->
            println("Dragging marker to: ${markerState.position}")
        },
        onMarkerDragEnd = { markerState ->
            println("Finished dragging marker: ${markerState.id}")
        }
    ) {
        // Map content with interactive markers
        Marker(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = DefaultIcon(
                fillColor = if (selectedMarker?.id == "marker1") Color.Yellow else Color.Blue,
                label = "1"
            ),
            draggable = true,
            extra = "Draggable marker 1"
        )

        Marker(
            position = GeoPointImpl.fromLatLong(37.7849, -122.4094),
            icon = DefaultIcon(
                fillColor = if (selectedMarker?.id == "marker2") Color.Yellow else Color.Red,
                label = "2"
            ),
            extra = "Clickable marker 2"
        )

        // Show info for selected marker
        selectedMarker?.let { marker ->
            // You could show an InfoBubble or other UI here
        }
    }
}
```

## Provider Differences

While the API is consistent across providers, there are some differences in:

### Supported Features
- **GroundImage**: Currently supported on Google Maps and ArcGIS
- **Marker Animation**: Available on Google Maps and Mapbox
- **Custom Styling**: Each provider has different map style options

### Performance Characteristics
- **Google Maps**: Excellent for general use, good marker performance
- **Mapbox**: Great for custom styling and large datasets
- **HERE Maps**: Optimized for location services integration
- **ArcGIS**: Best for GIS and enterprise applications

### Platform Integration
Each provider may have different requirements for API keys, permissions, and platform setup. Refer to the initialization documentation for provider-specific setup instructions.

## Best Practices

1. **Choose the Right Provider**: Select based on your app's specific needs (styling, performance, features)
2. **Consistent State Management**: Use the same state patterns regardless of provider
3. **Reusable Content**: Create provider-agnostic composable content when possible
4. **Event Handling**: Implement comprehensive event handling for better user experience
5. **Error Handling**: Always handle initialization failures and provide fallback UI
6. **Performance**: Consider using custom rendering strategies for large numbers of markers
7. **Testing**: Test your application with multiple providers to ensure compatibility
