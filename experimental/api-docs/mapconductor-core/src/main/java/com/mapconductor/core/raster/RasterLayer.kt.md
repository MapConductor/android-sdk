Excellent! Here is the high-quality SDK documentation for the provided code snippet, formatted in Markdown.

---

## `RasterLayerState`

### Description

Manages the state and configuration of a single raster layer on the map. This class is stateful and designed to be used within a Jetpack Compose environment. It holds all configurable properties of a raster layer, such as its source, opacity, and visibility. Changes to its properties will automatically trigger UI updates in a reactive framework.

### Constructor Signature

```kotlin
class RasterLayerState(
    source: RasterLayerSource,
    opacity: Float = 1.0f,
    visible: Boolean = true,
    zIndex: Int = 0,
    userAgent: String? = null,
    debug: Boolean = false,
    id: String? = null,
    extraHeaders: Map<String, String>? = null
)
```

### Parameters

| Parameter | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `source` | `RasterLayerSource` | - | The source of the raster tiles (e.g., a URL template). |
| `opacity` | `Float` | `1.0f` | The layer's opacity, ranging from `0.0` (fully transparent) to `1.0` (fully opaque). |
| `visible` | `Boolean` | `true` | Toggles the visibility of the layer. If `false`, the layer will not be rendered. |
| `zIndex` | `Int` | `0` | The stacking order of the layer. Layers with a higher `zIndex` are drawn on top of layers with a lower `zIndex`. |
| `userAgent` | `String?` | `null` | The custom User-Agent string to use for network requests when fetching tiles. If `null`, the system default is used. |
| `debug` | `Boolean` | `false` | Enables debug mode for the layer, which may overlay debugging information like tile boundaries. |
| `id` | `String?` | `null` | A unique identifier for the layer. If not provided, a stable ID is generated based on the layer's initial properties. |
| `extraHeaders` | `Map<String, String>?` | `null` | A map of additional HTTP headers to include in tile requests, such as for authentication tokens. |

### Properties

The `RasterLayerState` class exposes its constructor parameters as mutable properties. Changes to these properties will trigger recomposition in a Compose environment.

| Property | Type | Description |
| :--- | :--- | :--- |
| `id` | `String` | The unique identifier for the layer. This is a read-only property. |
| `source` | `RasterLayerSource` | The source of the raster tiles. |
| `opacity` | `Float` | The layer's opacity. |
| `visible` | `Boolean` | The visibility of the layer. |
| `zIndex` | `Int` | The stacking order of the layer. |
| `userAgent` | `String?` | The custom User-Agent string for network requests. |
| `debug` | `Boolean` | The debug mode status for the layer. |
| `extraHeaders` | `Map<String, String>?` | Additional HTTP headers for tile requests. |

### Methods

#### `copy()`

Creates a shallow copy of the `RasterLayerState`, allowing you to create a new instance with modified properties while keeping others unchanged.

**Signature**
```kotlin
fun copy(
    source: RasterLayerSource = this.source,
    opacity: Float = this.opacity,
    visible: Boolean = this.visible,
    zIndex: Int = this.zIndex,
    debug: Boolean = this.debug,
    userAgent: String? = this.userAgent,
    id: String? = this.id,
    extraHeaders: Map<String, String>? = this.extraHeaders
): RasterLayerState
```

**Returns**

| Type | Description |
| :--- | :--- |
| `RasterLayerState` | A new `RasterLayerState` instance. |

#### `fingerPrint()`

Generates a `RasterLayerFingerPrint` object representing the current state of the layer. This is useful for efficient state comparison and change detection.

**Signature**
```kotlin
fun fingerPrint(): RasterLayerFingerPrint
```

**Returns**

| Type | Description |
| :--- | :--- |
| `RasterLayerFingerPrint` | A fingerprint object containing hash codes of the layer's properties. |

#### `asFlow()`

Returns a `Flow` that emits a new `RasterLayerFingerPrint` whenever a property of the `RasterLayerState` changes. This is built on top of Jetpack Compose's `snapshotFlow` and is configured to only emit on distinct changes to the state's fingerprint.

**Signature**
```kotlin
fun asFlow(): Flow<RasterLayerFingerPrint>
```

**Returns**

| Type | Description |
| :--- | :--- |
| `Flow<RasterLayerFingerPrint>` | A flow that emits the layer's fingerprint upon state changes. |

### Example

```kotlin
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.material.Button
import androidx.compose.material.Slider
import androidx.compose.material.Text

// Assuming RasterLayerSource and MapComponent are defined elsewhere
// val rasterSource = RasterLayerSource("https://.../{z}/{x}/{y}.png")

@Composable
fun MapScreen() {
    // 1. Create and remember a RasterLayerState instance
    val satelliteLayerState = remember {
        RasterLayerState(
            source = rasterSource,
            opacity = 0.8f,
            zIndex = 1,
            extraHeaders = mapOf("Authorization" to "Bearer YOUR_TOKEN")
        )
    }

    // 2. Use the state in your map component
    MapComponent(rasterLayers = listOf(satelliteLayerState))

    // 3. Modify the state based on user interaction
    Button(onClick = {
        // Toggle visibility
        satelliteLayerState.visible = !satelliteLayerState.visible
    }) {
        Text("Toggle Satellite Layer")
    }

    Slider(
        value = satelliteLayerState.opacity,
        onValueChange = { newOpacity ->
            // Update opacity
            satelliteLayerState.opacity = newOpacity
        },
        valueRange = 0f..1f
    )
}
```

---

## `RasterLayerFingerPrint`

### Description

A data class that represents a unique snapshot of a `RasterLayerState`'s properties. It holds the hash codes of each property, providing a lightweight and efficient way to check for state changes, for example, within a `Flow`.

### Properties

| Property | Type | Description |
| :--- | :--- | :--- |
| `id` | `Int` | The hash code of the layer's ID. |
| `source` | `Int` | The hash code of the layer's source. |
| `opacity` | `Int` | The hash code of the layer's opacity. |
| `visible` | `Int` | The hash code of the layer's visibility status. |
| `zIndex` | `Int` | The hash code of the layer's z-index. |
| `userAgent` | `Int` | The hash code of the layer's User-Agent string. |
| `debug` | `Int` | The hash code of the layer's debug status. |
| `extra` | `Int` | The hash code of the layer's extra headers. |

---

## `RasterLayerEvent`

### Description

A data class that encapsulates an event related to a raster layer. It is typically used in event handlers to pass the current state of the layer that triggered the event.

### Properties

| Property | Type | Description |
| :--- | :--- | :--- |
| `state` | `RasterLayerState` | The state of the raster layer at the time of the event. |

---

## `OnRasterLayerEventHandler`

### Description

A type alias for a function that processes `RasterLayerEvent` objects. This provides a convenient and readable way to define event handlers for raster layer interactions, such as clicks or taps.

### Signature

```kotlin
typealias OnRasterLayerEventHandler = (RasterLayerEvent) -> Unit
```

### Example

```kotlin
val handleRasterLayerClick: OnRasterLayerEventHandler = { event ->
    println("Layer clicked! ID: ${event.state.id}, Source: ${event.state.source}")
    // You can now access any property from event.state
}
```