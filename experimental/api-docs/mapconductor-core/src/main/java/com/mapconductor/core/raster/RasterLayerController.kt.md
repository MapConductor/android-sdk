# SDK Documentation: RasterLayerController

## `RasterLayerController<ActualLayer>`

### Description

An abstract class responsible for managing the lifecycle of raster layers on a map. It acts as a bridge between the desired state of the layers (`RasterLayerState`) and their actual representation on the map (`ActualLayer`).

The controller handles adding, updating, and removing raster layers in a synchronized and efficient manner. It uses a `RasterLayerManager` to track layer entities and a `RasterLayerOverlayRenderer` to handle the platform-specific rendering logic. All operations that modify the layer state are protected by a semaphore to ensure thread safety and prevent race conditions.

This class implements the `OverlayControllerInterface`.

**Type Parameters**

| Name | Description |
| :--- | :--- |
| `ActualLayer` | The concrete, platform-specific layer object that is rendered on the map. Must be a non-nullable type (`Any`). |

### Constructor

```kotlin
abstract class RasterLayerController<ActualLayer : Any>(
    val rasterLayerManager: RasterLayerManagerInterface<ActualLayer>,
    open val renderer: RasterLayerOverlayRendererInterface<ActualLayer>,
    override var clickListener: OnRasterLayerEventHandler? = null,
)
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `rasterLayerManager` | `RasterLayerManagerInterface<ActualLayer>` | An instance that manages the storage and retrieval of raster layer entities. |
| `renderer` | `RasterLayerOverlayRendererInterface<ActualLayer>` | An instance responsible for the actual rendering of layers on the map. |
| `clickListener` | `OnRasterLayerEventHandler?` | An optional listener to handle click events on the raster layers. Defaults to `null`. |

### Properties

#### `zIndex`

The stacking order of the overlay. This value is currently fixed at `0`.

**Signature**
```kotlin
override val zIndex: Int = 0
```

---

#### `semaphore`

A semaphore that allows only one permit, used to ensure that all layer modification operations (add, update, remove, etc.) are executed atomically and serially. This prevents race conditions when multiple updates occur concurrently.

**Signature**
```kotlin
val semaphore = Semaphore(1)
```

---

### Methods

#### `add`

Synchronizes the map's raster layers with a provided list of `RasterLayerState` objects.

This method performs a diffing operation against the currently managed layers. It determines which layers need to be added, which need to be updated, and which should be removed. It then delegates these operations to the `renderer`. The entire process is executed as a single, atomic operation.

**Signature**
```kotlin
override suspend fun add(data: List<RasterLayerState>)
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `data` | `List<RasterLayerState>` | The complete and definitive list of raster layers that should be displayed on the map. |

---

#### `update`

Updates a single existing raster layer identified by its state's ID.

This method checks for changes by comparing the `fingerPrint` of the new state with the existing one. If no changes are detected, the operation is skipped to improve performance. If the layer with the given ID does not exist, the method does nothing.

**Signature**
```kotlin
override suspend fun update(state: RasterLayerState)
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `state` | `RasterLayerState` | The new state for the raster layer to be updated. |

---

#### `upsert`

Adds or updates a single layer without affecting other existing layers.

This is useful for managing a specific raster layer independently from the main collection of layers, such as an internal layer used for marker tiling. If a layer with the same ID already exists, it will be updated; otherwise, a new layer will be added.

**Signature**
```kotlin
suspend fun upsert(state: RasterLayerState)
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `state` | `RasterLayerState` | The state of the layer to add or update. |

**Example**

```kotlin
// Assume 'myLayerController' is an instance of a RasterLayerController implementation
// and 'myInternalLayerState' is a RasterLayerState for a debugging overlay.

// This will add or update the debugging layer without removing any other layers
// that might have been added via the 'add' method.
myLayerController.upsert(myInternalLayerState)
```

---

#### `removeById`

Removes a single raster layer by its unique identifier without clearing other layers.

If a layer with the specified ID is found, it is removed from both the `rasterLayerManager` and the map via the `renderer`. If no such layer exists, the method does nothing.

**Signature**
```kotlin
suspend fun removeById(id: String)
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `id` | `String` | The unique ID of the raster layer to remove. |

---

#### `clear`

Removes all raster layers currently managed by this controller from the map.

**Signature**
```kotlin
override suspend fun clear()
```

---

#### `find`

Finds a raster layer entity at a given geographical position.

**Note:** The base implementation does not support this operation and always returns `null`. Subclasses must override this method to provide find functionality.

**Signature**
```kotlin
override fun find(position: GeoPointInterface): RasterLayerEntityInterface<ActualLayer>?
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `position` | `GeoPointInterface` | The geographical coordinate to search at. |

**Returns**

| Type | Description |
| :--- | :--- |
| `RasterLayerEntityInterface<ActualLayer>?` | Always returns `null` in this base class. |

---

#### `onCameraChanged`

A callback method invoked when the map's camera position changes. It delegates the event to the `renderer`, which may use it to optimize layer rendering (e.g., for tiled layers).

**Signature**
```kotlin
override suspend fun onCameraChanged(mapCameraPosition: MapCameraPosition)
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `mapCameraPosition` | `MapCameraPosition` | The new position and state of the map camera. |

---

#### `destroy`

Cleans up resources used by the controller. The base implementation is empty as it does not manage any native resources directly. Subclasses should override this method to release any resources they have allocated.

**Signature**
```kotlin
override fun destroy()
```