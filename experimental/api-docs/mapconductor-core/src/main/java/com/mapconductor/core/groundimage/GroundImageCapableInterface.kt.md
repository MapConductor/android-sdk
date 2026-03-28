# GroundImageCapableInterface

An interface for components capable of managing and displaying ground images on a map surface. It provides methods for adding, updating, and querying ground images, as well as handling user interactions.

---

## Methods

### compositionGroundImages

Asynchronously replaces all currently displayed ground images with a new set. This function is useful for performing a complete refresh of the ground image layer. Being a `suspend` function, it should be called from a coroutine scope.

**Signature**

```kotlin
suspend fun compositionGroundImages(data: List<GroundImageState>)
```

**Parameters**

| Parameter | Type | Description |
|-----------|---------------------------|------------------------------------------------------------------------------------------|
| `data` | `List<GroundImageState>` | A list of `GroundImageState` objects that define the new set of ground images to be displayed. |

**Returns**

This is a suspend function and does not return a value. It completes once the composition operation is finished.

**Example**

```kotlin
// Assuming 'mapController' implements GroundImageCapableInterface
// and this code is executed within a coroutine scope.

val newImages = listOf(
    GroundImageState(
        id = "historic_map_1890",
        image = Image.fromResource(R.drawable.map_1890),
        bounds = LatLngBounds(...)
    ),
    GroundImageState(
        id = "weather_overlay_radar",
        image = Image.fromUrl("https://.../radar.png"),
        bounds = LatLngBounds(...)
    )
)

// Replace all existing ground images with the new set
mapController.compositionGroundImages(newImages)
```

---

### updateGroundImage

Asynchronously updates a single ground image. If a ground image with the same ID as the provided `state` exists, its properties will be updated. If it does not exist, a new ground image may be added. This is a `suspend` function and must be called from a coroutine scope.

**Signature**

```kotlin
suspend fun updateGroundImage(state: GroundImageState)
```

**Parameters**

| Parameter | Type | Description |
|-----------|--------------------|------------------------------------------------------------------------------------|
| `state` | `GroundImageState` | The state object containing the ID and updated properties of the ground image. |

**Returns**

This is a suspend function and does not return a value. It completes once the update operation is finished.

**Example**

```kotlin
// Assuming 'mapController' implements GroundImageCapableInterface
// and this code is executed within a coroutine scope.

val updatedImageState = GroundImageState(
    id = "weather_overlay_radar",
    transparency = 0.5f, // Update the transparency of an existing image
    image = Image.fromUrl("https://.../latest_radar.png"), // Or update the image source
    bounds = LatLngBounds(...)
)

// Apply the update
mapController.updateGroundImage(updatedImageState)
```

---

### setOnGroundImageClickListener

**Deprecated:** Use the `onClick` property within `GroundImageState` instead.

Sets a global listener to handle click events on any ground image.

**Signature**

```kotlin
@Deprecated("Use GroundImageState.onClick instead.")
fun setOnGroundImageClickListener(listener: OnGroundImageEventHandler?)
```

**Description**

This method sets a single listener for click events across all ground images. For more granular, state-driven event handling, it is recommended to set the `onClick` lambda on individual `GroundImageState` objects.

**Parameters**

| Parameter | Type | Description |
|-----------|------------------------------|----------------------------------------------------------------------------------------------------|
| `listener` | `OnGroundImageEventHandler?` | An instance of `OnGroundImageEventHandler` to process click events, or `null` to remove the current listener. |

**Example**

```kotlin
// This method is deprecated. The following is for reference only.
// Recommended approach: GroundImageState(id = "...", onClick = { ... })

val clickListener = object : OnGroundImageEventHandler {
    override fun onGroundImageClicked(state: GroundImageState) {
        Log.d("Map", "Ground image ${state.id} was clicked.")
    }
}

// Set the global listener
mapController.setOnGroundImageClickListener(clickListener)

// To remove the listener
mapController.setOnGroundImageClickListener(null)
```

---

### hasGroundImage

Synchronously checks if a ground image matching the identifier in the provided `state` object is currently managed by the component.

**Signature**

```kotlin
fun hasGroundImage(state: GroundImageState): Boolean
```

**Parameters**

| Parameter | Type | Description |
|-----------|--------------------|------------------------------------------------------------------------------------------------|
| `state` | `GroundImageState` | The `GroundImageState` object, which must contain the ID of the ground image to check for. |

**Returns**

`Boolean` - Returns `true` if the ground image exists, `false` otherwise.

**Example**

```kotlin
val imageToCheck = GroundImageState(id = "historic_map_1890")

if (mapController.hasGroundImage(imageToCheck)) {
    println("Ground image 'historic_map_1890' is already on the map.")
} else {
    println("Ground image 'historic_map_1890' is not on the map.")
}
```