package com.mapconductor.here.groundimage

import com.here.sdk.mapview.MapLayer
import com.here.sdk.mapview.datasource.RasterDataSource
import com.mapconductor.core.groundimage.GroundImageTileProvider

data class HereGroundImageHandle(
    val routeId: String,
    val generation: Long,
    val cacheKey: String,
    val sourceName: String,
    val layerName: String,
    val dataSource: RasterDataSource,
    val layer: MapLayer,
    val tileProvider: GroundImageTileProvider,
)
