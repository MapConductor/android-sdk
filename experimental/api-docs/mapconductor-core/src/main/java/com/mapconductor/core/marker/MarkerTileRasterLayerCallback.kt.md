# MarkerTileRasterLayerCallback

### Signature

```kotlin
fun interface MarkerTileRasterLayerCallback
```

### Description

This is a functional interface that acts as a callback for managing a `RasterLayer` from a `MarkerController`. Its primary purpose is to decouple the `MarkerController` from the `RasterLayerController`, allowing for more modular and maintainable code.

An implementation of this interface is provided to a `MarkerController` to handle updates to its associated raster layer. The controller will invoke the `onRasterLayerUpdate` method when the raster layer needs to be added, modified, or removed.

---

## onRasterLayerUpdate

This function is called when a marker's tile raster layer needs to be added, updated, or removed from the map.

### Signature

```kotlin
suspend fun onRasterLayerUpdate(state: RasterLayerState?)
```

### Description

The `suspend` modifier indicates that this function is designed to be called from a coroutine and may perform long-running or asynchronous operations, such as I/O or heavy computation, without blocking the main thread.

### Parameters

| Parameter | Type                 | Description                                                                                                                            |
|-----------|----------------------|----------------------------------------------------------------------------------------------------------------------------------------|
| `state`   | `RasterLayerState?`  | The desired state for the raster layer. Provide a `RasterLayerState` object to add or update the layer. Pass `null` to remove the layer. |

### Example

Here is an example of how to implement the `MarkerTileRasterLayerCallback` and use it to manage a raster layer.

```kotlin
import kotlinx.coroutines.runBlocking

// Assume RasterLayerController is a class that manages raster layers on the map.
class RasterLayerController {
    fun addOrUpdateLayer(state: RasterLayerState) {
        println("Adding or updating raster layer: ${state.id}")
        // ... logic to add/update the layer on the map
    }

    fun removeLayerByPrefix(layerIdPrefix: String) {
        println("Removing raster layer with ID prefix: $layerIdPrefix")
        // ... logic to find and remove the layer from the map
    }
}

// Assume MarkerController is configured with this callback.
// The markerId helps to uniquely identify the layer to be removed.
fun createMarkerCallback(
    rasterLayerController: RasterLayerController,
    markerId: String
): MarkerTileRasterLayerCallback {
    // Since it's a functional interface, we can use a lambda.
    return MarkerTileRasterLayerCallback { state ->
        if (state != null) {
            // If state is provided, add or update the raster layer.
            rasterLayerController.addOrUpdateLayer(state)
        } else {
            // If state is null, remove the raster layer associated with this marker.
            // We use the markerId to construct a unique prefix for the layerId to remove.
            val layerIdPrefixToRemove = "marker-raster-$markerId"
            rasterLayerController.removeLayerByPrefix(layerIdPrefixToRemove)
        }
    }
}

// A placeholder for RasterLayerState for the example to be self-contained.
data class RasterLayerState(val id: String, val sourceId: String)

// Main function to demonstrate usage
fun main() = runBlocking {
    val rasterController = RasterLayerController()
    val myMarkerId = "marker-123"

    // Create the callback instance for a specific marker.
    val markerCallback = createMarkerCallback(rasterController, myMarkerId)

    // --- Scenario 1: Add a new layer ---
    // Simulate the MarkerController calling the callback to add a layer.
    println("Scenario 1: Adding a layer")
    val newState = RasterLayerState(id = "marker-raster-$myMarkerId-xyz", sourceId = "my-source")
    markerCallback.onRasterLayerUpdate(newState)
    // Expected Output: Adding or updating raster layer: marker-raster-marker-123-xyz
    println()


    // --- Scenario 2: Remove the layer ---
    // Simulate the MarkerController calling the callback to remove the layer.
    println("Scenario 2: Removing the layer")
    markerCallback.onRasterLayerUpdate(null)
    // Expected Output: Removing raster layer with ID prefix: marker-raster-marker-123
}
```