Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

# Map Camera Extensions

This document provides detailed documentation for a set of Kotlin extension functions designed to facilitate conversion between the HERE SDK's map camera representations (`MapCameraUpdate`, `MapCamera.State`) and a custom, platform-agnostic `MapCameraPosition` class. These utilities are essential for creating a consistent camera control abstraction layer in a multi-map environment.

## toMapCameraUpdate

Converts a platform-agnostic `MapCameraPosition` object into a `MapCameraUpdate` object, which can be used to programmatically update the camera of a HERE `MapView`.

This function handles the necessary transformations, including converting the zoom level from a generic representation to the specific altitude-based zoom level used by the HERE SDK.

### Signature

```kotlin
fun MapCameraPosition.toMapCameraUpdate(): MapCameraUpdate
```

### Description

This extension function is called on a `MapCameraPosition` instance. It creates a `MapCameraUpdate` that instructs the map to look at the specified target coordinates (`position`), with the given orientation (`bearing` and `tilt`), and from a calculated distance (zoom level).

### Returns

| Type | Description |
| :--- | :--- |
| `MapCameraUpdate` | An object ready to be applied to the HERE `MapView` camera to change its position, orientation, and zoom. |

### Example

```kotlin
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.here.toMapCameraUpdate
import com.here.sdk.mapview.MapView // Assuming mapView is an initialized MapView instance

// 1. Define a target camera position
val customCameraPosition = MapCameraPosition(
    position = GeoPoint(latitude = 48.8584, longitude = 2.2945), // Eiffel Tower
    zoom = 15.0,
    bearing = 90.0,
    tilt = 25.0
)

// 2. Convert the custom position to a HERE SDK MapCameraUpdate
val cameraUpdate = customCameraPosition.toMapCameraUpdate()

// 3. Apply the update to the map view's camera
mapView.camera.applyUpdate(cameraUpdate)
```

---

## MapCameraPosition.Companion.from

A factory function that creates a `MapCameraPosition` instance from any object implementing the `MapCameraPositionInterface`.

### Signature

```kotlin
fun MapCameraPosition.Companion.from(position: MapCameraPositionInterface): MapCameraPosition
```

### Description

This companion object extension function serves as a convenient constructor. It takes an object that conforms to the `MapCameraPositionInterface` and converts it into a concrete `MapCameraPosition` instance. If the provided object is already a `MapCameraPosition`, it is returned directly to avoid unnecessary object creation.

### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `position` | `MapCameraPositionInterface` | An object containing camera position data (position, zoom, bearing, tilt, etc.). |

### Returns

| Type | Description |
| :--- | :--- |
| `MapCameraPosition` | A new `MapCameraPosition` instance based on the data from the `position` parameter. |

### Example

```kotlin
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapCameraPositionInterface

// Assume you have a class that implements the interface
data class MyCameraState(
    override val position: GeoPoint,
    override val zoom: Double,
    override val bearing: Double,
    override val tilt: Double,
    override val paddings: Any? = null,
    override val visibleRegion: Any? = null
) : MapCameraPositionInterface

// 1. Create an instance of your custom state object
val myState = MyCameraState(
    position = GeoPoint(51.5074, -0.1278), // London
    zoom = 12.0,
    bearing = 0.0,
    tilt = 0.0
)

// 2. Use the factory function to convert it to a standard MapCameraPosition
val mapCameraPosition = MapCameraPosition.from(myState)

println("Converted zoom: ${mapCameraPosition.zoom}") // Outputs: Converted zoom: 12.0
```

---

## toMapCameraPosition

Converts a HERE SDK `MapCamera.State` object into the platform-agnostic `MapCameraPosition` representation.

### Signature

```kotlin
fun MapCamera.State.toMapCameraPosition(): MapCameraPosition
```

### Description

This extension function is called on a `MapCamera.State` instance, which represents the current state of the HERE map camera. It extracts the target coordinates, orientation, and zoom level, converting them into a `MapCameraPosition` object. This is particularly useful for saving the current camera state or synchronizing it with other application components that use the abstract `MapCameraPosition` type. The function also handles the conversion from the HERE SDK's zoom level to the generic zoom representation.

### Returns

| Type | Description |
| :--- | :--- |
| `MapCameraPosition` | A `MapCameraPosition` object representing the current state of the HERE map camera. |

### Example

```kotlin
import com.mapconductor.here.toMapCameraPosition
import com.here.sdk.mapview.MapView // Assuming mapView is an initialized MapView instance

// 1. Get the current state of the map camera
val currentHereCameraState = mapView.camera.state

// 2. Convert the HERE SDK state to the custom MapCameraPosition
val customCameraPosition = currentHereCameraState.toMapCameraPosition()

// Now you can use the customCameraPosition object for other purposes,
// like saving it to preferences or passing it to another module.
println("Current Latitude: ${customCameraPosition.position.latitude}")
println("Current Zoom: ${customCameraPosition.zoom}")
```