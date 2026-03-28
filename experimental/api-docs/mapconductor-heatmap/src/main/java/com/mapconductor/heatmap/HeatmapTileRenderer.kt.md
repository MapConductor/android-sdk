# SDK Documentation: `HeatmapTileRenderer`

## `HeatmapTileRenderer` Class

### Description

A high-performance tile provider that renders heatmap tiles on-the-fly from a set of weighted geographical points. It implements the `TileProviderInterface`, making it compatible with map frameworks that support custom tile sources.

The renderer is optimized for performance, featuring:
-   **Multi-threaded Rendering**: Utilizes a pool of worker threads to render tiles in the background, preventing UI freezes.
-   **In-Memory Caching**: Employs an LRU cache to store recently rendered tiles, reducing redundant computations and providing instant tile delivery on subsequent requests.
-   **Efficient Data Structures**: Uses a spatial index for large datasets to quickly query points within a tile's bounds.
-   **Dynamic Optimization**: Adjusts rendering parameters based on camera zoom and tile complexity to maintain low latency.

---

### Constructor

#### Signature

```kotlin
class HeatmapTileRenderer(
    val tileSize: Int = DEFAULT_TILE_SIZE,
    cacheSizeKb: Int = DEFAULT_CACHE_SIZE_KB,
    maxConcurrentRenders: Int = DEFAULT_MAX_CONCURRENT_RENDERS,
    private val pngCompressionLevel: Int = DEFAULT_PNG_COMPRESSION_LEVEL
) : TileProviderInterface
```

#### Description

Creates a new instance of the `HeatmapTileRenderer`.

#### Parameters

| Parameter             | Type  | Default Value | Description                                                                                                                                                                                          |
| --------------------- | ----- | ------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `tileSize`            | `Int` | `512`         | The width and height of the tiles to generate, in pixels.                                                                                                                                            |
| `cacheSizeKb`         | `Int` | `8192`        | The maximum size of the in-memory tile cache in kilobytes (KB).                                                                                                                                      |
| `maxConcurrentRenders`| `Int` | `2`           | The number of worker threads to use for rendering tiles. More threads can increase throughput on multi-core devices but also increase CPU and memory usage.                                           |
| `pngCompressionLevel` | `Int` | `1`           | The PNG compression level (0-9). `0` is fastest with no compression, `1` offers a good balance (default), and `9` is slowest with the best compression. The renderer may dynamically use level `0` for complex tiles to ensure low latency. |

---

## Methods

### `update`

#### Signature

```kotlin
fun update(
    points: List<HeatmapPoint>,
    radiusPx: Int,
    gradient: HeatmapGradient,
    maxIntensity: Double?
)
```

#### Description

Updates the heatmap with a new dataset and rendering parameters. This operation is asynchronous. After this method is called, the renderer will start generating new tiles with the updated configuration. Any existing cached tiles are invalidated.

#### Parameters

| Parameter      | Type                  | Description                                                                                                                                                                                                |
| -------------- | --------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `points`       | `List<HeatmapPoint>`  | The list of data points to be rendered on the heatmap. Each point has a geographical location and an optional weight.                                                                                       |
| `radiusPx`     | `Int`                 | The radius of influence for each heatmap point, in pixels. This determines how "spread out" the heat from each point is.                                                                                    |
| `gradient`     | `HeatmapGradient`     | The color gradient used to style the heatmap, mapping intensity values to colors.                                                                                                                          |
| `maxIntensity` | `Double?`             | An optional value to manually set the maximum intensity of the heatmap. If `null`, the maximum intensity is calculated automatically based on point density. This is useful for maintaining a consistent color scale across different datasets. |

---

### `updateCameraZoom`

#### Signature

```kotlin
fun updateCameraZoom(zoom: Double)
```

#### Description

Informs the renderer about the current zoom level of the map camera. The renderer uses this information to adjust the effective radius of heatmap points, ensuring a visually consistent appearance and optimizing performance during zoom and pan operations. This method should be called frequently as the map's zoom level changes (e.g., in a camera move listener).

#### Parameters

| Parameter | Type     | Description                                        |
| --------- | -------- | -------------------------------------------------- |
| `zoom`    | `Double` | The current, potentially fractional, zoom level of the map. |

