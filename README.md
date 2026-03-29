# MapConductor Android SDK

A unified mapping library that provides a common API for multiple map providers including Google Maps, Mapbox, HERE, ArcGIS, and MapLibre. Write once, deploy across all major mapping platforms.

## Features

- **Multi-Provider Support**: Seamlessly switch between Google Maps, Mapbox, HERE, ArcGIS, and MapLibre with a single API
- **Unified Interface**: Common abstractions for markers, circles, polylines, polygons, ground overlays, heatmaps, and marker clustering
- **Reactive State**: Built on Kotlin StateFlow for reactive UI updates
- **Jetpack Compose**: Modern Android UI toolkit integration

## Module Structure

| Module | Artifact | Description |
|---|---|---|
| `mapconductor-bom` | `com.mapconductor:mapconductor-bom` | Bill of Materials — aligns all module versions |
| `android-sdk-core` | `com.mapconductor:core` | Core abstractions, geometry types, overlay states |
| `android-for-googlemaps` | `com.mapconductor:for-googlemaps` | Google Maps implementation |
| `android-for-mapbox` | `com.mapconductor:for-mapbox` | Mapbox implementation |
| `android-for-here` | `com.mapconductor:for-here` | HERE Maps implementation |
| `android-for-arcgis` | `com.mapconductor:for-arcgis` | ArcGIS implementation |
| `android-for-maplibre` | `com.mapconductor:for-maplibre` | MapLibre implementation |
| `android-icons` | `com.mapconductor:icons` | Composable marker icons (CircleIcon, FlagIcon, info bubbles) |
| `android-heatmap` | `com.mapconductor:heatmap` | Map-provider-agnostic heatmap overlay |
| `android-marker-clustering` | `com.mapconductor:marker-clustering` | Automatic marker clustering across all providers |

## Quick Start

### 1. Setup

Clone the repository:
```bash
git clone https://github.com/MapConductor/android-sdk.git
```

Add `secrets.properties` to the project root from https://github.com/MapConductor/map-sdk-credentials/

### 2. Add Dependencies

```kotlin
dependencies {
    implementation(platform("com.mapconductor:mapconductor-bom:$version"))
    implementation("com.mapconductor:core")
    implementation("com.mapconductor:for-googlemaps") // or your chosen provider
}
```

### 3. Basic Usage

```kotlin
val mapState = rememberGoogleMapViewState(
    initialCameraPosition = MapCameraPosition(
        target = GeoPoint(35.6762, 139.6503),
        zoom = 12.0,
    )
)

GoogleMapView(
    modifier = Modifier.fillMaxSize(),
    state = mapState,
) {
    Marker(rememberMarkerState(position = GeoPoint(35.6762, 139.6503)))
    Circle(rememberCircleState(center = GeoPoint(35.6762, 139.6503), radius = 500.0))
}
```

### 4. Switch Map Providers

Simply change the map view component — all overlays work unchanged:
```kotlin
// Google Maps
GoogleMapView(state = googleMapState) { /* overlays */ }

// Mapbox
MapboxMapView(state = mapboxState) { /* overlays */ }

// HERE Maps
HereMapView(state = hereState) { /* overlays */ }

// ArcGIS
ArcGISMapView(state = arcgisState) { /* overlays */ }

// MapLibre
MapLibreMapView(state = maplibreState) { /* overlays */ }
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

|                     | Google Maps | Mapbox   | HERE     | ArcGIS   | MapLibre |
|---------------------|-------------|----------|----------|----------|----------|
| Map                 | &#x2611;    | &#x2611; | &#x2611; | &#x2611; | &#x2611; |
| Marker              | &#x2611;    | &#x2611; | &#x2611; | &#x2611; | &#x2611; |
| Circle              | &#x2611;    | &#x2611; | &#x2611; | &#x2611; | &#x2611; |
| Polyline            | &#x2611;    | &#x2611; | &#x2611; | &#x2611; | &#x2611; |
| Polygon             | &#x2611;    | &#x2611; | &#x2611; | &#x2611; | &#x2611; |
| GroundImage         | &#x2611;    | &#x2611; | &#x2611; | &#x2611; | &#x2611; |
| Heatmap             | &#x2611;    | &#x2611; | &#x2611; | &#x2611; | &#x2611; |
| Marker Clustering   | &#x2611;    | &#x2611; | &#x2611; | &#x2611; | &#x2611; |
| RasterTileLayer     | &#x2610;    | &#x2611; | &#x2611; | &#x2611; | &#x2611; |
| VectorTileLayer     | &#x2610;    | &#x2610; | &#x2610; | &#x2610; | &#x2610; |


