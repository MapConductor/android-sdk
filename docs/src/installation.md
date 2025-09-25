# Installation and Versions

This page provides complete installation instructions and version information for all MapConductor Android SDK modules.

## Current Version

**Latest Release**: `1.0.0`

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

### Required Modules

#### mapconductor-bom
The bom module containing version information.

```kotlin
implementation "com.mapconductor:mapconductor-bom:1.0.0"
```

**Required for**: All MapConductor usage
**Size**: ~XXX KB

#### core
The core module containing shared functionality and base classes.

```kotlin
implementation "com.mapconductor:core"
```

**Required for**: All MapConductor usage
**Dependencies**: Jetpack Compose, Kotlin Coroutines
**Size**: ~XXX KB

### Map Provider Modules

Choose one or more map provider modules based on your needs:

#### for-googlemaps
Google Maps integration module.

```kotlin
implementation "com.mapconductor:for-googlemaps"
```

**Provides**: GoogleMapsView, GoogleMapViewStateImpl
**Requires**: Google Maps SDK setup
**Size**: ~XXX KB

#### for-mapbox
Mapbox integration module.

```kotlin
implementation "com.mapconductor:for-mapbox"
```

**Provides**: MapboxMapView, MapboxViewStateImpl
**Requires**: Mapbox SDK setup
**Size**: ~XXX KB

#### for-here
HERE Maps integration module.

```kotlin
implementation "com.mapconductor:for-here"
```

**Provides**: HereMapView, HereViewStateImpl
**Requires**: HERE SDK setup
**Size**: ~XXX KB

#### for-arcgis
ArcGIS integration module.

```kotlin
implementation "com.mapconductor:for-arcgis"
```

**Provides**: ArcGISMapView, ArcGISMapViewStateImpl
**Requires**: ArcGIS SDK setup
**Size**: ~XXX KB

## Experimental Modules

> **⚠️ Experimental**: These modules are experimental and may change significantly in future versions.

### icons
Custom marker icons with programmatic styling.

```kotlin
implementation "com.mapconductor:icons"
```

**Provides**: CircleIcon, FlagIcon
**Stability**: Experimental
**Size**: ~XXX KB

### marker-strategy
Advanced marker rendering strategies for performance optimization.

```kotlin
implementation "com.mapconductor:marker-strategy"
```

**Provides**: DefaultMarkerRenderingStrategy, SpatialMarkerRenderingStrategy
**Stability**: Experimental
**Size**: ~XXX KB

### marker-native-strategy
High-performance native C++ marker management.

```kotlin
implementation "com.mapconductor:marker-native-strategy"
```

**Provides**: NativeMarkerManager, SimpleNativeParallelStrategy
**Requires**: Native library support (arm64-v8a, armeabi-v7a, x86, x86_64)
**Stability**: Highly Experimental
**Size**: ~XXX KB + native libraries

## Complete Installation Examples

### Basic Installation (Single Provider)

```kotlin
dependencies {
    // Bom module (required)
    implementation "com.mapconductor:mapconductor-bom:1.0.0"
    // Core module (required)
    implementation "com.mapconductor:core"

    // Choose one map provider
    implementation "com.mapconductor:for-googlemaps"
}
```

### Multi-Provider Installation

```kotlin
dependencies {
    // Bom module (required)
    implementation "com.mapconductor:mapconductor-bom:1.0.0"
    // Core module (required)
    implementation "com.mapconductor:core"

    // Multiple map providers
    implementation "com.mapconductor:for-googlemaps"
    implementation "com.mapconductor:for-mapbox"
    implementation "com.mapconductor:for-here"
    implementation "com.mapconductor:for-arcgis"
}
```

### Full Installation (All Modules)

```kotlin
dependencies {
    // Bom module (required)
    implementation "com.mapconductor:mapconductor-bom:1.0.0"
    // Core module (required)
    implementation "com.mapconductor:core"

    // All map providers
    implementation "com.mapconductor:for-googlemaps"
    implementation "com.mapconductor:for-mapbox"
    implementation "com.mapconductor:for-here"
    implementation "com.mapconductor:for-arcgis"

    // Experimental modules (optional)
    implementation "com.mapconductor:icons"
    implementation "com.mapconductor:marker-strategy"
    implementation "com.mapconductor:marker-native-strategy"
}
```

## Module Dependencies

### Dependency Graph

