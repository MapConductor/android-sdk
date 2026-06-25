# MapConductor Android SDK

- [Japanese Doc](./README.ja.md)
- [Spanish Doc](./README.es-419.md)

**One Android map API for multiple map providers.**

MapConductor Android SDK is an open-source mapping library for Android that lets you work with multiple map SDKs through a single, consistent Jetpack Compose API.

Instead of writing different map code for Google Maps, Mapbox, HERE Maps, ArcGIS, and MapLibre, MapConductor provides shared abstractions for maps, camera state, markers, shapes, overlays, and advanced map features.

Write your map UI once.
Choose the map provider that fits your product.

---

## Why MapConductor?

Mobile map development often becomes tightly coupled to a specific map SDK. Each provider has its own API design, lifecycle model, rendering behavior, and feature set. This makes it hard to switch providers, support multiple map backends, or keep map-related code clean in a modern Compose application.

MapConductor solves this by providing a common layer on top of major Android map SDKs.

With MapConductor, you can:

* Use a Jetpack Compose-first API for map UI
* Switch between supported map providers with less rewrite work
* Share the same marker, circle, polyline, polygon, and overlay logic
* Build provider-independent map features such as heatmaps and marker clustering
* Keep your application code focused on map behavior, not SDK-specific differences

![](./docs/src/assets/top-page/english-comic-why-map-conductor.jpg)

---

## Supported Map Providers

MapConductor currently supports the following Android map providers:

| Provider        | Module                            |
| --------------- | --------------------------------- |
| Google Maps     | `com.mapconductor:for-googlemaps` |
| Mapbox          | `com.mapconductor:for-mapbox`     |
| HERE Maps       | `com.mapconductor:for-here`       |
| ArcGIS Maps SDK | `com.mapconductor:for-arcgis`     |
| MapLibre        | `com.mapconductor:for-maplibre`   |

You can choose one provider for your app, or structure your code so that the provider can be changed later.

---

## Core Features

MapConductor provides a unified API for common map UI and geospatial features:

* Map view components for multiple providers
* Camera state and camera position
* Markers
* Custom marker icons
* Info bubbles written with Jetpack Compose
* Circles with meter-based radius
* Polylines
* Polygons
* Ground images
* Raster tile layers
* Heatmaps
* Marker clustering
* GeoJSON layers
* Shared geometry types such as `GeoPoint`
* Reactive state management for map objects

The goal is not only to wrap each provider SDK, but also to provide consistent behavior where possible across different map engines.

---

## Installation

