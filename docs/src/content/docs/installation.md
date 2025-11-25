---
title: Installation
---

# Installation and Versions

This page explains how to add MapConductor Android SDK to a Gradle project and recommended version settings.

## Adding Dependencies

MapConductor is distributed from Maven Central as `mapconductor-bom` and individual modules. Using the BOM allows you to manage all MapConductor module versions centrally.

```kotlin
val mapconductorVersion = "1.1.0"

dependencies {
    // Use BOM to unify versions
    implementation(platform("com.mapconductor:mapconductor-bom:$mapconductorVersion"))

    // Core module
    implementation("com.mapconductor:core")

    // Add the map provider modules you need
    implementation("com.mapconductor:for-googlemaps")
    // implementation("com.mapconductor:for-mapbox")
    // implementation("com.mapconductor:for-here")
    // implementation("com.mapconductor:for-arcgis")
    // implementation("com.mapconductor:for-maplibre")
}
```

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

You can check for the latest MapConductor version at:

1. GitHub Releases: `android-sdk` releases page
2. Maven Central: search for `com.mapconductor`
3. Gradle plugins: dependency update checker plugins, etc.

### Updating with BOM

To update to a new MapConductor version, change the BOM version:

```kotlin
val mapconductorVersion = "1.1.1"

dependencies {
    implementation(platform("com.mapconductor:mapconductor-bom:$mapconductorVersion"))
    implementation("com.mapconductor:core")
    implementation("com.mapconductor:for-googlemaps")
    // Add other modules as needed
}
```

