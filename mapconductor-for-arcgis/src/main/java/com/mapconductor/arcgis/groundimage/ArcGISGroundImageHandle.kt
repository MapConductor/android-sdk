package com.mapconductor.arcgis.groundimage

import com.arcgismaps.mapping.layers.WebTiledLayer
import com.mapconductor.core.groundimage.GroundImageTileProvider

data class ArcGISGroundImageHandle(
    val routeId: String,
    val generation: Long,
    val tileProvider: GroundImageTileProvider,
    val layer: WebTiledLayer,
)

