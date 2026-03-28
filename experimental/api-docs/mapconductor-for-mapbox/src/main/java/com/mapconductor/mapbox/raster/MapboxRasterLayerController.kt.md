# SDK Documentation: MapboxRasterLayerController

## `MapboxRasterLayerController`

**Signature**
```kotlin
class MapboxRasterLayerController(
    rasterLayerManager: RasterLayerManagerInterface<MapboxRasterLayerHandle> = RasterLayerManager(),
    renderer: MapboxRasterLayerOverlayRenderer,
) : RasterLayerController<MapboxRasterLayerHandle>(rasterLayerManager, renderer)
```

### Description

A controller responsible for managing and rendering raster layers on a Mapbox map. This class extends the generic `RasterLayerController` and integrates it with Mapbox-specific rendering logic. It orchestrates the lifecycle of raster layers, bridging the abstract layer management with the concrete rendering implementation provided by `MapboxRasterLayerOverlayRenderer`.

### Constructor

Creates an instance of `MapboxRasterLayerController`.

#### Parameters

| Parameter | Type | Description |
|---|---|---|
| `rasterLayerManager` | `RasterLayerManagerInterface<MapboxRasterLayerHandle>` | The manager for raster layer entities. It handles the registration and tracking of layers. Defaults to a new `RasterLayerManager` instance. |
| `renderer` | `MapboxRasterLayerOverlayRenderer` | The Mapbox-specific renderer responsible for drawing the raster layers on the map. |

---

## Methods

### `reapplyStyle()`

**Signature**
```kotlin
suspend fun reapplyStyle()
```

### Description

Reloads and re-renders all managed raster layers to apply updated styling. This function is essential for scenarios where layer style properties have been modified but are not automatically reflected on the map.

It works by:
1.  Retrieving the current state of all managed layers.
2.  Instructing the renderer to create new visual layers based on these states.
3.  Replacing the old layers with the newly created ones in the layer manager.
4.  Executing any final post-processing steps required by the renderer.

As a `suspend` function, it must be called from a coroutine or another `suspend` function.

### Parameters

This method does not take any parameters.

### Returns

This method does not return a value.

### Example

The following example demonstrates how to call `reapplyStyle()` within a coroutine scope after modifying layer properties.

```kotlin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// Assume 'mapboxRasterLayerController' is an initialized instance
// of MapboxRasterLayerController.

// Use a coroutine scope appropriate for your application's lifecycle,
// such as viewModelScope or lifecycleScope.
runBlocking { // Using runBlocking for a simple, self-contained example.
    // ... perform some action that requires a style update ...
    // For example, changing a global style property that affects all layers.

    println("Applying new style to raster layers...")
    mapboxRasterLayerController.reapplyStyle()
    println("Style reapplied successfully.")
}
```