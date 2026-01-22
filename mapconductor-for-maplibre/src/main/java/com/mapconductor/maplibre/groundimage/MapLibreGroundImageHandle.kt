package com.mapconductor.maplibre.groundimage

import com.mapconductor.core.groundimage.GroundImageTileProvider

data class MapLibreGroundImageHandle(
    val routeId: String,
    val generation: Long,
    val cacheKey: String,
    val sourceId: String,
    val layerId: String,
    val tileProvider: GroundImageTileProvider,
)
