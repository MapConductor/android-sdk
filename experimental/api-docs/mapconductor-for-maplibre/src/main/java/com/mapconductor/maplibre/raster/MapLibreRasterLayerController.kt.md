Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

### Class `MapLibreRasterLayerController`

A controller responsible for managing the lifecycle of raster layers within a MapLibre map environment. It acts as a bridge between the generic `RasterLayerManager` and the MapLibre-specific `MapLibreRasterLayerOverlayRenderer`.

This controller orchestrates the addition, removal, and styling of raster layers on the map.

**Signature**
```kotlin
class MapLibreRasterLayerController(
    rasterLayerManager: RasterLayerManagerInterface<MapLibreRasterLayerHandle> = RasterLayerManager(),
    renderer: MapLibreRasterLayerOverlayRenderer,
) : RasterLayerController<MapLibreRasterLayerHandle>(rasterLayerManager, renderer)
```

### Constructor Parameters

| Parameter            | Type                                                              | Description                                                                                                                            |
| -------------------- | ----------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------- |
| `rasterLayerManager` | `RasterLayerManagerInterface<MapLibreRasterLayerHandle>`          | An instance that manages the state and lifecycle of raster layer entities. Defaults to a new `RasterLayerManager` instance.        |
| `renderer`           | `MapLibreRasterLayerOverlayRenderer`                              | The renderer responsible for drawing the raster layers onto the MapLibre map canvas.                                                   |

---

### Methods

#### `reapplyStyle`

Asynchronously reapplies the current style to all managed raster layers. This method is useful for refreshing the visual representation of layers after a style or theme change has occurred.

It operates by retrieving the state of all current layers, removing them, and then re-adding them using the renderer. This ensures that any updated style properties are correctly applied.

**Signature**
```kotlin
suspend fun reapplyStyle()
```

**Parameters**

None.

**Returns**

`Unit` - This method does not return any value.

**Example**

Here's an example of how to instantiate `MapLibreRasterLayerController` and call `reapplyStyle` from within a coroutine.

```kotlin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// Assume 'map' is an initialized MapLibre Map instance
// and 'myCoroutineScope' is a valid CoroutineScope.

// 1. Initialize the renderer
val renderer = MapLibreRasterLayerOverlayRenderer(map)

// 2. Initialize the controller
val rasterLayerController = MapLibreRasterLayerController(renderer = renderer)

// ... code to add some raster layers ...

// 3. When a style change occurs, call reapplyStyle within a coroutine
myCoroutineScope.launch {
    println("Reapplying styles to all raster layers...")
    rasterLayerController.reapplyStyle()
    println("Styles reapplied successfully.")
}
```