Of course! Here is the high-quality SDK documentation for the provided code snippet, formatted in Markdown.

---

### `GroundImageState`

A state holder class that defines and manages a ground image overlay on a map.

It encapsulates all properties necessary for rendering an image over a specific geographical area, such as its boundaries, the image resource, and its opacity. The properties are observable and mutable, allowing for dynamic updates to the ground overlay in a Compose-based environment.

#### Signature

```kotlin
class GroundImageState(
    bounds: GeoRectBounds,
    image: Drawable,
    opacity: Float = 1.0f,
    tileSize: Int = GroundImageTileProvider.DEFAULT_TILE_SIZE,
    id: String? = null,
    extra: Serializable? = null,
    onClick: OnGroundImageEventHandler? = null,
) : ComponentState
```

#### Parameters

| Parameter | Type | Description | Optional |
| :--- | :--- | :--- | :--- |
| `bounds` | `GeoRectBounds` | The geographical coordinates that define the rectangular area where the image will be placed. | No |
| `image` | `Drawable` | The `Drawable` resource to be displayed as the overlay. | No |
| `opacity` | `Float` | The opacity of the image, ranging from `0.0f` (fully transparent) to `1.0f` (fully opaque). | Yes, defaults to `1.0f`. |
| `tileSize` | `Int` | The size in pixels for the tiles used to render the image. Affects rendering performance and quality. | Yes, defaults to `GroundImageTileProvider.DEFAULT_TILE_SIZE`. |
| `id` | `String?` | A unique identifier for this ground image. If `null`, a stable ID is automatically generated based on the other properties. | Yes, defaults to `null`. |
| `extra` | `Serializable?` | Optional, serializable data that can be associated with the ground image for custom use cases. | Yes, defaults to `null`. |
| `onClick` | `OnGroundImageEventHandler?` | A callback function that is invoked when the user clicks on the ground image. | Yes, defaults to `null`. |

#### Properties

All constructor parameters are exposed as mutable `var` properties. You can modify these properties after initialization to dynamically update the ground image on the map.

| Property | Type | Description |
| :--- | :--- | :--- |
| `id` | `String` | The unique identifier for the component. |
| `bounds` | `GeoRectBounds` | The geographical area the image covers. |
| `image` | `Drawable` | The image `Drawable` to display. |
| `opacity` | `Float` | The opacity of the image. |
| `tileSize` | `Int` | The size of the rendering tiles. |
| `extra` | `Serializable?` | Extra data associated with the image. |
| `onClick` | `OnGroundImageEventHandler?` | The click event handler. |

#### Methods

##### `asFlow`

Returns a `Flow` that emits a value whenever any property of the `GroundImageState` changes. This is useful for observing state changes in a reactive programming paradigm. The flow only emits when the underlying data has actually changed.

**Signature**
```kotlin
fun asFlow(): Flow<GroundImageFingerPrint>
```

**Returns**

| Type | Description |
| :--- | :--- |
| `Flow<GroundImageFingerPrint>` | A flow that emits a unique fingerprint of the state upon any change. |

#### Example

```kotlin
// Assume you have a Drawable resource and GeoRectBounds defined
val imageDrawable: Drawable = ContextCompat.getDrawable(context, R.drawable.world_map_overlay)!!
val overlayBounds = GeoRectBounds(
    north = 85.0,
    south = -85.0,
    east = 180.0,
    west = -180.0
)

// Create a GroundImageState instance with a click handler
val groundImage = GroundImageState(
    bounds = overlayBounds,
    image = imageDrawable,
    opacity = 0.75f,
    onClick = { event ->
        println("Ground image clicked!")
        event.clicked?.let { point ->
            println("Click location: Lat ${point.latitude}, Lon ${point.longitude}")
        }
    }
)

// You can dynamically update properties later, which will trigger a redraw on the map.
// For example, to make the image more transparent:
groundImage.opacity = 0.5f
```

---

### `GroundImageEvent`

A data class that encapsulates information about a click event on a ground image. An instance of this class is passed to the `OnGroundImageEventHandler` when a click occurs.

#### Signature

```kotlin
data class GroundImageEvent(
    val state: GroundImageState,
    val clicked: GeoPoint?,
)
```

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `state` | `GroundImageState` | The state of the ground image that was clicked. |
| `clicked` | `GeoPoint?` | The geographical coordinates (`GeoPoint`) of the click location. This can be `null` if the exact point cannot be determined. |

---

### `OnGroundImageEventHandler`

A type alias for a function that handles click events on a `GroundImageState`.

#### Signature

```kotlin
typealias OnGroundImageEventHandler = (GroundImageEvent) -> Unit
```

#### Usage

This function type is used for the `onClick` parameter in the `GroundImageState` constructor.

```kotlin
// Define a handler for the click event
val handleGroundImageClick: OnGroundImageEventHandler = { event ->
    println("Image with ID ${event.state.id} was clicked.")
    // You can access the state and click location from the event object
}

// Assign it to a GroundImageState instance
val groundImage = GroundImageState(
    bounds = /* ... */,
    image = /* ... */,
    onClick = handleGroundImageClick
)
```