Add Maven Central and Google repositories to your Android project if they are not already configured.

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
```

Then add MapConductor dependencies.

```kotlin
dependencies {
    implementation(platform("com.mapconductor:mapconductor-bom:<latest-version>"))

    implementation("com.mapconductor:core")

    // Choose one or more map provider modules
    implementation("com.mapconductor:for-googlemaps")
    // implementation("com.mapconductor:for-mapbox")
    // implementation("com.mapconductor:for-here")
    // implementation("com.mapconductor:for-arcgis")
    // implementation("com.mapconductor:for-maplibre")

    // Optional feature modules
    implementation("com.mapconductor:icons")
    implementation("com.mapconductor:heatmap")
    implementation("com.mapconductor:marker-clustering")
    implementation("com.mapconductor:geojson-layer")
}
```

Each map provider may require its own API key, access token, Gradle setup, or Android manifest configuration. 

Please check the setup guide for the provider you are using.
- [Setup for Google Maps Android API](https://docs-android.mapconductor.com/setup/google-maps/)
- [Setup for MapBox](https://docs-android.mapconductor.com/setup/mapbox/)
- [Setup for HERE](https://docs-android.mapconductor.com/setup/here-maps/)
- [Setup for ArcGIS](https://docs-android.mapconductor.com/setup/arcgis/)
- [Setup for MapLibre](https://docs-android.mapconductor.com/setup/maplibre/)

---

## Basic Example

The following example shows a simple Compose map with a marker and a circle.

```kotlin
@Composable
fun SimpleMapScreen(modifier: Modifier) {
    val mapState = rememberMapLibreMapViewState(
        cameraPosition = MapCameraPosition(
            position = GeoPoint(35.6762, 139.6503),
            zoom = 15.0,
        ),
        mapDesign = MapLibreDesign.OpenMapTiles,
    )

    MapLibreMapView(
        modifier = modifier,
        state = mapState,
    ) {
        Marker(
            state = MarkerState(
                position = GeoPoint(35.6762, 139.6503),
            )
        )

        Circle(
            state = CircleState(
                center = GeoPoint(35.6762, 139.6503),
                radiusMeters = 500.0,
                fillColor = Color.Green.copy(alpha = 0.5f),
                strokeColor = Color.Blue,
                strokeWidth = 3.dp,
            )
        )
    }
}
```

This example uses MapLibre Maps, but the map objects are written using MapConductor concepts. The same overlay logic can be adapted to other supported providers.

![](./docs/src/assets/top-page/simple-map-screen.png)

---

## Switching Map Providers

One of the main ideas behind MapConductor is that your map overlays should not have to be rewritten when you change map providers.

For example:

- MapLibre

  ```kotlin
  val initCameraPosition = MapCameraPosition(...)

  val mapLibreMapState = rememberMapLibreMapViewState(
      cameraPosition = initCameraPosition,
      mapDesign = mapDesign = MapLibreDesign.OpenMapTiles,
  )

  MapLibreMapView(state = mapLibreMapState) {
      MapContent()
  }
  ```

- <details>
  <summary>Google Maps (Tap to open)</summary>

  ```kotlin
  val initCameraPosition = MapCameraPosition(...)

  val googleMapState = rememberGoogleMapViewState(
      cameraPosition = initCameraPosition,
      mapDesign = GoogleMapDesign.Normal,
  )

  GoogleMapView(state = googleMapState) {
      MapContent()
  }
  ```
</details>

- <details>
  <summary>Mapbox (Tap to open)</summary>

  ```kotlin
  val initCameraPosition = MapCameraPosition(...)

  val mapboxMapState = rememberMapboxMapViewState(
      cameraPosition = initCameraPosition,
      mapDesign = MapboxMapDesign.Standard,
  )

  MapboxMapView(state = mapboxMapState) {
      MapContent()
  }
  ```
</details>

- <details>
  <summary>HERE (Tap to open)</summary>

  ```kotlin
  val initCameraPosition = MapCameraPosition(...)

  val hereMapState = rememberHereMapViewState(
      cameraPosition = initCameraPosition,
      mapDesign = HereMapDesign.NormalDay,
  )

  HereMapView(state = hereMapState) {
      MapContent()
  }
  ```
</details>

- <details>
  <summary>ArcGIS 2D (Tap to open)</summary>

  ```kotlin
  val initCameraPosition = MapCameraPosition(...)

  val arcgisMapState = rememberArcGISMapViewState(
      cameraPosition = initCameraPosition,
      mapDesign = ArcGISDesign.Streets,
  )

  ArcGISMapView2D(state = arcgisMapState) {
      MapContent()
  }
  ```
</details>

- <details>
  <summary>ArcGIS 3D (Tap to open)</summary>

  ```kotlin
  val initCameraPosition = MapCameraPosition(...)

  val arcgisMapState = rememberArcGISMapViewState(
      cameraPosition = initCameraPosition,
      mapDesign = ArcGISDesign.Streets,
  )

  ArcGISMapView(state = arcgisMapState) {
      MapContent()
  }
  ```
</details>

Your reusable map content can contain markers, circles, polylines, polygons, heatmaps, clusters, or other MapConductor components.

```kotlin
@Composable
fun MapContent() {
    Marker(
        state = rememberMarkerState(
            position = GeoPoint(35.6762, 139.6503),
        )
    )

    Polyline(
        state = rememberPolylineState(
            points = listOf(
                GeoPoint(35.6762, 139.6503),
                GeoPoint(35.6895, 139.6917),
            )
        )
    )
}
```

Provider-specific setup is still required, but your application-level map UI can stay much more portable.

---

## Module Overview

| Module            | Artifact                             | Description                                                         |
| ----------------- | ------------------------------------ | ------------------------------------------------------------------- |
| BOM               | `com.mapconductor:mapconductor-bom`  | Aligns MapConductor module versions                                 |
| Core              | `com.mapconductor:core`              | Core abstractions, geometry types, camera state, and overlay states |
| Google Maps       | `com.mapconductor:for-googlemaps`    | Google Maps provider implementation                                 |
| Mapbox            | `com.mapconductor:for-mapbox`        | Mapbox provider implementation                                      |
| HERE Maps         | `com.mapconductor:for-here`          | HERE Maps provider implementation                                   |
| ArcGIS            | `com.mapconductor:for-arcgis`        | ArcGIS provider implementation                                      |
| MapLibre          | `com.mapconductor:for-maplibre`      | MapLibre provider implementation                                    |
| Icons             | `com.mapconductor:icons`             | Compose-based marker icons and info bubble utilities                |
| Heatmap           | `com.mapconductor:heatmap`           | Provider-independent heatmap overlay                                |
| Marker Clustering | `com.mapconductor:marker-clustering` | Marker clustering support                                           |
| GeoJSON Layer     | `com.mapconductor:geojson-layer`     | GeoJSON layer support                                               |

---

## Feature Status

| Feature           | Google Maps |  Mapbox | HERE Maps |  ArcGIS | MapLibre |
| ----------------- | ----------: | ------: | --------: | ------: | -------: |
| Map               |           ✅ |       ✅ |         ✅ |       ✅ |        ✅ |
| Marker            |           ✅ |       ✅ |         ✅ |       ✅ |        ✅ |
| Circle            |           ✅ |       ✅ |         ✅ |       ✅ |        ✅ |
| Polyline          |           ✅ |       ✅ |         ✅ |       ✅ |        ✅ |
| Polygon           |           ✅ |       ✅ |         ✅ |       ✅ |        ✅ |
| Ground Image      |           ✅ |       ✅ |         ✅ |       ✅ |        ✅ |
| Heatmap           |           ✅ |       ✅ |         ✅ |       ✅ |        ✅ |
| Marker Clustering |           ✅ |       ✅ |         ✅ |       ✅ |        ✅ |
| Raster Tile Layer |           ✅ |       ✅ |         ✅ |       ✅ |        ✅ |
| Vector Tile Layer |     Planned | Planned |   Planned | Planned |  Planned |

MapConductor is actively developed. Please check the documentation and release notes for the latest provider-specific behavior and limitations.

---

## Who Is This For?

MapConductor is useful if you are:

* Building an Android app with Jetpack Compose and maps
* Evaluating multiple map providers
* Planning a possible migration from one map SDK to another
* Maintaining map features across different customer or regional requirements
* Building reusable map UI components
* Looking for an open-source abstraction layer for mobile maps

It is especially helpful when you want your application code to describe what should appear on the map, rather than how each provider SDK expects that feature to be implemented.

---

## Documentation

Documentation is available at:

```text
https://docs-android.mapconductor.com/
```

The documentation includes:

* Getting started guides
* Provider-specific setup
* Map view components
* State management
* Event handling
* Core geometry classes
* Marker icons
* Heatmaps
* Marker clustering
* GeoJSON layers

---

## Project Status

MapConductor Android SDK is released and under active development.

The project aims to make map development more flexible, portable, and Compose-friendly across major Android map providers. Some advanced features may still be experimental or may have provider-specific differences.

Feedback, issues, and contributions are welcome.

---

## License

MapConductor Android SDK is released under the Apache License 2.0.
