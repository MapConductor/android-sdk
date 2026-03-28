Of course! Here is the high-quality SDK documentation for the provided Kotlin code snippet.

***

# Camera Position Conversion Utilities

This document outlines a set of extension functions designed to facilitate seamless conversion between the application-specific `MapCameraPosition` and the MapLibre SDK's `CameraPosition`. These utilities are essential for maintaining a consistent camera state representation across different parts of the application and the underlying map library.

---

### `toCameraPosition`

Converts a generic `MapCameraPosition` object into a MapLibre-specific `CameraPosition` object.

**Signature**
```kotlin
fun MapCameraPosition.toCameraPosition(): CameraPosition
```

**Description**
This extension function translates the properties of a `MapCameraPosition` instance (such as `position`, `zoom`, `tilt`, and `bearing`) into a `CameraPosition` object that can be directly used by the MapLibre map controller. It handles the necessary conversions, including transforming the `GeoPoint` to a `LatLng` and adjusting the zoom level from a generic representation to the one expected by MapLibre.

**Note:** The `padding` property is not currently converted.

**Returns**
| Type | Description |
|---|---|
| `CameraPosition` | A new `CameraPosition` instance configured with the properties of the source `MapCameraPosition`. |

**Example**
```kotlin
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.maplibre.toCameraPosition
import org.maplibre.android.maps.MapLibreMap

// Assuming 'mapLibreMap' is an initialized MapLibreMap instance

// 1. Create a generic MapCameraPosition
val genericCameraPosition = MapCameraPosition(
    position = GeoPoint.fromLongLat(-74.0060, 40.7128), // New York City
    zoom = 12.0,
    bearing = 45.0,
    tilt = 30.0
)

// 2. Convert it to a MapLibre CameraPosition
val maplibreCameraPosition = genericCameraPosition.toCameraPosition()

// 3. Use the converted position to move the map camera
// mapLibreMap.cameraPosition = maplibreCameraPosition
```

---

### `MapCameraPosition.Companion.from`

A factory function to create a `MapCameraPosition` instance from any object implementing the `MapCameraPositionInterface`.

**Signature**
```kotlin
fun MapCameraPosition.Companion.from(cameraPosition: MapCameraPositionInterface): MapCameraPosition
```

**Description**
This function acts as a safe and efficient constructor. It takes any object that conforms to the `MapCameraPositionInterface` and produces a concrete `MapCameraPosition` instance. If the provided object is already a `MapCameraPosition`, it is returned directly to avoid unnecessary object creation. Otherwise, a new `MapCameraPosition` is created by copying the properties from the interface.

**Parameters**
| Parameter | Type | Description |
|---|---|---|
| `cameraPosition` | `MapCameraPositionInterface` | The camera position object to convert into a concrete `MapCameraPosition`. |

**Returns**
| Type | Description |
|---|---|
| `MapCameraPosition` | A concrete `MapCameraPosition` instance. |

**Example**
```kotlin
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapCameraPositionInterface
import com.mapconductor.core.map.VisibleRegion

// 1. Define a custom class that implements the interface
data class CustomCameraState(
    override val position: GeoPoint,
    override val zoom: Double,
    override val bearing: Double,
    override val tilt: Double,
    override val visibleRegion: VisibleRegion? = null
) : MapCameraPositionInterface

// 2. Create an instance of the custom class
val customState = CustomCameraState(
    position = GeoPoint.fromLongLat(2.3522, 48.8566), // Paris
    zoom = 11.0,
    bearing = 0.0,
    tilt = 15.0
)

// 3. Create a MapCameraPosition from the custom state
val genericCameraPosition = MapCameraPosition.from(customState)

// Now 'genericCameraPosition' is a standard MapCameraPosition instance
println(genericCameraPosition.zoom) // Outputs: 11.0
```

---

### `toMapCameraPosition`

Converts a MapLibre `CameraPosition` object back into a generic `MapCameraPosition` object.

**Signature**
```kotlin
fun CameraPosition.toMapCameraPosition(): MapCameraPosition
```

**Description**
This extension function performs the reverse operation of `toCameraPosition`. It takes a MapLibre `CameraPosition` and converts it into the application's generic `MapCameraPosition` representation. It safely handles potentially null properties from the MapLibre object by providing default values:
- `target`: Defaults to `GeoPoint(0.0, 0.0)` if null.
- `bearing`: Defaults to `0.0` if null.
- `tilt`: Defaults to `0.0` if null.
- `visibleRegion`: Is always set to `null`.

The function also converts the MapLibre-specific zoom level back to the application's generic zoom representation.

**Returns**
| Type | Description |
|---|---|
| `MapCameraPosition` | A new `MapCameraPosition` instance populated with data from the MapLibre `CameraPosition`. |

**Example**
```kotlin
import com.mapconductor.maplibre.toMapCameraPosition
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng

// 1. Create a MapLibre CameraPosition
val maplibreCameraPosition = CameraPosition.Builder()
    .target(LatLng(35.6895, 139.6917)) // Tokyo
    .zoom(14.0) // MapLibre zoom level
    .bearing(90.0)
    .tilt(25.0)
    .build()

// 2. Convert it to the generic MapCameraPosition
val genericCameraPosition = maplibreCameraPosition.toMapCameraPosition()

// 'genericCameraPosition' can now be used within the application's core logic
println(genericCameraPosition.position) 
// Outputs a GeoPoint representation of Tokyo's coordinates
```