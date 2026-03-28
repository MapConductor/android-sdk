# RasterLayerOverlayRendererInterface<ActualLayer>

## Description

The `RasterLayerOverlayRendererInterface` defines a contract for rendering and managing the lifecycle of raster layer overlays on a map. Implementations of this interface are responsible for translating abstract raster layer state changes (add, change, remove) into concrete operations for a specific, underlying map SDK (e.g., Google Maps, Mapbox).

All operations are designed to be asynchronous and are executed within a provided `CoroutineScope`.

### Generic Type Parameters

| Parameter | Description |
| :--- | :--- |
| `<ActualLayer>` | The concrete layer class of the underlying map SDK (e.g., `TileOverlay` for Google Maps). |

## Properties

### coroutine

The coroutine scope in which all suspend functions of this interface will be executed. The implementation must provide this scope.

**Signature**
```kotlin
abstract val coroutine: CoroutineScope
```

---

## Functions

### onAdd

Called to add one or more new raster layers to the map. This function should process the provided layer states, create the corresponding native map layers, and add them to the map view.

**Signature**
```kotlin
suspend fun onAdd(data: List<AddParamsInterface>): List<ActualLayer?>
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `data` | `List<AddParamsInterface>` | A list of objects, where each object contains the state for a new layer to be added. |

**Returns**

A `List<ActualLayer?>` where each element corresponds to the newly created native map layer object at the same index as the input `data`. An element will be `null` if the layer creation failed for any reason.

---

### onChange

Called to update one or more existing raster layers. This function should handle changes to layer properties such as opacity, visibility, or tile sources.

**Signature**
```kotlin
suspend fun onChange(data: List<ChangeParamsInterface<ActualLayer>>): List<ActualLayer?>
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `data` | `List<ChangeParamsInterface<ActualLayer>>` | A list of objects, each containing the previous and current state of a layer to be updated. |

**Returns**

A `List<ActualLayer?>` where each element is the updated native map layer object corresponding to the input `data`. An element can be `null` if the update results in the layer being removed or if an error occurs.

---

### onRemove

Called to remove one or more raster layers from the map.

**Signature**
```kotlin
suspend fun onRemove(data: List<RasterLayerEntityInterface<ActualLayer>>)
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `data` | `List<RasterLayerEntityInterface<ActualLayer>>` | A list of layer entities to be removed from the map. |

---

### onCameraChanged

An optional callback invoked whenever the map's camera position changes. This can be overridden to implement logic that depends on the current map view, such as adjusting layer details based on zoom level. The default implementation is empty.

**Signature**
```kotlin
suspend fun onCameraChanged(mapCameraPosition: MapCameraPosition) {}
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `mapCameraPosition` | `MapCameraPosition` | An object containing the current camera state (e.g., target, zoom, tilt, bearing). |

---

### onPostProcess

A lifecycle hook called after a batch of add, change, or remove operations has been completed. This can be used for final cleanup, batching updates, or forcing a map redraw to ensure all changes are visually applied.

**Signature**
```kotlin
suspend fun onPostProcess()
```

---

## Nested Interfaces

### AddParamsInterface

A data structure holding the parameters required to add a new raster layer.

| Property | Type | Description |
| :--- | :--- | :--- |
| `state` | `RasterLayerState` | The state of the raster layer to be added. |

### ChangeParamsInterface<ActualLayer>

A data structure holding the parameters required to change an existing raster layer.

| Property | Type | Description |
| :--- | :--- | :--- |
| `current` | `RasterLayerEntityInterface<ActualLayer>` | The new entity representing the layer's updated state. |
| `prev` | `RasterLayerEntityInterface<ActualLayer>` | The old entity representing the layer's state before the change. |

---

## Example

The following example demonstrates a conceptual implementation of `RasterLayerOverlayRendererInterface` for a hypothetical map SDK.

```kotlin
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.raster.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// Assume these are classes from a hypothetical map SDK
class NativeMapSDK {
    fun addTileOverlay(options: Any): NativeTileOverlay { /* ... */ return NativeTileOverlay() }
    fun removeOverlay(overlay: NativeTileOverlay) { /* ... */ }
}
class NativeTileOverlay {
    fun setOpacity(opacity: Float) { /* ... */ }
}

/**
 * An example implementation that bridges the interface with a hypothetical map SDK.
 *
 * @param mapSDK The instance of the native map object.
 * @param coroutine The CoroutineScope for running suspend functions.
 */
class MyRasterLayerRenderer(
    private val mapSDK: NativeMapSDK,
    override val coroutine: CoroutineScope
) : RasterLayerOverlayRendererInterface<NativeTileOverlay> {

    override suspend fun onAdd(data: List<AddParamsInterface>): List<NativeTileOverlay?> {
        return data.map { params ->
            // Logic to convert abstract state to native options
            val nativeOptions = createOptionsFromState(params.state)
            // Add the overlay to the actual map
            mapSDK.addTileOverlay(nativeOptions)
        }
    }

    override suspend fun onChange(data: List<ChangeParamsInterface<NativeTileOverlay>>): List<NativeTileOverlay?> {
        return data.map { params ->
            val nativeLayer = params.prev.actual
            // Apply changes to the existing native layer
            nativeLayer?.apply {
                // Example: Update opacity based on the new state
                // setOpacity(params.current.state.opacity)
            }
            nativeLayer // Return the updated layer
        }
    }

    override suspend fun onRemove(data: List<RasterLayerEntityInterface<NativeTileOverlay>>) {
        data.forEach { entity ->
            entity.actual?.let { nativeLayer ->
                // Remove the layer from the actual map
                mapSDK.removeOverlay(nativeLayer)
            }
        }
    }

    override suspend fun onCameraChanged(mapCameraPosition: MapCameraPosition) {
        // Optional: React to camera changes, e.g., for level-of-detail adjustments
        println("Camera moved to zoom level: ${mapCameraPosition.zoom}")
    }

    override suspend fun onPostProcess() {
        // Optional: Force a map refresh if needed by the underlying SDK
        // mapSDK.invalidate()
    }

    private fun createOptionsFromState(state: RasterLayerState): Any {
        // In a real implementation, you would convert the RasterLayerState
        // into a native TileOverlayOptions object.
        return Any()
    }
}
```