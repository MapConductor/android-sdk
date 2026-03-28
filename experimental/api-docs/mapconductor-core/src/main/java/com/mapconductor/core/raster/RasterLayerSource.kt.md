Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

## RasterLayerSource

`RasterLayerSource` is a sealed class that defines the various sources from which a raster tile layer can be loaded. It provides a type-safe way to specify the configuration for different raster data providers.

The available source types are:
*   `UrlTemplate`: For sources defined by a URL template with x, y, and z placeholders.
*   `TileJson`: For sources defined by a URL pointing to a TileJSON manifest file.
*   `ArcGisService`: For sources from an ArcGIS MapServer or ImageServer service.

### Companion Object

#### `DEFAULT_TILE_SIZE`
The default size of the map tiles in pixels.

**Signature**
```kotlin
const val DEFAULT_TILE_SIZE: Int = 512
```

---

## TileScheme

An enum defining the tile coordinate system for raster layers.

**Signature**
```kotlin
enum class TileScheme {
    XYZ,
    TMS,
}
```

**Values**

| Value | Description |
| :---- | :---------- |
| `XYZ` | The standard "slippy map" tile scheme, where the origin (0,0) is at the top-left corner of the map. This is the most common scheme, used by services like OpenStreetMap and Google Maps. |
| `TMS` | The Tile Map Service scheme, where the origin (0,0) is at the bottom-left corner of the map. |

---

## RasterLayerSource.UrlTemplate

A data class representing a raster layer source defined by a URL template. This is a flexible way to load tiles from various services that follow a predictable URL structure.

**Signature**
```kotlin
data class UrlTemplate(
    val template: String,
    val tileSize: Int = DEFAULT_TILE_SIZE,
    val minZoom: Int? = null,
    val maxZoom: Int? = null,
    val attribution: String? = null,
    val scheme: TileScheme = TileScheme.XYZ,
) : RasterLayerSource()
```

**Description**
This source type uses a string template to generate URLs for individual map tiles. The template should include placeholders `{x}`, `{y}`, and `{z}` which will be replaced with the tile's column, row, and zoom level, respectively.

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `template` | `String` | The URL template for fetching tiles. Must contain `{x}`, `{y}`, and `{z}` placeholders. For example: `https://a.tile.openstreetmap.org/{z}/{x}/{y}.png`. |
| `tileSize` | `Int` | The size of the tiles in pixels. Defaults to `512`. |
| `minZoom` | `Int?` | The minimum zoom level at which this layer is available. Defaults to `null`. |
| `maxZoom` | `Int?` | The maximum zoom level at which this layer is available. Defaults to `null`. |
| `attribution` | `String?` | The attribution text to display on the map for this layer's data source. Defaults to `null`. |
| `scheme` | `TileScheme` | The tile coordinate system to use. Defaults to `TileScheme.XYZ`. |

**Example**
```kotlin
// Create a raster layer source for OpenStreetMap tiles
val osmSource = RasterLayerSource.UrlTemplate(
    template = "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png",
    tileSize = 256,
    maxZoom = 19,
    attribution = "© OpenStreetMap contributors"
)
```

---

## RasterLayerSource.TileJson

A data class representing a raster layer source defined by a URL pointing to a TileJSON resource.

**Signature**
```kotlin
data class TileJson(
    val url: String,
) : RasterLayerSource()
```

**Description**
This source type loads its configuration from a TileJSON file. The TileJSON file is a JSON object that contains metadata about the tileset, including the URL template, zoom levels, and attribution.

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `url` | `String` | The URL of the TileJSON resource (e.g., `https://example.com/tiles.json`). |

**Example**
```kotlin
// Create a raster layer source from a TileJSON endpoint
val mapboxStreetsSource = RasterLayerSource.TileJson(
    url = "https://api.mapbox.com/v4/mapbox.streets.json?access_token=YOUR_ACCESS_TOKEN"
)
```

---

## RasterLayerSource.ArcGisService

A data class representing a raster layer source from an ArcGIS MapServer or ImageServer service.

**Signature**
```kotlin
data class ArcGisService(
    val serviceUrl: String,
) : RasterLayerSource()
```

**Description**
This source type connects to an ArcGIS REST service endpoint to fetch map tiles. The SDK will automatically handle communication with the service to retrieve tile URLs and metadata.

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `serviceUrl` | `String` | The base URL of the ArcGIS MapServer or ImageServer service (e.g., `https://.../MapServer`). |

**Example**
```kotlin
// Create a raster layer source from an ArcGIS World Imagery service
val arcGisImagerySource = RasterLayerSource.ArcGisService(
    serviceUrl = "https://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer"
)
```