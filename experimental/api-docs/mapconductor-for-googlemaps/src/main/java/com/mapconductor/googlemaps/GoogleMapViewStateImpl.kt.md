Of course! Here is the high-quality SDK documentation for the provided code snippet, formatted in Markdown.

---

# Google Maps State Management for Compose

This document provides detailed documentation for the state management components used with the Google Maps SDK in a Jetpack Compose environment. The primary entry point for developers is the `rememberGoogleMapViewState` composable function.

## `rememberGoogleMapViewState`

A composable function that creates and remembers an instance of `GoogleMapViewState`. This is the recommended way to manage the state of a map view within a Jetpack Compose application. It ensures that the map's state, such as camera position and map style, is preserved across recompositions, configuration changes, and process death.

### Signature
```kotlin
@Composable
fun rememberGoogleMapViewState(
    mapDesign: GoogleMapDesign = GoogleMapDesign.Normal,
    cameraPosition: MapCameraPositionInterface = MapCameraPosition.Default,
): GoogleMapViewState
```

### Description
This function acts as a state holder factory for your map. It leverages `rememberSaveable` with a custom `GoogleMapViewSaver` to automatically save and restore the essential map state. You should create one instance of this state per map view in your UI.

### Parameters
| Parameter | Type | Description | Default |
|---|---|---|---|
| `mapDesign` | `GoogleMapDesign` | The initial visual style of the map (e.g., Normal, Satellite, Terrain). | `GoogleMapDesign.Normal` |
| `cameraPosition` | `MapCameraPositionInterface` | The initial position and configuration of the map's camera, including location, zoom, tilt, and bearing. | `MapCameraPosition.Default` |

### Returns
| Type | Description |
|---|---|
| `GoogleMapViewState` | A stable `GoogleMapViewState` object that can be passed to a `GoogleMapView` composable and used to programmatically control the map. |

### Example
Here's how to create and use `rememberGoogleMapViewState` in your composable screen.

```kotlin
import androidx.compose.runtime.Composable
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.googlemaps.GoogleMapDesign
import com.mapconductor.googlemaps.rememberGoogleMapViewState

@Composable
fun MyMapScreen() {
    // Create and remember the map state
    val mapState = rememberGoogleMapViewState(
        mapDesign = GoogleMapDesign.Satellite,
        cameraPosition = MapCameraPosition(
            position = GeoPoint(40.7128, -74.0060), // New York City
            zoom = 12.0f
        )
    )

    // The mapState can now be passed to your map view composable
    // and used to control the map.
    //
    // GoogleMapView(
    //     modifier = Modifier.fillMaxSize(),
    //     state = mapState
    // )
}
```

---

## `GoogleMapViewState`

A state-holder class that manages all state-related information for a Google Map view, such as camera position, map design, and padding. It provides properties to read the current state and methods to modify it.

### Description
An instance of `GoogleMapViewState` is the single source of truth for your map's UI. It is created and managed by the `rememberGoogleMapViewState` function. You can use this object to read the map's current camera position or programmatically update the map's camera and design.

### Properties
| Property | Type | Description |
|---|---|---|
| `id` | `String` | A unique, read-only identifier for the map state instance. |
| `cameraPosition` | `MapCameraPosition` | A read-only property representing the current camera position, including target coordinates, zoom, tilt, and bearing. This value is updated automatically as the user interacts with the map. |
| `padding` | `StateFlow<MapPaddings>` | A `StateFlow` that emits the current padding applied to the map. This is useful for ensuring UI elements don't obscure map controls or content. |
| `mapDesignType` | `GoogleMapDesignType` | A mutable property to get or set the current map design. Setting this property will dynamically update the map's visual style. |

### Methods

#### `moveCameraTo`
Moves the map camera to a new position. This method has two overloads for convenience.

**Overload 1: Move to a `GeoPoint`**

Moves the camera to a specific geographical coordinate, keeping the current zoom, tilt, and bearing.

##### Signature
```kotlin
fun moveCameraTo(
    position: GeoPoint,
    durationMillis: Long? = null,
)
```
##### Parameters
| Parameter | Type | Description |
|---|---|---|
| `position` | `GeoPoint` | The target geographical coordinates (latitude and longitude) to center the map on. |
| `durationMillis` | `Long?` | The duration of the camera animation in milliseconds. If `null` or `0`, the camera moves instantly. |

**Overload 2: Move to a `MapCameraPosition`**

Moves the camera to a detailed camera position, allowing you to specify coordinates, zoom, tilt, and bearing simultaneously.

##### Signature
```kotlin
fun moveCameraTo(
    cameraPosition: MapCameraPosition,
    durationMillis: Long? = null,
)
```
##### Parameters
| Parameter | Type | Description |
|---|---|---|
| `cameraPosition` | `MapCameraPosition` | The complete target camera configuration. |
| `durationMillis` | `Long?` | The duration of the camera animation in milliseconds. If `null` or `0`, the camera moves instantly. |

##### Example
```kotlin
// Assuming mapState is obtained from rememberGoogleMapViewState
val mapState: GoogleMapViewState = ...

// Animate camera to a new position over 1 second
val sanFrancisco = GeoPoint(37.7749, -122.4194)
mapState.moveCameraTo(
    position = sanFrancisco,
    durationMillis = 1000L
)

// Instantly move camera with a new zoom level
val newCameraPosition = MapCameraPosition(
    position = sanFrancisco,
    zoom = 15.0f
)
mapState.moveCameraTo(newCameraPosition)
```