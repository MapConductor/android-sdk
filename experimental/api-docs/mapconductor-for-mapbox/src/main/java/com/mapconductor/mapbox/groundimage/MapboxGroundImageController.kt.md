Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

## `MapboxGroundImageController`
The `MapboxGroundImageController` is a specialized controller for managing `GroundImage` overlays on a Mapbox map. It extends the generic `GroundImageController` and bridges the abstract ground image management logic with the concrete rendering implementation provided by `MapboxGroundImageOverlayRenderer`.

This controller is responsible for orchestrating the addition, removal, and styling of ground images on the map canvas.

### Signature
```kotlin
class MapboxGroundImageController(
    groundImageManager: GroundImageManagerInterface<MapboxActualGroundImage> = GroundImageManager(),
    renderer: MapboxGroundImageOverlayRenderer,
) : GroundImageController<MapboxActualGroundImage>(groundImageManager, renderer)
```

### Parameters
| Parameter | Type | Description | Default |
|---|---|---|---|
| `groundImageManager` | `GroundImageManagerInterface<MapboxActualGroundImage>` | The manager responsible for tracking the state and entities of all ground images. | `GroundImageManager()` |
| `renderer` | `MapboxGroundImageOverlayRenderer` | The Mapbox-specific renderer responsible for drawing the ground images on the map. | - |

---

## Functions

### `reapplyStyle()`
Asynchronously reapplies the style to all managed ground images.

This method is essential for scenarios where the map's style is changed at runtime. It retrieves the state of all current ground images, instructs the renderer to re-add them to the map, and updates the internal entity registry with the new map objects. This ensures that ground images remain visible and correctly styled after a map style transition.

Since this is a `suspend` function, it must be called from a coroutine or another suspend function.

#### Signature
```kotlin
suspend fun reapplyStyle()
```

#### Parameters
This method does not take any parameters.

#### Returns
This method does not return any value.

#### Example
This function is typically called in response to a map style change event.

```kotlin
// Assuming 'controller' is an initialized instance of MapboxGroundImageController
// and you are within a coroutine scope (e.g., viewModelScope).

// Call this method when the Mapbox map's style has been loaded or changed.
viewModelScope.launch {
    try {
        controller.reapplyStyle()
        Log.d("Mapbox", "Ground image styles have been successfully reapplied.")
    } catch (e: Exception) {
        Log.e("Mapbox", "Failed to reapply ground image styles.", e)
    }
}
```