Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

# PolygonRasterTileRenderer

The `PolygonRasterTileRenderer` class is a tile provider responsible for rendering complex polygons onto raster map tiles. It implements the `TileProviderInterface`, allowing it to be integrated into a map's rendering pipeline.

This renderer can draw polygons with solid fills, strokes, and interior holes. It supports both standard rhumb line and geodesic (great-circle) edges, making it suitable for displaying large geographical areas accurately. By rendering polygons into image tiles on demand, it offers an efficient way to display large, static geometric data on a map.

The renderer's properties (such as `points`, `fillColor`, etc.) are thread-safe and can be updated dynamically, with changes reflected in subsequently rendered tiles.

## Signature

```kotlin
class PolygonRasterTileRenderer(
    private val tileSizePx: Int = 256,
) : TileProviderInterface
```

## Constructor

### `PolygonRasterTileRenderer(tileSizePx)`

Creates a new instance of the polygon tile renderer.

#### Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `tileSizePx` | `Int` | `256` | The width and height of the tiles to be rendered, in pixels. |

## Properties

The following properties can be configured to customize the appearance and behavior of the rendered polygon.

| Property | Type | Description |
|---|---|---|
| `points` | `List<GeoPointInterface>` | The list of geographic points that define the outer boundary of the polygon. The polygon will not be rendered if this list is empty. |
| `holes` | `List<List<GeoPointInterface>>` | A list of inner rings (holes) to be cut out from the main polygon. Each element in the list is another list of `GeoPointInterface` defining a single hole. |
| `fillColor` | `Int` | The ARGB integer color used to fill the polygon's area. Use `android.graphics.Color` constants or a custom integer value. Defaults to `Color.TRANSPARENT`. |
| `strokeColor` | `Int` | The ARGB integer color for the polygon's outline. The stroke is only drawn if `strokeWidthPx` is greater than 0 and the color is not fully transparent. Defaults to `Color.TRANSPARENT`. |
| `strokeWidthPx` | `Float` | The width of the polygon's outline in pixels. If set to `0f`, no stroke will be drawn. Defaults to `0f`. |
| `geodesic` | `Boolean` | If `true`, the polygon edges are rendered as geodesic lines (the shortest path on the Earth's surface), which appear as curves on a Mercator projection. If `false`, edges are rendered as straight rhumb lines on the map. Defaults to `false`. |
| `outerBounds` | `GeoRectBounds?` | An optional rectangular bounding box for the polygon. If provided, the renderer will quickly skip rendering tiles that do not intersect with these bounds, significantly improving performance. It is highly recommended to set this for large or complex polygons. |

## Methods

### renderTile

Renders a single map tile based on the provided `TileRequest`. This method is typically called by the map's tile rendering engine. It calculates which part of the polygon is visible on the requested tile, projects the coordinates, and draws the polygon shape onto a bitmap. The final bitmap is then compressed into a PNG byte array.

#### Signature

```kotlin
fun renderTile(request: TileRequest): ByteArray?
```

#### Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `request` | `TileRequest` | An object containing the tile's zoom level (`z`) and coordinates (`x`, `y`). |

#### Returns

**Type**: `ByteArray?`

A `ByteArray` containing the rendered tile as a PNG image.
- Returns a pre-rendered transparent tile if the `points` list is empty or if the tile is outside the `outerBounds`.
- Returns `null` if the requested tile coordinates are invalid (e.g., `y` is out of range for the given zoom level).

## Example

The following example demonstrates how to create and configure a `PolygonRasterTileRenderer` to draw a polygon with a hole.

```kotlin
import android.graphics.Color
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.polygon.PolygonRasterTileRenderer

// 1. Instantiate the renderer
val polygonRenderer = PolygonRasterTileRenderer(tileSizePx = 512)

// 2. Define the outer boundary of the polygon (e.g., a square)
val outerRing = listOf(
    GeoPoint(40.7128, -74.0060), // New York City
    GeoPoint(34.0522, -118.2437), // Los Angeles
    GeoPoint(25.7617, -80.1918),  // Miami
    GeoPoint(40.7128, -74.0060)   // Close the ring
)

// 3. Define an inner boundary for a hole (e.g., a triangle inside the square)
val holeRing = listOf(
    GeoPoint(39.7392, -104.9903), // Denver
    GeoPoint(36.1699, -115.1398), // Las Vegas
    GeoPoint(33.4484, -112.0740)  // Phoenix
)

// 4. Configure the renderer's properties
polygonRenderer.apply {
    points = outerRing
    holes = listOf(holeRing)
    fillColor = Color.argb(128, 0, 0, 255) // Semi-transparent blue
    strokeColor = Color.rgb(0, 0, 139)     // Dark blue
    strokeWidthPx = 3.0f
    geodesic = true // Render with curved lines on the map
}

// 5. Add the renderer to a tile provider layer on your map
// (The exact API may vary based on your map framework)
//
// mapController.addLayer(
//     TileProviderLayer(
//         tileProvider = polygonRenderer,
//         id = "my-polygon-layer"
//     )
// )

// The map will now request tiles from polygonRenderer, which will return
// PNG images of the specified polygon.
```