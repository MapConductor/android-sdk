package com.mapconductor.maplibre.groundimage

import com.mapconductor.core.groundimage.GroundImageTileProvider

data class MapLibreGroundImageHandle(
    val routeId: String,
    val version: Long,
    val sourceId: String,
    val layerId: String,
    val tileProvider: GroundImageTileProvider,
)

