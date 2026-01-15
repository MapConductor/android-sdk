package com.mapconductor.mapbox.groundimage

import com.mapconductor.core.groundimage.GroundImageTileProvider

data class MapboxGroundImageHandle(
    val routeId: String,
    val generation: Long,
    val sourceId: String,
    val layerId: String,
    val tileProvider: GroundImageTileProvider,
)
