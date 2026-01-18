## HERE MarkerTileLayer tuning

If tiled marker icons look blurry on HERE, prefer:

- `MarkerTilingOptions.finalTileDownscaleFilter = false` (sharper, less smoothing)
- Higher output tile resolution on high-DPI devices (to avoid GPU upscaling blur; CPU/memory tradeoff)
- If you still see blur on high-DPI devices, consider increasing `MarkerTilingOptions.tileSize` (CPU/memory tradeoff)

Default for HERE uses `finalTileDownscaleFilter = false` in `HereMarkerController.create(...)`.
Also, `HereMarkerController` renders higher-res output tiles on high-DPI devices while keeping the world-tile math at 256px
(handled by `MarkerTileRenderer(worldTileSize=256, tileSize=512)` internally), so icon sizing stays consistent.

## Zoom alignment

HERE's raster tile requests use an integer zoom (LOD) that can be offset from the camera zoom in `MapCameraPosition`.
`HereMapViewController` normalizes this by converting between MapConductor zoom (Google-canonical) and HERE camera zoom using
`ZoomAltitudeConverter.TILE_ZOOM_LEVEL_OFFSET` (currently `1.0`), so marker tile indexing/scaling matches the `{z}` used by
HERE's `TileUrlProviderCallback` without embedding magic numbers at call sites.

For `autoScalable=true` markers, HERE's LOD-to-scale relationship can still be effectively shifted by one level compared to
Google's zoom definition. `HereMarkerController` applies `scaleZoomOffset = ZoomAltitudeConverter.TILE_ZOOM_LEVEL_OFFSET`
only to zoom-dependent scaling (it does not change the tile grid), so icon sizing matches Google while still capping growth
when zooming in.

HERE may also display integer-LOD raster tiles at fractional camera zoom by scaling tiles. `HereMarkerController` enables
`useCameraZoomCompensation` so marker sizing stays consistent during/after fractional zoom (e.g. 12.1).
