Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

## Class `MapLibreGroundImageController`

A controller responsible for managing the lifecycle of ground image overlays on a MapLibre map. It serves as a specialized implementation of `GroundImageController`, connecting the generic state management of `GroundImageManager` with the MapLibre-specific rendering logic of `MapLibreGroundImageOverlayRenderer`.

This controller is essential for handling map-specific events, such as style changes, that require ground images to be re-rendered.

```kotlin
class MapLibreGroundImageController(
    groundImageManager: GroundImageManagerInterface<MapLibreActualGroundImage> = GroundImageManager(),
    renderer: MapLibreGroundImageOverlayRenderer,
) : GroundImageController<MapLibreActualGroundImage>(groundImageManager, renderer)
```

### Constructor

Initializes a new instance of the `MapLibreGroundImageController`.

| Parameter          | Type                                                          | Description                                                                                             | Default              |
| ------------------ | ------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------- | -------------------- |
| `groundImageManager` | `GroundImageManagerInterface<MapLibreActualGroundImage>`      | The manager responsible for tracking the state and properties of all ground images.                     | `GroundImageManager()` |
| `renderer`         | `MapLibreGroundImageOverlayRenderer`                          | The renderer responsible for drawing and managing ground image layers and sources on the MapLibre map.  | -                    |

---

### Functions

#### `reapplyStyle`

Re-adds all managed ground images to the map. This function is crucial for scenarios where the map's style is reloaded, a process that typically destroys all existing layers and sources.

`reapplyStyle` works by retrieving the saved state of all current ground images, instructing the renderer to add them back to the newly loaded map style, and then updating the internal manager to track the newly created map objects.

**Signature**

```kotlin
suspend fun reapplyStyle()
```

**Description**

This is a `suspend` function and must be called from a coroutine or another `suspend` function. It ensures that all ground image overlays persist across map style changes.

**Parameters**

This method has no parameters.

**Returns**

This method does not return any value.

**Example**

```kotlin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Assuming 'map' is your MapLibre Map instance and 'style' is the loaded Style
// val renderer = MapLibreGroundImageOverlayRenderer(map, style)
// val groundImageController = MapLibreGroundImageController(renderer = renderer)

// This would typically be called within a listener for map style changes.
fun onMapStyleLoaded() {
    // Use a coroutine scope to call the suspend function
    CoroutineScope(Dispatchers.Main).launch {
        // Re-add all previously managed ground images to the new style
        groundImageController.reapplyStyle()
    }
}
```