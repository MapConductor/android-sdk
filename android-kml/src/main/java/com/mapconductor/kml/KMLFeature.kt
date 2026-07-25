package com.mapconductor.kml

/**
 * Lightweight, non-Compose data class for static/bulk KML features.
 * Use this (instead of [KMLFeatureState]) when loading large KML files
 * that don't need per-feature reactive state — e.g. via [KMLParser.parse].
 */
data class KMLFeature(
    val id: String? = null,
    val geometry: KMLGeometry,
    val properties: Map<String, Any?> = emptyMap(),
    val strokeColor: Int? = null,
    val fillColor: Int? = null,
    val strokeWidth: Float? = null,
    val pointRadius: Float? = null,
    val visible: Boolean = true,
)
