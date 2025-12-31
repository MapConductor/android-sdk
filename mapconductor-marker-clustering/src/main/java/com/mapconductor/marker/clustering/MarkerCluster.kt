package com.mapconductor.marker.clustering

import java.io.Serializable

data class MarkerCluster(
    val count: Int,
    val markerIds: List<String>,
) : Serializable
