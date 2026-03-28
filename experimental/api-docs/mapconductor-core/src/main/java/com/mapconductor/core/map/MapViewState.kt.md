Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

# SDK Documentation

This document provides detailed information about the core map interfaces, classes, and enums for the Map Conductor SDK.

## `InitState`

### Signature

```kotlin
enum class InitState
```

### Description

Represents the various initialization states of the map SDK and its components. This enum is used to track the progress of the map setup, from the initial state to a fully rendered map or a failure state.

### Enum Values

| Value            | Description                                                              |
| ---------------- | ------------------------------------------------------------------------ |
| `NotStarted`     | The initialization process has not yet begun.                            |
| `Initializing`   | The SDK is currently in the process of initializing.                     |
| `SdkInitialized` | The core SDK has been successfully initialized.                          |
| `MapViewCreated` | The underlying native map view has been created.                         |
| `MapCreated`     | The map object is fully created and ready for interaction.               |
| `Failed`         | An error occurred at some point during the initialization process.       |

---

## `MapViewStateInterface<ActualMapDesignType>`

### Signature

```kotlin
interface MapViewStateInterface<ActualMapDesignType>
```

### Description

Defines the contract for managing the state of a map view. This includes properties like camera position and map style, as well as methods for manipulating the camera. It is a generic interface that allows for different types of map designs or styles.

### Type Parameters

| Name                  | Description                                                                 |
| --------------------- | --------------------------------------------------------------------------- |
| `ActualMapDesignType` | The generic type representing the specific map design or style being used. |

### Properties

| Property         | Type                    | Description                                                              |
| ---------------- | ----------------------- | ------------------------------------------------------------------------ |
| `id`             | `String`                | A unique identifier for the map view state.                              |
| `cameraPosition` | `MapCameraPosition`     | The current position of the map's camera (location, zoom, tilt, bearing). |
| `mapDesignType`  | `ActualMapDesignType`   | The current design or style applied to the map.                          |

### Methods

#### `moveCameraTo(cameraPosition)`

##### Signature

```kotlin
fun moveCameraTo(
    cameraPosition: MapCameraPosition,
    durationMillis: Long? = 0,
)
```

##### Description

Moves the map camera to a specified `MapCameraPosition` over a given duration.

##### Parameters

| Parameter        | Type                | Default | Description                                                              |
| ---------------- | ------------------- | ------- | ------------------------------------------------------------------------ |
| `cameraPosition` | `MapCameraPosition` | -       | The target camera position, including coordinates, zoom, tilt, and bearing. |
| `durationMillis` | `Long?`             | `0`     | The duration of the camera animation in milliseconds. A value of `0` results in an instantaneous move. |

#### `moveCameraTo(position)`

##### Signature

```kotlin
fun moveCameraTo(
    position: GeoPoint,
    durationMillis: Long? = 0,
)
```

##### Description

Moves the map camera to a specific geographical coordinate (`GeoPoint`), preserving the current zoom, tilt, and bearing.

##### Parameters

| Parameter        | Type      | Default | Description                                                              |
| ---------------- | --------- | ------- | ------------------------------------------------------------------------ |
| `position`       | `GeoPoint`| -       | The target geographical coordinates (latitude and longitude).            |
| `durationMillis` | `Long?`   | `0`     | The duration of the camera animation in milliseconds. A value of `0` results in an instantaneous move. |

#### `getMapViewHolder()`

##### Signature

```kotlin
fun getMapViewHolder(): MapViewHolderInterface<*, *>?
```

##### Description

Retrieves the `MapViewHolderInterface` associated with this map view state. The view holder is responsible for managing the lifecycle of the actual map view UI component.

##### Returns

| Type                             | Description                                                              |
| -------------------------------- | ------------------------------------------------------------------------ |
| `MapViewHolderInterface<*, *>?`  | The associated map view holder, or `null` if it is not available.        |

---

## `MapViewState<ActualMapDesignType>`

