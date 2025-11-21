---
title: Introduction
---

# MapConductor Android SDK Documentation

MapConductor is a unified mapping library that provides a common API for multiple map providers including Google Maps, Mapbox, HERE, ArcGIS, and now MapLibre. This documentation covers the public API components available through Maven distribution for **v1.1.0**.

## Overview

The MapConductor SDK allows you to use a single API to work with different map providers. The SDK automatically handles provider-specific implementations while providing a consistent interface for your application.

### Supported Map Providers

- **Google Maps**: `GoogleMapViewStateImpl` / `GoogleMapsView`
- **Mapbox**: `MapboxViewStateImpl` / `MapboxMapView`
- **HERE Maps**: `HereViewStateImpl` / `HereMapView`
- **ArcGIS**: `ArcGISMapViewStateImpl` / `ArcGISMapView`
- **MapLibre**: `MapLibreViewStateImpl` / `MapLibreMapView`

### Core Classes

The SDK provides fundamental geographic classes:

- **GeoPoint**: Represents geographic coordinates (latitude, longitude, altitude)
- **GeoRectBounds**: Defines rectangular geographic areas with southwest/northeast corners
- **MapCameraPosition**: Represents camera position (target, zoom, bearing, tilt, paddings)

### Key Components

The SDK provides the following core components:

1. **Map View Components**: Provider-specific map view components (GoogleMapsView, MapboxMapView, HereMapView, ArcGISMapView, MapLibreMapView)
2. **Marker**: Point markers with customizable icons and interactions
3. **Circle**: Circular overlays with styling options
4. **Polyline**: Line segments connecting multiple points
5. **Polygon**: Filled shapes with stroke and fill styling
6. **GroundImage**: Image overlays positioned geographically (Google Maps only)

## Getting Started

To use MapConductor in your project:

### 1. Installation

See the [Installation and Versions](/installation) page for complete dependency information and version details.

### 2. SDK-Specific Setup

> **Important**: MapConductor provides a unified API layer on top of existing map SDKs. You must set up each map SDK independently before using MapConductor's integration.

Each map provider requires its own SDK setup with API keys, permissions, and configuration:

- **[Google Maps Setup](/setup/google-maps)** – Configure Google Maps SDK with API keys and permissions
- **[Mapbox Setup](/setup/mapbox)** – Set up Mapbox access tokens and style configuration
- **[HERE Maps Setup](/setup/here-maps)** – Configure HERE SDK with API keys and licensing
- **[ArcGIS Maps Setup](/setup/arcgis)** – Set up ArcGIS SDK with API keys and licensing
- **[MapLibre Setup](/setup/maplibre/)** – Configure tiles and style information

You only need to configure the SDKs you plan to use in your application.

### 3. Basic Usage

Create a map view with a marker and a circle:

```kotlin
@Composable
fun BasicMapExample(modifier: Modifier = Modifier) {
    val sanFrancisco = GeoPointImpl.fromLatLong(37.7749, -122.4194)
    val camera = MapCameraPositionImpl(
        position = sanFrancisco,
        zoom = 13.0,
    )
    // Replace with your map SDK provider
    // - Google Maps -> rememberGoogleMapViewState
    // - Mapbox -> rememberMapboxViewState
    // ... and so on
    val mapViewState = rememberGoogleMapViewState(
        cameraPosition = camera,
    )

    // Replace MapView with your chosen map provider
    // - Google Maps -> GoogleMapsView
    // - Mapbox -> MapboxMapView
    // ... and so on
    GoogleMapsView(
        modifier = modifier,
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
            position = sanFrancisco,
            icon = DefaultIcon(label = "SF"),
            extra = "San Francisco marker"
        )

        // Add a circle
        Circle(
            center = sanFrancisco,
            radius = 1000.0,
            strokeColor = Color.Blue,
            fillColor = Color.Blue.copy(alpha = 0.3f)
        )
    }
}
```
![a marker with a circle are drew on the map](/img/introduction/basic-googlemaps-example.jpg)

### 4. Switching Map Providers

To switch between map providers, simply change the `MapViewState` implementation:

```kotlin
// Google Maps
val googleMapState = rememberGoogleMapViewState()

// Mapbox
val mapboxState = rememberMapboxMapViewState()

// HERE Maps
val hereState = rememberHereMapViewState()

// ArcGIS
val arcgisState = rememberArcGISMapViewState()

// MapLibre
val mapLibreState = rememberMapLibreMapViewState()
```

The rest of your code remains the same – all components work consistently across providers.

## What’s New in v1.1.0

Compared to v1.0.0, v1.1.0 includes:

- Unified camera move callbacks (`onCameraMoveStart`, `onCameraMove`, `onCameraMoveEnd`) exposed via map view components and `MapViewContainer`
- Improved `MapViewState` camera handling with explicit `MapCameraPositionImpl`
- Refined marker and overlay controller interfaces (`MarkerCapable`, `MarkerOverlayRenderer`) for better provider-specific implementations
- Example app updates demonstrating camera events, visible region inspection, and advanced marker strategies

## Next Steps

Explore the detailed documentation for:

- [Modules Overview](/modules)
- [Installation and Versions](/installation)
- Provider setup, core components, state classes, and examples.
