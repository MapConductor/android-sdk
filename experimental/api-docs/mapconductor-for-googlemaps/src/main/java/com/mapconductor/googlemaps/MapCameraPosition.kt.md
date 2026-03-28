Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

This document provides details on a set of Kotlin extension functions designed to facilitate conversion between the platform-agnostic `MapCameraPosition` and the Google Maps specific `CameraPosition`. These utilities are essential for interoperability within the MapConductor framework when using the Google Maps provider.

## `toCameraPosition`

### Signature

```kotlin
fun MapCameraPosition.toCameraPosition(): CameraPosition
```

### Description

Converts a platform-agnostic `MapCameraPosition` object into a Google Maps `CameraPosition` object. This is useful when you need to apply a camera state defined in the core library to an actual Google Map view.

The function maps the `position`, `zoom`, `tilt`, and `bearing` from the `MapCameraPosition` to the corresponding fields in the `CameraPosition.Builder`.

### Returns

| Type | Description |
| :--- | :--- |
| `CameraPosition` | A new `CameraPosition` instance configured with the properties of the source `MapCameraPosition`. |

### Example

```kotlin
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.googlemaps.toCameraPosition

// 1. Create a platform-agnostic MapCameraPosition
val mapCameraPos = MapCameraPosition(
    position = GeoPoint(latitude = 40.7128, longitude = -74.0060),
    zoom = 12.0,
    tilt = 30.0,
    bearing = 45.0
)

// 2. Convert it to a Google Maps specific CameraPosition
val googleCameraPosition = mapCameraPos.toCameraPosition()

// googleCameraPosition can now be used with a GoogleMap instance, for example:
// googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(googleCameraPosition))

println("Zoom: ${googleCameraPosition.zoom}")       // Outputs: Zoom: 12.0
println("Tilt: ${googleCameraPosition.tilt}")       // Outputs: Tilt: 30.0
println("Bearing: ${googleCameraPosition.bearing}") // Outputs: Bearing: 45.0
```

---

## `MapCameraPosition.from`

### Signature

```kotlin
fun MapCameraPosition.Companion.from(position: MapCameraPositionInterface): MapCameraPosition
```

### Description

A factory function that creates a concrete `MapCameraPosition` instance from any object that implements the `MapCameraPositionInterface`.

If the provided `position` is already a `MapCameraPosition`, it is returned directly. Otherwise, a new `MapCameraPosition` is constructed by copying the properties from the source interface. This ensures type safety and provides a consistent object to work with.

### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `position` | `MapCameraPositionInterface` | The source camera position object to convert from. |

### Returns

| Type | Description |
| :--- | :--- |
| `MapCameraPosition` | A concrete `MapCameraPosition` instance. |

### Example

```kotlin
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapCameraPositionInterface
import com.mapconductor.googlemaps.from

// Assume you have a custom implementation of MapCameraPositionInterface
class CustomCameraPosition : MapCameraPositionInterface {
    override val position: GeoPoint = GeoPoint(34.0522, -118.2437)
    override val zoom: Double = 10.0
    override val bearing: Double = 0.0
    override val tilt: Double = 0.0
    // ... other properties
}

val customPosition = CustomCameraPosition()

// Create a concrete MapCameraPosition from the interface
val mapCameraPosition = MapCameraPosition.from(customPosition)

println(mapCameraPosition.position.latitude) // Outputs: 34.0522
println(mapCameraPosition.zoom)              // Outputs: 10.0
```

---

## `toMapCameraPosition`

### Signature

```kotlin
fun CameraPosition.toMapCameraPosition(paddings: MapPaddingsInterface = MapPaddings.Zeros): MapCameraPosition
```

### Description

Converts a Google Maps `CameraPosition` object into a platform-agnostic `MapCameraPosition` object. This is typically used when capturing the current state of the map view to be used in the core, platform-independent logic.

This function performs a key calculation: it converts the Google Maps `zoom` level into an `altitude` value for the `GeoPoint` in the resulting `MapCameraPosition`. The conversion takes into account the zoom level, latitude, and tilt. The `visibleRegion` property of the returned object is set to `null`.

### Parameters

| Parameter | Type | Default Value | Description |
| :--- | :--- | :--- | :--- |
| `paddings` | `MapPaddingsInterface` | `MapPaddings.Zeros` | Optional map paddings to associate with the resulting `MapCameraPosition`. |

### Returns

| Type | Description |
| :--- | :--- |
| `MapCameraPosition` | A new `MapCameraPosition` instance representing the state of the source `CameraPosition`. |

### Example

```kotlin
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.mapconductor.googlemaps.toMapCameraPosition

// 1. Create a Google Maps CameraPosition (e.g., from a map listener)
val googleCameraPosition = CameraPosition.Builder()
    .target(LatLng(48.8566, 2.3522))
    .zoom(15f)
    .tilt(45f)
    .bearing(90f)
    .build()

// 2. Convert it to a platform-agnostic MapCameraPosition
val mapCameraPos = googleCameraPosition.toMapCameraPosition()

// The resulting object can be used in platform-agnostic business logic
println("Latitude: ${mapCameraPos.position.latitude}") // Outputs: Latitude: 48.8566
println("Zoom: ${mapCameraPos.zoom}")                   // Outputs: Zoom: 15.0
println("Altitude: ${mapCameraPos.position.altitude}")  // Outputs a calculated altitude, e.g., 385.0
```