Of course! Here is the high-quality SDK documentation for the provided `GroundImageTileProvider` class.

---

# GroundImageTileProvider

The `GroundImageTileProvider` is a tile provider that renders a georeferenced image, known as a ground overlay, onto map tiles. It implements the `TileProviderInterface`, allowing it to be used as a source for a map layer.

This class handles the projection of a source bitmap from its geographic coordinates (`GeoRectBounds`) onto the corresponding Web Mercator map tiles for any given zoom level. It includes an in-memory LRU cache to optimize performance by storing recently rendered tiles.

## Signature

```kotlin
class GroundImageTileProvider(
    val tileSize: Int = DEFAULT_TILE_SIZE,
    cacheSizeKb: Int = DEFAULT_CACHE_SIZE_KB,
) : TileProviderInterface
```

## Constructor

### Description

Creates a new instance of the `GroundImageTileProvider`.

### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `tileSize` | `Int` | The width and height of the tiles to generate, in pixels. Defaults to `512`. |
| `cacheSizeKb` | `Int` | The size of the in-memory LRU cache for storing rendered tiles, in kilobytes. Defaults to `8192` (8 MB). |

---

## Methods

### update

#### Signature

```kotlin
fun update(
    state: GroundImageState,
    opacity: Float = state.opacity,
)
```

#### Description

Sets or updates the ground image to be rendered on the map. This method configures the provider with the image, its geographic bounds, and its opacity.

Calling `update` will invalidate and clear the entire tile cache for this provider, forcing all visible tiles to be re-rendered with the new state.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `state` | `GroundImageState` | An object containing the ground image state, which must include the `image` (`Drawable`) and its geographic `bounds` (`GeoRectBounds`). |
| `opacity` | `Float` | The opacity of the overlay, clamped between `0.0` (fully transparent) and `1.0` (fully opaque). If provided, this value overrides the opacity from the `state` object. |

---

### renderTile

#### Signature

```kotlin
override fun renderTile(request: TileRequest): ByteArray?
```

#### Description

Generates the image data for a single map tile based on the current ground image overlay. This method is part of the `TileProviderInterface` and is typically called by the map rendering engine, not directly by the developer.

The method first checks its cache for a valid tile. If a cached tile is not found, it renders a new one by calculating the intersection between the ground image and the requested tile bounds. The resulting tile is then cached for future requests.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `request` | `TileRequest` | An object containing the coordinates (`x`, `y`, `z`) of the tile to be rendered. |

#### Returns

| Type | Description |
| :--- | :--- |
| `ByteArray?` | A `ByteArray` containing the PNG-encoded image data for the requested tile. Returns `null` if the ground image does not intersect with the tile's bounds or if no ground image has been set via the `update` method. |

---

## Example

Here is an example of how to instantiate `GroundImageTileProvider`, update it with a ground image, and add it to a map layer.

```kotlin
import android.graphics.drawable.Drawable
import com.mapconductor.core.groundimage.GroundImageState
import com.mapconductor.core.groundimage.GroundImageTileProvider
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoRectBounds

// Assume you have a mapController and a drawable resource
val mapController: MapController = // ...
val imageDrawable: Drawable = getDrawable(R.drawable.ground_overlay_image)

// 1. Define the geographic bounds for the image
val southWest = GeoPoint(latitude = 34.0, longitude = -118.2)
val northEast = GeoPoint(latitude = 34.1, longitude = -118.1)
val imageBounds = GeoRectBounds(southWest, northEast)

// 2. Create the state for the ground image
val groundImageState = GroundImageState(
    image = imageDrawable,
    bounds = imageBounds,
    opacity = 0.85f // Set a default opacity
)

// 3. Instantiate the GroundImageTileProvider
// You can customize tile size and cache size if needed
val groundImageProvider = GroundImageTileProvider(
    tileSize = 512,
    cacheSizeKb = 16 * 1024 // 16 MB cache
)

// 4. Update the provider with the image state
// You can also override the opacity here
groundImageProvider.update(state = groundImageState, opacity = 0.9f)

// 5. Add the provider to a raster layer on the map
mapController.addLayer(
    "ground-overlay-layer",
    "ground-overlay-source",
    groundImageProvider
)

// To remove the overlay later, you can update with a state that has an empty image
// or simply remove the layer from the map.
// mapController.removeLayer("ground-overlay-layer")
```