```
mapconductor-bom (base)
core (base)
├── googlemaps
├── for-mapbox
├── for-here
├── for-arcgis
├── icons
├── marker-strategy
└── marker-native-strategy
```

### External Dependencies

Each module requires different external dependencies:

#### Core Module Dependencies
- `androidx.compose.ui:ui`
- `androidx.compose.runtime:runtime`
- `org.jetbrains.kotlinx:kotlinx-coroutines-android`

#### Google Maps Module
- `com.google.android.gms:play-services-maps`
- `com.google.maps.android:maps-compose`

#### Mapbox Module
- `com.mapbox.maps:android`
- `com.mapbox.extension:maps-compose`

#### HERE Maps Module
- `com.here.sdk:sdk-core`
- `com.here.sdk:sdk-search`

#### ArcGIS Module
- `com.esri.arcgisruntime:arcgis-android`

## Version Compatibility

### MapConductor Version Matrix

| MapConductor | Min Android | Target Android | Kotlin | Compose |
|--------------|-------------|----------------|---------|---------|
| 1.0.0 | API 26 (8.0) | API 35 (15) | 1.9.x | 1.7.x |

### Map Provider SDK Compatibility

| Provider | SDK Version | MapConductor 1.0.0 |
|----------|-------------|-------------------|
| Google Maps | 18.2.x+ | ✅ Compatible |
| Mapbox | 11.x+ | ✅ Compatible |
| HERE | 4.23.x+ | ✅ Compatible |
| ArcGIS | 200.x+ | ✅ Compatible |

## Gradle Configuration

### Project-level build.gradle

```kotlin
buildscript {
    ext {
        compose_version = '1.7.1'
        kotlin_version = '1.9.25'
    }
}
```

### Module-level build.gradle

```kotlin
android {
    compileSdk 35

    defaultConfig {
        minSdk 26
        targetSdk 35
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = '17'
    }

    buildFeatures {
        compose true
    }

    composeOptions {
        kotlinCompilerExtensionVersion compose_version
    }
}
```

### ProGuard Configuration

For release builds, add these ProGuard rules:

```proguard
# MapConductor Core
-keep class com.mapconductor.core.** { *; }

# Map Provider Specific
-keep class com.mapconductor.googlemaps.** { *; }
-keep class com.mapconductor.mapbox.** { *; }
-keep class com.mapconductor.here.** { *; }
-keep class com.mapconductor.arcgis.** { *; }

# Native Strategy (if using)
-keep class com.mapconductor.marker.nativestrategy.** { *; }
```

## Version Updates

### Checking for Updates

To check for the latest MapConductor version:

1. **GitHub Releases**: Check the [releases page](https://github.com/mapconductor/android-sdk/releases)
2. **Maven Central**: Search for `com.mapconductor` on [Maven Central](https://central.sonatype.com/)
3. **Gradle Version Catalog**: Use dependency version checking tools

### Updating Versions

To update to a new MapConductor version:

```kotlin
// Update all modules to the same version
def mapconductor_version = "1.1.0"  // New version

dependencies {
    implementation "com.mapconductor:mapconductor-bom:$mapconductor_version"
    implementation "com.mapconductor:core"
    implementation "com.mapconductor:for-googlemaps"
    // ... other modules
}
```


## Release Notes

### Version 1.0.0 (Current)
- Initial stable release
- Support for Google Maps, Mapbox, HERE, and ArcGIS
- Core components: Marker, Circle, Polyline, Polygon, GroundImage
- Experimental modules: Icons, Marker Strategy, Native Strategy
- Jetpack Compose integration
- Android 8.0+ support

### Upcoming Releases

Future versions will include:
- Additional map providers
- Enhanced clustering algorithms
- Offline map support
- Performance improvements
- New experimental features

## Support and Compatibility

### Minimum Requirements
- **Android**: API 26 (Android 8.0) or higher
- **Kotlin**: 1.9.0 or higher
- **Jetpack Compose**: 1.7.0 or higher
- **Java**: JDK 17 or higher

### Device Support
- **ARM**: arm64-v8a, armeabi-v7a (native strategy only)
- **x86**: x86, x86_64 (emulator support for native strategy)
- **Memory**: Minimum 2GB RAM recommended for large datasets

For questions about versions or compatibility, please check the [documentation](https://docs.mapconductor.com) or file an issue on [GitHub](https://github.com/mapconductor/android-sdk/issues).
