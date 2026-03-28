Of course! Here is the high-quality SDK documentation for the provided `GroundImageController` code snippet.

---

# GroundImageController<ActualGroundImage>

## Description

`GroundImageController` is an abstract class that orchestrates the display and management of ground image overlays on a map. It acts as a bridge between the abstract representation of ground images (`GroundImageState`) and their platform-specific rendering (`ActualGroundImage`).

This controller implements the `OverlayControllerInterface` and manages the complete lifecycle of ground images, including adding, updating, and removing them from the map. It uses a `GroundImageManagerInterface` to track the state of ground image entities and a `GroundImageOverlayRendererInterface` to handle the actual rendering on the map.

Operations that modify the state of ground images are thread-safe, managed internally by a `Semaphore`.

**Generic Parameters**

| Name | Description |
| :--- | :--- |
| `ActualGroundImage` | The platform-specific class or type that represents a rendered ground image overlay (e.g., `GroundOverlay` in Google Maps). |

## Constructor

```kotlin
abstract class GroundImageController<ActualGroundImage>(
    val groundImageManager: GroundImageManagerInterface<ActualGroundImage>,
    open val renderer: GroundImageOverlayRendererInterface<ActualGroundImage>,
    override var clickListener: OnGroundImageEventHandler? = null,
)
```

### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `groundImageManager` | `GroundImageManagerInterface<ActualGroundImage>` | An instance that manages the collection and state of ground image entities. |
| `renderer` | `GroundImageOverlayRendererInterface<ActualGroundImage>` | An instance responsible for rendering the ground images on the map. |
| `clickListener` | `OnGroundImageEventHandler?` | An optional listener that is invoked when any ground image managed by this controller is clicked. Defaults to `null`. |

## Properties

| Property | Type | Description |
| :--- | :--- | :--- |
| `zIndex` | `Int` | The z-index for the overlay layer, which determines its stacking order on the map. Hardcoded to `2`. |
| `semaphore` | `Semaphore` | A semaphore that ensures thread-safe, sequential access to methods that modify ground images (`add`, `update`, `clear`). |

## Methods

### dispatchClick

#### Signature

```kotlin
fun dispatchClick(event: GroundImageEvent)
```

#### Description

Dispatches a click event to the appropriate listeners. This method is typically called by the underlying map framework when a user taps on a ground image. It triggers both the `onClick` lambda defined in the `GroundImageState` of the specific image and the controller-wide `clickListener`.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `event` | `GroundImageEvent` | The event object containing details about the click, including the state of the clicked ground image. |

### add

#### Signature

```kotlin
override suspend fun add(data: List<GroundImageState>)
```

#### Description

Synchronizes the ground images on the map with the provided list of `GroundImageState` objects. The method intelligently computes the difference between the current state and the new state:
-   **Adds** new ground images that are in `data` but not on the map.
-   **Updates** existing ground images whose state has changed.
-   **Removes** ground images that are on the map but not in `data`.

This operation is performed atomically and is thread-safe.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `data` | `List<GroundImageState>` | The complete list of ground images that should be displayed on the map. |

### update

#### Signature

```kotlin
override suspend fun update(state: GroundImageState)
```

#### Description

Updates a single ground image on the map based on the provided `GroundImageState`. To optimize performance, the update is only performed if the new state's `fingerPrint()` differs from the existing one. This operation is thread-safe.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `state` | `GroundImageState` | The new state for the ground image to be updated. The `id` within the state is used to identify the image. |

### clear

#### Signature

```kotlin
override suspend fun clear()
```

#### Description

Removes all ground images managed by this controller from the map and clears all internal references. This operation is thread-safe.

### find

#### Signature

```kotlin
override fun find(position: GeoPointInterface): GroundImageEntityInterface<ActualGroundImage>?
```

#### Description

Finds and returns the ground image entity located at a specific geographical coordinate. This is useful for identifying which ground image is present at a given point on the map. The search is delegated to the `groundImageManager`.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `position` | `GeoPointInterface` | The geographical coordinates to search at. |

#### Returns

| Type | Description |
| :--- | :--- |
| `GroundImageEntityInterface<ActualGroundImage>?` | The found ground image entity, or `null` if no image exists at the specified position. |

### onCameraChanged

#### Signature

```kotlin
override suspend fun onCameraChanged(mapCameraPosition: MapCameraPosition)
```

#### Description

A lifecycle method from `OverlayControllerInterface`. This implementation is empty and serves as a placeholder for future functionality that may need to react to map camera movements.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `mapCameraPosition` | `MapCameraPosition` | The new position and state of the map camera. |

### destroy

#### Signature

```kotlin
override fun destroy()
```

#### Description

A lifecycle method from `OverlayControllerInterface` for releasing resources. This implementation is empty as the class does not hold any native resources that require explicit cleanup.

## Example

Since `GroundImageController` is an abstract class, you must first create a concrete implementation.

```kotlin
// Assume ActualGroundImage is a String for this example
// and other interfaces are mocked or implemented elsewhere.

// 1. Create a concrete implementation of GroundImageController
class MyGroundImageController(
    groundImageManager: GroundImageManagerInterface<String>,
    renderer: GroundImageOverlayRendererInterface<String>,
    clickListener: OnGroundImageEventHandler? = null
) : GroundImageController<String>(groundImageManager, renderer, clickListener) {
    // No additional implementation needed for this example
}

// 2. Define some state for our ground images
val initialState = GroundImageState(
    id = "image-1",
    bounds = /* some GeoBounds object */,
    image = /* some ImageDescriptor */,
    onClick = { event -> println("Clicked on ${event.state.id}") }
)

val updatedState = initialState.copy(
    transparency = 0.5f // Change transparency
)

// 3. Instantiate the controller with its dependencies
val myManager = MyGroundImageManager() // A concrete implementation
val myRenderer = MyGroundImageRenderer() // A concrete implementation
val groundImageController = MyGroundImageController(myManager, myRenderer)

// 4. Use the controller to manage ground images
suspend fun manageImages() {
    // Add the initial ground image to the map
    println("Adding initial image...")
    groundImageController.add(listOf(initialState))
    // The renderer's onAdd method will be called here.

    // Update the ground image with new transparency
    println("Updating image transparency...")
    groundImageController.update(updatedState)
    // The renderer's onChange method will be called here.

    // Find the image at a specific location
    val foundImage = groundImageController.find(/* some GeoPoint */)
    if (foundImage != null) {
        println("Found image with ID: ${foundImage.state.id}")
    }

    // Remove all ground images from the map
    println("Clearing all images...")
    groundImageController.clear()
    // The renderer's onRemove method will be called here.
}
```