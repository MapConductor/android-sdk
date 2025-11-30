---
title: "Initialization"
---

This section covers how to properly initialize and configure MapConductor for different map providers.

## Basic Initialization

### Gradle Dependencies

Add MapConductor to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation "com.mapconductor:mapconductor-bom:$version"
    implementation "com.mapconductor:core"

    // Choose your map provider(s)
    implementation "com.mapconductor:for-googlemaps"
    implementation "com.mapconductor:for-mapbox"
    implementation "com.mapconductor:for-here"
    implementation "com.mapconductor:for-arcgis"
}
```

### Map Provider Setup

Each map provider requires specific setup and API keys.

#### Google Maps

```kotlin
// In your Activity or Fragment
@Composable
fun GoogleMapsExample() {
    val mapViewState = rememberGoogleMapViewState()

    // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(state = mapViewState) {
        // Map content
    }
}
```

#### Mapbox

```kotlin
@Composable
fun MapboxExample() {
    val mapViewState = rememberMapboxMapViewState()

    // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(state = mapViewState) {
        // Map content
    }
}
```

#### HERE Maps

```kotlin
@Composable
fun HereExample() {
    val mapViewState = rememberHereMapViewState()

    // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(state = mapViewState) {
        // Map content
    }
}
```

#### ArcGIS

```kotlin
@Composable
fun ArcGISExample() {
    val mapViewState = rememberArcGISMapViewState()

    // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
MapView(state = mapViewState) {
        // Map content
    }
}
```

## Initialization

```kotlin
@Composable
fun CustomMapConfiguration() {
    val mapViewState = remember {
        GoogleMapViewStateImpl().apply {
            // Set initial camera position
            initCameraPosition = MapCameraPositionImpl(
                target = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                zoom = 12f,
                bearing = 45f,
                tilt = 30f
            )

            // Set map design/style
            mapDesignType = GoogleMapDesignType.SATELLITE
        }
    }

    // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
    GoogleMapView(
        state = mapViewState,
        onMapLoaded = {
            println("Map loaded and ready")
        }
    ) {
        // Map content
    }
}
```
