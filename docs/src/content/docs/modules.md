---
title: Modules Overview
---

MapConductor is split into multiple Gradle modules so that you can depend only on what you need. This page summarizes each `mapconductor-xxx` module in v1.1.0.

## Core and BOM

### `mapconductor-bom`

Bill of Materials for all MapConductor artifacts.

- Aligns versions across all modules
- Recommended for all projects

```kotlin
implementation(platform("com.mapconductor:mapconductor-bom:1.1.1"))
```

### `mapconductor-core`

Core runtime and shared abstractions:

- Geometry types: `GeoPoint`, `GeoRectBounds`
- Camera types: `MapCameraPosition`, `VisibleRegion`
- Map abstractions: `MapViewState`, `MapViewController`, `MapViewBase`
- Overlay primitives: `MarkerState`, `PolylineState`, `PolygonState`, `CircleState`, `GroundImageState`

All other modules build on top of `mapconductor-core`.

## Map Provider Integrations

Provider-specific modules implement the unified APIs for each SDK:

### `mapconductor-for-googlemaps`

- `GoogleMapView` composable
- `GoogleMapViewStateImpl`
- Google Maps–specific overlay controllers

### `mapconductor-for-mapbox`

- `MapboxMapView` composable
- `MapboxViewStateImpl`

### `mapconductor-for-here`

- `HereMapView` composable
- `HereViewStateImpl`

### `mapconductor-for-arcgis`

- `ArcGISMapView` composable
- `ArcGISMapViewStateImpl`

### `mapconductor-for-maplibre`

- `MapLibreMapView` composable
- `MapLibreViewStateImpl`

Each provider module:

- Implements `MapViewState` and controller bindings
- Maps provider-specific camera + visible region into `MapCameraPosition`
- Exposes overlay controllers (marker, polyline, polygon, circle, ground image where supported)

## Experimental / Utility Modules

### `mapconductor-icons`

Composable marker icons implemented in pure Compose:

- `CircleIcon`, `FlagIcon`
- Info bubble styles (round, tail, etc.)

Useful when you want consistent marker visuals across providers without relying on provider-specific drawables.

### `mapconductor-marker-strategy`

High-level marker rendering strategies:

- Spatial / clustering-like strategies
- Abstractions for remote-driven marker sets

Designed to work with any provider module via shared marker interfaces.

### `mapconductor-marker-native-strategy`

Native-accelerated strategies for very large marker sets:

- Focus on performance at scale
- Typically combined with `mapconductor-marker-strategy`

## Example Applications

### `example-app`

Showcase application demonstrating:

- Basic map usage and provider switching
- Camera handling and visible region (`VisibleRegionPage`, `ZoomCalibrationPage`)
- Polylines, polygons, circles, and ground images
- Info bubbles and custom marker icons

### `simple-map-app`

Minimal example for quick integration tests and debugging.

## How to Choose Modules

Typical configurations:

```kotlin
// Minimal: Google Maps only
implementation(platform("com.mapconductor:mapconductor-bom:1.1.1"))
implementation("com.mapconductor:core")
implementation("com.mapconductor:for-googlemaps")
```
```
// Multi-provider with icons and strategies
implementation(platform("com.mapconductor:mapconductor-bom:1.1.1"))
implementation("com.mapconductor:core")
implementation("com.mapconductor:for-googlemaps")
implementation("com.mapconductor:for-mapbox")
implementation("com.mapconductor:icons")
implementation("com.mapconductor:marker-strategy")
```

Use this page as a high-level map; detailed API information for each area (core, components, states, experimental) can be migrated from the existing mdBook sections (`docs/src/core`, `docs/src/components`, etc.).

