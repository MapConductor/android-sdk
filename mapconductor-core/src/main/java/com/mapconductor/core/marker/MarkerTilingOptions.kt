package com.mapconductor.core.marker

/**
 * Options for marker tiling optimization.
 *
 * When enabled, large sets of static markers can be rendered as tile overlays
 * to avoid per-marker add/update cost in native map SDKs.
 */
data class MarkerTilingOptions(
    val enabled: Boolean = true,
    /**
     * When enabled, draws debug overlay onto marker tiles: top/left border lines and a label
     * containing z/x/y and basic render stats. Useful to debug caching/scaling artifacts.
     */
    val debugTileOverlay: Boolean = false,
    val minMarkerCount: Int = 2000,
    val cacheSize: Int = 8 * 1024 * 1024,
    /**
     * Extra scale multiplier applied per marker per zoom during marker tiling.
     *
     * The renderer computes:
     * `effectiveScale = (markerState.icon?.scale ?: 1.0) * (iconScaleCallback?.invoke(markerState, zoom) ?: 1.0)`
     */
    val iconScaleCallback: ((MarkerState, Int) -> Double)? = null,
) {
    companion object {
        val Disabled: MarkerTilingOptions = MarkerTilingOptions(enabled = false)
        val Default: MarkerTilingOptions = MarkerTilingOptions()
    }
}
