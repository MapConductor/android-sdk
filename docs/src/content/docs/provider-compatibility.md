---
title: Provider Compatibility
---

This page shows which MapConductor components are supported by each map provider. While MapConductor aims to provide a unified API, some features are not available on all map providers due to underlying SDK limitations.

## Component Support Matrix

| Component          | Google Maps | Mapbox | HERE Maps | ArcGIS | MapLibre |
|--------------------|-------------|--------|-----------|--------|----------|
| **Map View**       | ✅         | ✅     | ✅        | ✅     | ✅       |
| **Marker**         | ✅         | ✅     | ✅        | ✅     | ✅       |
| **Circle**         | ✅         | ✅     | ✅        | ✅     | ✅       |
| **Polyline**       | ✅         | ✅     | ✅        | ✅     | ✅       |
| **Polygon**        | ✅         | ✅     | ✅        | ✅     | ✅       |
| **GroundImage**    | ✅         | ❌     | ❌        | ❌     | ❌       |

### Legend

- ✅ **Fully Supported**: Feature is available and tested
- ❌ **Not Supported**: Feature is not available due to SDK limitations
- ⚠️ **Limited Support**: Feature is available with restrictions (none currently)

## Core Components (All Providers)

### Map View Components

All map providers support the basic map view functionality including:

- Camera positioning and movement
- User interaction (pan, zoom, rotate, tilt – where supported)
- Map styling and appearance
- Event handling (tap, long press, camera move events)

In v1.1.0 the following camera callbacks are available (where the underlying SDK exposes them):

- `onCameraMoveStart`
- `onCameraMove`
- `onCameraMoveEnd`

### Marker

All providers support marker functionality with:

- Custom icons and colors
- Click events and interactions
- Drag and drop
- Info windows / info bubbles (where supported by provider)

### Circle, Polyline, Polygon

All providers support:

- Circle overlays with center + radius
- Polylines connecting multiple points
- Polygons with fill and stroke styling

Exact visual appearance may vary slightly per provider due to SDK implementations.

## Provider-Specific Limitations

### GroundImage (Google Maps Only)

GroundImage overlays are only supported on Google Maps.

- **Google Maps**: Native `GroundOverlay` API provides direct support for image overlays with geographic bounds.
- **Mapbox / HERE / ArcGIS / MapLibre**: There is no simple, built-in equivalent in the mobile SDKs. Similar visuals can be implemented with custom layers, but MapConductor does not ship a unified abstraction for those yet.

## Handling Unsupported Features

When using components that aren't supported on all providers, prefer runtime detection and graceful fallbacks.

### Runtime Detection Example

```kotlin
@Composable
fun CompatibilityAwareMap() {
    val mapViewState = rememberGoogleMapViewState()
    val supportsGroundImage = mapViewState is GoogleMapViewStateImpl

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
    MapView(state = mapViewState) {
        // Always supported components
        Marker(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = DefaultIcon()
        )

        Circle(
            center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            radius = 1000.0,
            fillColor = Color.Blue.copy(alpha = 0.3f)
        )

        // Conditionally supported components
        if (supportsGroundImage) {
            GroundImage(
                bounds = imageBounds,
                image = overlayImage,
                opacity = 0.7f
            )
        }
    }
}
```

## Best Practices

1. **Feature Detection** – check provider capabilities before using provider-specific features.
2. **Graceful Degradation** – provide fallbacks (or hide controls) when features are not supported.
3. **Documentation** – document which providers your app supports and any feature gaps.
4. **Testing** – test your app with all target providers to catch visual or behavioral differences.

As new features are added to MapConductor or the underlying provider SDKs, this page should be updated to reflect the latest compatibility status.

