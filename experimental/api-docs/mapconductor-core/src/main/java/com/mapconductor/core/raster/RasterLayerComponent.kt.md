Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

## RasterLayer

The `RasterLayer` composable is used to add a raster tile layer to the map. A raster layer is composed of a grid of image tiles, typically fetched from a remote server, which are stitched together to form a complete map image.

This composable must be called from within the scope of a `MapView` composable. The layer is automatically added to the map when it enters the composition and removed when it leaves.

There are two overloads for this function:
1.  A convenience overload that accepts individual properties like `source`, `opacity`, and `visible`. This is the most common way to add a raster layer.
2.  An overload that accepts a `RasterLayerState` object, which is useful for advanced cases where the layer's state is managed externally.

***

### RasterLayer (Property-based)

This composable adds a raster layer to the map by defining its properties directly. It is the recommended approach for most use cases.

#### Signature
```kotlin
@Composable
fun MapViewScope.RasterLayer(
    source: RasterLayerSource,
    opacity: Float = 1.0f,
    visible: Boolean = true,
    zIndex: Int = 0,
    userAgent: String? = null,
    id: String? = null,
    extraHeaders: Map<String, String>? = null,
)
```

#### Description
Creates and manages a raster layer on the map. You define the tile source and can optionally customize its appearance and behavior, such as opacity, visibility, and draw order. The layer's lifecycle is automatically managed by Jetpack Compose.

#### Parameters
| Parameter      | Type                    | Description                                                                                                                            | Default |
|----------------|-------------------------|----------------------------------------------------------------------------------------------------------------------------------------|---------|
| `source`       | `RasterLayerSource`     | The source of the raster tiles. This object defines where and how to fetch the map tiles (e.g., a URL template).                         | (none)  |
| `opacity`      | `Float`                 | The opacity of the layer, ranging from `0.0` (fully transparent) to `1.0` (fully opaque).                                              | `1.0f`  |
| `visible`      | `Boolean`               | Toggles the visibility of the layer. If `false`, the layer will not be rendered.                                                       | `true`  |
| `zIndex`       | `Int`                   | The vertical stacking order of the layer. Layers with a higher `zIndex` are drawn on top of layers with a lower `zIndex`.                | `0`     |
| `userAgent`    | `String?`               | An optional custom `User-Agent` string to be sent with the tile requests.                                                              | `null`  |
| `id`           | `String?`               | A unique identifier for the layer. If not provided, a unique ID will be generated internally.                                          | `null`  |
| `extraHeaders` | `Map<String, String>?`  | An optional map of extra HTTP headers to be included in the tile requests. Useful for authentication tokens or other custom headers. | `null`  |

#### Returns
This composable does not return a value. It adds the specified raster layer to the map as a side effect.

#### Example
Here is an example of adding a standard OpenStreetMap raster layer to a `MapView`.

```kotlin
import androidx.compose.runtime.Composable
import com.mapconductor.core.MapView
import com.mapconductor.core.raster.RasterLayer
import com.mapconductor.core.raster.source.RasterTileSource

@Composable
fun MapWithRasterLayer() {
    MapView {
        // Add a raster layer from a tile server
        RasterLayer(
            source = RasterTileSource(
                url = "https://a.tile.openstreetmap.org/{z}/{x}/{y}.png"
            ),
            opacity = 0.8f,
            zIndex = 1
        )
    }
}
```

***

### RasterLayer (State-based)

This overload adds a raster layer using a `RasterLayerState` object. This is useful for advanced scenarios where you need to hoist and manage the layer's state outside the composable, for instance, in a `ViewModel`.

#### Signature
```kotlin
@Composable
fun MapViewScope.RasterLayer(state: RasterLayerState)
```

#### Description
Adds a raster layer to the map using a provided `RasterLayerState` instance. The composable observes the `state` object; any changes to its properties will be automatically reflected on the map layer. This allows for dynamic updates to the layer's properties from external logic.

#### Parameters
| Parameter | Type               | Description                                                                                                                                                           |
|-----------|--------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `state`   | `RasterLayerState` | An object that encapsulates all properties of the raster layer. Changes to this state object will cause the layer on the map to update. |

#### Returns
This composable does not return a value.

#### Example
In this example, the `RasterLayerState` is created and remembered within the composable. A button is provided to dynamically change the layer's opacity, demonstrating how the map updates when the state changes.

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material.Button
import androidx.compose.material.Text
import com.mapconductor.core.MapView
import com.mapconductor.core.raster.RasterLayer
import com.mapconductor.core.raster.RasterLayerState
import com.mapconductor.core.raster.source.RasterTileSource

@Composable
fun ControllableRasterLayer() {
    // Hoist the state to control it from other UI elements
    var layerState by remember {
        mutableStateOf(
            RasterLayerState(
                source = RasterTileSource(
                    url = "https://a.tile.openstreetmap.org/{z}/{x}/{y}.png"
                ),
                opacity = 1.0f
            )
        )
    }

    MapView {
        // Add the layer using the state object
        RasterLayer(state = layerState)
    }

    Button(onClick = {
        // Modify the state to update the layer on the map
        val newOpacity = if (layerState.opacity > 0.5f) 0.3f else 1.0f
        layerState = layerState.copy(opacity = newOpacity)
    }) {
        Text("Toggle Opacity")
    }
}
```