### Signature

```kotlin
abstract class MapViewState<ActualMapDesignType> : MapViewStateInterface<ActualMapDesignType>
```

### Description

An abstract base class that provides a partial implementation of the `MapViewStateInterface`. It is intended to be subclassed by concrete map state implementations.

### Type Parameters

| Name                  | Description                                                                 |
| --------------------- | --------------------------------------------------------------------------- |
| `ActualMapDesignType` | The generic type representing the specific map design or style being used. |

---

## `MapOverlayInterface<DataType>`

### Signature

```kotlin
interface MapOverlayInterface<DataType>
```

### Description

Defines the contract for a map overlay. An overlay is a layer of custom data that can be rendered on top of the map. This interface is generic, allowing overlays to handle any type of data.

### Type Parameters

| Name       | Description                                                                 |
| ---------- | --------------------------------------------------------------------------- |
| `DataType` | The type of data objects that this overlay will manage and render.          |

### Properties

| Property | Type                                     | Description                                                                                             |
| -------- | ---------------------------------------- | ------------------------------------------------------------------------------------------------------- |
| `flow`   | `StateFlow<MutableMap<String, DataType>>` | A `StateFlow` that emits the current state of the overlay data, keyed by a unique `String` identifier. |

### Methods

#### `render`

##### Signature

```kotlin
suspend fun render(
    data: MutableMap<String, DataType>,
    controller: MapViewControllerInterface,
)
```

##### Description

A suspend function responsible for rendering the provided data onto the map. This function is called when the overlay needs to be drawn or updated.

##### Parameters

| Parameter    | Type                         | Description                                                              |
| ------------ | ---------------------------- | ------------------------------------------------------------------------ |
| `data`       | `MutableMap<String, DataType>` | The map of data items to be rendered, where the key is a unique ID for each item. |
| `controller` | `MapViewControllerInterface` | The controller for the map view, used to perform drawing operations.     |

---

## `MapOverlayRegistry`

### Signature

```kotlin
class MapOverlayRegistry
```

### Description

A singleton-like registry responsible for managing all `MapOverlayInterface` instances. It allows for the registration of new overlays and provides a way to retrieve all registered overlays.

### Methods

#### `register`

##### Signature

```kotlin
fun register(overlay: MapOverlayInterface<*>)
```

##### Description

Registers a new map overlay with the registry. If the overlay has already been registered, the method does nothing.

##### Parameters

| Parameter | Type                     | Description                               |
| --------- | ------------------------ | ----------------------------------------- |
| `overlay` | `MapOverlayInterface<*>` | The map overlay instance to register.     |

#### `getAll`

##### Signature

```kotlin
fun getAll(): List<MapOverlayInterface<*>>
```

##### Description

Retrieves a list of all currently registered map overlays.

##### Returns

| Type                           | Description                               |
| ------------------------------ | ----------------------------------------- |
| `List<MapOverlayInterface<*>>` | An immutable list of all registered overlays. |

### Example

Here is an example of how to create a custom overlay and register it with the `MapOverlayRegistry`.

```kotlin
// Define a data class for your overlay items
data class Poi(val id: String, val name: String, val location: GeoPoint)

// Implement the MapOverlayInterface
class PoiOverlay : MapOverlayInterface<Poi> {
    override val flow: StateFlow<MutableMap<String, Poi>> = MutableStateFlow(mutableMapOf())

    override suspend fun render(data: MutableMap<String, Poi>, controller: MapViewControllerInterface) {
        // Add logic here to draw each POI on the map using the controller
        println("Rendering ${data.size} POIs...")
    }
}

// In your application setup code
fun registerOverlays() {
    val registry = MapOverlayRegistry()
    val poiOverlay = PoiOverlay()

    // Register the overlay
    registry.register(poiOverlay)

    // You can now retrieve all overlays
    val allOverlays = registry.getAll()
    println("Registered ${allOverlays.size} overlays.")
}
```