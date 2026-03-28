Of course! Here is the high-quality SDK documentation for the provided code snippet, formatted in Markdown.

***

# ArcGIS Map State SDK

This document provides detailed documentation for the state management components of the ArcGIS Map SDK for Jetpack Compose. These components are essential for creating, remembering, and programmatically controlling the state of an ArcGIS map within your application.

## `rememberArcGISMapViewState`

This composable function is the primary entry point for creating and remembering the state of an ArcGIS map view. It leverages `rememberSaveable` to ensure that the map's state, including camera position and map design, is preserved across configuration changes (like screen rotation) and process recreation.

### Signature
```kotlin
@Composable
fun rememberArcGISMapViewState(
    mapDesign: ArcGISDesign = ArcGISDesign.Streets,
    cameraPosition: MapCameraPositionInterface = MapCameraPosition.Default,
): ArcGISMapViewState
```

### Description
Creates an `ArcGISMapViewState` instance that is remembered across recompositions. The state is automatically saved and restored, making it robust against configuration changes. The returned `ArcGISMapViewState` object is the main handle used to interact with and control the map programmatically.

### Parameters
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `mapDesign` | `ArcGISDesign` | The initial base map style to be applied to the map. Defaults to `ArcGISDesign.Streets`. |
| `cameraPosition` | `MapCameraPositionInterface` | The initial camera position of the map, including location, zoom, tilt, and heading. Defaults to `MapCameraPosition.Default`. |

### Returns
An `ArcGISMapViewState` instance that is remembered across recompositions and saved across configuration changes.

### Example
Here's how to create and use `rememberArcGISMapViewState` within a composable screen.

```kotlin
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapconductor.arcgis.map.ArcGISDesign
import com.mapconductor.arcgis.map.rememberArcGISMapViewState
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition

@Composable
fun MyMapScreen() {
    // Create and remember the map view state
    val mapViewState = rememberArcGISMapViewState(
        mapDesign = ArcGISDesign.Topographic,
        cameraPosition = MapCameraPosition(
            position = GeoPoint(latitude = 34.0562, longitude = -117.1956), // ESRI Headquarters
            zoom = 12.0
        )
    )

    // Assuming an ArcGISMapView composable exists that accepts this state
    ArcGISMapView(
        state = mapViewState,
        modifier = Modifier.fillMaxSize()
    )
}
```

---

## `ArcGISMapViewState` Class

A stateful class that holds and manages the properties of an ArcGIS map, such as its camera position and visual style. It serves as the primary interface for programmatically controlling the map's behavior and appearance. An instance of this class is typically created using the `rememberArcGISMapViewState` composable.

### Key Properties

| Property | Type | Description |
| :--- | :--- | :--- |
| `id` | `String` | A unique identifier for the map state instance. |
| `cameraPosition` | `MapCameraPosition` | (Read-only) The current position of the map's camera. This property is updated as the user interacts with the map or when camera movements are initiated programmatically. |
| `mapDesignType` | `ArcGISDesignTypeInterface` | The current visual style (basemap) of the map. This property can be set to a new `ArcGISDesignTypeInterface` to dynamically change the map's appearance. |
| `padding` | `StateFlow<MapPaddingsInterface>` | A `StateFlow` representing the padding applied to the map view. This is useful for informing the map about UI elements that overlay it, ensuring that features like the compass or attribution are not obscured. |

### Methods

#### `moveCameraTo(cameraPosition, durationMillis)`
Moves the map's camera to a specified `MapCameraPosition`.

##### Signature
```kotlin
fun moveCameraTo(
    cameraPosition: MapCameraPosition,
    durationMillis: Long?
)
```

##### Description
Moves the map's camera to a specified `MapCameraPosition`. If a `durationMillis` greater than zero is provided, the camera animates to the new position over the specified duration. Otherwise, the camera moves instantly.

##### Parameters
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `cameraPosition` | `MapCameraPosition` | The target camera position, including location, zoom, tilt, and heading. |
| `durationMillis` | `Long?` | The duration of the camera animation in milliseconds. If `null` or `0`, the camera moves instantly. |

---

#### `moveCameraTo(position, durationMillis)`
A convenience function to move the map's camera to a new geographical coordinate (`GeoPoint`).

##### Signature
```kotlin
fun moveCameraTo(
    position: GeoPoint,
    durationMillis: Long?
)
```

##### Description
Moves the map's camera to center on a new geographical coordinate (`GeoPoint`) while preserving the current zoom, tilt, and heading.

##### Parameters
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `position` | `GeoPoint` | The target geographical coordinate to center the map on. |
| `durationMillis` | `Long?` | The duration of the camera animation in milliseconds. If `null` or `0`, the camera moves instantly. |

---

## `ArcGISMapViewStateInterface` Interface

An interface that defines the public contract for an ArcGIS map view state object.

### Description
This interface extends the generic `MapViewStateInterface` and specifies `ArcGISDesignTypeInterface` as the design type. It ensures a consistent API for map state management across different map providers within the MapConductor ecosystem.

---

## `ArcGISMapViewSaver` Class

A `Saver` implementation for `ArcGISMapViewState`.

### Description
This class is used internally by `rememberArcGISMapViewState` to save and restore the map's state, allowing it to survive configuration changes and process death. Developers typically do not need to interact with this class directly, as its functionality is automatically handled by the `rememberArcGISMapViewState` composable.