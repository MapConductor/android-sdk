Of course! Here is a high-quality SDK document for the provided code snippet, formatted in Markdown.

# Mapbox Map State SDK

This document provides detailed documentation for the Mapbox Map State management components, designed for use with Jetpack Compose. The primary entry point is the `rememberMapboxMapViewState` composable function, which creates and manages the state of a Mapbox map view.

---

## `rememberMapboxMapViewState`

A Jetpack Compose function that creates and remembers a `MapboxViewState` instance. This function is lifecycle-aware and preserves the map's state across recompositions and configuration changes (e.g., screen rotation) by leveraging `rememberSaveable`.

### Signature

```kotlin
@Composable
fun rememberMapboxMapViewState(
    mapDesign: MapboxDesignType = Standard,
    cameraPosition: MapCameraPositionInterface = MapCameraPosition.Default,
): MapboxViewState
```

### Description

Use this composable function within your UI to instantiate a `MapboxViewState` object. This state object can then be passed to a `MapboxMapView` composable (not shown in the snippet) and used to programmatically control the map's camera and style.

### Parameters

| Parameter | Type | Description |
|---|---|---|
| `mapDesign` | `MapboxDesignType` | The initial map style to be applied. Defaults to `MapboxMapDesign.Standard`. |
| `cameraPosition` | `MapCameraPositionInterface` | The initial camera position of the map, including location, zoom, tilt, and bearing. Defaults to `MapCameraPosition.Default`. |

### Returns

| Type | Description |
|---|---|
| `MapboxViewState` | A state object that holds the map's configuration and provides methods to control it. |

### Example

The following example demonstrates how to create a `MapboxViewState` and use it to control the map from a button click.

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.material.Button
import androidx.compose.material.Text
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.mapbox.rememberMapboxMapViewState

@Composable
fun MyMapScreen() {
    // 1. Create and remember the map state.
    // The state will be preserved across recompositions and screen rotations.
    val mapState = rememberMapboxMapViewState(
        cameraPosition = MapCameraPosition(
            position = GeoPoint(latitude = 40.7128, longitude = -74.0060), // New York City
            zoom = 12.0
        )
    )

    // 2. Pass the state to your MapView composable (implementation assumed).
    // MapboxMapView(state = mapState)

    // 3. Use the state object to control the map programmatically.
    Button(onClick = {
        val sanFrancisco = GeoPoint(latitude = 37.7749, longitude = -122.4194)
        // Animate the camera to a new location over 1.5 seconds.
        mapState.moveCameraTo(sanFrancisco, durationMillis = 1500L)
    }) {
        Text("Go to San Francisco")
    }
}
```

---

## `MapboxViewState` Class

A state holder and controller for a Mapbox map. It manages the map's camera position and design style and provides methods to manipulate the map view. An instance of this class is created using the `rememberMapboxMapViewState` composable.

### Properties

| Property | Type | Description |
|---|---|---|
| `mapDesignType` | `var mapDesignType: MapboxDesignType` | Gets or sets the current design style of the map. Setting this property will update the map's appearance in real-time. |
| `cameraPosition` | `val cameraPosition: MapCameraPosition` | A read-only property representing the current camera position of the map (location, zoom, tilt, bearing). This value is updated automatically as the user interacts with the map. |

### Methods

#### `moveCameraTo(position: GeoPoint, ...)`

Moves the map camera to a specific geographic coordinate, preserving the current zoom, tilt, and bearing.

**Signature**
```kotlin
fun moveCameraTo(
    position: GeoPoint,
    durationMillis: Long? = null,
)
```

**Parameters**
| Parameter | Type | Description |
|---|---|---|
| `position` | `GeoPoint` | The geographic coordinate (`latitude`, `longitude`) to center the map camera on. |
| `durationMillis` | `Long?` | The duration of the camera animation in milliseconds. If `null` or `0`, the camera moves instantly. Defaults to `null`. |

#### `moveCameraTo(cameraPosition: MapCameraPosition, ...)`

Moves the map camera to a new, fully specified camera position. This allows you to change the location, zoom, tilt, and bearing simultaneously.

**Signature**
```kotlin
fun moveCameraTo(
    cameraPosition: MapCameraPosition,
    durationMillis: Long? = null,
)
```

**Parameters**
| Parameter | Type | Description |
|---|---|---|
| `cameraPosition` | `MapCameraPosition` | The complete target camera state, including position, zoom, tilt, and bearing. |
| `durationMillis` | `Long?` | The duration of the camera animation in milliseconds. If `null` or `0`, the camera moves instantly. Defaults to `null`. |