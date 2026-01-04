package com.mapconductor.marker.clustering

import java.io.Serializable

data class MarkerCluster(
    val count: Int,
    val markerIds: List<String>,
) : Serializable

data class MarkerClusterDebugInfo(
    val id: String,
    val center: com.mapconductor.core.features.GeoPointInterface,
    val radiusMeters: Double,
    val count: Int,
) : Serializable