---

### `renderTile`

#### Signature

```kotlin
override fun renderTile(request: TileRequest): ByteArray?
```

#### Description

Renders a single map tile for the given request. This method is part of the `TileProviderInterface` and is typically called by the map framework. It first checks the in-memory cache for the requested tile. If not found, it queues a render job to be processed by a worker thread. The call will block until the tile is rendered or retrieved from a concurrent request for the same tile.

#### Parameters

| Parameter | Type          | Description                                                |
| --------- | ------------- | ---------------------------------------------------------- |
| `request` | `TileRequest` | An object containing the tile coordinates (`z`, `x`, `y`). |

#### Returns

| Type          | Description                                                                                                                                                           |
| ------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `ByteArray?`  | The rendered tile as a PNG image in a `ByteArray`. A fully transparent tile is returned for areas with no data. Returns `null` if the tile could not be rendered. |

---

## Example

The following example demonstrates how to set up and use the `HeatmapTileRenderer`.

```kotlin
import android.graphics.Color
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.tileserver.TileRequest
import com.mapconductor.heatmap.HeatmapTileRenderer

// Assume these data classes are defined elsewhere in your project or SDK.
// They are included here for a complete, runnable example.
data class GeoPoint(
    override val latitude: Double,
    override val longitude: Double
) : GeoPointInterface

data class HeatmapPoint(
    val position: GeoPointInterface,
    val weight: Double = 1.0
)

data class ColorStop(val position: Float, val color: Int)
data class HeatmapGradient(val stops: List<ColorStop>)


fun main() {
    // 1. Create an instance of the renderer
    val heatmapRenderer = HeatmapTileRenderer(
        tileSize = 512,
        cacheSizeKb = 16 * 1024, // 16 MB cache
        maxConcurrentRenders = 4
    )

    // 2. Define the data points for the heatmap
    val points = listOf(
        HeatmapPoint(GeoPoint(latitude = 34.0522, longitude = -118.2437), weight = 10.0), // Los Angeles
        HeatmapPoint(GeoPoint(latitude = 40.7128, longitude = -74.0060), weight = 25.0),  // New York
        HeatmapPoint(GeoPoint(latitude = 41.8781, longitude = -87.6298), weight = 15.0),  // Chicago
        HeatmapPoint(GeoPoint(latitude = 40.7500, longitude = -74.0000), weight = 30.0)   // Near New York
    )

    // 3. Define the color gradient for the heatmap
    val gradient = HeatmapGradient(
        stops = listOf(
            ColorStop(0.0f, Color.TRANSPARENT),
            ColorStop(0.2f, Color.argb(255, 0, 0, 255)),   // Blue
            ColorStop(0.5f, Color.argb(255, 0, 255, 0)),   // Green
            ColorStop(0.8f, Color.argb(255, 255, 255, 0)), // Yellow
            ColorStop(1.0f, Color.argb(255, 255, 0, 0))    // Red
        )
    )

    // 4. Update the renderer with the data and configuration
    heatmapRenderer.update(
        points = points,
        radiusPx = 30,
        gradient = gradient,
        maxIntensity = null // Auto-calculate max intensity
    )

    // 5. In your map's camera change listener, update the renderer's zoom.
    // This is a conceptual example of how you would integrate with a map view.
    // map.onCameraMoveListener = { cameraPosition ->
    //     heatmapRenderer.updateCameraZoom(cameraPosition.zoom)
    // }
    // For this example, we'll set it manually.
    heatmapRenderer.updateCameraZoom(5.0)


    // 6. The map framework will now call renderTile() as needed.
    // For demonstration, we can call it directly to get a tile.
    val tileRequest = TileRequest(z = 5, x = 5, y = 12) // A tile covering the US East Coast
    val tilePngData: ByteArray? = heatmapRenderer.renderTile(tileRequest)

    if (tilePngData != null) {
        println("Successfully rendered tile for z=${tileRequest.z}, x=${tileRequest.x}, y=${tileRequest.y}.")
        println("Tile size: ${tilePngData.size} bytes.")
        // The map framework would take this byte array and display it on the map.
    } else {
        println("Tile rendering failed or tile is empty.")
    }
}
```