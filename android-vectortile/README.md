# MapConductor Vector Tile

## Description

MapConductor Vector Tile draws a MapLibre vector style on **any** map implementation, by rasterising the style to tiles on the device and serving them through the SDK's local tile server.

The backend only ever sees an ordinary raster layer. That is what makes it work on Google Maps, MapKit, HERE, ArcGIS, TomTom and the rest — none of which can render a vector style themselves.

The renderer is a Rust core shared with the iOS and web modules, so the same style and the same source tile produce a byte-identical PNG on all three platforms.

## Setup

https://mapconductor.com/setup/

------------------------------------------------------------------------

## Requirement: cleartext traffic to loopback

Tiles are served over plain HTTP on `127.0.0.1`. Without a network security config permitting it, **the layer simply stays blank** — no error, no crash.

```xml
<!-- res/xml/network_security_config.xml -->
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="false">127.0.0.1</domain>
        <domain includeSubdomains="false">localhost</domain>
    </domain-config>
</network-security-config>
```

This is not specific to this module — every tile-server-backed layer in the SDK needs it, which is why `example-app` already carries one.

------------------------------------------------------------------------

## Usage

### Basic VectorTileLayer

Place `VectorTileLayer` inside any `XxxMapView` content block:

```kotlin
XxxMapView(...) {
    VectorTileLayer(
        styleJson = styleJson,
        opacity = 0.9f,
    )
}
```

`styleJson` is the text of a `style.json`. Fetching it is the caller's job — the layer does not decide how your app does network I/O.

### Keeping tiles across launches

```kotlin
val cacheDir = remember { context.cacheDir.resolve("vectortile") }

XxxMapView(...) {
    VectorTileLayer(
        styleJson = styleJson,
        diskCacheDir = cacheDir,
    )
}
```

Deliberately the caller's choice rather than something the module digs out of a `Context`: apps have their own opinions about cache location and lifetime.

It does not make the first view faster — the tiles still have to be fetched and rasterised once. On a Pixel 5a a second launch served all seven visible tiles from disk with zero renders, against roughly 1 s of fetch plus 1 s of rasterising each on the first.

### Choosing a rasteriser

```kotlin
VectorTileLayer(
    styleJson = styleJson,
    renderMode = VectorTileProvider.RenderMode.GPU,
)
```

The default probes for an OpenGL ES context and falls back to the CPU when there is none.

### Reporting what a style will not draw

```kotlin
var diagnostics by remember { mutableStateOf<List<String>>(emptyList()) }

VectorTileLayer(
    styleJson = styleJson,
    onDiagnostics = { diagnostics = it },
)
```

### Without Compose

`VectorTileProvider` is the layer's engine and can be used on its own — register it with `TileServerRegistry` and point a `RasterLayerState` at the resulting URL template. `VectorTileLayer` is that wiring plus the lifecycle.

------------------------------------------------------------------------

## How it draws

The layer mounts **two** raster layers, not one:

- the **ground** — fills, lines and circles, the half no font arriving can change
- a transparent **label overlay** above it, replaced on its own when glyph ranges land

To the map and the user they read as one layer. The split is what stops a late glyph range forcing a redraw of the whole style, and it lets the two halves render in parallel.

Attribution is handled for you: the credits the style's sources ask for are fed to the map's attribution overlay through `AttributionRule`, on both halves.

------------------------------------------------------------------------

## API Reference

### VectorTileLayer

| Parameter | Type | Default | Description |
|---|---|---|---|
| `styleJson` | `String` | — | The text of a `style.json` |
| `tileSize` | `Int` | `512` | Output tile size in pixels |
| `opacity` | `Float` | `1.0f` | Layer opacity (0.0–1.0) |
| `visible` | `Boolean` | `true` | |
| `maxZoom` | `Int` | `22` | |
| `headers` | `Map<String, String>` | empty | Sent with every source tile request — auth tokens, API keys |
| `renderMode` | `VectorTileProvider.RenderMode` | `AUTO` | Which rasteriser to use |
| `diskCacheDir` | `File?` | `null` | Where to keep rendered tiles between launches; null disables it |
| `onDiagnostics` | `((List<String>) -> Unit)?` | `null` | Reasons the style may not render as intended |

### VectorTileProvider.RenderMode

| Value | Description |
|---|---|
| `CPU` | `tiny-skia`, on the calling thread. Always available. |
| `GPU` | OpenGL ES. Roughly 3x faster on a mid-range device and, more to the point, moves the drawing off the CPU that the map SDK and the app are already competing for. |
| `AUTO` | `GPU` where a context can be created, otherwise `CPU`. |

`GPU` output is not bit-identical to `CPU`: anti-aliasing comes from MSAA rather than analytic coverage, which measured at 0.39% of pixels differing on a dense street tile, all of it on thin-line edges.

### VectorTileProvider constants

| Constant | Value |
|---|---|
| `DEFAULT_TILE_SIZE` | `512` |
| `DEFAULT_CACHE_BYTES` | 48 MiB |
| `DEFAULT_DISK_CACHE_BYTES` | 64 MiB |
| `DEFAULT_GLYPH_CACHE_BYTES` | 32 MiB |
| `DEFAULT_SPRITE_CACHE_BYTES` | 4 MiB |

------------------------------------------------------------------------

## License

Apache License 2.0. See [LICENSE](./LICENSE).
