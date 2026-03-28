# Class `BaseMapViewSaver<T>`

## Signature

```kotlin
abstract class BaseMapViewSaver<T : MapViewStateInterface<*>>
```

## Description

An abstract base class for creating `Saver` instances that handle saving and restoring the state of a `MapView`.

This class provides the core logic for persisting the camera's position (location, zoom, tilt, bearing) and delegates the handling of map design specifics and state object creation to its subclasses. It is designed to be used with Jetpack Compose's `rememberSaveable` to preserve the map state across configuration changes or process death.

## Type Parameters

| Name | Description |
| :--- | :--- |
| `T` | The specific type of `MapViewStateInterface` that this saver will manage. |

---

## Functions

### `createSaver()`

Creates and returns a `Saver` object configured to save and restore the map view state. This is the primary entry point for using the class.

The resulting `Saver` orchestrates the entire process, calling the abstract methods (`saveMapDesign`, `createState`, etc.) at the appropriate times. It is intended for use with `rememberSaveable` in a Jetpack Compose UI.

**Signature**

```kotlin
fun createSaver(): Saver<T, Bundle>
```

**Returns**

| Type | Description |
| :--- | :--- |
| `Saver<T, Bundle>` | A `Saver` instance that can manage the lifecycle of the map view state. |

**Example**

Here is an example of how to implement `BaseMapViewSaver` and use it in a Composable.

1.  **Define your custom state and saver:**

    ```kotlin
    // 1. Define a custom map view state
    data class MyMapViewState(
        val id: String,
        override var cameraPosition: MapCameraPosition,
        val mapStyle: String // Custom property
    ) : MapViewStateInterface<Any> {
        // Implementation of MapViewStateInterface
    }

    // 2. Implement the abstract BaseMapViewSaver
    class MyMapViewSaver : BaseMapViewSaver<MyMapViewState>() {
        override fun saveMapDesign(state: MyMapViewState, bundle: Bundle) {
            bundle.putString("mapStyle", state.mapStyle)
        }

        override fun createState(
            stateId: String,
            mapDesignBundle: Bundle?,
            cameraPosition: MapCameraPosition
        ): MyMapViewState {
            val style = mapDesignBundle?.getString("mapStyle") ?: "default_style"
            return MyMapViewState(stateId, cameraPosition, style)
        }

        override fun getStateId(state: MyMapViewState): String {
            return state.id
        }
    }
    ```

2.  **Use it in your Composable:**

    ```kotlin
    @Composable
    fun MyMapScreen() {
        // Instantiate your custom saver
        val saver = remember { MyMapViewSaver().createSaver() }

        // Use rememberSaveable with your saver to manage the state
        var mapViewState by rememberSaveable(saver = saver) {
            mutableStateOf(
                MyMapViewState(
                    id = "map1",
                    cameraPosition = MapCameraPosition(GeoPoint.fromLatLong(40.7128, -74.0060)),
                    mapStyle = "satellite_view"
                )
            )
        }

        // Your MapView composable that uses mapViewState
        MapView(state = mapViewState)
    }
    ```

---

### `saveMapDesign()`

Saves the custom map design information from the current state into a `Bundle`. Subclasses must implement this method to persist any design-related properties, such as the map style or theme.

**Signature**

```kotlin
protected abstract fun saveMapDesign(
    state: T,
    bundle: Bundle,
)
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `state` | `T` | The current map view state instance from which to save data. |
| `bundle` | `Bundle` | The `Bundle` object where the map design data should be written. |

---

### `createState()`

Creates a new instance of the map view state (`T`) from the restored data. Subclasses must implement this factory method to reconstruct the state object using the provided camera position and map design information.

**Signature**

```kotlin
protected abstract fun createState(
    stateId: String,
    mapDesignBundle: Bundle?,
    cameraPosition: MapCameraPosition,
): T
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `stateId` | `String` | The unique identifier for the state being restored. |
| `mapDesignBundle` | `Bundle?` | A `Bundle` containing the custom map design data, previously saved by `saveMapDesign`. Can be `null` if no data was saved. |
| `cameraPosition` | `MapCameraPosition` | The restored `MapCameraPosition`. |

**Returns**

| Type | Description |
| :--- | :--- |
| `T` | A new instance of the map view state (`T`). |

---

### `getStateId()`

Extracts a unique identifier from the given state object. This ID is used to track the state instance during the save/restore process.

**Signature**

```kotlin
protected abstract fun getStateId(state: T): String
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `state` | `T` | The current map view state instance. |

**Returns**

| Type | Description |
| :--- | :--- |
| `String` | A `String` representing the unique ID of the state. |

---

### `getCameraPaddings()`

Provides optional padding values to be applied when restoring the camera position. Subclasses can override this method to specify custom paddings. The default implementation returns `null`, indicating no padding.

**Signature**

```kotlin
protected open fun getCameraPaddings(): MapPaddingsInterface?
```

**Returns**

| Type | Description |
| :--- | :--- |
| `MapPaddingsInterface?` | An instance of `MapPaddingsInterface` or `null`. |