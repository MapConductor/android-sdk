# MapConductor Android SDK

A unified mapping library that provides a common API for multiple map providers including Google Maps, Mapbox, HERE, and ArcGIS. Write once, deploy across all major mapping platforms.

## Features

- **🗺️ Multi-Provider Support**: Seamlessly switch between Google Maps, Mapbox, HERE, and ArcGIS with a single API
- **🎯 Unified Interface**: Common abstractions for markers, circles, polylines, polygons, and ground overlays
- **⚡ High Performance**: Spatial indexing with hexagonal cells for efficient marker clustering
- **🔄 Reactive State**: Built on Kotlin StateFlow for reactive UI updates
- **🎨 Jetpack Compose**: Modern Android UI toolkit integration
- **📱 Production Ready**: Used in real-world applications with comprehensive testing

## Architecture

### Module Structure

- **`mapconductor-core`**: Core abstractions and shared functionality
- **`mapconductor-for-googlemaps`**: Google Maps implementation
- **`mapconductor-for-mapbox`**: Mapbox implementation
- **`mapconductor-for-here`**: HERE Maps implementation
- **`mapconductor-for-arcgis`**: ArcGIS implementation
- **`mapconductor-icons`**: Reusable marker icon components
- **`example-app`**: Comprehensive demo application

### Key Components

- **MapViewController**: Abstract controller interface for all map providers
- **MapViewBase**: Generic Compose-based map view component
- **Overlay Managers**: Separate managers for markers, circles, polylines, and polygons
- **Projection Utilities**: WebMercator and WGS84 coordinate transformations
- **HexGeocell**: Spatial indexing system for performance optimization

## Quick Start

### 1. Setup

Clone the repository:
```bash
git clone https://github.com/MapConductor/android-sdk.git
```

Add `secrets.properties` to the project root from https://github.com/MapConductor/map-sdk-credentials/

### 2. Basic Usage

```kotlin
@Composable
fun MyMapScreen() {
    GoogleMapView(
        modifier = Modifier.fillMaxSize(),
        onMapReady = { controller ->
            // Add markers, circles, polylines, etc.
        }
    ) { mapState, controller ->
        // Your map content here
    }
}
```

### 3. Switch Map Providers

Simply change the map view component:
```kotlin
// Google Maps
GoogleMapView { /* ... */ }

// Mapbox
MapboxMapView { /* ... */ }

// HERE Maps  
HereMapView { /* ... */ }

// ArcGIS
ArcGISMapView { /* ... */ }
```

## Development

### Building
```bash
./gradlew build
```

### Code Style
This project follows KtLint conventions:
```bash
./gradlew allLintChecks
```


## Feature Implementation Status

|                 | Google Maps | Mapbox   | Here     | ArcGIS         |
|-----------------|-------------|----------|----------|----------------|
| Map             | &#x2611;    | &#x2611; | &#x2611; | &#x2611;       |
| Marker          | &#x2611;    | &#x2611; | &#x2611; | &#x2611;       |
| Circle          | &#x2611;    | &#x2611; | &#x2610; | &#x2610; (wip) |
| Polyline        | &#x2611;    | &#x2611; | &#x2611; | &#x2611;       |
| Polygon         | &#x2610;    | &#x2610; | &#x2610; | &#x2610;       |
| GroundImage     | &#x2610;    | N/A      | N/A      | N/A            |
| RasterTileLayer | &#x2610;    | &#x2610; | &#x2610; | &#x2610;       |
| VectorTileLayer | &#x2610;    | &#x2610; | &#x2610; | &#x2610;       |
