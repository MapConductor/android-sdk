# MapboxMapViewScope

## Signature
```kotlin
class MapboxMapViewScope : MapViewScope()
```

## Description
Provides a dedicated scope for interacting with a Mapbox map instance within the Map conductor framework.

This class extends the base `MapViewScope`, inheriting all common map functionalities like camera control, marker management, and UI settings. Its primary purpose is to serve as an extension point for features and APIs that are unique to the Mapbox Maps SDK.

When you are working within the context of a Mapbox map, you will be provided with an instance of `MapboxMapViewScope`. This allows you to access both the shared functionalities from `MapViewScope` and any Mapbox-specific methods that are defined within this class.

## Example
The `MapboxMapViewScope` is typically accessed within the lambda of a map initialization block, such as `MapConductor.showMap`. This provides the correct context to interact with the map.

```kotlin
// Assume MapConductor is configured to use Mapbox
MapConductor.showMap {
    // `this` refers to MapboxMapViewScope

    // You can call common functions from the parent MapViewScope
    moveCamera(
        target = LatLng(35.681236, 139.767125), // Tokyo Station
        zoom = 15.0
    )

    // You can also call Mapbox-specific functions defined in this class.
    // For example (hypothetical):
    // setMapboxStyle("mapbox://styles/mapbox/streets-v11")
}
```