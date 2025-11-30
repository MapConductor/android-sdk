---
title: Tutorial
---

import { Tabs, TabItem } from '@astrojs/starlight/components';

# MapConductor Tutorial

This tutorial walks you through how to use the MapConductor Android SDK to display a map, add markers and shapes, and handle user interactions.

## What you’ll learn

- Installing and configuring the MapConductor SDK
- Displaying a map
- Adding markers, circles, and polylines
- Handling tap and click events
- Controlling camera position
- Switching map SDKs

## Prerequisites

- Android Studio installed
- Basic knowledge of Jetpack Compose
- Basic Kotlin knowledge

## Step 1: Project Setup

### 1-1. Add dependencies

Add MapConductor dependencies to your module-level `build.gradle.kts` or `build.gradle`:

<Tabs>
<TabItem label="Kotlin (build.gradle.kts)">

```kotlin
dependencies {
    val mapconductorVersion = "{BOM_MODULE_VERSION}"

    // Use BOM to unify versions
    implementation(platform("com.mapconductor:mapconductor-bom:$mapconductorVersion"))

    // Core module (required)
    implementation("com.mapconductor:core")

    // Use Google Maps
    implementation("com.mapconductor:for-googlemaps")

    // Or Mapbox
    // implementation("com.mapconductor:for-mapbox")

    // Or HERE Maps
    // implementation("com.mapconductor:for-here")

    // Or ArcGIS
    // implementation("com.mapconductor:for-arcgis")

    // Or MapLibre
    // implementation("com.mapconductor:for-maplibre")
}
```

</TabItem>
<TabItem label="Groovy (build.gradle)">

```groovy
dependencies {
    def mapconductorVersion = "{BOM_MODULE_VERSION}"

    // Use BOM to unify versions
    implementation platform("com.mapconductor:mapconductor-bom:$mapconductorVersion")

    // Core module (required)
    implementation "com.mapconductor:core"

    // Use Google Maps
    implementation "com.mapconductor:for-googlemaps"

    // Or Mapbox
    // implementation "com.mapconductor:for-mapbox"

    // Or HERE Maps
    // implementation "com.mapconductor:for-here"

    // Or ArcGIS
    // implementation "com.mapconductor:for-arcgis"

    // Or MapLibre
    // implementation "com.mapconductor:for-maplibre"
}
```

</TabItem>
</Tabs>

### 1-2. Android configuration

Add the following configuration to `build.gradle.kts` or `build.gradle`:

<Tabs>
<TabItem label="Kotlin (build.gradle.kts)">

```kotlin
android {
    compileSdk = {ANDROID_TARGET_SDK_VERSION}

    defaultConfig {
        minSdk = {ANDROID_MIN_SDK_VERSION}
        targetSdk = {ANDROID_TARGET_SDK_VERSION}
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_{JAVA_VERSION}
        targetCompatibility = JavaVersion.VERSION_{JAVA_VERSION}
    }

    kotlinOptions {
        jvmTarget = "{JAVA_VERSION}"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "{JETPACK_COMPOSE_VERSION}"
    }
}
```

</TabItem>
<TabItem label="Groovy (build.gradle)">

```groovy
android {
    compileSdk {ANDROID_TARGET_SDK_VERSION}

    defaultConfig {
        minSdk {ANDROID_MIN_SDK_VERSION}
        targetSdk {ANDROID_TARGET_SDK_VERSION}
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_{JAVA_VERSION}
        targetCompatibility JavaVersion.VERSION_{JAVA_VERSION}
    }

    kotlinOptions {
        jvmTarget = '{JAVA_VERSION}'
    }

    buildFeatures {
        compose true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "{JETPACK_COMPOSE_VERSION}"
    }
}
```

</TabItem>
</Tabs>

### 1-3. Map SDK setup

> **Important**: MapConductor is a unified API layer on top of existing map SDKs. You must configure each map SDK independently before using MapConductor.

Each map SDK requires its own API keys, permissions, and configuration:

- **[Google Maps Setup](/setup/google-maps)** – API keys and permissions for Google Maps SDK
- **[Mapbox Setup](/setup/mapbox)** – Mapbox access tokens and style configuration
- **[HERE Maps Setup](/setup/here-maps)** – HERE SDK API keys and licensing
- **[ArcGIS Setup](/setup/arcgis)** – ArcGIS SDK API keys and licensing
- **[MapLibre Setup](/setup/maplibre/)** – Tile and style configuration

## Step 2: Displaying a map

### 2-1. Basic MapView

```kotlin
@Composable
fun BasicMapExample(modifier: Modifier = Modifier) {
    val sanFrancisco = GeoPointImpl.fromLatLong(37.7749, -122.4194)
    val camera = MapCameraPositionImpl(
        position = sanFrancisco,
        zoom = 13.0,
    )

    val mapViewState = rememberGoogleMapViewState(
        cameraPosition = camera,
    )

    GoogleMapView(
        modifier = modifier,
        state = mapViewState,
        onMapClick = { geoPoint ->
            println("Map clicked at: ${geoPoint.latitude}, ${geoPoint.longitude}")
        }
    ) {
        // Map content goes here
    }
}
```

## Step 3: Adding markers and shapes

### 3-1. Add a marker

```kotlin
GoogleMapView(
    state = mapViewState
) {
    Marker(
        position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        icon = DefaultIcon(label = "SF"),
        extra = "San Francisco marker"
    )
}
```

### 3-2. Add circles and polylines

```kotlin
GoogleMapView(
    state = mapViewState
) {
    // Circle
    Circle(
        center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        radiusMeters = 1000.0,
        strokeColor = Color.Blue,
        fillColor = Color.Blue.copy(alpha = 0.3f)
    )

    // Polyline
    Polyline(
        points = listOf(
            GeoPointImpl.fromLatLong(37.7749, -122.4194),
            GeoPointImpl.fromLatLong(37.7849, -122.4094),
        ),
        strokeColor = Color.Magenta,
        strokeWidth = 3.dp
    )
}
```

## Step 4: Handling user interactions

### 4-1. Map click events

```kotlin
GoogleMapView(
    state = mapViewState,
    onMapClick = { geoPoint ->
        println("Map clicked at: ${geoPoint.latitude}, ${geoPoint.longitude}")
    }
) {
    // Content
}
```

### 4-2. Marker click events

```kotlin
GoogleMapView(
    state = mapViewState,
    onMarkerClick = { markerState ->
        println("Marker clicked: ${markerState.extra}")
    }
) {
    Marker(
        position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        icon = DefaultIcon(label = "SF"),
        extra = "San Francisco marker"
    )
}
```

## Step 5: Switching map SDKs

To switch from Google Maps to Mapbox, just change the state and view types:

```kotlin
// Google Maps
val googleMapState = rememberGoogleMapViewState()
GoogleMapView(state = googleMapState) { /* content */ }

// Mapbox
val mapboxState = rememberMapboxMapViewState()
MapboxMapView(state = mapboxState) { /* same content */ }
```

All overlay components (`Marker`, `Circle`, `Polyline`, etc.) can be reused across providers as long as you swap out the `MapViewState` and map view composable.

## Next Steps

In this tutorial, you learned the basics of using the MapConductor SDK:

- How to install and configure dependencies
- How to display a map
- How to add markers and shapes
- How to handle user interactions
- How to switch between map SDKs

To dive deeper, check out:

- [Modules Overview](/modules/)
+- [Provider Compatibility](/provider-compatibility/)
- [SDK Version Compatibility](/sdk-version-compatibility/)
