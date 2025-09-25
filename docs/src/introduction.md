# MapConductor Android SDK Documentation

MapConductor is a unified mapping library that provides a common API for multiple map providers including Google Maps, Mapbox, HERE, and ArcGIS. This documentation covers the public API components available through Maven distribution.

## Overview

The MapConductor SDK allows you to use a single API to work with different map providers. The SDK automatically handles provider-specific implementations while providing a consistent interface for your application.

### Supported Map Providers

- **Google Maps**: `GoogleMapViewStateImpl`
- **Mapbox**: `MapboxViewStateImpl`
- **HERE Maps**: `HereViewStateImpl`
- **ArcGIS**: `ArcGISMapViewStateImpl`

### Core Classes

The SDK provides fundamental geographic classes:

- **GeoPoint**: Represents geographic coordinates (latitude, longitude, altitude)
- **GeoRectBounds**: Defines rectangular geographic areas with southwest/northeast corners

### Key Components

The SDK provides the following core components:

1. **MapViewComponent**: Provider-specific map view components (GoogleMapsView, MapboxMapView, etc.)
2. **Marker**: Point markers with customizable icons and interactions
3. **Circle**: Circular overlays with styling options
4. **Polyline**: Line segments connecting multiple points
5. **Polygon**: Filled shapes with stroke and fill styling
6. **GroundImage**: Image overlays positioned geographically

## Getting Started

To use MapConductor in your project:

### 1. Installation

See the [Installation and Versions](./installation.md) page for complete dependency information and version details.

### 2. SDK-Specific Setup

> **Important**: MapConductor provides a unified API layer on top of existing map SDKs. You must set up each map SDK independently before using MapConductor's integration.

Each map provider requires its own SDK setup with API keys, permissions, and configuration:

- **[Google Maps Setup](./setup/google-maps.md)** - Configure Google Maps SDK with API keys and permissions
- **[Mapbox Setup](./setup/mapbox.md)** - Set up Mapbox access tokens and style configuration
- **[HERE Maps Setup](./setup/here-maps.md)** - Configure HERE SDK with API keys and licensing
- **[ArcGIS Maps Setup](./setup/arcgis.md)** - Set up ArcGIS SDK with API keys and licensing

**Choose the setup guide(s) for your target map provider(s).** You only need to configure the SDKs you plan to use in your application.

### 3. Basic Usage

Create a map view with markers:

```kotlin
@Composable
fun BasicMapExample() {
    // Choose your map provider
    val mapViewState = rememberGoogleMapViewState()

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(
        state = mapViewState,
        onMapClick = { geoPoint ->
            println("Map clicked at: ${geoPoint.latitude}, ${geoPoint.longitude}")
        },
        onMarkerClick = { markerState ->
            println("Marker clicked: ${markerState.extra}")
        }
    ) {
        // Add a marker
        Marker(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = DefaultIcon(label = "SF"),
            extra = "San Francisco marker"
        )

        // Add a circle
        Circle(
            center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            radius = 1000.0,
            strokeColor = Color.Blue,
            fillColor = Color.Blue.copy(alpha = 0.3f)
        )
    }
}
```

### 4. Switching Map Providers

To switch between map providers, simply change the `MapViewState` implementation:

```kotlin
// Google Maps
val googleMapState = GoogleMapViewStateImpl()

// Mapbox
val mapboxState = MapboxViewStateImpl()

// HERE Maps
val hereState = HereViewStateImpl()

// ArcGIS
val arcgisState = ArcGISMapViewStateImpl()
```

The rest of your code remains the same - all components work consistently across providers.

## Features

### Compose Integration
All components are built for Jetpack Compose with:
- Reactive state updates
- Automatic lifecycle management
- Declarative UI patterns

### Event Handling
Rich event system supporting:
- Map clicks and long presses
- Marker interactions (click, drag)
- Overlay interactions
- Animation events

### Customization
Extensive customization options:
- Custom marker icons and styling
- Overlay appearance and behavior
- Animation and interaction settings
- Provider-specific optimizations

## Next Steps

Explore the detailed documentation for each component to learn about their properties, methods, and usage patterns.