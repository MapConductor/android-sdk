Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

# GroundImageOverlayRendererInterface<ActualGroundImage>

## Description

The `GroundImageOverlayRendererInterface` defines a contract for rendering and managing ground image overlays on a map. Implementations of this interface are responsible for handling the lifecycle of ground images, including their creation, modification, and removal, by translating abstract state changes into calls to a specific, underlying map SDK.

This interface is designed to be asynchronous, using `suspend` functions to ensure that map operations do not block the main thread.

### Type Parameters

| Name | Description |
| :--- | :--- |
| `ActualGroundImage` | The native ground image object type from the specific map SDK being used (e.g., `com.google.android.gms.maps.model.GroundOverlay`). |

---

## Nested Interfaces

### AddParamsInterface

An interface representing the parameters required to add a new ground image to the map.

#### Signature

```kotlin
interface AddParamsInterface
```

#### Properties

| Name | Type | Description |
| :--- | :--- | :--- |
| `state` | `GroundImageState` | Holds the complete configuration and state for the new ground image to be created, such as its image source, geographic bounds, and visual properties. |

### ChangeParamsInterface<ActualGroundImage>

An interface representing the parameters required to update an existing ground image on the map.

#### Signature

```kotlin
interface ChangeParamsInterface<ActualGroundImage>
```

#### Properties

| Name | Type | Description |
| :--- | :--- | :--- |
| `current` | `GroundImageEntityInterface<ActualGroundImage>` | The entity representing the new, updated state of the ground image. |
| `prev` | `GroundImageEntityInterface<ActualGroundImage>` | The entity representing the previous state of the ground image before the change. |

---

## Functions

### onAdd

Called when one or more new ground images need to be added to the map. The implementation should create the native map SDK ground image objects based on the provided data.

#### Signature

```kotlin
suspend fun onAdd(data: List<AddParamsInterface>): List<ActualGroundImage?>
```

#### Parameters

| Name | Type | Description |
| :--- | :--- | :--- |
| `data` | `List<AddParamsInterface>` | A list of parameter objects, each containing the state for a new ground image to be rendered. |

#### Returns

**Type**: `List<ActualGroundImage?>`

A list of the newly created native `ActualGroundImage` objects. The size and order of this list must match the input `data` list. If a specific ground image fails to be created, its corresponding element in the returned list should be `null`.

### onChange

Called when properties of one or more existing ground images have been modified. The implementation should update the corresponding native ground image objects on the map.

#### Signature

```kotlin
suspend fun onChange(data: List<ChangeParamsInterface<ActualGroundImage>>): List<ActualGroundImage?>
```

#### Parameters

| Name | Type | Description |
| :--- | :--- | :--- |
| `data` | `List<ChangeParamsInterface<ActualGroundImage>>` | A list of change sets. Each element contains the previous and current states of a ground image that needs to be updated. |

#### Returns

**Type**: `List<ActualGroundImage?>`

A list of the updated native `ActualGroundImage` objects. The size and order of this list must match the input `data` list. If an update fails or is not applicable, the corresponding element can be `null`.

### onRemove

Called when one or more ground images need to be removed from the map.

#### Signature

```kotlin
suspend fun onRemove(data: List<GroundImageEntityInterface<ActualGroundImage>>)
```

#### Parameters

| Name | Type | Description |
| :--- | :--- | :--- |
| `data` | `List<GroundImageEntityInterface<ActualGroundImage>>` | A list of ground image entities that should be removed from the map. The implementation should use the `ActualGroundImage` within each entity to remove it from the map view. |

### onPostProcess

A lifecycle hook called after all add, change, and remove operations for a given update cycle are complete. This can be used for final cleanup, batch processing, or triggering a map refresh if required by the SDK.

#### Signature

```kotlin
suspend fun onPostProcess()
```

---

## Example

Here is a conceptual example of how to implement `GroundImageOverlayRendererInterface` for a hypothetical map SDK.

```kotlin
// Assume MapController and MapSDKGroundImage are part of a fictional map SDK.
class MyMapGroundImageRenderer(
    private val mapController: MapController
) : GroundImageOverlayRendererInterface<MapSDKGroundImage> {

    override suspend fun onAdd(data: List<AddParamsInterface>): List<MapSDKGroundImage?> {
        return data.map { params ->
            // Convert abstract state to map-specific options
            val options = createOptionsFromState(params.state)
            // Add the ground image to the map
            mapController.addGroundImage(options)
        }
    }

    override suspend fun onChange(data: List<ChangeParamsInterface<MapSDKGroundImage>>): List<MapSDKGroundImage?> {
        return data.map { change ->
            val nativeImage = change.prev.nativeImage // Get the actual map object
            val newState = change.current.state

            // Apply new properties to the existing native image
            nativeImage.updateBounds(newState.bounds)
            nativeImage.updateBitmap(newState.image)
            nativeImage.updateOpacity(newState.opacity)
            
            nativeImage
        }
    }

    override suspend fun onRemove(data: List<GroundImageEntityInterface<MapSDKGroundImage>>) {
        data.forEach { entity ->
            // Remove the ground image from the map
            entity.nativeImage?.removeFromMap()
        }
    }

    override suspend fun onPostProcess() {
        // For some map SDKs, you might need to trigger a redraw after batch operations.
        mapController.refresh()
        println("Ground image processing complete for this cycle.")
    }

    private fun createOptionsFromState(state: GroundImageState): MapSDKGroundImageOptions {
        // Logic to convert generic GroundImageState to map-specific options
        return MapSDKGroundImageOptions().apply {
            bounds = state.bounds
            image = state.image
            opacity = state.opacity
        }
    }
}
```