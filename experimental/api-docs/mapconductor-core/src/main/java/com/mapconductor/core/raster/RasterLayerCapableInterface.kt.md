# Interface `RasterLayerCapableInterface`

The `RasterLayerCapableInterface` defines a contract for classes that can manage and display raster layers on a map. It provides a standardized way to handle the composition, updating, and querying of raster layers. Implement this interface in any class, such as a map controller or view, that needs to interact with a collection of raster data layers.

---

## `compositionRasterLayers`

Composes a new set of raster layers, replacing any existing ones. This function asynchronously updates the map to display exactly the layers specified in the input list, in the given order. It is useful for setting or resetting the entire raster layer stack.

### Signature

```kotlin
suspend fun compositionRasterLayers(data: List<RasterLayerState>)
```

### Description

This asynchronous suspend function takes a complete list of `RasterLayerState` objects and applies them to the map. The existing collection of raster layers is replaced with the new one. The function will handle adding new layers, removing obsolete ones, and reordering existing ones to match the provided list.

### Parameters

| Parameter | Type                     | Description                                                                 |
| :-------- | :----------------------- | :-------------------------------------------------------------------------- |
| `data`    | `List<RasterLayerState>` | The complete and ordered list of raster layer states to be displayed on the map. |

### Example

Here is an example of how a class might implement and use `compositionRasterLayers`.

```kotlin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// Assume RasterLayerState is a data class like:
// data class RasterLayerState(val id: String, val url: String, val opacity: Float)

class MapController : RasterLayerCapableInterface {
    private val currentLayers = mutableMapOf<String, RasterLayerState>()

    override suspend fun compositionRasterLayers(data: List<RasterLayerState>) {
        println("Composing ${data.size} raster layers...")
        // In a real implementation, this would involve complex logic
        // to add, remove, and reorder layers on a map view.
        currentLayers.clear()
        data.forEach { layer -> currentLayers[layer.id] = layer }
        println("Composition complete. Current layers: ${currentLayers.keys}")
    }

    // other interface methods...
    override suspend fun updateRasterLayer(state: RasterLayerState) { /* ... */ }
    override fun hasRasterLayer(state: RasterLayerState): Boolean { /* ... */ return false }
}

fun main() = runBlocking {
    val mapController = MapController()

    val initialLayers = listOf(
        RasterLayerState("layer1", "http://example.com/tiles/weather", 0.8f),
        RasterLayerState("layer2", "http://example.com/tiles/terrain", 1.0f)
    )

    // Set the initial layers on the map
    launch {
        mapController.compositionRasterLayers(initialLayers)
    }
}
```

---

## `updateRasterLayer`

Adds a new raster layer or updates an existing one. This function is ideal for making targeted changes to a single layer without affecting the rest of the layer stack.

### Signature

```kotlin
suspend fun updateRasterLayer(state: RasterLayerState)
```

### Description

This asynchronous suspend function takes a single `RasterLayerState` object and applies its state to the map. If a layer with the same unique identifier already exists, its properties (e.g., opacity, visibility) are updated. If no such layer exists, a new one is added to the map.

### Parameters

| Parameter | Type              | Description                                                              |
| :-------- | :---------------- | :----------------------------------------------------------------------- |
| `state`   | `RasterLayerState` | The state object representing the raster layer to be added or updated. |

### Example

```kotlin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MapController : RasterLayerCapableInterface {
    private val currentLayers = mutableMapOf<String, RasterLayerState>()

    override suspend fun updateRasterLayer(state: RasterLayerState) {
        println("Updating layer with ID: ${state.id}")
        // In a real implementation, this would update a layer's properties
        // (e.g., opacity) or add it if it's new.
        currentLayers[state.id] = state
        println("Layer ${state.id} updated with opacity ${state.opacity}.")
    }
    
    // other interface methods...
    override suspend fun compositionRasterLayers(data: List<RasterLayerState>) { /* ... */ }
    override fun hasRasterLayer(state: RasterLayerState): Boolean { /* ... */ return false }
}

fun main() = runBlocking {
    val mapController = MapController()

    val weatherLayer = RasterLayerState("layer1", "http://example.com/tiles/weather", 0.5f)
    
    // Add or update the weather layer
    launch {
        mapController.updateRasterLayer(weatherLayer)
    }

    // Later, update its opacity
    val updatedWeatherLayer = weatherLayer.copy(opacity = 0.9f)
    launch {
        mapController.updateRasterLayer(updatedWeatherLayer)
    }
}
```

---

## `hasRasterLayer`

Checks if a specific raster layer is currently part of the map's layer stack.

### Signature

```kotlin
fun hasRasterLayer(state: RasterLayerState): Boolean
```

### Description

This synchronous function determines whether a raster layer corresponding to the provided `RasterLayerState` exists on the map. The check is typically performed using a unique identifier within the `state` object.

### Parameters

| Parameter | Type              | Description                                                              |
| :-------- | :---------------- | :----------------------------------------------------------------------- |
| `state`   | `RasterLayerState` | The state object of the raster layer to check for.                       |

### Returns

| Type      | Description                                            |
| :-------- | :----------------------------------------------------- |
| `Boolean` | Returns `true` if the layer exists, `false` otherwise. |

### Example

```kotlin
class MapController : RasterLayerCapableInterface {
    private val currentLayers = mutableMapOf<String, RasterLayerState>()

    init {
        // Pre-populate with a layer for the example
        val existingLayer = RasterLayerState("layer1", "http://example.com/tiles/weather", 1.0f)
        currentLayers[existingLayer.id] = existingLayer
    }

    override fun hasRasterLayer(state: RasterLayerState): Boolean {
        return currentLayers.containsKey(state.id)
    }

    // other interface methods...
    override suspend fun compositionRasterLayers(data: List<RasterLayerState>) { /* ... */ }
    override suspend fun updateRasterLayer(state: RasterLayerState) { /* ... */ }
}

fun main() {
    val mapController = MapController()

    val layerToCheck = RasterLayerState("layer1", "http://...", 1.0f)
    val nonExistentLayer = RasterLayerState("layer99", "http://...", 1.0f)

    if (mapController.hasRasterLayer(layerToCheck)) {
        println("Layer '${layerToCheck.id}' exists on the map.")
    } else {
        println("Layer '${layerToCheck.id}' does not exist on the map.")
    }

    if (mapController.hasRasterLayer(nonExistentLayer)) {
        println("Layer '${nonExistentLayer.id}' exists on the map.")
    } else {
        println("Layer '${nonExistentLayer.id}' does not exist on the map.")
    }
}
// Expected Output:
// Layer 'layer1' exists on the map.
// Layer 'layer99' does not exist on the map.
```