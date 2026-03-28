# Mapbox Camera Conversion Utilities

This document provides an overview of the Kotlin extension functions used for converting between Mapbox-specific camera objects (`CameraOptions`, `CameraState`, `CameraChanged`) and a generic `MapCameraPosition` data class. These utilities help in creating a platform-agnostic camera management layer.

---

### `CameraChanged.toMapCameraPosition()`

Converts a `CameraChanged` event object into a `CameraOptions` object.

**Note:** Despite its name, this function returns `CameraOptions`, not `MapCameraPosition`. It serves as a helper to extract camera properties from a `CameraChanged` event. It also converts the zoom level from the Mapbox scale to the Google Maps scale.

#### Signature
```kotlin
fun CameraChanged.toMapCameraPosition(): CameraOptions
```

#### Parameters
| Parameter | Type | Description |
| :--- | :--- | :--- |
| **(receiver)** | `CameraChanged` | The `CameraChanged` event object from which to extract camera state. |

#### Returns
A `CameraOptions` object containing the camera properties from the `CameraChanged` event.

#### Example
```kotlin
val cameraChangedListener = CameraChanged { cameraChangedEvent ->
    // The cameraChangedEvent is a CameraChanged object
    val cameraOptions = cameraChangedEvent.toMapCameraPosition()
    
    // The returned object is of type CameraOptions
    println("Converted to CameraOptions with zoom: ${cameraOptions.zoom}")
}
```

---

### `MapCameraPosition.toCameraOptions()`

Converts a generic `MapCameraPosition` object into a Mapbox-specific `CameraOptions` object. This is useful for applying a defined camera position to the Mapbox map. The function converts the zoom level from the Google Maps scale to the Mapbox scale.

**Note:** The conversion for `paddings` is currently unimplemented (`TODO` in the source code).

#### Signature
```kotlin
fun MapCameraPosition.toCameraOptions(): CameraOptions
```

#### Parameters
| Parameter | Type | Description |
| :--- | :--- | :--- |
| **(receiver)** | `MapCameraPosition` | The source `MapCameraPosition` object to convert. |

#### Returns
A `CameraOptions` object that can be used with `MapboxMap.setCamera()`.

#### Example
```kotlin
val mapCameraPosition = MapCameraPosition(
    position = GeoPoint.fromLongLat(-74.0060, 40.7128), // New York City
    zoom = 12.0,
    tilt = 30.0,
    bearing = 45.0
)

val cameraOptions = mapCameraPosition.toCameraOptions()

// Use the result to set the camera on a MapboxMap instance
// mapboxMap.setCamera(cameraOptions)
```

---

### `MapCameraPosition.toCameraState()`

Converts a `MapCameraPosition` object into a Mapbox `CameraState` object. `CameraState` represents a snapshot of the map's camera properties. This function converts the zoom level from the Google Maps scale to the Mapbox scale and initializes padding to zero.

#### Signature
```kotlin
fun MapCameraPosition.toCameraState(): CameraState
```

#### Parameters
| Parameter | Type | Description |
| :--- | :--- | :--- |
| **(receiver)** | `MapCameraPosition` | The source `MapCameraPosition` object to convert. |

#### Returns
A `CameraState` object representing the given camera position.

#### Example
```kotlin
val mapCameraPosition = MapCameraPosition(
    position = GeoPoint.fromLongLat(-0.1278, 51.5074), // London
    zoom = 14.0,
    tilt = 0.0,
    bearing = 0.0
)

val cameraState = mapCameraPosition.toCameraState()

println("Created CameraState with center: ${cameraState.center}")
```

---

### `MapCameraPosition.Companion.from()`

A factory function that creates a `MapCameraPosition` instance from any object that implements the `MapCameraPositionInterface`. This provides a standardized way to convert different camera position representations into a concrete `MapCameraPosition`. If the input object is already a `MapCameraPosition`, it is returned directly to avoid redundant object creation.

#### Signature
```kotlin
fun MapCameraPosition.Companion.from(cameraPosition: MapCameraPositionInterface): MapCameraPosition
```

#### Parameters
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `cameraPosition` | `MapCameraPositionInterface` | An object conforming to the `MapCameraPositionInterface`. |

#### Returns
A `MapCameraPosition` instance based on the provided interface implementation.

#### Example
```kotlin
// Assume MyCustomCameraPosition implements MapCameraPositionInterface
// data class MyCustomCameraPosition(...) : MapCameraPositionInterface

val customCamera = MyCustomCameraPosition(
    position = GeoPoint.fromLongLat(139.6917, 35.6895), // Tokyo
    zoom = 10.0,
    bearing = 0.0,
    tilt = 0.0,
    paddings = null,
    visibleRegion = null
)

// Use the factory function to create a standard MapCameraPosition
val mapCameraPosition = MapCameraPosition.from(customCamera)

println("Converted to MapCameraPosition with zoom: ${mapCameraPosition.zoom}")
```

---

### `CameraOptions.toMapCameraPosition()`

Converts a Mapbox `CameraOptions` object into a generic `MapCameraPosition` object. This is useful for abstracting camera details away from the Mapbox SDK. The function handles nullable properties in `CameraOptions` by providing sensible defaults and converts the zoom level from the Mapbox scale to the Google Maps scale.

#### Signature
```kotlin
fun CameraOptions.toMapCameraPosition(): MapCameraPosition
```

#### Parameters
| Parameter | Type | Description |
| :--- | :--- | :--- |
| **(receiver)** | `CameraOptions` | The source Mapbox `CameraOptions` object. |

#### Returns
A new `MapCameraPosition` object populated with data from the `CameraOptions`.

#### Example
```kotlin
val cameraOptions = CameraOptions.Builder()
    .center(Point.fromLngLat(2.3522, 48.8566)) // Paris
    .zoom(11.0) // Mapbox zoom level
    .build()

val mapCameraPosition = cameraOptions.toMapCameraPosition()

// The zoom level is now converted to the Google Maps scale
println("Converted to MapCameraPosition with zoom: ${mapCameraPosition.zoom}")
```

---

### `CameraState.toMapCameraPosition()`

Converts a Mapbox `CameraState` object into a generic `MapCameraPosition`. This allows you to capture the current state of the Mapbox map's camera and store it in a platform-agnostic format. The zoom level is converted from the Mapbox scale to the Google Maps scale.

#### Signature
```kotlin
fun CameraState.toMapCameraPosition(): MapCameraPosition
```

#### Parameters
| Parameter | Type | Description |
| :--- | :--- | :--- |
| **(receiver)** | `CameraState` | The source Mapbox `CameraState` object. |

#### Returns
A new `MapCameraPosition` object representing the camera's state.

#### Example
```kotlin
// Typically, you would get CameraState from a MapboxMap instance
// val currentState = mapboxMap.cameraState

// For demonstration, we create one manually
val cameraState = CameraState(
    Point.fromLngLat(151.2093, -33.8688), // Sydney
    EdgeInsets(0.0, 0.0, 0.0, 0.0),
    13.0, // Mapbox zoom level
    0.0,
    0.0
)

val mapCameraPosition = cameraState.toMapCameraPosition()

// The zoom level is now converted
println("Converted to MapCameraPosition with position: ${mapCameraPosition.position}")
```