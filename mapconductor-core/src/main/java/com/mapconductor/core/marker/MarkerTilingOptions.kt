package com.mapconductor.core.marker

/**
 * Options for marker tiling optimization.
 *
 * When enabled, large sets of static markers can be rendered as tile overlays
 * to avoid per-marker add/update cost in native map SDKs.
 */
data class MarkerTilingOptions(
    val enabled: Boolean = true,
    val minMarkerCount: Int = 2000,
    /**
     * Tile size (in pixels) used for the marker TileOverlay.
     * 256 is the standard; 512 can reduce perceived blur on high-DPI devices at the cost of CPU/memory/bandwidth.
     */
    val tileSize: Int = 256,
    /**
     * When enabled, emits performance logs to help diagnose slow marker ingestion / tiling.
     * Logs are sampled and only emitted when exceeding thresholds.
     */
    val debugLogging: Boolean = false,
    /**
     * When enabled, draws debug overlay onto marker tiles: top/left border lines and a label
     * containing z/x/y and basic render stats. Useful to debug caching/scaling artifacts.
     */
    val debugTileOverlay: Boolean = false,
) {
    companion object {
        val Disabled: MarkerTilingOptions = MarkerTilingOptions(enabled = false)
        val Default: MarkerTilingOptions = MarkerTilingOptions()
    }
}
