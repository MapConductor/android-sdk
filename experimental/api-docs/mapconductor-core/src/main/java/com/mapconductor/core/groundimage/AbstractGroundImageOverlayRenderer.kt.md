# AbstractGroundImageOverlayRenderer&lt;ActualGroundImage&gt;

Provides a foundational implementation for rendering ground image overlays on a map. This abstract class simplifies the creation of platform-specific renderers by handling the common logic of adding, updating, and removing batches of images.

Subclasses are responsible for implementing the core, platform-specific logic for creating, updating, and removing individual ground image objects (e.g., a `GroundOverlay` on Google Maps or an `ImageLayer` on Mapbox).

## Generic Parameters

| Name | Description |
| :--- | :--- |
| `ActualGroundImage` | The concrete, platform-specific class that represents a ground image on the map. |

## Properties

### holder
The map view holder that provides access to the map instance and its context.

**Signature**
```kotlin
abstract val holder: MapViewHolderInterface<*, *>
```

### coroutine
The coroutine scope used for launching and managing asynchronous rendering operations.

**Signature**
```kotlin
abstract val coroutine: CoroutineScope
```

## Functions

### onPostProcess
A lifecycle hook called after a batch of add, change, or remove operations has been processed. Subclasses can override this method to perform any finalization or cleanup tasks, such as refreshing the map view. The default implementation is empty.

**Signature**
```kotlin
override suspend fun onPostProcess()
```

### createGroundImage
**(Abstract)** Creates a new, platform-specific ground image instance on the map. Subclasses must provide an implementation for this method.

**Signature**
```kotlin
abstract suspend fun createGroundImage(state: GroundImageState): ActualGroundImage?
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `state` | `GroundImageState` | An object containing all the properties (e.g., image source, position, dimensions) needed to create the ground image. |

**Returns**

`ActualGroundImage?` - The newly created platform-specific ground image object, or `null` if creation fails.

### updateGroundImageProperties
**(Abstract)** Updates the properties of an existing ground image on the map. Subclasses must provide an implementation for this method.

**Signature**
```kotlin
abstract suspend fun updateGroundImageProperties(
    groundImage: ActualGroundImage,
    current: GroundImageEntityInterface<ActualGroundImage>,
    prev: GroundImageEntityInterface<ActualGroundImage>,
): ActualGroundImage?
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `groundImage` | `ActualGroundImage` | The existing platform-specific ground image object to be updated. |
| `current` | `GroundImageEntityInterface<ActualGroundImage>` | The entity representing the new state of the ground image. |
| `prev` | `GroundImageEntityInterface<ActualGroundImage>` | The entity representing the previous state of the ground image. |

**Returns**

`ActualGroundImage?` - The updated ground image object, which may be the same instance as the input `groundImage` or a new one, depending on the platform's API. Returns `null` if the update fails.

### removeGroundImage
**(Abstract)** Removes a ground image from the map. Subclasses must provide an implementation for this method.

**Signature**
```kotlin
abstract suspend fun removeGroundImage(entity: GroundImageEntityInterface<ActualGroundImage>)
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `entity` | `GroundImageEntityInterface<ActualGroundImage>` | The entity containing the ground image object to be removed. |

### onAdd
Handles the addition of a batch of new ground images. It iterates through the provided list and calls `createGroundImage` for each item.

**Signature**
```kotlin
override suspend fun onAdd(
    data: List<GroundImageOverlayRendererInterface.AddParamsInterface>,
): List<ActualGroundImage?>
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `data` | `List<GroundImageOverlayRendererInterface.AddParamsInterface>` | A list of parameter objects, each containing the state for a new ground image to be created. |

**Returns**

`List<ActualGroundImage?>` - A list of the newly created platform-specific ground image objects. Each element corresponds to an item in the input `data` list. An element will be `null` if the corresponding image creation failed.

### onChange
Handles property changes for a batch of existing ground images. It iterates through the list and calls `updateGroundImageProperties` for each image that needs an update.

**Signature**
```kotlin
override suspend fun onChange(
    data: List<GroundImageOverlayRendererInterface.ChangeParamsInterface<ActualGroundImage>>,
): List<ActualGroundImage?>
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `data` | `List<GroundImageOverlayRendererInterface.ChangeParamsInterface<ActualGroundImage>>` | A list of parameter objects, each containing the previous and current states of a ground image to be updated. |

**Returns**

`List<ActualGroundImage?>` - A list of the updated ground image objects. Each element corresponds to an item in the input `data` list. An element will be `null` if the corresponding update failed.

### onRemove
Handles the removal of a batch of ground images from the map. It iterates through the list of entities and calls `removeGroundImage` for each one.

**Signature**
```kotlin
override suspend fun onRemove(data: List<GroundImageEntityInterface<ActualGroundImage>>)
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `data` | `List<GroundImageEntityInterface<ActualGroundImage>>` | A list of ground image entities to be removed. |

## Example

Below is a conceptual example of how to subclass `AbstractGroundImageOverlayRenderer` for a hypothetical Google Maps implementation.

```kotlin
// Assume GoogleMap, GroundOverlay, and GroundOverlayOptions are from the Google Maps SDK
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.GroundOverlay
import com.google.android.gms.maps.model.GroundOverlayOptions
import com.mapconductor.core.groundimage.*
import com.mapconductor.core.map.MapViewHolderInterface
import kotlinx.coroutines.CoroutineScope

class GoogleMapsGroundImageRenderer(
    override val holder: MapViewHolderInterface<GoogleMap, *>,
    override val coroutine: CoroutineScope
) : AbstractGroundImageOverlayRenderer<GroundOverlay>() {

    private val googleMap: GoogleMap = holder.map

    override suspend fun createGroundImage(state: GroundImageState): GroundOverlay? {
        // Logic to convert GroundImageState to GroundOverlayOptions
        val options = GroundOverlayOptions().apply {
            positionFromBounds(state.bounds)
            image(state.imageDescriptor)
            transparency(1f - state.opacity)
            zIndex(state.zIndex)
            visible(state.isVisible)
        }
        return googleMap.addGroundOverlay(options)
    }

    override suspend fun updateGroundImageProperties(
        groundImage: GroundOverlay,
        current: GroundImageEntityInterface<GroundOverlay>,
        prev: GroundImageEntityInterface<GroundOverlay>
    ): GroundOverlay? {
        val currentState = current.state
        val prevState = prev.state

        if (currentState.bounds != prevState.bounds) {
            groundImage.positionFromBounds(currentState.bounds)
        }
        if (currentState.imageDescriptor != prevState.imageDescriptor) {
            groundImage.setImage(currentState.imageDescriptor)
        }
        if (currentState.opacity != prevState.opacity) {
            groundImage.transparency = 1f - currentState.opacity
        }
        // ... update other properties as needed
        
        return groundImage
    }

    override suspend fun removeGroundImage(entity: GroundImageEntityInterface<GroundOverlay>) {
        entity.groundImage.remove()
    }
}
```