Of course! Here is the high-quality SDK documentation for the provided code snippet, formatted in Markdown.

***

## MapLibre State Management for Compose

This document provides an overview of the state management components for the MapLibre map view in a Jetpack Compose environment. The primary entry point for developers is the `rememberMapLibreMapViewState` composable function.

### `rememberMapLibreMapViewState`

A Jetpack Compose composable function that creates and remembers an instance of `MapLibreViewState`.

It is the recommended way to create a state object for your map. This function ensures that the map's state, including camera position and style, is correctly preserved across recompositions, configuration changes, and process death.

#### Signature

```kotlin
@Composable
fun rememberMapLibreMapViewState(
    mapDesign: MapLibreMapDesignTypeInterface = MapLibreDesign.DemoTiles,
    cameraPosition: MapCameraPositionInterface = MapCameraPosition.Default,
): MapLibreViewState
```

#### Parameters

| Parameter        | Type                           | Description                                                                                             |
|------------------|--------------------------------|---------------------------------------------------------------------------------------------------------|
| `mapDesign`      | `MapLibreMapDesignTypeInterface` | The initial visual style and theme for the map. Defaults to `MapLibreDesign.DemoTiles`.                 |
| `cameraPosition` | `MapCameraPositionInterface`   | The initial camera settings, including location, zoom, tilt, and bearing. Defaults to `MapCameraPosition.Default`. |

#### Returns

A stable `MapLibreViewState` instance that can be used to control the map and is automatically saved and restored.

#### Example

Here is a basic example of how to create a map state and pass it to a `MapLibreMapView` composable.

```kotlin
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.maplibre.MapLibreDesign
import com.mapconductor.maplibre.rememberMapLibreMapViewState

@Composable
fun MyMapScreen() {
    // Create and remember the map state
    val mapState = rememberMapLibreMapViewState(
        mapDesign = MapLibreDesign.OsmBright,
        cameraPosition = MapCameraPosition(
            position = GeoPoint(48.8584, 2.2945), // Paris
            zoom = 14.0
        )
    )

    // The state is then passed to the MapView composable (assumed to exist)
    /*
    MapLibreMapView(
        state = mapState,
        modifier = Modifier.fillMaxSize()
    )
    */
}
```

---

### `MapLibreViewState`

A state-holder class that manages the properties and interactions for a MapLibre map view. It holds the current camera position and map design and provides methods to programmatically control the map. An instance of this class is typically created and managed by the `rememberMapLibreMapViewState` composable.

#### Class Signature

```kotlin
class MapLibreViewState(
    mapDesignType: MapLibreMapDesignTypeInterface,
    override val id: String,
    cameraPosition: MapCameraPosition = MapCameraPosition.Default,
)
```

#### Properties

| Property         | Type                             | Description                                                                                                                            |
|------------------|----------------------------------|----------------------------------------------------------------------------------------------------------------------------------------|
| `cameraPosition` | `MapCameraPosition`              | (Read-only) Gets the current camera position of the map, including location, zoom, tilt, and bearing.                                  |
| `mapDesignType`  | `MapLibreMapDesignTypeInterface` | (Read-Write) Gets or sets the current map design/style. Setting a new value will asynchronously update the map's visual appearance. |

#### Methods

##### `moveCameraTo(position: GeoPoint, ...)`

Moves the map camera to a specific geographic coordinate, preserving the current zoom, tilt, and bearing.

**Signature**
```kotlin
fun moveCameraTo(
    position: GeoPoint,
    durationMillis: Long? = null,
)
```

**Parameters**
| Parameter        | Type    | Description                                                                                             |
|------------------|---------|---------------------------------------------------------------------------------------------------------|
| `position`       | `GeoPoint` | The target geographic coordinate (`latitude`, `longitude`) to center the map on.                        |
| `durationMillis` | `Long?` | The duration of the camera animation in milliseconds. If `null` or `0`, the camera moves instantly. |

##### `moveCameraTo(cameraPosition: MapCameraPosition, ...)`

Moves the map camera to a new, fully specified camera position.

**Signature**
```kotlin
fun moveCameraTo(
    cameraPosition: MapCameraPosition,
    durationMillis: Long? = null,
)
```

**Parameters**
| Parameter        | Type                | Description                                                                                             |
|------------------|---------------------|---------------------------------------------------------------------------------------------------------|
| `cameraPosition` | `MapCameraPosition` | The target camera state, including position, zoom, tilt, and bearing.                                   |
| `durationMillis` | `Long?`             | The duration of the camera animation in milliseconds. If `null` or `0`, the camera moves instantly. |

---

### `MapLibreMapViewSaver`

A `Saver` implementation for `MapLibreViewState`. It handles the serialization and deserialization of the map state, enabling it to be persisted via `rememberSaveable`.

This class is used internally by `rememberMapLibreMapViewState` and developers typically do not need to interact with it directly unless implementing custom state-saving logic.

#### Class Signature

```kotlin
class MapLibreMapViewSaver : BaseMapViewSaver<MapLibreViewState>()
```

#### Description

`MapLibreMapViewSaver` saves the essential properties of `MapLibreViewState`, such as the camera position and map style URL, into a `Bundle`. It can then reconstruct the `MapLibreViewState` from this `Bundle` when the composable is recreated, ensuring a seamless user experience across configuration changes.