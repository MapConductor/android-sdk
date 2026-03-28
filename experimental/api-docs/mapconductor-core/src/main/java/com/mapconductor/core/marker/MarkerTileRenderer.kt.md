Excellent. Here is the high-quality SDK documentation for the provided code snippet.

***

# MarkerTileRenderer<ActualMarker>

## Signature

```kotlin
class MarkerTileRenderer<ActualMarker>(
    val markerManager: MarkerManager<ActualMarker>,
    val tileSize: Int,
    cacheSizeBytes: Int,
    private val debugTileOverlay: Boolean = false,
    private val iconScaleCallback: ((MarkerState, Int) -> Double)? = null,
) : TileProviderInterface
```

## Description

The `MarkerTileRenderer` is a highly efficient renderer that generates map tiles with marker icons. It implements the `TileProviderInterface`, making it suitable for use with a local tile server to display markers as a raster layer on any map SDK. This approach provides a flexible, SDK-agnostic way to render a large number of markers.

Key features include:
- **Fixed Pixel Size:** Markers maintain a consistent size on the screen across different zoom levels.
- **Dynamic Scaling:** An optional callback allows for adjusting marker size based on zoom level or other state.
- **Efficient Caching:** An internal LruCache stores recently rendered tiles to reduce redundant processing and improve performance.
- **Decluttering:** By querying markers within the tile's bounds, it naturally supports decluttering strategies implemented in the `MarkerManager`.
- **Thread Safety:** The `renderTile` method is designed to be called concurrently from multiple threads.

The generic parameter `<ActualMarker>` represents the specific type of marker object being managed by the provided `MarkerManager`.

## Constructor

| Parameter | Type | Description |
|---|---|---|
| `markerManager` | `MarkerManager<ActualMarker>` | The `MarkerManager` instance that provides the marker data to be rendered. |
| `tileSize` | `Int` | The size of the tiles to be generated, specified in density-independent pixels (dp). A standard value is `256`. |
| `cacheSizeBytes` | `Int` | The maximum size of the in-memory tile cache, specified in bytes. |
| `debugTileOverlay` | `Boolean` | (Optional) If `true`, a debug overlay with tile coordinates and marker counts will be drawn on each tile. Defaults to `false`. |
| `iconScaleCallback` | `((MarkerState, Int) -> Double)?` | (Optional) A callback function to dynamically adjust the scale of each marker's icon. It receives the `MarkerState` and the current `zoom` level and should return a scale multiplier (e.g., `1.0` for normal size). Defaults to `null`. |

## Methods

### invalidate

Invalidates the internal tile cache. This forces all tiles to be re-rendered on their next request. This method should be called whenever the underlying marker data in the `MarkerManager` changes (e.g., markers are added, removed, or their state is updated) to ensure the map displays the most current data.

**Signature**
```kotlin
fun invalidate()
```

**Returns**
`Unit`

### clear

Clears all cached tiles from memory. This is functionally equivalent to `invalidate()`.

**Signature**
```kotlin
fun clear()
```

**Returns**
`Unit`

### renderTile

Renders a single map tile based on the provided `TileRequest`. This method is part of the `TileProviderInterface` and is typically called by a tile server. It queries the `MarkerManager` for markers within the tile's geographic bounds, draws them onto a bitmap, and returns the result as a PNG-encoded `ByteArray`.

**Signature**
```kotlin
override fun renderTile(request: TileRequest): ByteArray?
```

**Parameters**
| Parameter | Type | Description |
|---|---|---|
| `request` | `TileRequest` | An object containing the tile coordinates (`x`, `y`, `z`) for the tile to be rendered. |

**Returns**
`ByteArray?` - A byte array containing the PNG image data for the rendered tile. Returns `null` if the tile contains no markers (and `debugTileOverlay` is `false`) or if the request coordinates are invalid.

## Example

Here is an example of how to set up and use the `MarkerTileRenderer`.

```kotlin
import com.mapconductor.core.marker.MarkerTileRenderer
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerState

// Assume MyMarker is your custom marker data class
data class MyMarker(val id: String, val name: String)

// 1. Initialize a MarkerManager
val markerManager = MarkerManager<MyMarker>()

// 2. Create an instance of MarkerTileRenderer
val markerRenderer = MarkerTileRenderer(
    markerManager = markerManager,
    tileSize = 256, // Standard tile size in dp
    cacheSizeBytes = 32 * 1024 * 1024, // 32 MB cache
    debugTileOverlay = false,
    iconScaleCallback = { markerState, zoom ->
        // Example: Make markers smaller at lower zoom levels
        if (zoom < 10) 0.8 else 1.0
    }
)

// 3. This renderer would then be passed to a local tile server,
// which in turn provides tiles to a raster layer on your map.
// (The following is conceptual and depends on your map SDK)
//
// val localTileServer = LocalTileServer(provider = markerRenderer)
// map.addLayer(RasterLayer(source = localTileServer.source))

// 4. When you update your markers, invalidate the renderer's cache
// to force the tiles to be redrawn with the new data.
val newMarkerState = MarkerState(position = GeoPoint(40.7128, -74.0060))
markerManager.addMarker("nyc", MyMarker("nyc", "New York"), newMarkerState)

// Invalidate to reflect the change on the map
markerRenderer.invalidate()
```