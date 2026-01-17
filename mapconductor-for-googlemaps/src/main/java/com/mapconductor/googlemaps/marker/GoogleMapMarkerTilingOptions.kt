package com.mapconductor.googlemaps.marker

/**
 * Options for Google Maps marker tiling optimization.
 *
 * When enabled, large sets of static markers can be rendered as a TileOverlay
 * to avoid per-marker add/update cost in the Google Maps SDK.
 */
data class GoogleMapMarkerTilingOptions(
    val enabled: Boolean = true,
    val minMarkerCount: Int = 2000,
    /**
     * Tile size (in pixels) used for the marker TileOverlay.
     * 256 is the standard; 512 can reduce perceived blur on high-DPI devices at the cost of CPU/memory/bandwidth.
     */
    val tileSize: Int = 256,
    /**
     * If true, marker icons are rendered at a fixed pixel size regardless of zoom.
     * If false, icon size may change with zoom (map-distance based scaling).
     *
     * Default is fixed pixels; zoom-dependent sizing is business-logic specific.
     */
    val fixedMarkerPixelSize: Boolean = true,
    /**
     * Reference zoom level for fixed marker pixel size scaling.
     * When [fixedMarkerPixelSize] is true, markers will be rendered at their original
     * bitmap size at this zoom level, and scaled appropriately at other zoom levels
     * to maintain consistent screen size.
     *
     * Default is 10. Set to a higher value if you want markers to appear larger at
     * zoomed-out levels, or lower if you want them smaller.
     */
    val fixedMarkerPixelSizeReferenceZoom: Int = 10,
    /**
     * When enabled, emits performance logs to help diagnose slow marker ingestion / tiling.
     * Logs are sampled and only emitted when exceeding thresholds.
     */
    val debugLogging: Boolean = false,
    /**
     * Only log slow operations whose elapsed time exceeds this threshold.
     */
    val slowOpThresholdMs: Long = 50L,
    /**
     * Sample rate for high-frequency logs (e.g., getTile). 1 means log every time, 100 means ~1%.
     */
    val logSampleRate: Int = 100,
    /**
     * Emit an aggregated getTile summary every N rendered tiles (0 disables).
     * This is useful because getTile is high-frequency and sampling can miss hotspots.
     */
    val tileSummaryEvery: Long = 0L,
    /**
     * Quantize marker scale (bitmapPxToWorldPx) to reduce resampling blur when zooming.
     * 0 disables quantization. Example: 0.125 quantizes to 1/8 increments.
     */
    val markerScaleQuantizationStep: Double = 0.0,
    /**
     * Filter flag used when downscaling the rendered tile bitmap to the returned tileSize.
     * Keeping this `true` is smoother but can look blurrier; `false` can look sharper.
     */
    val finalTileDownscaleFilter: Boolean = true,
    /**
     * When enabled, draws debug overlay onto marker tiles: top/left border lines and a label
     * containing z/x/y and basic render stats. Useful to debug caching/scaling artifacts.
     */
    val debugTileOverlay: Boolean = false,
    /**
     * When enabled, declutters markers at low zoom levels by sampling markers per tile.
     * This reduces worst-case tile render time when zoomed out.
     */
    val declutterEnabled: Boolean = false,
    /**
     * Apply decluttering when `zoomInt <= declutterMaxZoomInt`.
     * Example: 7 means apply at zoom levels 0..7 (zoomed out).
     */
    val declutterMaxZoomInt: Int = 7,
    /**
     * Max number of markers to draw per tile (per neighbor tile pass) while decluttering.
     * Lower values are faster but hide more markers.
     */
    val declutterMaxMarkersPerTile: Int = 800,
    /**
     * Approximate icon size (in pixels) used for overlap-based decluttering at low zoom levels.
     * Markers whose bounding boxes overlap already-kept markers will be skipped.
     */
    val declutterIconPx: Int = 28,
    /**
     * Grid cell size (in pixels) for overlap-based decluttering.
     * Smaller values keep more markers but cost more; larger values remove more.
     */
    val declutterCellPx: Int = 8,
    /**
     * Override internal render scale for tiled marker rendering.
     * `null` uses ResourceProvider-based scale (default). `1` forces rendering at tileSize (e.g. 256x256).
     */
    val renderScaleOverride: Int? = null,
) {
    companion object {
        val Disabled: GoogleMapMarkerTilingOptions = GoogleMapMarkerTilingOptions(enabled = false)
    }
}
