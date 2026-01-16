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
) {
    companion object {
        val Disabled: GoogleMapMarkerTilingOptions = GoogleMapMarkerTilingOptions(enabled = false)
    }
}

