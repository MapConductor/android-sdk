# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

MapConductor Android SDK is a unified mapping library that provides a common API for multiple map providers (Google Maps, Mapbox, HERE, ArcGIS). The project follows a multi-module architecture with a core module and provider-specific implementations.

## Build and Development Commands

### Building the Project
```bash
./gradlew build
```

### Linting and Code Style
The project uses KtLint for code formatting. Run lint checks and auto-formatting:
```bash
./gradlew allLintChecks
```

This command runs `ktlintFormat` and `lint` for all modules defined in `projects.properties`.

### Running Tests
```bash
./gradlew test
```

### Running Specific Module Tests
```bash
./gradlew :mapconductor-core:test
./gradlew :mapconductor-for-googlemaps:test
```

## Module Architecture

### Core Module (`mapconductor-core`)
- **Purpose**: Contains shared abstractions, base classes, and common functionality
- **Key Components**:
  - `MapViewController`: Abstract controller interface for all map providers
  - `MapViewBase`: Generic Compose-based map view component
  - `MarkerManager`, `CircleManager`, `PolylineOverlayManager`: Feature management
  - `HexGeocell`: Spatial indexing for efficient marker clustering
  - Projection utilities (`WebMercator`, `WGS84`)

### Provider-Specific Modules
Each map provider has its own module that implements the core abstractions:

- **`mapconductor-for-googlemaps`**: Google Maps implementation
- **`mapconductor-for-mapbox`**: Mapbox implementation  
- **`mapconductor-for-here`**: HERE Maps implementation
- **`mapconductor-for-arcgis`**: ArcGIS implementation

### Supporting Modules
- **`mapconductor-icons`**: Reusable marker icon components
- **`example-app`**: Demo application showcasing all map providers

## Key Design Patterns

### Generic Type System
The architecture uses extensive generics to maintain type safety while supporting multiple map SDKs:
```kotlin
interface MapViewController<ActualMarker, ActualCircle, ActualPolyline>
class MapViewBase<SpecificState, SpecificController, ActualMapView, ActualMap, SpecificViewHolder>
```

### State Management
- Uses Kotlin StateFlow for reactive state management
- Debounced updates (100ms) to prevent excessive recomposition
- `MapViewState` manages initialization lifecycle

### Overlay Management
- Separate managers for markers, circles, and polylines
- Renderer pattern for provider-specific drawing logic
- Spatial indexing with hexagonal cells for performance

## Configuration Files

### `projects.properties`
Defines all modules included in the build. Update this file when adding new modules.

### `secrets.properties` 
Contains API keys for map providers. Must be obtained from the credentials repository.

### `gradle/libs.versions.toml`
Centralized dependency management using Gradle version catalogs.

## Feature Implementation Status

Currently working on Polyline support across all providers. Check the README.md table for current feature completion status.

## Development Guidelines

- Follow existing naming conventions and package structure
- New features should be implemented in the core module first, then in each provider module
- Use the existing renderer pattern for drawing operations
- Maintain type safety with appropriate generic constraints
- Provider-specific code should be isolated to respective modules

## Testing
- Unit tests in each module's `src/test` directory
- Instrumented tests in `src/androidTest` directories
- Use existing test patterns and mock objects where appropriate