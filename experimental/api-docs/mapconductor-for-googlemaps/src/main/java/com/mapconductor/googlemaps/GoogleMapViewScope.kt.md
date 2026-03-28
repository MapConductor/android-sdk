# GoogleMapViewScope

## Signature

```kotlin
class GoogleMapViewScope : MapViewScope()
```

## Description

`GoogleMapViewScope` is a specialized scope class for the Google Maps implementation within the MapConductor framework. It extends the base `MapViewScope`, inheriting all common map functionalities, while also providing access to features unique to the Google Maps SDK.

This scope is the context for map configuration and control when using Google Maps as the provider. Use it to access Google-specific functionalities that are not part of the common map abstraction layer, such as Street View.

## Example

The `GoogleMapViewScope` is provided as the receiver (`this`) within the map setup block. You can call common methods from the parent `MapViewScope` as well as Google-specific methods defined in this class.

```kotlin
// Assume 'mapFragment' is a Fragment containing the map
mapFragment.getMapAsync {
    // 'this' is an instance of GoogleMapViewScope

    // Example of calling a common function from the parent MapViewScope
    setCameraPosition(
        latitude = 35.681236,
        longitude = 139.767125,
        zoom = 15.0
    )

    // Example of calling a hypothetical Google Maps-specific function
    // that would be defined in this class.
    launchStreetView(
        latitude = 35.681236,
        longitude = 139.767125
    )
}
```