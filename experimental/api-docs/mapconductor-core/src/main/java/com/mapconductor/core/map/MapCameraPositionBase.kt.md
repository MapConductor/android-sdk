Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

# MapCameraPosition

The `MapCameraPosition` class is an immutable representation of the map's camera state. It encapsulates all the visual properties of the map's viewpoint, such as its geographical location, zoom level, bearing (rotation), and tilt (pitch).

This class is essential for controlling and inspecting the map's current view. You can use it to programmatically move the map to a specific state or to retrieve the current camera parameters.

## Constructor

### Signature

```kotlin
MapCameraPosition(
    position: GeoPointInterface,
    zoom: Double = 0.0,
    bearing: Double = 0.0,
    tilt: Double = 0.0,
    paddings: MapPaddingsInterface? = MapPaddings.Companion.Zeros,
    visibleRegion: VisibleRegion? = null
)
```

### Description

Creates a new `MapCameraPosition` instance. While `position` is required, other parameters are optional and have sensible defaults.

### Parameters

| Parameter | Type | Description |
| --- | --- | --- |
| `position` | `GeoPointInterface` | The geographical coordinate (`latitude`, `longitude`) at the center of the camera's view. |
| `zoom` | `Double` | **Optional.** The zoom level of the camera. Higher values are more zoomed in. Defaults to `0.0`. |
| `bearing` | `Double` | **Optional.** The camera's orientation, in degrees, clockwise from North. Defaults to `0.0`. |
| `tilt` | `Double` | **Optional.** The camera's tilt angle, in degrees, from the nadir (straight down). Defaults to `0.0`. |
| `paddings` | `MapPaddingsInterface?` | **Optional.** The padding applied to the map view, which affects the apparent center. Defaults to zero padding. |
| `visibleRegion` | `VisibleRegion?` | **Optional.** The geographic region currently visible on the screen. Defaults to `null`. |

## Properties

| Property | Type | Description |
| --- | --- | --- |
| `position` | `GeoPoint` | The geographical coordinate at the center of the camera's view. |
| `zoom` | `Double` | The zoom level of the camera. |
| `bearing` | `Double` | The camera's orientation, in degrees, clockwise from North. |
| `tilt` | `Double` | The camera's tilt angle, in degrees, from the nadir. |
| `paddings` | `MapPaddingsInterface?` | The padding applied to the map view. |
| `visibleRegion` | `VisibleRegion?` | The geographic region currently visible on the screen. |

## Companion Object

### Default

Provides a default `MapCameraPosition` instance, centered at latitude/longitude `(0,0)` with `0.0` zoom, bearing, and tilt.

#### Signature

```kotlin
val Default: MapCameraPosition
```

#### Example

```kotlin
// Set the map camera to a default, world-out view
map.setCameraPosition(MapCameraPosition.Default)
```

## Methods

### equals

Compares this `MapCameraPosition` with another for equality. This method uses a small tolerance (`1e-2`) when comparing `zoom`, `bearing`, and `tilt` values to account for floating-point inaccuracies. The `position` is compared exactly.

#### Signature

```kotlin
fun equals(other: MapCameraPositionInterface): Boolean
```

#### Parameters

| Parameter | Type | Description |
| --- | --- | --- |
| `other` | `MapCameraPositionInterface` | The other camera position to compare against. |

#### Returns

`Boolean` - `true` if the positions are considered equal within the defined tolerance, `false` otherwise.

#### Example

```kotlin
val position1 = MapCameraPosition(position = GeoPoint(40.7128, -74.0060), zoom = 12.001)
val position2 = MapCameraPosition(position = GeoPoint(40.7128, -74.0060), zoom = 12.002)
val position3 = MapCameraPosition(position = GeoPoint(40.7128, -74.0060), zoom = 12.05)

// true, because the zoom difference is within the tolerance
val areEqual1 = position1.equals(position2)
println("position1 equals position2: $areEqual1") // true

// false, because the zoom difference is outside the tolerance
val areEqual2 = position1.equals(position3)
println("position1 equals position3: $areEqual2") // false
```

### copy

Creates a new `MapCameraPosition` instance by copying the current object's properties and optionally overriding specified values. This is useful for creating a modified state from an existing one without altering the original immutable object.

#### Signature

```kotlin
fun copy(
    position: GeoPointInterface? = this.position,
    zoom: Double? = this.zoom,
    bearing: Double? = this.bearing,
    tilt: Double? = this.tilt,
    paddings: MapPaddingsInterface? = this.paddings,
    visibleRegion: VisibleRegion? = this.visibleRegion
): MapCameraPosition
```

#### Parameters

| Parameter | Type | Description |
| --- | --- | --- |
| `position` | `GeoPointInterface?` | **Optional.** A new geographical coordinate for the camera. If `null`, the current `position` is used. |
| `zoom` | `Double?` | **Optional.** A new zoom level. If `null`, the current `zoom` is used. |
| `bearing` | `Double?` | **Optional.** A new bearing. If `null`, the current `bearing` is used. |
| `tilt` | `Double?` | **Optional.** A new tilt angle. If `null`, the current `tilt` is used. |
| `paddings` | `MapPaddingsInterface?` | **Optional.** New map paddings. If `null`, the current `paddings` are used. |
| `visibleRegion` | `VisibleRegion?` | **Optional.** A new visible region. If `null`, the current `visibleRegion` is used. |

#### Returns

`MapCameraPosition` - A new `MapCameraPosition` instance with the updated properties.

#### Example

```kotlin
val initialPosition = MapCameraPosition(
    position = GeoPoint(34.0522, -118.2437), // Los Angeles
    zoom = 10.0
)

// Create a new camera position with a higher zoom level and a new bearing
val updatedPosition = initialPosition.copy(zoom = 14.0, bearing = 45.0)

println("Initial zoom: ${initialPosition.zoom}")       // 10.0
println("Updated zoom: ${updatedPosition.zoom}")       // 14.0
println("Initial bearing: ${initialPosition.bearing}") // 0.0
println("Updated bearing: ${updatedPosition.bearing}") // 45.0
```