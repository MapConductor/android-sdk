---
title: Installation and Versions
---

This page provides complete installation instructions and version information for all MapConductor Android SDK modules.

## Current Version

**Latest Release**: `1.1.0`

## Versioning Strategy

MapConductor follows a unified versioning approach across all modules:

### Major Version Alignment

- All modules share the same **major version** to ensure compatibility
- When any core module requires breaking changes, all modules increment their major version together

### Core Module Versioning

- **Minor version** increments when:
  - Map provider SDKs receive major updates requiring significant API changes
  - New core components are added due to expanded map provider capabilities
  - Significant new features are introduced to the unified API
- **Patch version** increments for:
  - Bug fixes and stability improvements
  - Performance optimizations
  - Documentation updates

### Experimental Module Versioning

- **Major version** increments when:
  - New significant features or icon types are added
  - Breaking API changes are introduced
  - Module graduates from experimental to stable
- **Patch version** increments for:
  - Minor bug fixes and improvements
  - Small additions that don't break existing APIs

## Core Modules

### BOM

#### `mapconductor-bom`

The BOM module containing version information for all published artifacts.

```kotlin
implementation("com.mapconductor:mapconductor-bom:1.1.0")
```

Use this BOM in combination with Gradle’s platform support to keep all modules on the same version.

### Core Runtime

#### `mapconductor-core`

The core module containing shared functionality and base classes.

```kotlin
implementation("com.mapconductor:core")
```

**Required for**: All MapConductor usage  
**Depends on**: Jetpack Compose, Kotlin Coroutines

### Map Provider Modules

Choose one or more map provider modules based on your needs:

#### `mapconductor-for-googlemaps`

Google Maps integration module.

```kotlin
implementation("com.mapconductor:for-googlemaps")
```

Provides `GoogleMapsView` and `GoogleMapViewStateImpl`. Requires Google Maps SDK setup.

#### `mapconductor-for-mapbox`

Mapbox integration module.

```kotlin
implementation("com.mapconductor:for-mapbox")
```

Provides `MapboxMapView` and `MapboxViewStateImpl`. Requires Mapbox SDK setup.

#### `mapconductor-for-here`

HERE Maps integration module.

```kotlin
implementation("com.mapconductor:for-here")
```

Provides `HereMapView` and `HereViewStateImpl`. Requires HERE SDK setup.

#### `mapconductor-for-arcgis`

ArcGIS integration module.

```kotlin
implementation("com.mapconductor:for-arcgis")
```

Provides `ArcGISMapView` and `ArcGISMapViewStateImpl`. Requires ArcGIS SDK setup.

#### `mapconductor-for-maplibre`

MapLibre integration module.

```kotlin
implementation("com.mapconductor:for-maplibre")
```

Provides `MapLibreMapView` and `MapLibreViewStateImpl`. Requires MapLibre setup (tiles, styles).

## Experimental Modules

> **Experimental**: These modules are experimental and may change in future versions.

### `mapconductor-icons`

Custom marker icons with programmatic styling.

```kotlin
implementation("com.mapconductor:icons")
```

Provides icon components such as `CircleIcon`, `FlagIcon`, and info bubble icons.

### `mapconductor-marker-strategy`

Advanced marker rendering strategies for performance optimization (e.g., clustering, server-side strategies).

```kotlin
implementation("com.mapconductor:marker-strategy")
```

### `mapconductor-marker-native-strategy`

Native-accelerated strategies for large-scale marker rendering.

```kotlin
implementation("com.mapconductor:marker-native-strategy")
```

## Gradle Configuration

### Project-level `build.gradle` / `build.gradle.kts`

Configure Kotlin and Compose versions according to the example app:

```kotlin
buildscript {
    ext {
        compose_version = "1.7.1"
        kotlin_version = "1.9.25"
    }
}
```

### Module-level `build.gradle` / `build.gradle.kts`

```kotlin
android {
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        targetSdk = 35
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = compose_version
    }
}
```

### ProGuard / R8 Configuration

For release builds, add these rules:

```proguard
# MapConductor Core
-keep class com.mapconductor.core.** { *; }

# Map Provider Specific
-keep class com.mapconductor.googlemaps.** { *; }
-keep class com.mapconductor.mapbox.** { *; }
-keep class com.mapconductor.here.** { *; }
-keep class com.mapconductor.arcgis.** { *; }
-keep class com.mapconductor.maplibre.** { *; }

# Native Strategy (if using)
-keep class com.mapconductor.marker.nativestrategy.** { *; }
```

## Version Updates

### Checking for Updates

To check for the latest MapConductor version:

1. GitHub Releases: check the `android-sdk` releases page.
2. Maven Central: search for `com.mapconductor`.
3. Gradle tools: use your dependency update checker plugin of choice.

### Updating Versions

To update to a new MapConductor version using the BOM:

```kotlin
val mapconductorVersion = "1.1.0"

dependencies {
    implementation(platform("com.mapconductor:mapconductor-bom:$mapconductorVersion"))
    implementation("com.mapconductor:core")
    implementation("com.mapconductor:for-googlemaps")
    // ... other modules as needed
}
```

## Release Notes (Summary)

### Version 1.1.0 (Current)

- Unified camera move event handling across providers (`onCameraMoveStart`, `onCameraMove`, `onCameraMoveEnd`)
- Improved `MapViewState` camera position handling and `VisibleRegion` integration
- Refactored marker controller interfaces for clearer provider integrations
- Expanded example app to demonstrate advanced camera and visible region workflows

### Version 1.0.0 (Previous)

- Initial stable release
- Support for Google Maps, Mapbox, HERE, and ArcGIS
- Core components: Marker, Circle, Polyline, Polygon, GroundImage
- Experimental modules: Icons, Marker Strategy, Native Strategy
- Jetpack Compose integration
- Android 8.0+ support

